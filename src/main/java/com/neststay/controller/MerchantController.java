package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.config.security.JwtUtil;
import com.neststay.entity.MerchantEntity;
import com.neststay.entity.view.MerchantView;
import com.neststay.service.MerchantService;
import com.neststay.service.UserTokenService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.MPUtil;
import com.neststay.utils.MediaPaths;
import com.neststay.utils.PageUtils;
import com.neststay.utils.PrivacyMask;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
 * 商家 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:34
 */
@RestController
@RequestMapping("/merchant")
public class MerchantController {
  @Autowired private MerchantService merchantService;

  @Autowired private UserTokenService tokenService;

  @Autowired private JwtUtil jwtUtil;

  /** 登录 */
  @IgnoreAuth
  @RequestMapping(value = "/login")
  public R login(String username, String password, String captcha, HttpServletRequest request) {
    MerchantEntity u =
        merchantService.getOne(new QueryWrapper<MerchantEntity>().eq("merchant_account", username));
    if (u == null || !u.getPasswordHash().equals(password)) {
      return R.error("账号或密码不正确");
    }

    String jwtToken = jwtUtil.generateToken(String.valueOf(u.getId()), "商家", username, "merchant");
    String oldToken = tokenService.generateToken(u.getId(), username, "merchant", "商家");
    return R.ok().put("token", jwtToken).put("oldToken", oldToken);
  }

  /** 注册 */
  @IgnoreAuth
  @RequestMapping("/register")
  public R register(@RequestBody MerchantEntity merchant) {
    if (merchant == null) return R.error(400, "注册信息不能为空");
    if (StringUtils.isBlank(merchant.getMerchantAccount())) return R.error(400, "商家账号不能为空");
    if (StringUtils.isBlank(merchant.getPasswordHash())) return R.error(400, "密码不能为空");
    if (StringUtils.isBlank(merchant.getMerchantName())) return R.error(400, "商家名称不能为空");
    MerchantEntity u =
        merchantService.getOne(
            new QueryWrapper<MerchantEntity>()
                .eq("merchant_account", merchant.getMerchantAccount().trim()));
    if (u != null) {
      return R.error("注册用户已存在");
    }
    Long uId = new Date().getTime();
    merchant.setId(uId);
    merchant.setMerchantAccount(merchant.getMerchantAccount().trim());
    merchantService.save(merchant);
    return R.ok();
  }

  /** 退出 */
  @RequestMapping("/logout")
  public R logout(HttpServletRequest request) {
    request.getSession().invalidate();
    return R.ok("退出成功");
  }

  /** 获取当前登录商家的资料，不含密码。 */
  @RequestMapping("/session")
  public R getCurrUser(HttpServletRequest request) {
    Long id = currentMerchantId(request);
    if (id == null) return merchantAuthError(request);
    MerchantEntity u = merchantService.getById(id);
    return u == null ? R.error(404, "用户不存在") : R.ok().put("data", profileData(u));
  }

  /** 密码重置 */
  @IgnoreAuth
  @RequestMapping(value = "/resetPass")
  public R resetPass(String username, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    MerchantEntity u =
        merchantService.getOne(new QueryWrapper<MerchantEntity>().eq("merchant_account", username));
    if (u == null) {
      return R.error("账号不存在");
    }
    u.setPasswordHash("123456");
    merchantService.updateById(u);
    return R.ok("密码已重置为：123456");
  }

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      MerchantEntity merchant,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<MerchantEntity> ew = new QueryWrapper<MerchantEntity>();

