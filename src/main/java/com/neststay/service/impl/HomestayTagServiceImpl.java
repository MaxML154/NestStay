package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.HomestayListingTagDao;
import com.neststay.dao.HomestayTagDao;
import com.neststay.entity.HomestayListingTagEntity;
import com.neststay.entity.HomestayTagEntity;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.service.HomestayTagService;
import com.neststay.service.PlatformViewService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomestayTagServiceImpl extends ServiceImpl<HomestayTagDao, HomestayTagEntity>
    implements HomestayTagService {
  @Autowired private HomestayListingTagDao listingTagDao;
  @Autowired private PlatformViewService platformViewService;

  @Override
  public List<HomestayTagEntity> listEnabled() {
    return list(
        new QueryWrapper<HomestayTagEntity>()
            .eq("enabled", 1)
            .orderByAsc("sort_order")
            .orderByAsc("id"));
  }

  @Override
  public List<Long> tagIdsForListing(Long listingId) {
    if (listingId == null) return Collections.emptyList();
    List<HomestayListingTagEntity> rels =
        listingTagDao.selectList(
            new QueryWrapper<HomestayListingTagEntity>()
                .eq("listing_id", listingId)
                .orderByAsc("id"));
    List<Long> ids = new ArrayList<>();
    for (HomestayListingTagEntity rel : rels) {
      if (rel.getTagId() != null) ids.add(rel.getTagId());
    }
    return ids;
  }

  @Override
  public List<Long> listingIdsByTag(Long tagId) {
    if (tagId == null) return Collections.emptyList();
    List<HomestayListingTagEntity> rels =
        listingTagDao.selectList(
            new QueryWrapper<HomestayListingTagEntity>().eq("tag_id", tagId));
    List<Long> ids = new ArrayList<>();
    for (HomestayListingTagEntity rel : rels) {
      if (rel.getListingId() != null) ids.add(rel.getListingId());
    }
    return ids;
  }

  @Override
  public Map<Long, String> namesByListingIds(Collection<Long> listingIds) {
    Map<Long, String> result = new LinkedHashMap<>();
    if (listingIds == null || listingIds.isEmpty()) return result;
    List<HomestayListingTagEntity> rels =
        listingTagDao.selectList(
            new QueryWrapper<HomestayListingTagEntity>().in("listing_id", listingIds));
    if (rels.isEmpty()) return result;
    Set<Long> tagIds = new LinkedHashSet<>();
    for (HomestayListingTagEntity rel : rels) {
      if (rel.getTagId() != null) tagIds.add(rel.getTagId());
    }
    if (tagIds.isEmpty()) return result;
    Map<Long, HomestayTagEntity> tags = new LinkedHashMap<>();
    for (HomestayTagEntity tag : listByIds(tagIds)) {
      if (tag.getEnabled() != null && tag.getEnabled() == 0) continue;
      tags.put(tag.getId(), tag);
    }
    Map<Long, List<HomestayTagEntity>> grouped = new LinkedHashMap<>();
    for (HomestayListingTagEntity rel : rels) {
      HomestayTagEntity tag = tags.get(rel.getTagId());
      if (tag == null) continue;
      grouped.computeIfAbsent(rel.getListingId(), key -> new ArrayList<>()).add(tag);
    }
    for (Map.Entry<Long, List<HomestayTagEntity>> entry : grouped.entrySet()) {
      entry
          .getValue()
          .sort(
              Comparator.comparingInt(
                      (HomestayTagEntity tag) ->
                          tag.getSortOrder() == null ? 0 : tag.getSortOrder())
                  .thenComparingLong(tag -> tag.getId() == null ? 0L : tag.getId()));
      List<String> names = new ArrayList<>();
      for (HomestayTagEntity tag : entry.getValue()) {
        if (StringUtils.isNotBlank(tag.getName())) names.add(tag.getName().trim());
      }
      result.put(entry.getKey(), String.join(",", names));
    }
    return result;
  }

  @Override
  @Transactional
  public void replaceListingTags(Long listingId, Collection<?> tagIds) {
    if (listingId == null || tagIds == null) return;
    listingTagDao.delete(
        new QueryWrapper<HomestayListingTagEntity>().eq("listing_id", listingId));
    Set<Long> unique = new LinkedHashSet<>();
    for (Object raw : tagIds) {
      Long id = toLong(raw);
      if (id != null) unique.add(id);
    }
    if (!unique.isEmpty()) {
      Map<Long, HomestayTagEntity> enabled = new LinkedHashMap<>();
      for (HomestayTagEntity tag : listByIds(unique)) {
        if (tag.getEnabled() != null && tag.getEnabled() == 0) continue;
        enabled.put(tag.getId(), tag);
      }
      for (Long tagId : unique) {
        if (!enabled.containsKey(tagId)) continue;
        HomestayListingTagEntity rel = new HomestayListingTagEntity();
        rel.setListingId(listingId);
        rel.setTagId(tagId);
        listingTagDao.insert(rel);
      }
    }
    syncPropertyFeatures(listingId);
  }

  @Override
  @Transactional
  public void deleteTags(Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) return;
    List<HomestayListingTagEntity> rels =
        listingTagDao.selectList(
            new QueryWrapper<HomestayListingTagEntity>().in("tag_id", ids));
    Set<Long> listingIds = new LinkedHashSet<>();
    for (HomestayListingTagEntity rel : rels) {
      if (rel.getListingId() != null) listingIds.add(rel.getListingId());
    }
    listingTagDao.delete(new QueryWrapper<HomestayListingTagEntity>().in("tag_id", ids));
    removeByIds(ids);
    for (Long listingId : listingIds) {
      syncPropertyFeatures(listingId);
    }
  }

  @Override
  public void clearListingTags(Collection<Long> listingIds) {
    if (listingIds == null || listingIds.isEmpty()) return;
    listingTagDao.delete(
        new QueryWrapper<HomestayListingTagEntity>().in("listing_id", listingIds));
  }

  @Override
  public void attachToSearchItems(List<Map<String, Object>> items) {
    if (items == null || items.isEmpty()) return;
    Set<Long> ids = new LinkedHashSet<>();
    for (Map<String, Object> item : items) {
      Long id = toLong(item.get("id"));
      if (id != null) ids.add(id);
    }
    Map<Long, String> names = namesByListingIds(ids);
    Map<Long, List<Long>> tagIds = tagIdsByListingIds(ids);
    for (Map<String, Object> item : items) {
      Long id = toLong(item.get("id"));
      item.put("tags", id == null ? "" : names.getOrDefault(id, ""));
      item.put("tagIds", id == null ? Collections.emptyList() : tagIds.getOrDefault(id, Collections.emptyList()));
    }
  }

  private Map<Long, List<Long>> tagIdsByListingIds(Collection<Long> listingIds) {
    Map<Long, List<Long>> result = new LinkedHashMap<>();
    if (listingIds == null || listingIds.isEmpty()) return result;
    List<HomestayListingTagEntity> rels =
        listingTagDao.selectList(
            new QueryWrapper<HomestayListingTagEntity>().in("listing_id", listingIds));
    for (HomestayListingTagEntity rel : rels) {
      if (rel.getListingId() == null || rel.getTagId() == null) continue;
      result.computeIfAbsent(rel.getListingId(), key -> new ArrayList<>()).add(rel.getTagId());
    }
    return result;
  }

  private void syncPropertyFeatures(Long listingId) {
    PlatformViewEntity patch = new PlatformViewEntity();
    patch.setId(listingId);
    String names = namesByListingIds(Collections.singleton(listingId)).get(listingId);
    patch.setPropertyFeatures(StringUtils.defaultString(names));
    platformViewService.updateById(patch);
  }

  private Long toLong(Object value) {
    if (value == null || StringUtils.isBlank(value.toString())) return null;
    try {
      return Long.valueOf(value.toString());
    } catch (NumberFormatException exception) {
      return null;
    }
  }
}
