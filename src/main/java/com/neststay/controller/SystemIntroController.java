package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.SystemIntroEntity;
import com.neststay.service.SystemIntroService;
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
 * 系统简介 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:36
 */
@RestController
@RequestMapping({"/systemIntro", "/systemintro"})
public class SystemIntroController {
  @Autowired private SystemIntroService systemintroService;

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      SystemIntroEntity systemintro,
      HttpServletRequest request) {
    QueryWrapper<SystemIntroEntity> ew = new QueryWrapper<SystemIntroEntity>();

    PageUtils page =
        systemintroService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, systemintro), params), params));

    return R.ok().put("data", page);
  }

  /** 前端列表 */
  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      SystemIntroEntity systemintro,
      HttpServletRequest request) {
    QueryWrapper<SystemIntroEntity> ew = new QueryWrapper<SystemIntroEntity>();

    PageUtils page =
        systemintroService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, systemintro), params), params));
    return R.ok().put("data", page);
  }

  /** 列表 */
  @RequestMapping("/lists")
  public R list(SystemIntroEntity systemintro) {
    QueryWrapper<SystemIntroEntity> ew = new QueryWrapper<SystemIntroEntity>();
    ew.allEq(MPUtil.allEQMapPre(systemintro, "system_intro"));
    return R.ok().put("data", systemintroService.list(ew));
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(SystemIntroEntity systemintro) {
    QueryWrapper<SystemIntroEntity> ew = new QueryWrapper<SystemIntroEntity>();
    ew.allEq(MPUtil.allEQMapPre(systemintro, "system_intro"));
    SystemIntroEntity systemintroView = systemintroService.getOne(ew);
    return R.ok("查询系统简介成功").put("data", systemintroView);
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    SystemIntroEntity systemintro = systemintroService.getById(id);
    return R.ok().put("data", systemintro);
  }

  /** 前端详情 */
  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id) {
    SystemIntroEntity systemintro = systemintroService.getById(id);
    return R.ok().put("data", systemintro);
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody SystemIntroEntity systemintro, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(systemintro);
    systemintroService.save(systemintro);
    return R.ok();
  }

  /** 前端保存 */
  @RequestMapping("/add")
  public R add(@RequestBody SystemIntroEntity systemintro, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(systemintro);
    systemintroService.save(systemintro);
    return R.ok();
  }

  /** 获取用户密保 */
  @RequestMapping("/security")
  @IgnoreAuth
  public R security(@RequestParam String username) {
    SystemIntroEntity systemintro =
        systemintroService.getOne(new QueryWrapper<SystemIntroEntity>().eq("", username));
    return R.ok().put("data", systemintro);
  }

  /** 修改 */
  @RequestMapping("/update")
  @Transactional
  @IgnoreAuth
  public R update(@RequestBody SystemIntroEntity systemintro, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(systemintro);
    systemintroService.updateById(systemintro); // 全部更新
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    systemintroService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  /** 前端智能排序 */
  @IgnoreAuth
  @RequestMapping("/autoSort")
  public R autoSort(
      @RequestParam Map<String, Object> params,
      SystemIntroEntity systemintro,
      HttpServletRequest request,
      String pre) {
    QueryWrapper<SystemIntroEntity> ew = new QueryWrapper<SystemIntroEntity>();
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
        systemintroService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, systemintro), params), params));
    return R.ok().put("data", page);
  }
}
