package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.OnlineInquiryEntity;
import com.neststay.entity.view.OnlineInquiryView;
import com.neststay.service.OnlineInquiryService;
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
 * 在线咨询 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@RestController
@RequestMapping("/onlineInquiry")
public class OnlineInquiryController {
  @Autowired private OnlineInquiryService onlineInquiryService;

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      OnlineInquiryEntity onlineInquiryEntity,
      HttpServletRequest request) {
    Object tableNameAttr = request.getSession().getAttribute("tableName");
    String tableName = tableNameAttr != null ? tableNameAttr.toString() : "";
    if (tableName.equals("merchant")) {
      onlineInquiryEntity.setMerchantAccount(
          (String) request.getSession().getAttribute("username"));
    }
    if (tableName.equals("consumer")) {
      onlineInquiryEntity.setAccount((String) request.getSession().getAttribute("username"));
    }
    QueryWrapper<OnlineInquiryEntity> ew = new QueryWrapper<OnlineInquiryEntity>();

    PageUtils page =
        onlineInquiryService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, onlineInquiryEntity), params), params));

    return R.ok().put("data", page);
  }

  /** 前端列表 */
  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      OnlineInquiryEntity onlineInquiryEntity,
      HttpServletRequest request) {
    QueryWrapper<OnlineInquiryEntity> ew = new QueryWrapper<OnlineInquiryEntity>();

    PageUtils page =
        onlineInquiryService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, onlineInquiryEntity), params), params));
    return R.ok().put("data", page);
  }

  /** 列表 */
  @RequestMapping("/lists")
  public R list(OnlineInquiryEntity onlineInquiryEntity) {
    QueryWrapper<OnlineInquiryEntity> ew = new QueryWrapper<OnlineInquiryEntity>();
    ew.allEq(MPUtil.allEQMapPre(onlineInquiryEntity, "online_inquiry"));
    return R.ok().put("data", onlineInquiryService.selectListView(ew));
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(OnlineInquiryEntity onlineInquiryEntity) {
    QueryWrapper<OnlineInquiryEntity> ew = new QueryWrapper<OnlineInquiryEntity>();
    ew.allEq(MPUtil.allEQMapPre(onlineInquiryEntity, "online_inquiry"));
    OnlineInquiryView onlineInquiryEntityView = onlineInquiryService.selectView(ew);
    return R.ok("查询在线咨询成功").put("data", onlineInquiryEntityView);
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    OnlineInquiryEntity onlineInquiryEntity = onlineInquiryService.getById(id);
    return R.ok().put("data", onlineInquiryEntity);
  }

  /** 前端详情 */
  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id) {
    OnlineInquiryEntity onlineInquiryEntity = onlineInquiryService.getById(id);
    return R.ok().put("data", onlineInquiryEntity);
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody OnlineInquiryEntity onlineInquiryEntity, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(onlineInquiryEntity);
    onlineInquiryService.save(onlineInquiryEntity);
    return R.ok();
  }

  /** 前端保存 */
  @RequestMapping("/add")
  public R add(@RequestBody OnlineInquiryEntity onlineInquiryEntity, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(onlineInquiryEntity);
    onlineInquiryService.save(onlineInquiryEntity);
    return R.ok();
  }

  /** 修改 */
  @RequestMapping("/update")
  @Transactional
  public R update(
      @RequestBody OnlineInquiryEntity onlineInquiryEntity, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(onlineInquiryEntity);
    onlineInquiryService.updateById(onlineInquiryEntity); // 全部更新
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    onlineInquiryService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }
}
