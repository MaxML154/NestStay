package com.neststay.task;

import com.neststay.service.UnpaidOrderTimeoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "shell.preview", havingValue = "false", matchIfMissing = true)
public class UnpaidOrderTimeoutTask {
  private static final Logger log = LoggerFactory.getLogger(UnpaidOrderTimeoutTask.class);

  @Autowired private UnpaidOrderTimeoutService unpaidOrderTimeoutService;

  @Scheduled(fixedDelay = 60000)
  public void expireUnpaidOrders() {
    int n = unpaidOrderTimeoutService.expireDueOrders();
    if (n > 0) log.info("auto-cancelled {} unpaid bookings past the 2-hour pay window", n);
  }
}
