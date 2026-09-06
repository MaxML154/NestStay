package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.SystemConfigEntity;
import com.neststay.service.SystemConfigService;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 登录相关 */
@RequestMapping({"/config", "/systemConfig"})
@RestController
public class SystemConfigController {

  @Autowired private SystemConfigService configService;

  /** 列表 */
  @RequestMapping("/page")
  public R page(@RequestParam Map<String, Object> params, SystemConfigEntity config) {
    QueryWrapper<SystemConfigEntity> ew = new QueryWrapper<SystemConfigEntity>();
    PageUtils page =
        configService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, config), params), params));
    return R.ok().put("data", page);
  }

  /** 列表 */
  @IgnoreAuth
  @RequestMapping("/list")
  public R list(@RequestParam Map<String, Object> params, SystemConfigEntity config) {
    QueryWrapper<SystemConfigEntity> ew = new QueryWrapper<SystemConfigEntity>();
    PageUtils page =
        configService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, config), params), params));
    return R.ok().put("data", page);
  }

  /** 信息 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") String id) {
    SystemConfigEntity config = configService.getById(id);
    return R.ok().put("data", config);
  }

  /** 详情 */
  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") String id) {
    SystemConfigEntity config = configService.getById(id);
    return R.ok().put("data", config);
  }

  /** 根据name获取信息 */
  @RequestMapping("/info")
  public R infoByName(@RequestParam String name) {
    SystemConfigEntity config =
        configService.getOne(new QueryWrapper<SystemConfigEntity>().eq("name", "faceFile"));
    return R.ok().put("data", config);
  }

  /** 保存 */
  @PostMapping("/save")
  public R save(@RequestBody SystemConfigEntity config) {
    //    	ValidatorUtils.validateEntity(config);
    configService.save(config);
    return R.ok();
  }

  /** 修改 */
  @RequestMapping("/update")
  public R update(@RequestBody SystemConfigEntity config) {
    //        ValidatorUtils.validateEntity(config);
    configService.updateById(config); // 全部更新
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    configService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }
}
