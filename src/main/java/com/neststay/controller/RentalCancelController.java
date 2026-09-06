package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.entity.RentalCancelEntity;
import com.neststay.entity.view.RentalCancelView;
import com.neststay.service.RentalCancelService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租赁取消 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@RestController
@RequestMapping("/rentalCancel")
public class RentalCancelController {
  @Autowired private RentalCancelService rentalCancelService;

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      RentalCancelEntity rentalCancelEntity,
      HttpServletRequest request) {
    if (AuthSupport.userId(request) == null) return R.error(401, "请先登录");
    applyOwnerScope(rentalCancelEntity, request);
    QueryWrapper<RentalCancelEntity> ew = new QueryWrapper<RentalCancelEntity>();

    PageUtils page =
        rentalCancelService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, rentalCancelEntity), params), params));

    return R.ok().put("data", page);
  }

  /** 前端列表，仅本人、所属商家或管理员可读。 */
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      RentalCancelEntity rentalCancelEntity,
      HttpServletRequest request) {
    return page(params, rentalCancelEntity, request);
  }

  /** 列表 */
  @RequestMapping("/lists")
  public R list(RentalCancelEntity rentalCancelEntity, HttpServletRequest request) {
    if (AuthSupport.userId(request) == null) return R.error(401, "请先登录");
    applyOwnerScope(rentalCancelEntity, request);
    QueryWrapper<RentalCancelEntity> ew = new QueryWrapper<RentalCancelEntity>();
    ew.allEq(MPUtil.allEQMapPre(rentalCancelEntity, "rental_cancel"));
    return R.ok().put("data", rentalCancelService.selectListView(ew));
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(RentalCancelEntity rentalCancelEntity, HttpServletRequest request) {
    if (AuthSupport.userId(request) == null) return R.error(401, "请先登录");
    applyOwnerScope(rentalCancelEntity, request);
    QueryWrapper<RentalCancelEntity> ew = new QueryWrapper<RentalCancelEntity>();
    ew.allEq(MPUtil.allEQMapPre(rentalCancelEntity, "rental_cancel"));
    RentalCancelView rentalCancelEntityView = rentalCancelService.selectView(ew);
    if (rentalCancelEntityView == null) return R.error(404, "记录不存在");
    RentalCancelEntity existing = rentalCancelService.getById(rentalCancelEntityView.getId());
    if (existing == null || !canAccess(existing, request)) return R.error(404, "记录不存在");
    return R.ok("查询租赁取消成功").put("data", rentalCancelEntityView);
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id, HttpServletRequest request) {
    return readCancel(id, request);
  }

  /** 前端详情，仅本人、所属商家或管理员可读。 */
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id, HttpServletRequest request) {
    return readCancel(id, request);
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody RentalCancelEntity rentalCancelEntity, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(rentalCancelEntity);
    rentalCancelService.save(rentalCancelEntity);
    return R.ok();
  }

  /** 前端保存 */
  @RequestMapping("/add")
  public R add(@RequestBody RentalCancelEntity rentalCancelEntity, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(rentalCancelEntity);
    rentalCancelService.save(rentalCancelEntity);
    return R.ok();
  }

  /** 修改 */
  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody RentalCancelEntity rentalCancelEntity, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(rentalCancelEntity);
    rentalCancelService.updateById(rentalCancelEntity); // 全部更新
    return R.ok();
  }

  /** 审核 */
  @RequestMapping("/shBatch")
  @Transactional
  public R update(@RequestBody Long[] ids, @RequestParam String sfsh, @RequestParam String shhf) {
    List<RentalCancelEntity> list = new ArrayList<RentalCancelEntity>();
    for (Long id : ids) {
      RentalCancelEntity rentalCancelEntity = rentalCancelService.getById(id);
      rentalCancelEntity.setApprovalStatus(sfsh);
      rentalCancelEntity.setApprovalReply(shhf);
      list.add(rentalCancelEntity);
    }
    rentalCancelService.updateBatchById(list);
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    rentalCancelService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  private R readCancel(Long id, HttpServletRequest request) {
    if (AuthSupport.userId(request) == null) return R.error(401, "请先登录");
    RentalCancelEntity entity = rentalCancelService.getById(id);
    if (entity == null || !canAccess(entity, request)) return R.error(404, "记录不存在");
    return R.ok().put("data", entity);
  }

  private void applyOwnerScope(RentalCancelEntity entity, HttpServletRequest request) {
    if (entity == null) return;
    if (AuthSupport.isMerchant(request)) {
      entity.setMerchantAccount(AuthSupport.username(request));
    } else if (AuthSupport.isConsumer(request)) {
      entity.setAccount(AuthSupport.username(request));
    }
  }

  private boolean canAccess(RentalCancelEntity entity, HttpServletRequest request) {
    if (entity == null) return false;
    if (AuthSupport.isAdmin(request)) return true;
    String username = AuthSupport.username(request);
    if (AuthSupport.isMerchant(request)) {
      return username != null && username.equals(entity.getMerchantAccount());
    }
    if (AuthSupport.isConsumer(request)) {
      return username != null && username.equals(entity.getAccount());
    }
    return false;
  }
}