    PageUtils page =
        merchantService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, merchant), params), params));

    return R.ok().put("data", toStaffListPage(page));
  }

  /** 前端列表 */
  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      MerchantEntity merchant,
      HttpServletRequest request) {
    QueryWrapper<MerchantEntity> ew = new QueryWrapper<MerchantEntity>();

    PageUtils page =
        merchantService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, merchant), params), params));
    return R.ok().put("data", toPublicPage(page));
  }

  /** 列表 */
  @RequestMapping("/lists")
  public R list(MerchantEntity merchant, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<MerchantEntity> ew = new QueryWrapper<MerchantEntity>();
    ew.allEq(MPUtil.allEQMapPre(merchant, "merchant"));
    return R.ok().put("data", maskMerchantRows(merchantService.selectListView(ew)));
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(MerchantEntity merchant, HttpServletRequest request) {
    QueryWrapper<MerchantEntity> ew = new QueryWrapper<MerchantEntity>();
    ew.allEq(MPUtil.allEQMapPre(merchant, "merchant"));
    MerchantView merchantView = merchantService.selectView(ew);
    if (merchantView == null) return R.error(404, "商家不存在");
    if (AuthSupport.isAdmin(request) || isSelf(merchantView, request)) {
      merchantView.setPasswordHash(null);
      return R.ok("查询商家成功").put("data", merchantView);
    }
    return R.ok("查询商家成功").put("data", publicMerchantData(merchantView));
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    MerchantEntity merchant = merchantService.getById(id);
    if (merchant == null) return R.error(404, "商家不存在");
    merchant.setPasswordHash(null);
    return R.ok().put("data", merchant);
  }

  /** 前端详情，公开接口不返回营业执照和身份证。 */
  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id, HttpServletRequest request) {
    MerchantEntity merchant = merchantService.getById(id);
    if (merchant == null) return R.error(404, "商家不存在");
    if (AuthSupport.isAdmin(request) || isSelf(merchant, request)) {
      merchant.setPasswordHash(null);
      return R.ok().put("data", merchant);
    }
    return R.ok().put("data", publicMerchantData(merchant));
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody MerchantEntity merchant, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (merchantService.count(
            new QueryWrapper<MerchantEntity>()
                .eq("merchant_account", merchant.getMerchantAccount()))
        > 0) {
      return R.error("商家账号已存在");
    }
    merchant.setId(
        new Date().getTime() + Double.valueOf(Math.floor(Math.random() * 1000)).longValue());
    // ValidatorUtils.validateEntity(merchant);
    MerchantEntity u =
        merchantService.getOne(
            new QueryWrapper<MerchantEntity>()
                .eq("merchant_account", merchant.getMerchantAccount()));
    if (u != null) {
      return R.error("用户已存在");
    }
    merchant.setId(new Date().getTime());
    merchantService.save(merchant);
    return R.ok();
  }

  /** 前端保存 */
  @RequestMapping("/add")
  public R add(@RequestBody MerchantEntity merchant, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (merchantService.count(
            new QueryWrapper<MerchantEntity>()
                .eq("merchant_account", merchant.getMerchantAccount()))
        > 0) {
      return R.error("商家账号已存在");
    }
    merchant.setId(
        new Date().getTime() + Double.valueOf(Math.floor(Math.random() * 1000)).longValue());
    // ValidatorUtils.validateEntity(merchant);
    MerchantEntity u =
        merchantService.getOne(
            new QueryWrapper<MerchantEntity>()
                .eq("merchant_account", merchant.getMerchantAccount()));
    if (u != null) {
      return R.error("用户已存在");
    }
    merchant.setId(new Date().getTime());
    merchantService.save(merchant);
    return R.ok();
  }

  /** 修改：管理员可改任意商家；商家只能改自己的资料字段。 */
  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody MerchantEntity merchant, HttpServletRequest request) {
    if (AuthSupport.isAdmin(request)) return adminUpdate(merchant);
    if (AuthSupport.isMerchant(request)) return selfUpdate(merchant, request);
    return AuthSupport.userId(request) == null
        ? R.error(401, "请先登录")
        : R.error(403, "无权修改商家资料");
  }

  /** 商家修改自己的登录密码。 */
  @RequestMapping("/changePassword")
  @Transactional
  public R changePassword(@RequestBody Map<String, String> passwords, HttpServletRequest request) {
    Long userId = currentMerchantId(request);
    if (userId == null) return merchantAuthError(request);
    MerchantEntity existing = merchantService.getById(userId);
    if (existing == null) return R.error(404, "用户不存在");
    String currentPassword = passwords == null ? null : passwords.get("currentPassword");
    String newPassword = passwords == null ? null : passwords.get("newPassword");
    if (existing.getPasswordHash() == null || !existing.getPasswordHash().equals(currentPassword)) {
      return R.error(400, "原密码不正确");
    }
    if (StringUtils.isBlank(newPassword) || newPassword.length() < 6 || newPassword.length() > 32) {
      return R.error(400, "新密码长度应为6至32位");
    }
    if (newPassword.equals(currentPassword)) return R.error(400, "新密码不能与原密码相同");
    existing.setPasswordHash(newPassword);
    merchantService.updateById(existing);
    return R.ok("密码修改成功");
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    merchantService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  private R adminUpdate(MerchantEntity merchant) {
    if (merchant == null || merchant.getId() == null) return R.error(400, "商家ID不能为空");
    MerchantEntity existing = merchantService.getById(merchant.getId());
    if (existing == null) return R.error(404, "商家不存在");
    if (StringUtils.isBlank(merchant.getMerchantAccount())) {
      merchant.setMerchantAccount(existing.getMerchantAccount());
    }
    if (merchantService.count(
            new QueryWrapper<MerchantEntity>()
                .ne("id", merchant.getId())
                .eq("merchant_account", merchant.getMerchantAccount()))
        > 0) {
      return R.error("商家账号已存在");
    }
    if (StringUtils.isBlank(merchant.getPasswordHash())) {
      merchant.setPasswordHash(null);
    }
    merchantService.updateById(merchant);
    return R.ok();
  }

  private R selfUpdate(MerchantEntity submitted, HttpServletRequest request) {
    Long userId = currentMerchantId(request);
    MerchantEntity existing = merchantService.getById(userId);
    if (existing == null) return R.error(404, "用户不存在");
    if (submitted != null
        && submitted.getId() != null
        && !userId.equals(submitted.getId())) {
      return R.error(403, "不能修改其他商家资料");
    }
    if (submitted == null || StringUtils.isBlank(submitted.getMerchantName())) {
      return R.error(400, "商家名称不能为空");
    }
    String phone = StringUtils.trimToNull(submitted.getMerchantPhone());
    if (phone != null && !phone.matches("^1[3-9]\\d{9}$")) {
      return R.error(400, "商家电话格式不正确");
    }
    existing.setMerchantName(submitted.getMerchantName().trim());
    existing.setMerchantAddress(StringUtils.trimToNull(submitted.getMerchantAddress()));
    existing.setMerchantPhone(phone);
    if (submitted.getAvatar() != null) {
      existing.setAvatar(MediaPaths.store(submitted.getAvatar()));
    }
    existing.setBusinessLicense(StringUtils.trimToNull(submitted.getBusinessLicense()));
    existing.setIdCardFront(StringUtils.trimToNull(submitted.getIdCardFront()));
    existing.setIdCardBack(StringUtils.trimToNull(submitted.getIdCardBack()));
    merchantService.updateById(existing);
    return R.ok().put("data", profileData(existing));
  }

  private Map<String, Object> profileData(MerchantEntity user) {
    Map<String, Object> data = new HashMap<>();
    data.put("id", user.getId());
    data.put("merchantAccount", user.getMerchantAccount());
    data.put("merchantName", user.getMerchantName());
    data.put("merchantAddress", user.getMerchantAddress());
    data.put("merchantPhone", user.getMerchantPhone());
    data.put("avatar", user.getAvatar());
    data.put("businessLicense", user.getBusinessLicense());
    data.put("idCardFront", user.getIdCardFront());
    data.put("idCardBack", user.getIdCardBack());
    data.put("createTime", user.getCreateTime());
    return data;
  }

  private Map<String, Object> publicMerchantData(MerchantEntity user) {
    Map<String, Object> data = new HashMap<>();
    if (user == null) return data;
    data.put("id", user.getId());
    data.put("merchantAccount", user.getMerchantAccount());
    data.put("merchantName", user.getMerchantName());
    data.put("merchantAddress", user.getMerchantAddress());
    data.put("merchantPhone", user.getMerchantPhone());
    data.put("avatar", user.getAvatar());
    data.put("createTime", user.getCreateTime());
    return data;
  }

  private PageUtils toPublicPage(PageUtils page) {
    if (page == null || page.getList() == null) return page;
    List<Map<String, Object>> publicList = new ArrayList<>();
    for (Object item : page.getList()) {
      if (item instanceof MerchantEntity) {
        publicList.add(publicMerchantData((MerchantEntity) item));
      }
    }
    page.setList(publicList);
    return page;
  }

  private PageUtils toStaffListPage(PageUtils page) {
    if (page == null || page.getList() == null) return page;
    page.setList(maskMerchantRows(page.getList()));
    return page;
  }

  private List<Map<String, Object>> maskMerchantRows(List<?> rows) {
    List<Map<String, Object>> list = new ArrayList<>();
    if (rows == null) return list;
    for (Object item : rows) {
      if (item instanceof MerchantEntity) {
        MerchantEntity merchant = (MerchantEntity) item;
        Map<String, Object> row = new HashMap<>();
        row.put("id", merchant.getId());
        row.put("merchantAccount", merchant.getMerchantAccount());
        row.put("merchantName", merchant.getMerchantName());
        row.put("merchantAddress", merchant.getMerchantAddress());
        row.put("merchantPhone", PrivacyMask.maskPhone(merchant.getMerchantPhone()));
        row.put("avatar", merchant.getAvatar());
        row.put("createTime", merchant.getCreateTime());
        list.add(row);
      }
    }
    return list;
  }

  private boolean isSelf(MerchantEntity merchant, HttpServletRequest request) {
    return merchant != null
        && merchant.getId() != null
        && AuthSupport.isMerchant(request)
        && merchant.getId().equals(AuthSupport.userId(request));
  }

  private Long currentMerchantId(HttpServletRequest request) {
    return AuthSupport.isMerchant(request) ? AuthSupport.userId(request) : null;
  }

  private R merchantAuthError(HttpServletRequest request) {
    return AuthSupport.userId(request) == null
        ? R.error(401, "请先登录")
        : R.error(403, "仅商家可操作商家资料");
  }
}
