package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.HomestayCategoryEntity;
import com.neststay.entity.view.HomestayCategoryView;
import com.neststay.service.HomestayCategoryService;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 民宿类型 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
@RestController
@RequestMapping("/homestayCategory")
public class HomestayCategoryController {
  @Autowired private HomestayCategoryService homestayCategoryService;

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      HomestayCategoryEntity homestayCategory,
      HttpServletRequest request) {
    QueryWrapper<HomestayCategoryEntity> ew = new QueryWrapper<HomestayCategoryEntity>();

    PageUtils page =
        homestayCategoryService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, homestayCategory), params), params));

    return R.ok().put("data", page);
  }

  /** 前端列表 */
  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      HomestayCategoryEntity homestayCategory,
      HttpServletRequest request) {
    QueryWrapper<HomestayCategoryEntity> ew = new QueryWrapper<HomestayCategoryEntity>();

    PageUtils page =
        homestayCategoryService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, homestayCategory), params), params));
    return R.ok().put("data", page);
  }

  /** 列表 */
  @RequestMapping("/lists")
  public R list(HomestayCategoryEntity homestayCategory) {
    QueryWrapper<HomestayCategoryEntity> ew = new QueryWrapper<HomestayCategoryEntity>();
    ew.allEq(MPUtil.allEQMapPre(homestayCategory, "homestay_category"));
    return R.ok().put("data", homestayCategoryService.selectListView(ew));
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(HomestayCategoryEntity homestayCategory) {
    QueryWrapper<HomestayCategoryEntity> ew = new QueryWrapper<HomestayCategoryEntity>();
    ew.allEq(MPUtil.allEQMapPre(homestayCategory, "homestay_category"));
    HomestayCategoryView homestayCategoryView = homestayCategoryService.selectView(ew);
    return R.ok("查询民宿类型成功").put("data", homestayCategoryView);
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    HomestayCategoryEntity homestayCategory = homestayCategoryService.getById(id);
    return R.ok().put("data", homestayCategory);
  }

  /** 前端详情 */
  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id) {
    HomestayCategoryEntity homestayCategory = homestayCategoryService.getById(id);
    return R.ok().put("data", homestayCategory);
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody HomestayCategoryEntity homestayCategory, HttpServletRequest request) {
    if (homestayCategoryService.count(
            new QueryWrapper<HomestayCategoryEntity>()
                .eq("homestay_category", homestayCategory.getHomestayCategory()))
        > 0) {
      return R.error("民宿类型已存在");
    }
    // ValidatorUtils.validateEntity(homestayCategory);
    homestayCategoryService.save(homestayCategory);
    return R.ok();
  }

  /** 前端保存 */
  @RequestMapping("/add")
  public R add(@RequestBody HomestayCategoryEntity homestayCategory, HttpServletRequest request) {
    if (homestayCategoryService.count(
            new QueryWrapper<HomestayCategoryEntity>()
                .eq("homestay_category", homestayCategory.getHomestayCategory()))
        > 0) {
      return R.error("民宿类型已存在");
    }
    // ValidatorUtils.validateEntity(homestayCategory);
    homestayCategoryService.save(homestayCategory);
    return R.ok();
  }

  /** 修改 */
  @RequestMapping("/update")
  @Transactional
  public R update(
      @RequestBody HomestayCategoryEntity homestayCategory, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(homestayCategory);
    if (homestayCategoryService.count(
            new QueryWrapper<HomestayCategoryEntity>()
                .ne("id", homestayCategory.getId())
                .eq("homestay_category", homestayCategory.getHomestayCategory()))
        > 0) {
      return R.error("民宿类型已存在");
    }
    homestayCategoryService.updateById(homestayCategory); // 全部更新
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    homestayCategoryService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }
}
