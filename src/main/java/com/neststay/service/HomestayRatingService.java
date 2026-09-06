package com.neststay.service;

import java.util.List;
import java.util.Map;

public interface HomestayRatingService {
  void recalcListing(Long listingId);

  void attachDisplay(List<Map<String, Object>> items);
}
