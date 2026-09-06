package com.neststay.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

  @Bean
  public OpenAPI nestStayOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("NestStay 民宿管理系统 API")
                .description("NestStay 民宿管理系统后端接口文档")
                .contact(new Contact().name("NestStay Team"))
                .version("v1.0"));
  }
}
