# Build admin dist only for NestStay-Shell
param(
    [string]$NestStayRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\NestStay")).Path,
    [string]$ShellRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$ErrorActionPreference = "Stop"
$config = Get-Content (Join-Path $ShellRoot "shell-patches\shell-config.json") -Raw | ConvertFrom-Json
$contactUrl = $config.contactUrl
$adminSrc = Join-Path $NestStayRoot "src\main\resources\admin\admin"
$adminDist = Join-Path $ShellRoot "src\main\resources\admin\admin\dist"
$tempRoot = Join-Path $env:TEMP ("neststay-admin-build-" + [guid]::NewGuid().ToString("n"))

Write-Host "Building admin -> $adminDist"

New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null
$work = Join-Path $tempRoot "admin"
Copy-Item $adminSrc $work -Recurse -Force
if (Test-Path (Join-Path $work "node_modules")) { Remove-Item (Join-Path $work "node_modules") -Recurse -Force }
if (Test-Path (Join-Path $work "dist")) { Remove-Item (Join-Path $work "dist") -Recurse -Force }

$stub = @'
<template><div class="main-content" :style='{"padding":"30px"}'><el-empty description="预览版模块占位" /></div></template>
<script>export default { name: "AdminModuleStub" }</script>
'@

Get-ChildItem (Join-Path $work "src\views\modules") -Recurse -Filter "list.vue" | ForEach-Object {
    Set-Content $_.FullName $stub -Encoding UTF8
}

Get-ChildItem (Join-Path $work "src\views\modules") -Recurse -Filter "add-or-update.vue" | ForEach-Object {
    Set-Content $_.FullName $stub -Encoding UTF8
}

$aside = Join-Path $work "src\components\index\IndexAsideStatic.vue"
$guardFile = Join-Path $ShellRoot "shell-patches\admin-sidebar-guard.js"
$guardBody = (Get-Content $guardFile -Raw).Replace('__SHELL_CONTACT_URL__', $contactUrl).Trim()
$content = Get-Content $aside -Raw
$pattern = '(?s)\t\tmenuHandler\(name\) \{.*?\n\t\t\},'
if ($content -notmatch 'SHELL_CONTACT_URL') {
    $content = [regex]::Replace($content, $pattern, $guardBody, 1)
    Set-Content $aside $content -Encoding UTF8
}

Push-Location $work
cmd /c "npm install --legacy-peer-deps"
if ($LASTEXITCODE -ne 0) { throw "npm install failed" }
cmd /c "npm run build"
if ($LASTEXITCODE -ne 0) { throw "npm run build failed" }
Pop-Location

if (-not (Test-Path (Join-Path $work "dist\index.html"))) { throw "dist missing" }
New-Item -ItemType Directory -Force -Path (Split-Path $adminDist) | Out-Null
if (Test-Path $adminDist) { Remove-Item $adminDist -Recurse -Force }
Copy-Item (Join-Path $work "dist") $adminDist -Recurse -Force
Remove-Item $tempRoot -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "Admin build complete."
