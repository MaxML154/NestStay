package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.NewsCategoryEntity;
import com.neststay.entity.view.NewsCategoryView;
import com.neststay.service.NewsCategoryService;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 民宿资讯分类 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@RestController
@RequestMapping({"/newsCategory", "/newstype"})
public class NewsCategoryController {
  @Autowired private NewsCategoryService newstypeService;

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      NewsCategoryEntity newstype,
      HttpServletRequest request) {
    QueryWrapper<NewsCategoryEntity> ew = new QueryWrapper<NewsCategoryEntity>();

    PageUtils page =
        newstypeService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, newstype), params), params));

    return R.ok().put("data", page);
  }

  /** 前端列表 */
  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      NewsCategoryEntity newstype,
      HttpServletRequest request) {
    QueryWrapper<NewsCategoryEntity> ew = new QueryWrapper<NewsCategoryEntity>();

    PageUtils page =
        newstypeService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, newstype), params), params));
    return R.ok().put("data", page);
  }

  /** 列表 */
  @RequestMapping("/lists")
  public R list(NewsCategoryEntity newstype) {
    QueryWrapper<NewsCategoryEntity> ew = new QueryWrapper<NewsCategoryEntity>();
    ew.allEq(MPUtil.allEQMapPre(newstype, "news_category"));
    return R.ok().put("data", newstypeService.selectListView(ew));
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(NewsCategoryEntity newstype) {
    QueryWrapper<NewsCategoryEntity> ew = new QueryWrapper<NewsCategoryEntity>();
    ew.allEq(MPUtil.allEQMapPre(newstype, "news_category"));
    NewsCategoryView newstypeView = newstypeService.selectView(ew);
    return R.ok("查询民宿资讯分类成功").put("data", newstypeView);
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    NewsCategoryEntity newstype = newstypeService.getById(id);
    return R.ok().put("data", newstype);
  }

  /** 前端详情 */
  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id) {
    NewsCategoryEntity newstype = newstypeService.getById(id);
    return R.ok().put("data", newstype);
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody NewsCategoryEntity newstype, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(newstype);
    newstypeService.save(newstype);
    return R.ok();
  }

  /** 前端保存 */
  @RequestMapping("/add")
  public R add(@RequestBody NewsCategoryEntity newstype, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(newstype);
    newstypeService.save(newstype);
    return R.ok();
  }

  /** 获取用户密保 */
  @RequestMapping("/security")
  @IgnoreAuth
  public R security(@RequestParam String username) {
    NewsCategoryEntity newstype =
        newstypeService.getOne(new QueryWrapper<NewsCategoryEntity>().eq("", username));
    return R.ok().put("data", newstype);
  }

  /** 修改 */
  @RequestMapping("/update")
  @Transactional
  @IgnoreAuth
  public R update(@RequestBody NewsCategoryEntity newstype, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(newstype);
    newstypeService.updateById(newstype); // 全部更新
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    newstypeService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  /** 前端智能排序 */
  @IgnoreAuth
  @RequestMapping("/autoSort")
  public R autoSort(
      @RequestParam Map<String, Object> params,
      NewsCategoryEntity newstype,
      HttpServletRequest request,
      String pre) {
    QueryWrapper<NewsCategoryEntity> ew = new QueryWrapper<NewsCategoryEntity>();
    Map<String, Object> newMap = new HashMap<String, Object>();
    Map<String, Object> param = new HashMap<String, Object>();
    Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      String key = entry.getKey();
      String newKey = entry.getKey();
      if (pre.endsWith(".")) {
        newMap.put(pre + newKey, entry.getValue());
      } else if (StringUtils.isEmpty(pre)) {
        newMap.put(newKey, entry.getValue());
      } else {
        newMap.put(pre + "." + newKey, entry.getValue());
      }
    }
    params.put("sort", "create_time");
    params.put("order", "desc");
    PageUtils page =
        newstypeService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, newstype), params), params));
    return R.ok().put("data", page);
  }
}
