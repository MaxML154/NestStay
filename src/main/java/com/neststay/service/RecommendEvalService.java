package com.neststay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.dao.PlatformViewDao;
import com.neststay.dao.RecommendEventDao;
import com.neststay.entity.PlatformViewEntity;
import com.neststay.entity.RecommendEventEntity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 用 recommend_event 做窗口漏斗：曝光→点击→收藏/下单，并与上一窗口对比。不做在线 A/B。 */
@Service
public class RecommendEvalService {

  @Autowired private RecommendEventDao recommendEventDao;
  @Autowired private PlatformViewDao platformViewDao;

  public Map<String, Object> summarize(int days) {
    int window = Math.max(1, Math.min(days, 90));
    Date now = new Date();
    Date currentStart = daysAgo(now, window);
    Date previousStart = daysAgo(now, window * 2);

    List<RecommendEventEntity> recent =
        recommendEventDao.selectList(
            new QueryWrapper<RecommendEventEntity>().ge("create_time", previousStart));

    Funnel current = funnel(recent, currentStart, now);
    Funnel previous = funnel(recent, previousStart, currentStart);

    Map<Long, String> names = new HashMap<>();
    Map<Long, Integer> selectedMap = new HashMap<>();
    for (PlatformViewEntity listing : platformViewDao.selectList(new QueryWrapper<>())) {
      if (listing.getId() == null) continue;
      names.put(listing.getId(), listing.getHomestayName());
      selectedMap.put(listing.getId(), listing.getIsSelected() == null ? 0 : listing.getIsSelected());
    }

    Map<String, Object> result = new HashMap<>();
    result.put("days", window);
    result.put("current", current.toMap());
    result.put("previous", previous.toMap());
    result.put("delta", delta(current, previous));
    result.put("selected", splitSelected(recent, currentStart, now, selectedMap));
    result.put("topListings", topListings(recent, currentStart, now, names, 8));
    result.put("note", "CTR=点击/曝光。无曝光时 CTR 记 0。对比上一同等天数窗口，不是随机 A/B。");
    return result;
  }

  private Funnel funnel(List<RecommendEventEntity> rows, Date from, Date to) {
    Funnel f = new Funnel();
    for (RecommendEventEntity row : rows) {
      if (!inRange(row.getCreateTime(), from, to)) continue;
      String type = row.getEventType() == null ? "" : row.getEventType().toLowerCase();
      if ("expose".equals(type)) f.expose++;
      else if ("click".equals(type)) f.click++;
      else if ("favorite".equals(type)) f.favorite++;
      else if ("order".equals(type)) f.order++;
      else if ("stay".equals(type)) f.stay++;
    }
    return f;
  }

  private Map<String, Object> splitSelected(
      List<RecommendEventEntity> rows, Date from, Date to, Map<Long, Integer> selectedMap) {
    Funnel yes = new Funnel();
    Funnel no = new Funnel();
    for (RecommendEventEntity row : rows) {
      if (!inRange(row.getCreateTime(), from, to) || row.getListingId() == null) continue;
      Funnel target = Integer.valueOf(1).equals(selectedMap.get(row.getListingId())) ? yes : no;
      String type = row.getEventType() == null ? "" : row.getEventType().toLowerCase();
      if ("expose".equals(type)) target.expose++;
      else if ("click".equals(type)) target.click++;
      else if ("favorite".equals(type)) target.favorite++;
      else if ("order".equals(type)) target.order++;
    }
    Map<String, Object> split = new HashMap<>();
    split.put("selected", yes.toMap());
    split.put("other", no.toMap());
    return split;
  }

  private List<Map<String, Object>> topListings(
      List<RecommendEventEntity> rows,
      Date from,
      Date to,
      Map<Long, String> names,
      int limit) {
    Map<Long, Funnel> byListing = new HashMap<>();
    for (RecommendEventEntity row : rows) {
      if (!inRange(row.getCreateTime(), from, to) || row.getListingId() == null) continue;
      Funnel f = byListing.computeIfAbsent(row.getListingId(), key -> new Funnel());
      String type = row.getEventType() == null ? "" : row.getEventType().toLowerCase();
      if ("expose".equals(type)) f.expose++;
      else if ("click".equals(type)) f.click++;
      else if ("favorite".equals(type)) f.favorite++;
      else if ("order".equals(type)) f.order++;
    }
    List<Map<String, Object>> ranked = new ArrayList<>();
    for (Map.Entry<Long, Funnel> entry : byListing.entrySet()) {
      Funnel f = entry.getValue();
      if (f.expose < 3 && f.click < 1) continue;
      Map<String, Object> row = f.toMap();
      row.put("listingId", entry.getKey());
      String name = names.get(entry.getKey());
      row.put("name", name == null ? ("#" + entry.getKey()) : name);
      ranked.add(row);
    }
    ranked.sort(Comparator.comparing((Map<String, Object> row) -> (Double) row.get("ctr")).reversed());
    if (ranked.size() > limit) return new ArrayList<>(ranked.subList(0, limit));
    return ranked;
  }

  private static Map<String, Object> delta(Funnel current, Funnel previous) {
    Map<String, Object> d = new HashMap<>();
    d.put("ctr", round4(current.ctr() - previous.ctr()));
    d.put("favoriteRate", round4(current.favoriteRate() - previous.favoriteRate()));
    d.put("orderRate", round4(current.orderRate() - previous.orderRate()));
    return d;
  }

  private static boolean inRange(Date time, Date from, Date to) {
    if (time == null) return false;
    return !time.before(from) && time.before(to);
  }

  private static Date daysAgo(Date now, int days) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(now);
    cal.add(Calendar.DAY_OF_YEAR, -days);
    return cal.getTime();
  }

  private static double round4(double value) {
    return Math.round(value * 10000.0) / 10000.0;
  }

  private static class Funnel {
    long expose;
    long click;
    long favorite;
    long order;
    long stay;

    double ctr() {
      return expose <= 0 ? 0 : round4(click / (double) expose);
    }

    double favoriteRate() {
      return click <= 0 ? 0 : round4(favorite / (double) click);
    }

    double orderRate() {
      return click <= 0 ? 0 : round4(order / (double) click);
    }

    Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("expose", expose);
      map.put("click", click);
      map.put("favorite", favorite);
      map.put("order", order);
      map.put("stay", stay);
      map.put("ctr", ctr());
      map.put("favoriteRate", favoriteRate());
      map.put("orderRate", orderRate());
      return map;
    }
  }
}
