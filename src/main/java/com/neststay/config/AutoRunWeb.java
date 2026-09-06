package com.neststay.config;

import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@ConditionalOnProperty(name = "shell.preview", havingValue = "false", matchIfMissing = true)
public class AutoRunWeb {

  /** 监听事件（当项目启动后），启动浏览器 */
  @EventListener(ApplicationReadyEvent.class)
  void applicationReadyEvent() {
    // 需要启动的url
    String url = "http://localhost:8080/neststay/front/front/dist/index.html";
    Runtime runtime = Runtime.getRuntime();
    try {
      // rundll32 url.dll,FileProtocolHandler是Windows系统下用来打开默认浏览器并访问指定URL的命令
      runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
