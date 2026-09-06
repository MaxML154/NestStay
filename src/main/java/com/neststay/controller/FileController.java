package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.EIException;
import com.neststay.entity.SystemConfigEntity;
import com.neststay.service.SystemConfigService;
import com.neststay.utils.R;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 上传文件映射表 */
@RestController
@RequestMapping("file")
@SuppressWarnings({"unchecked", "rawtypes"})
public class FileController {
  @Autowired private SystemConfigService configService;

  private File classpathUploadDirectory() throws IOException {
    File staticDirectory = new File(ResourceUtils.getURL("classpath:static").getPath());
    return new File(staticDirectory, "upload");
  }

  private File runtimeUploadDirectory() {
    return new File("static/upload");
  }

  private File[] uploadDirectories() throws IOException {
    return new File[] {classpathUploadDirectory(), runtimeUploadDirectory()};
  }

  /** 上传文件 */
  @RequestMapping("/upload")
  @IgnoreAuth
  public R upload(@RequestParam("file") MultipartFile file, String type) throws Exception {
    if (file == null || file.isEmpty()) {
      throw new EIException("上传文件不能为空");
    }
    String originalName = file.getOriginalFilename();
    String fileExt = "bin";
    if (StringUtils.isNotBlank(originalName) && originalName.lastIndexOf('.') >= 0) {
      fileExt = originalName.substring(originalName.lastIndexOf('.') + 1);
    }
    String fileName = new Date().getTime() + "." + fileExt;
    if (StringUtils.isNotBlank(type) && type.contains("_template")) {
      fileName = type + "." + fileExt;
    }

    byte[] content = file.getBytes();
    for (File directory : uploadDirectories()) {
      if (!directory.exists() && !directory.mkdirs()) {
        throw new IOException("无法创建上传目录: " + directory.getAbsolutePath());
      }
      Files.write(new File(directory, fileName).toPath(), content);
    }

    if ("1".equals(type)) {
      SystemConfigEntity configEntity =
          configService.getOne(
              new QueryWrapper<SystemConfigEntity>().eq("display_name", "faceFile"));
      if (configEntity == null) {
        configEntity = new SystemConfigEntity();
        configEntity.setName("faceFile");
      }
      configEntity.setConfigValue(fileName);
      configService.saveOrUpdate(configEntity);
    }
    return R.ok().put("file", fileName);
  }

  /** 下载文件（从本地获取） */
  @IgnoreAuth
  @RequestMapping("/download")
  public ResponseEntity<byte[]> download(@RequestParam String fileName) {
    if (StringUtils.isBlank(fileName) || fileName.contains("..") || fileName.contains("/")) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    try {
      for (File directory : uploadDirectories()) {
        File file = new File(directory, fileName);
        if (file.isFile()) {
          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
          headers.setContentDispositionFormData("attachment", fileName);
          return new ResponseEntity<>(
              FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
        }
      }
    } catch (IOException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
}
