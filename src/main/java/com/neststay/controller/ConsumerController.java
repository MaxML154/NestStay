package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.config.security.JwtUtil;
import com.neststay.entity.ConsumerEntity;
import com.neststay.entity.FavoriteEntity;
import com.neststay.entity.ForumPostEntity;
import com.neststay.entity.HomestayRentalEntity;
import com.neststay.entity.NewsArticleEntity;
import com.neststay.entity.view.ConsumerView;
import com.neststay.service.ConsumerService;
import com.neststay.service.FavoriteService;
import com.neststay.service.ForumPostService;
import com.neststay.service.HomestayRentalService;
import com.neststay.service.NewsArticleService;
import com.neststay.service.UserTokenService;
import com.neststay.utils.AdminAuditSupport;
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

/** 消费者 后端接口 */
@RestController
@RequestMapping("/consumer")
public class ConsumerController {
  private static final String SMS_VERIFIED_UNTIL = "smsVerifiedUntil";
  private static final long SMS_TTL_MS = 10 * 60 * 1000L;

  @Autowired private ConsumerService consumerService;
  @Autowired private UserTokenService tokenService;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private HomestayRentalService homestayRentalService;
  @Autowired private ForumPostService forumPostService;
  @Autowired private NewsArticleService newsArticleService;
  @Autowired private FavoriteService favoriteService;
  @Autowired private AdminAuditSupport adminAuditSupport;

  @IgnoreAuth
  @RequestMapping(value = "/login")
  public R login(String username, String password, String captcha, HttpServletRequest request) {
    ConsumerEntity u =
        consumerService.getOne(new QueryWrapper<ConsumerEntity>().eq("account", username));
    if (u == null || !u.getPasswordHash().equals(password)) {
      return R.error("账号或密码不正确");
    }
    String jwtToken = jwtUtil.generateToken(String.valueOf(u.getId()), "消费者", username, "consumer");
    String oldToken = tokenService.generateToken(u.getId(), username, "consumer", "消费者");
    return R.ok().put("token", jwtToken).put("oldToken", oldToken);
  }

  @IgnoreAuth
  @RequestMapping("/loginBySms")
  public R loginBySms() {
    return R.error("手机号验证码登录即将开放");
  }

  @IgnoreAuth
  @RequestMapping("/register")
  public R register(@RequestBody ConsumerEntity consumerEntity) {
    if (consumerEntity == null) return R.error(400, "注册信息不能为空");
    if (StringUtils.isBlank(consumerEntity.getAccount())) return R.error(400, "账号不能为空");
    if (StringUtils.isBlank(consumerEntity.getPasswordHash())) return R.error(400, "密码不能为空");
    String nickname = StringUtils.trimToEmpty(consumerEntity.getNickname());
    String realName = StringUtils.trimToEmpty(consumerEntity.getRealName());
    if (nickname.length() < 2 || nickname.length() > 24) return R.error(400, "用户名长度应为2至24字");
    if (realName.length() < 2 || realName.length() > 20) return R.error(400, "姓名长度应为2至20字");
    if (StringUtils.isBlank(consumerEntity.getIdCardFront())) return R.error(400, "身份证正面不能为空");
    if (StringUtils.isBlank(consumerEntity.getIdCardBack())) return R.error(400, "身份证反面不能为空");
    ConsumerEntity u =
        consumerService.getOne(
            new QueryWrapper<ConsumerEntity>().eq("account", consumerEntity.getAccount().trim()));
    if (u != null) {
      return R.error("注册用户已存在");
    }
    Long uId = new Date().getTime();
    consumerEntity.setId(uId);
    consumerEntity.setAccount(consumerEntity.getAccount().trim());
    consumerEntity.setNickname(nickname);
    consumerEntity.setRealName(realName);
    if (consumerEntity.getPrivacyPosts() == null) consumerEntity.setPrivacyPosts(1);
    if (consumerEntity.getPrivacyArticles() == null) consumerEntity.setPrivacyArticles(1);
    if (consumerEntity.getPrivacyFavorites() == null) consumerEntity.setPrivacyFavorites(1);
    consumerService.save(consumerEntity);
    return R.ok();
  }

