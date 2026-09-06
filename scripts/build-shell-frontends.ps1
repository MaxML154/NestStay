# Build NestStay-Shell frontends from full NestStay sources with preview guards.
param(
    [string]$NestStayRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\NestStay")).Path,
    [string]$ShellRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$ErrorActionPreference = "Stop"
$configPath = Join-Path $ShellRoot "shell-patches\shell-config.json"
$config = Get-Content $configPath -Raw | ConvertFrom-Json
$contactUrl = $config.contactUrl

Write-Host "NestStay root: $NestStayRoot"
Write-Host "Shell root:    $ShellRoot"
Write-Host "Contact URL:   $contactUrl"

$tempRoot = Join-Path $env:TEMP ("neststay-shell-build-" + [guid]::NewGuid().ToString("n"))
New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null

function Build-Frontend {
    param(
        [string]$Name,
        [string]$SourceDir,
        [string]$DestDist,
        [scriptblock]$PatchFn
    )

    Write-Host "`n=== Building $Name ==="
    $work = Join-Path $tempRoot $Name
    Copy-Item $SourceDir $work -Recurse -Force
    if (Test-Path (Join-Path $work "node_modules")) { Remove-Item (Join-Path $work "node_modules") -Recurse -Force }
    if (Test-Path (Join-Path $work "dist")) { Remove-Item (Join-Path $work "dist") -Recurse -Force }
    & $PatchFn $work

    Push-Location $work
    if (-not (Test-Path "node_modules\vite\bin\vite.js")) {
        Write-Host "npm install ($Name)..."
        cmd /c "npm install --legacy-peer-deps"
        if ($LASTEXITCODE -ne 0) { throw "npm install failed for $Name" }
    }
    Write-Host "npm run build ($Name)..."
    cmd /c "npm run build"
    if ($LASTEXITCODE -ne 0) { throw "npm run build failed for $Name" }
    Pop-Location

    $built = Join-Path $work "dist"
    if (-not (Test-Path $built)) { throw "Missing dist for $Name" }
    New-Item -ItemType Directory -Force -Path (Split-Path $DestDist) | Out-Null
    if (Test-Path $DestDist) { Remove-Item $DestDist -Recurse -Force }
    Copy-Item $built $DestDist -Recurse -Force
    Write-Host "Copied dist -> $DestDist"
}

$consumerSrc = Join-Path $NestStayRoot "src\main\resources\front\front"
$adminSrc = Join-Path $NestStayRoot "src\main\resources\admin\admin"
$consumerDist = Join-Path $ShellRoot "src\main\resources\front\front\dist"
$adminDist = Join-Path $ShellRoot "src\main\resources\admin\admin\dist"

function Repair-AdminVueFiles {
    param([string]$work)
    $stub = @'
<template><div class="main-content" :style='{"padding":"30px"}'><el-empty description="预览版模块占位" /></div></template>
<script>export default { name: "AdminModuleStub" }</script>
'@
    Get-ChildItem (Join-Path $work "src\views\modules") -Recurse -Filter "list.vue" -ErrorAction SilentlyContinue | ForEach-Object {
        Set-Content $_.FullName $stub -Encoding UTF8
    }
    Get-ChildItem (Join-Path $work "src\views\modules") -Recurse -Filter "add-or-update.vue" -ErrorAction SilentlyContinue | ForEach-Object {
        Set-Content $_.FullName $stub -Encoding UTF8
    }
}

Build-Frontend -Name "consumer" -SourceDir $consumerSrc -DestDist $consumerDist -PatchFn {
    param($work)
    $router = Join-Path $work "src\router\router.js"
    $guardFile = Join-Path $ShellRoot "shell-patches\consumer-router-guard.js"
    $guard = (Get-Content $guardFile -Raw).Replace('__SHELL_CONTACT_URL__', $contactUrl)
    $content = Get-Content $router -Raw
    if ($content -notmatch 'shellDetailPaths') {
        $content = $content -replace "export default router", ($guard + "`nexport default router")
        Set-Content $router $content -Encoding UTF8
    }
}

Build-Frontend -Name "admin" -SourceDir $adminSrc -DestDist $adminDist -PatchFn {
    param($work)
    Repair-AdminVueFiles $work
    $aside = Join-Path $work "src\components\index\IndexAsideStatic.vue"
    $guardFile = Join-Path $ShellRoot "shell-patches\admin-sidebar-guard.js"
    $guardBody = (Get-Content $guardFile -Raw).Replace('__SHELL_CONTACT_URL__', $contactUrl).Trim()
    $content = Get-Content $aside -Raw
    $pattern = '(?s)\t\tmenuHandler\(name\) \{.*?\n\t\t\},'
    if ($content -notmatch 'SHELL_CONTACT_URL') {
        $content = [regex]::Replace($content, $pattern, $guardBody, 1)
        Set-Content $aside $content -Encoding UTF8
    }
}

Remove-Item $tempRoot -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "`nFrontend build complete."
