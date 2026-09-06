package com.neststay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 拦截器配置 注意：认证拦截器已由 Spring Security + JWT 替代 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

  /** springboot 配置静态资源映射 */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 管理端前端（支持原路径和短路径）
    //   /admin/admin/dist/**  → classpath:/admin/admin/dist/ （保持原来URL兼容）
    //   /admin/**             → classpath:/admin/admin/dist/ （短路径）
    registry
        .addResourceHandler("/admin/admin/dist/**")
        .addResourceLocations("classpath:/admin/admin/dist/");
    // 用户端前端（支持原路径和短路径）
    registry
        .addResourceHandler("/front/front/dist/**")
        .addResourceLocations("classpath:/front/front/dist/");
    registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/front/dist/");
    registry
        .addResourceHandler("/pay-logos/**")
        .addResourceLocations(
            "classpath:/static/pay-logos/",
            "file:src/main/resources/static/pay-logos/",
            "file:./static/pay-logos/");
    registry
        .addResourceHandler("/upload/**")
        .addResourceLocations(
            "classpath:/static/upload/",
            "file:src/main/resources/static/upload/",
            "file:./static/upload/");
    // 其他静态资源
    registry
        .addResourceHandler("/**")
        .addResourceLocations("classpath:/resources/")
        .addResourceLocations("classpath:/static/")
        .addResourceLocations("classpath:/public/");
  }
}