  @RequestMapping("/logout")
  public R logout(HttpServletRequest request) {
    request.getSession().invalidate();
    return R.ok("退出成功");
  }

  @RequestMapping("/legacySession")
  public R getCurrUser(HttpServletRequest request) {
    Long id = currentConsumerId(request);
    if (id == null) return consumerAuthError(request);
    ConsumerEntity u = consumerService.getById(id);
    return u == null ? R.error(404, "用户不存在") : R.ok().put("data", profileData(u, true));
  }

  @IgnoreAuth
  @RequestMapping(value = "/resetPass")
  public R resetPass(String username, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    ConsumerEntity u =
        consumerService.getOne(new QueryWrapper<ConsumerEntity>().eq("account", username));
    if (u == null) {
      return R.error("账号不存在");
    }
    u.setPasswordHash("123456");
    consumerService.updateById(u);
    return R.ok("密码已重置为：123456");
  }

  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      ConsumerEntity consumerEntity,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    stripSensitiveQuery(consumerEntity);
    QueryWrapper<ConsumerEntity> ew = new QueryWrapper<ConsumerEntity>();
    PageUtils page =
        consumerService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, consumerEntity), params), params));
    return R.ok().put("data", maskConsumerPage(page));
  }

  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      ConsumerEntity consumerEntity,
      HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    stripSensitiveQuery(consumerEntity);
    QueryWrapper<ConsumerEntity> ew = new QueryWrapper<ConsumerEntity>();
    PageUtils page =
        consumerService.queryPage(
            params,
            MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, consumerEntity), params), params));
    return R.ok().put("data", maskConsumerPage(page));
  }

  @RequestMapping("/lists")
  public R list(ConsumerEntity consumerEntity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    stripSensitiveQuery(consumerEntity);
    QueryWrapper<ConsumerEntity> ew = new QueryWrapper<ConsumerEntity>();
    ew.allEq(MPUtil.allEQMapPre(consumerEntity, "consumer"));
    PageUtils page = new PageUtils(maskConsumerRows(consumerService.selectListView(ew)), 0, 10, 1);
    return R.ok().put("data", page.getList());
  }

  @RequestMapping("/query")
  public R query(ConsumerEntity consumerEntity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    QueryWrapper<ConsumerEntity> ew = new QueryWrapper<ConsumerEntity>();
    ew.allEq(MPUtil.allEQMapPre(consumerEntity, "consumer"));
    ConsumerView consumerEntityView = consumerService.selectView(ew);
    return R.ok("查询消费者成功").put("data", consumerEntityView);
  }

  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id, HttpServletRequest request) {
    ConsumerEntity consumerEntity = consumerService.getById(id);
    if (consumerEntity == null) return R.error(404, "用户不存在");
    if (AuthSupport.isAdmin(request)) {
      adminAuditSupport.log(request, "view_unmasked", "consumer", String.valueOf(id), "info");
      return R.ok().put("data", fullStaffData(consumerEntity));
    }
    if (AuthSupport.isMerchant(request)) {
      if (!relatedToMerchant(consumerEntity, request)) {
        return R.error(403, "仅可查看本店订单相关住客");
      }
      adminAuditSupport.log(request, "view_unmasked", "consumer", String.valueOf(id), "merchant");
      return R.ok().put("data", fullStaffData(consumerEntity));
    }
    return AuthSupport.adminError(request);
  }

  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id, HttpServletRequest request) {
    Long currentId = currentConsumerId(request);
    if (currentId == null) return consumerAuthError(request);
    if (!currentId.equals(id)) return R.error(403, "不能查看其他消费者信息");
    ConsumerEntity consumerEntity = consumerService.getById(id);
    return consumerEntity == null
        ? R.error(404, "用户不存在")
        : R.ok().put("data", profileData(consumerEntity, true));
  }

  @IgnoreAuth
  @RequestMapping("/public/{id}")
  public R publicProfile(@PathVariable("id") Long id) {
    ConsumerEntity user = consumerService.getById(id);
    if (user == null) return R.error(404, "用户不存在");
    boolean showPosts = visible(user.getPrivacyPosts());
    boolean showArticles = visible(user.getPrivacyArticles());
    boolean showFavorites = visible(user.getPrivacyFavorites());
    Map<String, Object> data = new HashMap<>();
    data.put("id", user.getId());
    data.put("account", user.getAccount());
    data.put("nickname", displayNickname(user));
    data.put("avatar", user.getAvatar());
    data.put("bio", user.getBio());
    data.put("profileCover", user.getProfileCover());
    data.put("profileCoverType", user.getProfileCoverType());
    data.put("privacyPosts", showPosts ? 1 : 0);
    data.put("privacyArticles", showArticles ? 1 : 0);
    data.put("privacyFavorites", showFavorites ? 1 : 0);
    List<ForumPostEntity> posts =
        forumPostService.list(
            new QueryWrapper<ForumPostEntity>()
                .eq("user_id", id)
                .and(w -> w.eq("parent_id", 0).or().isNull("parent_id"))
                .orderByDesc("id")
                .last("LIMIT 40"));
    List<NewsArticleEntity> articles =
        newsArticleService.list(
            new QueryWrapper<NewsArticleEntity>().eq("author_id", id).orderByDesc("id").last("LIMIT 40"));
    List<FavoriteEntity> favorites =
        favoriteService.list(
            new QueryWrapper<FavoriteEntity>().eq("user_id", id).orderByDesc("id").last("LIMIT 60"));
    int forumLevel = 1;
    for (ForumPostEntity post : posts) {
      if (post.getAuthorLevel() != null && post.getAuthorLevel() > forumLevel) {
        forumLevel = post.getAuthorLevel();
      }
    }
    data.put("createTime", user.getCreateTime());
    data.put("forumLevel", forumLevel);
    data.put("postCount", posts.size());
    data.put("articleCount", articles.size());
    data.put("favoriteCount", favorites.size());
    data.put("postsHidden", !showPosts);
    data.put("articlesHidden", !showArticles);
    data.put("favoritesHidden", !showFavorites);
    data.put("posts", showPosts ? publicPosts(posts) : new ArrayList<>());
    data.put("articles", showArticles ? publicArticles(articles) : new ArrayList<>());
    data.put("favorites", showFavorites ? publicFavorites(favorites) : new ArrayList<>());
    return R.ok().put("data", data);
  }

  @RequestMapping("/save")
  public R save(@RequestBody ConsumerEntity consumerEntity, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    if (consumerService.count(
            new QueryWrapper<ConsumerEntity>().eq("account", consumerEntity.getAccount()))
        > 0) {
      return R.error("账号已存在");
    }
    consumerEntity.setId(
        new Date().getTime() + Double.valueOf(Math.floor(Math.random() * 1000)).longValue());
    ConsumerEntity u =
        consumerService.getOne(
            new QueryWrapper<ConsumerEntity>().eq("account", consumerEntity.getAccount()));
    if (u != null) {
      return R.error("用户已存在");
    }
    consumerEntity.setId(new Date().getTime());
    consumerService.save(consumerEntity);
    return R.ok();
  }

  @RequestMapping("/add")
  public R add(@RequestBody ConsumerEntity consumerEntity, HttpServletRequest request) {
    return save(consumerEntity, request);
  }

  @RequestMapping("/update")
  @Transactional
  public R update(@RequestBody ConsumerEntity consumerEntity, HttpServletRequest request) {
    if (!isAdmin(request)) {
      return currentUserId(request) == null ? R.error(401, "请先登录") : R.error(403, "消费者请使用个人资料接口");
    }
    if (consumerEntity == null || consumerEntity.getId() == null) {
      return R.error(400, "用户ID不能为空");
    }
    if (consumerService.getById(consumerEntity.getId()) == null) {
      return R.error(404, "用户不存在");
    }
    if (consumerEntity.getAccount() == null) {
      consumerEntity.setAccount(consumerService.getById(consumerEntity.getId()).getAccount());
    }
    if (consumerService.count(
            new QueryWrapper<ConsumerEntity>()
                .ne("id", consumerEntity.getId())
                .eq("account", consumerEntity.getAccount()))
        > 0) {
      return R.error("账号已存在");
    }
    consumerService.updateById(consumerEntity);
    return R.ok();
  }

  @RequestMapping("/session")
  public R profileSession(HttpServletRequest request) {
    Long userId = currentConsumerId(request);
    if (userId == null) return consumerAuthError(request);
    ConsumerEntity user = consumerService.getById(userId);
    return user == null ? R.error(404, "用户不存在") : R.ok().put("data", profileData(user, true));
  }

  @RequestMapping("/verifySms")
  public R verifySms(@RequestBody Map<String, String> body, HttpServletRequest request) {
    if (currentConsumerId(request) == null) return consumerAuthError(request);
    String phone = body == null ? null : StringUtils.trimToEmpty(body.get("phone"));
    String code = body == null ? null : StringUtils.trimToEmpty(body.get("code"));
    if (!code.matches("\\d{6}")) return R.error(400, "请输入6位数字验证码");
    request.getSession().setAttribute(SMS_VERIFIED_UNTIL, System.currentTimeMillis() + SMS_TTL_MS);
    request.getSession().setAttribute("smsVerifiedPhone", phone);
    return R.ok("验证成功");
  }

  @RequestMapping("/profile")
  @Transactional
  public R updateProfile(@RequestBody ConsumerEntity submitted, HttpServletRequest request) {
    Long userId = currentConsumerId(request);
    if (userId == null) return consumerAuthError(request);
    ConsumerEntity existing = consumerService.getById(userId);
    if (existing == null) return R.error(404, "用户不存在");
    if (submitted == null) return R.error(400, "资料不能为空");

    String nickname = StringUtils.trimToNull(submitted.getNickname());
    if (nickname != null) {
      if (nickname.length() < 2 || nickname.length() > 24) return R.error(400, "用户名长度应为2至24字");
      existing.setNickname(nickname);
    }
    if (submitted.getBio() != null) existing.setBio(StringUtils.trimToNull(submitted.getBio()));
    if (submitted.getProfileCover() != null) {
      existing.setProfileCover(MediaPaths.store(submitted.getProfileCover()));
    }
    if (submitted.getProfileCoverType() != null) {
      existing.setProfileCoverType(StringUtils.trimToNull(submitted.getProfileCoverType()));
    }
    if (submitted.getPrivacyPosts() != null) existing.setPrivacyPosts(submitted.getPrivacyPosts());
    if (submitted.getPrivacyArticles() != null)
      existing.setPrivacyArticles(submitted.getPrivacyArticles());
    if (submitted.getPrivacyFavorites() != null)
      existing.setPrivacyFavorites(submitted.getPrivacyFavorites());

    String phone = StringUtils.trimToNull(submitted.getPhone());
    if (phone != null && !phone.matches("^1[3-9]\\d{9}$")) {
      return R.error(400, "手机号格式不正确");
    }
    String gender = StringUtils.trimToNull(submitted.getGender());
    if (gender != null && !Arrays.asList("男", "女", "其他").contains(gender)) {
      return R.error(400, "性别选项不正确");
    }
    if (gender != null) existing.setGender(gender);
    if (submitted.getAvatar() != null) {
      existing.setAvatar(MediaPaths.store(submitted.getAvatar()));
    }

    boolean identityChanged =
        nameChanged(existing.getRealName(), submitted.getRealName())
            || valueChanged(existing.getIdNumber(), submitted.getIdNumber())
            || valueChanged(existing.getIdCardFront(), submitted.getIdCardFront())
            || valueChanged(existing.getIdCardBack(), submitted.getIdCardBack());
    boolean phoneChanged = phone != null && !phone.equals(StringUtils.trimToEmpty(existing.getPhone()));
    if ((identityChanged || phoneChanged) && !smsVerified(request)) {
      return R.error(400, "修改身份信息或手机号前请先完成验证码校验");
    }
    if (StringUtils.isNotBlank(submitted.getRealName())) {
      String realName = submitted.getRealName().trim();
      if (realName.length() < 2 || realName.length() > 20) return R.error(400, "姓名长度应为2至20字");
      existing.setRealName(realName);
    }
    if (submitted.getIdNumber() != null) existing.setIdNumber(submitted.getIdNumber());
    if (submitted.getIdCardFront() != null) existing.setIdCardFront(submitted.getIdCardFront());
    if (submitted.getIdCardBack() != null) existing.setIdCardBack(submitted.getIdCardBack());
    if (phone != null) existing.setPhone(phone);

    consumerService.updateById(existing);
    return R.ok().put("data", profileData(existing, true));
  }

  @RequestMapping("/changePassword")
  @Transactional
  public R changePassword(@RequestBody Map<String, String> passwords, HttpServletRequest request) {
    Long userId = currentConsumerId(request);
    if (userId == null) return consumerAuthError(request);
    ConsumerEntity existing = consumerService.getById(userId);
    if (existing == null) return R.error(404, "用户不存在");
    String currentPassword = passwords == null ? null : passwords.get("currentPassword");
    String newPassword = passwords == null ? null : passwords.get("newPassword");
    if (!existing.getPasswordHash().equals(currentPassword)) {
      return R.error(400, "原密码不正确");
    }
    if (StringUtils.isBlank(newPassword) || newPassword.length() < 6 || newPassword.length() > 32) {
      return R.error(400, "新密码长度应为6至32位");
    }
    if (newPassword.equals(currentPassword)) return R.error(400, "新密码不能与原密码相同");
    existing.setPasswordHash(newPassword);
    consumerService.updateById(existing);
    return R.ok("密码修改成功");
  }

  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
    if (!AuthSupport.isAdmin(request)) return AuthSupport.adminError(request);
    consumerService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  private Map<String, Object> profileData(ConsumerEntity user, boolean owner) {
    Map<String, Object> data = new HashMap<>();
    data.put("id", user.getId());
    data.put("account", user.getAccount());
    data.put("nickname", displayNickname(user));
    data.put("realName", user.getRealName());
    data.put("gender", user.getGender());
    data.put("avatar", user.getAvatar());
    data.put("phone", user.getPhone());
    data.put("bio", user.getBio());
    data.put("profileCover", user.getProfileCover());
    data.put("profileCoverType", user.getProfileCoverType());
    data.put("privacyPosts", user.getPrivacyPosts() == null ? 1 : user.getPrivacyPosts());
    data.put("privacyArticles", user.getPrivacyArticles() == null ? 1 : user.getPrivacyArticles());
    data.put("privacyFavorites", user.getPrivacyFavorites() == null ? 1 : user.getPrivacyFavorites());
    data.put("createTime", user.getCreateTime());
    if (owner) {
      data.put("idNumber", user.getIdNumber());
      data.put("idCardFront", user.getIdCardFront());
      data.put("idCardBack", user.getIdCardBack());
    }
    return data;
  }

  private Map<String, Object> fullStaffData(ConsumerEntity user) {
    Map<String, Object> data = profileData(user, true);
    data.put("idNumberMasked", PrivacyMask.maskIdNumber(user.getIdNumber()));
    return data;
  }

  private PageUtils maskConsumerPage(PageUtils page) {
    if (page == null || page.getList() == null) return page;
    page.setList(maskConsumerRows(page.getList()));
    return page;
  }

  private List<Map<String, Object>> maskConsumerRows(List<?> rows) {
    List<Map<String, Object>> list = new ArrayList<>();
    if (rows == null) return list;
    for (Object item : rows) {
      if (item instanceof ConsumerEntity) {
        list.add(maskedConsumerRow((ConsumerEntity) item));
      }
    }
    return list;
  }

  private Map<String, Object> maskedConsumerRow(ConsumerEntity user) {
    Map<String, Object> row = new HashMap<>();
    row.put("id", user.getId());
    row.put("account", user.getAccount());
    row.put("nickname", displayNickname(user));
    row.put("avatar", user.getAvatar());
    row.put("gender", user.getGender());
    String maskedName = PrivacyMask.maskName(user.getRealName());
    String maskedPhone = PrivacyMask.maskPhone(user.getPhone());
    row.put("realName", maskedName);
    row.put("maskedRealName", maskedName);
    row.put("phone", maskedPhone);
    row.put("maskedPhone", maskedPhone);
    row.put("createTime", user.getCreateTime());
    return row;
  }

  private List<Map<String, Object>> publicPosts(List<ForumPostEntity> posts) {
    List<Map<String, Object>> list = new ArrayList<>();
    for (ForumPostEntity post : posts) {
      Map<String, Object> row = new HashMap<>();
      row.put("id", post.getId());
      row.put("title", post.getTitle());
      row.put("createTime", post.getCreateTime());
      row.put("kind", "forum");
      list.add(row);
    }
    return list;
  }

  private List<Map<String, Object>> publicArticles(List<NewsArticleEntity> articles) {
    List<Map<String, Object>> list = new ArrayList<>();
    for (NewsArticleEntity article : articles) {
      Map<String, Object> row = new HashMap<>();
      row.put("id", article.getId());
      row.put("title", article.getTitle());
      row.put("picture", article.getPicture());
      row.put("createTime", article.getCreateTime());
      row.put("kind", "news");
      list.add(row);
    }
    return list;
  }

  private List<Map<String, Object>> publicFavorites(List<FavoriteEntity> favorites) {
    List<Map<String, Object>> list = new ArrayList<>();
    for (FavoriteEntity favorite : favorites) {
      Map<String, Object> row = new HashMap<>();
      row.put("id", favorite.getId());
      row.put("refid", favorite.getRefid());
      row.put("tableName", favorite.getTableName());
      row.put("name", favorite.getName());
      row.put("picture", favorite.getPicture());
      row.put("kind", favoriteKind(favorite.getTableName()));
      list.add(row);
    }
    return list;
  }

  private String favoriteKind(String tableName) {
    if ("forum".equals(tableName) || "forumPost".equals(tableName)) return "forum";
    if ("news".equals(tableName) || "newsArticle".equals(tableName)) return "news";
    return "stay";
  }

  private boolean relatedToMerchant(ConsumerEntity consumer, HttpServletRequest request) {
    if (consumer == null || StringUtils.isBlank(consumer.getAccount())) return false;
    return homestayRentalService.count(
            new QueryWrapper<HomestayRentalEntity>()
                .eq("merchant_account", AuthSupport.username(request))
                .eq("account", consumer.getAccount()))
        > 0;
  }

  private boolean smsVerified(HttpServletRequest request) {
    Object value = request.getSession().getAttribute(SMS_VERIFIED_UNTIL);
    if (value == null) return false;
    try {
      return Long.parseLong(value.toString()) >= System.currentTimeMillis();
    } catch (NumberFormatException exception) {
      return false;
    }
  }

  private boolean visible(Integer flag) {
    return flag == null || flag != 0;
  }

  private String displayNickname(ConsumerEntity user) {
    if (user == null) return "";
    if (StringUtils.isNotBlank(user.getNickname())) return user.getNickname();
    return StringUtils.defaultString(user.getAccount());
  }

  private boolean nameChanged(String current, String next) {
    return StringUtils.isNotBlank(next) && !StringUtils.trimToEmpty(next).equals(StringUtils.trimToEmpty(current));
  }

  private boolean valueChanged(String current, String next) {
    return next != null && !StringUtils.trimToEmpty(next).equals(StringUtils.trimToEmpty(current));
  }

  private void stripSensitiveQuery(ConsumerEntity consumerEntity) {
    if (consumerEntity == null) return;
    consumerEntity.setIdNumber(null);
    consumerEntity.setIdCardFront(null);
    consumerEntity.setIdCardBack(null);
    consumerEntity.setPhone(null);
    consumerEntity.setRealName(null);
  }

  private Long currentUserId(HttpServletRequest request) {
    Object value = request.getSession().getAttribute("userId");
    if (value == null) return null;
    try {
      return Long.valueOf(value.toString());
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private Long currentConsumerId(HttpServletRequest request) {
    return "consumer".equals(String.valueOf(request.getSession().getAttribute("tableName")))
        ? currentUserId(request)
        : null;
  }

  private boolean isAdmin(HttpServletRequest request) {
    return "管理员".equals(String.valueOf(request.getSession().getAttribute("role")));
  }

  private R consumerAuthError(HttpServletRequest request) {
    return currentUserId(request) == null ? R.error(401, "请先登录") : R.error(403, "仅消费者可操作个人资料");
  }
}
