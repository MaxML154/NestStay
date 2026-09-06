package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.OnlineReplyEntity;
import com.neststay.entity.view.OnlineReplyView;
import com.neststay.service.OnlineReplyService;
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
 * 在线回复 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@RestController
@RequestMapping("/onlineReply")
public class OnlineReplyController {
  @Autowired private OnlineReplyService onlineReplyService;

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      OnlineReplyEntity onlineReplyEntity,
      HttpServletRequest request) {
    Object tableNameAttr = request.getSession().getAttribute("tableName");
    String tableName = tableNameAttr != null ? tableNameAttr.toString() : "";
    if (tableName.equals("merchant")) {
      onlineReplyEntity.setMerchantAccount((String) request.getSession().getAttribute("username"));
    }
    if (tableName.equals("consumer")) {
      onlineReplyEntity.setAccount((String) request.getSession().getAttribute("username"));
    }
    QueryWrapper<OnlineReplyEntity> ew = new QueryWrapper<OnlineReplyEntity>();

    PageUtils page =
        onlineReplyService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, onlineReplyEntity), params), params));

    return R.ok().put("data", page);
  }

  /** 前端列表 */
  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      OnlineReplyEntity onlineReplyEntity,
      HttpServletRequest request) {
    QueryWrapper<OnlineReplyEntity> ew = new QueryWrapper<OnlineReplyEntity>();

    PageUtils page =
        onlineReplyService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, onlineReplyEntity), params), params));
    return R.ok().put("data", page);
  }

  /** 列表 */
  @RequestMapping("/lists")
  public R list(OnlineReplyEntity onlineReplyEntity) {
    QueryWrapper<OnlineReplyEntity> ew = new QueryWrapper<OnlineReplyEntity>();
    ew.allEq(MPUtil.allEQMapPre(onlineReplyEntity, "online_reply"));
    return R.ok().put("data", onlineReplyService.selectListView(ew));
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(OnlineReplyEntity onlineReplyEntity) {
    QueryWrapper<OnlineReplyEntity> ew = new QueryWrapper<OnlineReplyEntity>();
    ew.allEq(MPUtil.allEQMapPre(onlineReplyEntity, "online_reply"));
    OnlineReplyView onlineReplyEntityView = onlineReplyService.selectView(ew);
    return R.ok("查询在线回复成功").put("data", onlineReplyEntityView);
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    OnlineReplyEntity onlineReplyEntity = onlineReplyService.getById(id);
    return R.ok().put("data", onlineReplyEntity);
  }

  /** 前端详情 */
  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id) {
    OnlineReplyEntity onlineReplyEntity = onlineReplyService.getById(id);
    return R.ok().put("data", onlineReplyEntity);
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody OnlineReplyEntity onlineReplyEntity, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(onlineReplyEntity);
    onlineReplyService.save(onlineReplyEntity);
    return R.ok();
  }

  /** 前端保存 */
  @RequestMapping("/add")
  public R add(@RequestBody OnlineReplyEntity onlineReplyEntity, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(onlineReplyEntity);
    onlineReplyService.save(onlineReplyEntity);
    return R.ok();
  }

  /** 修改 */
  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody OnlineReplyEntity onlineReplyEntity, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(onlineReplyEntity);
    onlineReplyService.updateById(onlineReplyEntity); // 全部更新
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    onlineReplyService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }
}
