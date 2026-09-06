package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.AboutUsEntity;
import com.neststay.service.AboutUsService;
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
 * 关于我们 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@RestController
@RequestMapping({"/aboutUs", "/aboutus"})
public class AboutUsController {
  @Autowired private AboutUsService aboutusService;

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params, AboutUsEntity aboutus, HttpServletRequest request) {
    QueryWrapper<AboutUsEntity> ew = new QueryWrapper<AboutUsEntity>();

    PageUtils page =
        aboutusService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, aboutus), params), params));

    return R.ok().put("data", page);
  }

  /** 前端列表 */
  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params, AboutUsEntity aboutus, HttpServletRequest request) {
    QueryWrapper<AboutUsEntity> ew = new QueryWrapper<AboutUsEntity>();

    PageUtils page =
        aboutusService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, aboutus), params), params));
    return R.ok().put("data", page);
  }

  /** 列表 */
  @RequestMapping("/lists")
  public R list(AboutUsEntity aboutus) {
    QueryWrapper<AboutUsEntity> ew = new QueryWrapper<AboutUsEntity>();
    ew.allEq(MPUtil.allEQMapPre(aboutus, "about_us"));
    return R.ok().put("data", aboutusService.list(ew));
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(AboutUsEntity aboutus) {
    QueryWrapper<AboutUsEntity> ew = new QueryWrapper<AboutUsEntity>();
    ew.allEq(MPUtil.allEQMapPre(aboutus, "about_us"));
    AboutUsEntity aboutusView = aboutusService.getOne(ew);
    return R.ok("查询关于我们成功").put("data", aboutusView);
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    AboutUsEntity aboutus = aboutusService.getById(id);
    return R.ok().put("data", aboutus);
  }

  /** 前端详情 */
  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id) {
    AboutUsEntity aboutus = aboutusService.getById(id);
    return R.ok().put("data", aboutus);
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody AboutUsEntity aboutus, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(aboutus);
    aboutusService.save(aboutus);
    return R.ok();
  }

  /** 前端保存 */
  @RequestMapping("/add")
  public R add(@RequestBody AboutUsEntity aboutus, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(aboutus);
    aboutusService.save(aboutus);
    return R.ok();
  }

  /** 获取用户密保 */
  @RequestMapping("/security")
  @IgnoreAuth
  public R security(@RequestParam String username) {
    AboutUsEntity aboutus =
        aboutusService.getOne(new QueryWrapper<AboutUsEntity>().eq("", username));
    return R.ok().put("data", aboutus);
  }

  /** 修改 */
  @RequestMapping("/update")
  @Transactional
  @IgnoreAuth
  public R update(@RequestBody AboutUsEntity aboutus, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(aboutus);
    aboutusService.updateById(aboutus); // 全部更新
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    aboutusService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  /** 前端智能排序 */
  @IgnoreAuth
  @RequestMapping("/autoSort")
  public R autoSort(
      @RequestParam Map<String, Object> params,
      AboutUsEntity aboutus,
      HttpServletRequest request,
      String pre) {
    QueryWrapper<AboutUsEntity> ew = new QueryWrapper<AboutUsEntity>();
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
        aboutusService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, aboutus), params), params));
    return R.ok().put("data", page);
  }
}
