package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neststay.entity.AssistantKnowledgeEntity;
import com.neststay.service.AssistantKnowledgeService;
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
@RequestMapping("/assistantKnowledge")
public class AssistantKnowledgeController {
  @Autowired private AssistantKnowledgeService assistantKnowledgeService;

  @RequestMapping("/page")
  public R page(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit,
      AssistantKnowledgeEntity query,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<AssistantKnowledgeEntity> wrapper = new QueryWrapper<>();
    if (StringUtils.isNotBlank(query.getTitle())) wrapper.like("title", query.getTitle().trim());
    if (StringUtils.isNotBlank(query.getCategory())) {
      wrapper.eq("category", query.getCategory().trim());
    }
    wrapper.orderByAsc("sort_order").orderByAsc("id");
    return R.ok()
        .put("data", new PageUtils(assistantKnowledgeService.page(new Page<>(page, limit), wrapper)));
  }

  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    AssistantKnowledgeEntity entity = assistantKnowledgeService.getById(id);
    if (entity == null) return R.error(404, "条目不存在");
    return R.ok().put("data", entity);
  }

  @RequestMapping("/save")
  public R save(@RequestBody AssistantKnowledgeEntity entity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (entity == null || StringUtils.isBlank(entity.getTitle()) || StringUtils.isBlank(entity.getContent())) {
      return R.error(400, "请填写标题和内容");
    }
    entity.setTitle(entity.getTitle().trim());
    if (entity.getEnabled() == null) entity.setEnabled(1);
    if (entity.getSortOrder() == null) entity.setSortOrder(0);
    if (entity.getIsSample() == null) entity.setIsSample(0);
    if (StringUtils.isBlank(entity.getCategory())) entity.setCategory("业务说明");
    assistantKnowledgeService.saveOrUpdate(entity);
    return R.ok();
  }

  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (ids == null || ids.length == 0) return R.error(400, "请选择要删除的条目");
    assistantKnowledgeService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }
}
