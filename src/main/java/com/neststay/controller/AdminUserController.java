package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.config.security.JwtUtil;
import com.neststay.entity.AdminUserEntity;
import com.neststay.service.AdminUserService;
import com.neststay.service.UserTokenService;
import com.neststay.utils.AuthSupport;
import com.neststay.utils.MPUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 登录相关 */
@RequestMapping("users")
@RestController
public class AdminUserController {

  @Autowired private AdminUserService userService;

  @Autowired private UserTokenService tokenService;

  @Autowired private JwtUtil jwtUtil;

  /** 登录 */
  @IgnoreAuth
  @RequestMapping(value = "/login")
  public R login(String username, String password, String captcha, HttpServletRequest request) {
    AdminUserEntity user =
        userService.getOne(new QueryWrapper<AdminUserEntity>().eq("username", username));
    if (user == null || !user.getPassword().equals(password)) {
      return R.error("账号或密码不正确");
    }
    // 生成 JWT token（兼容旧版，同时保留旧 token）
    String jwtToken =
        jwtUtil.generateToken(String.valueOf(user.getId()), user.getRole(), username, "users");
    // 旧版 token（部分旧代码仍依赖）
    String oldToken = tokenService.generateToken(user.getId(), username, "users", user.getRole());
    return R.ok().put("token", jwtToken).put("oldToken", oldToken);
  }

  /** 注册 */
  @IgnoreAuth
  @PostMapping(value = "/register")
  public R register(@RequestBody AdminUserEntity user, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    //    	ValidatorUtils.validateEntity(user);
    if (userService.getOne(new QueryWrapper<AdminUserEntity>().eq("username", user.getUsername()))
        != null) {
      return R.error("用户已存在");
    }
    userService.save(user);
    return R.ok();
  }

  /** 退出 */
  @GetMapping(value = "logout")
  public R logout(HttpServletRequest request) {
    request.getSession().invalidate();
    return R.ok("退出成功");
  }

  /** 密码重置 */
  @IgnoreAuth
  @RequestMapping(value = "/resetPass")
  public R resetPass(String username, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    AdminUserEntity user =
        userService.getOne(new QueryWrapper<AdminUserEntity>().eq("username", username));
    if (user == null) {
      return R.error("账号不存在");
    }
    user.setPassword("123456");
    userService.update(user, null);
    return R.ok("密码已重置为：123456");
  }

  /** 列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      AdminUserEntity user,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<AdminUserEntity> ew = new QueryWrapper<AdminUserEntity>();
    PageUtils page =
        userService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.allLike(ew, user), params), params));
    return R.ok().put("data", page);
  }

  /** 列表 */
  @RequestMapping("/list")
  public R list(AdminUserEntity user, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<AdminUserEntity> ew = new QueryWrapper<AdminUserEntity>();
    ew.allEq(MPUtil.allEQMapPre(user, "user"));
    return R.ok().put("data", userService.selectListView(ew));
  }

  /** 信息 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") String id, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    AdminUserEntity user = userService.getById(id);
    return R.ok().put("data", user);
  }

  /** 获取用户的session用户信息 */
  @RequestMapping("/session")
  public R getCurrUser(HttpServletRequest request) {
    Long id = (Long) request.getSession().getAttribute("userId");
    AdminUserEntity user = userService.getById(id);
    return R.ok().put("data", user);
  }

  /** 保存 */
  @PostMapping("/save")
  public R save(@RequestBody AdminUserEntity user, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    //    	ValidatorUtils.validateEntity(user);
    if (userService.getOne(new QueryWrapper<AdminUserEntity>().eq("username", user.getUsername()))
        != null) {
      return R.error("用户已存在");
    }
    userService.save(user);
    return R.ok();
  }

  /** 修改 */
  @RequestMapping("/update")
  public R update(@RequestBody AdminUserEntity user, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    //        ValidatorUtils.validateEntity(user);
    AdminUserEntity u =
        userService.getOne(new QueryWrapper<AdminUserEntity>().eq("username", user.getUsername()));
    if (u != null && u.getId() != user.getId() && u.getUsername().equals(user.getUsername())) {
      return R.error("用户名已存在。");
    }
    userService.updateById(user); // 全部更新
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    userService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }
}
