package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.entity.HomestayInfoEntity;
import com.neststay.entity.PlatformViewEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Consumer-facing stays are merchant-owned platform_view rows. Selected is only a badge. */
@Component
public class MerchantStayLookup {
  @Autowired private PlatformViewService platformViewService;
  @Autowired private HomestayInfoService homestayInfoService;

  public PlatformViewEntity find(String sourceType, Long id) {
    if (id == null || id <= 0) return null;
    if (!"homestay_info".equals(sourceType)) {
      return usable(platformViewService.getById(id));
    }
    HomestayInfoEntity info = homestayInfoService.getById(id);
    if (info == null || StringUtils.isBlank(info.getName())) return null;
    PlatformViewEntity match =
        platformViewService.getOne(
            new QueryWrapper<PlatformViewEntity>()
                .eq("homestay_name", info.getName())
                .isNotNull("merchant_account")
                .ne("merchant_account", "")
                .ne("merchant_account", "platform")
                .last("limit 1"),
            false);
    return usable(match);
  }

  /** Seed/legacy orders often have homestay_id=0; resolve by merchant + name. */
  public PlatformViewEntity resolve(
      String sourceType, Long homestayId, String merchantAccount, String homestayName) {
    PlatformViewEntity found = find(sourceType, homestayId);
    if (found != null) return found;
    if (homestayId != null && homestayId > 0) {
      found = usable(platformViewService.getById(homestayId));
      if (found != null) return found;
    }
    if (StringUtils.isBlank(homestayName)) return null;
    QueryWrapper<PlatformViewEntity> wrapper =
        new QueryWrapper<PlatformViewEntity>()
            .eq("homestay_name", homestayName)
            .isNotNull("merchant_account")
            .ne("merchant_account", "")
            .ne("merchant_account", "platform");
    if (StringUtils.isNotBlank(merchantAccount)) {
      wrapper.eq("merchant_account", merchantAccount);
    }
    return usable(platformViewService.getOne(wrapper.last("limit 1"), false));
  }

  public boolean isMerchantOwned(PlatformViewEntity stay) {
    return usable(stay) != null;
  }

  private PlatformViewEntity usable(PlatformViewEntity stay) {
    if (stay == null) return null;
    String account = stay.getMerchantAccount();
    if (StringUtils.isBlank(account) || "platform".equalsIgnoreCase(account.trim())) {
      return null;
    }
    return stay;
  }
}
