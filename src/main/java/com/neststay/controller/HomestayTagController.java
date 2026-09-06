package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.HomestayTagEntity;
import com.neststay.service.HomestayTagService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/homestayTag")
public class HomestayTagController {
  @Autowired private HomestayTagService homestayTagService;

  @IgnoreAuth
  @RequestMapping("/enabled")
  public R enabled() {
    return R.ok().put("data", homestayTagService.listEnabled());
  }

  @RequestMapping("/page")
  public R page(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit,
      HomestayTagEntity query,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request) && !AuthSupport.isMerchant(request)) {
      return AuthSupport.staffError(request);
    }
    QueryWrapper<HomestayTagEntity> wrapper = new QueryWrapper<>();
    if (StringUtils.isNotBlank(query.getName())) {
      wrapper.like("name", query.getName().trim());
    }
    if (StringUtils.isNotBlank(query.getCategory())) {
      wrapper.eq("category", query.getCategory().trim());
    }
    wrapper.orderByAsc("sort_order").orderByAsc("id");
    Page<HomestayTagEntity> result =
        homestayTagService.page(new Page<>(page, limit), wrapper);
    return R.ok().put("data", new PageUtils(result));
  }

  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    HomestayTagEntity entity = homestayTagService.getById(id);
    if (entity == null) return R.error(404, "标签不存在");
    return R.ok().put("data", entity);
  }

  @RequestMapping("/save")
  public R save(@RequestBody HomestayTagEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (entity == null || StringUtils.isBlank(entity.getName())) {
      return R.error(400, "标签名不能为空");
    }
    entity.setName(entity.getName().trim());
    if (entity.getName().length() > 12) {
      return R.error(400, "标签名最多 12 个字");
    }
    HomestayTagEntity existing =
        homestayTagService.getOne(
            new QueryWrapper<HomestayTagEntity>().eq("name", entity.getName()).last("limit 1"));
    if (existing != null && (entity.getId() == null || !existing.getId().equals(entity.getId()))) {
      return R.error(400, "已有同名标签");
    }
    if (entity.getEnabled() == null) entity.setEnabled(1);
    if (entity.getSortOrder() == null) entity.setSortOrder(0);
    if (StringUtils.isBlank(entity.getCategory())) entity.setCategory("设施");
    homestayTagService.saveOrUpdate(entity);
    return R.ok();
  }

  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (ids == null || ids.length == 0) return R.error(400, "请选择要删除的标签");
    homestayTagService.deleteTags(Arrays.asList(ids));
    return R.ok();
  }
}
