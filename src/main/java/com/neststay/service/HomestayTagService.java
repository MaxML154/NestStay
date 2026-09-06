package com.neststay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neststay.entity.HomestayTagEntity;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface HomestayTagService extends IService<HomestayTagEntity> {
  List<HomestayTagEntity> listEnabled();

  List<Long> tagIdsForListing(Long listingId);

  List<Long> listingIdsByTag(Long tagId);

  Map<Long, String> namesByListingIds(Collection<Long> listingIds);

  void replaceListingTags(Long listingId, Collection<?> tagIds);

  void deleteTags(Collection<Long> ids);

  void clearListingTags(Collection<Long> listingIds);

  void attachToSearchItems(List<Map<String, Object>> items);
}
