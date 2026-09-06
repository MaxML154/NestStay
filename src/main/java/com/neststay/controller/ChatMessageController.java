package com.neststay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neststay.annotation.IgnoreAuth;
import com.neststay.entity.ChatMessageEntity;
import com.neststay.entity.view.ChatMessageView;
import com.neststay.service.ChatMessageService;
import com.neststay.utils.AuthSupport;
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
 * 联系平台客服 后端接口
 *
 * @author
 * @email
 * @date 2024-03-29 22:30:35
 */
@RestController
@RequestMapping({"/chatMessage", "/chat"})
public class ChatMessageController {
  @Autowired private ChatMessageService chatService;

  /** 后端列表 */
  @RequestMapping("/page")
  public R page(
      @RequestParam Map<String, Object> params,
      ChatMessageEntity chat,
      HttpServletRequest request) {
    // 兼容前端旧的列名：addtime → create_time
    if ("addtime".equals(params.get("sort"))) {
      params.put("sort", "create_time");
    }
    applyLegacyParams(params, chat);
    if (!AuthSupport.isAdmin(request)) {
      Long userId = AuthSupport.userId(request);
      if (userId != null) {
        chat.setUserId(userId);
      }
    }
    QueryWrapper<ChatMessageEntity> ew = new QueryWrapper<ChatMessageEntity>();

    PageUtils page =
        chatService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chat), params), params));

    return R.ok().put("data", page);
  }

  /** 前端列表 */
  @IgnoreAuth
  @RequestMapping("/list")
  public R list(
      @RequestParam Map<String, Object> params,
      ChatMessageEntity chat,
      HttpServletRequest request) {
    applyLegacyParams(params, chat);
    if (!AuthSupport.isAdmin(request)) {
      Long userId = AuthSupport.userId(request);
      if (userId != null) {
        chat.setUserId(userId);
      }
    }
    QueryWrapper<ChatMessageEntity> ew = new QueryWrapper<ChatMessageEntity>();

    PageUtils page =
        chatService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chat), params), params));
    return R.ok().put("data", page);
  }

  /** 列表 */
  @RequestMapping("/lists")
  public R list(ChatMessageEntity chat) {
    QueryWrapper<ChatMessageEntity> ew = new QueryWrapper<ChatMessageEntity>();
    ew.allEq(MPUtil.allEQMapPre(chat, "chat"));
    return R.ok().put("data", chatService.selectListView(ew));
  }

  /** 查询 */
  @RequestMapping("/query")
  public R query(ChatMessageEntity chat) {
    QueryWrapper<ChatMessageEntity> ew = new QueryWrapper<ChatMessageEntity>();
    ew.allEq(MPUtil.allEQMapPre(chat, "chat"));
    ChatMessageView chatView = chatService.selectView(ew);
    return R.ok("查询联系平台客服成功").put("data", chatView);
  }

  /** 后端详情 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    ChatMessageEntity chat = chatService.getById(id);
    return R.ok().put("data", chat);
  }

  /** 前端详情 */
  @IgnoreAuth
  @RequestMapping("/detail/{id}")
  public R detail(@PathVariable("id") Long id) {
    ChatMessageEntity chat = chatService.getById(id);
    return R.ok().put("data", chat);
  }

  /** 后端保存 */
  @RequestMapping("/save")
  public R save(@RequestBody ChatMessageEntity chat, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(chat);
    if (StringUtils.isNotBlank(chat.getAsk())) {
      ChatMessageEntity updateEntity = new ChatMessageEntity();
      updateEntity.setIsReplied(0);
      chatService.update(
          updateEntity,
          new QueryWrapper<ChatMessageEntity>()
              .eq("user_id", request.getSession().getAttribute("userId")));
      chat.setUserId((Long) request.getSession().getAttribute("userId"));
      chat.setIsReplied(1);
    }
    if (StringUtils.isNotBlank(chat.getReply())) {
      ChatMessageEntity updateEntity = new ChatMessageEntity();
      updateEntity.setIsReplied(0);
      chatService.update(
          updateEntity, new QueryWrapper<ChatMessageEntity>().eq("user_id", chat.getUserId()));
      // 智能助手回复时，如果 userId 未设置，从 session 获取
      if (chat.getUserId() == null) {
        chat.setUserId((Long) request.getSession().getAttribute("userId"));
      }
      chat.setAdminId((Long) request.getSession().getAttribute("userId"));
    }
    chatService.save(chat);
    return R.ok();
  }

  /** 前端保存 */
  @RequestMapping("/add")
  public R add(@RequestBody ChatMessageEntity chat, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(chat);
    chat.setUserId((Long) request.getSession().getAttribute("userId"));
    if (StringUtils.isNotBlank(chat.getAsk())) {
      ChatMessageEntity updateEntity = new ChatMessageEntity();
      updateEntity.setIsReplied(0);
      chatService.update(
          updateEntity,
          new QueryWrapper<ChatMessageEntity>()
              .eq("user_id", request.getSession().getAttribute("userId")));
      chat.setUserId((Long) request.getSession().getAttribute("userId"));
      chat.setIsReplied(1);
    }
    if (StringUtils.isNotBlank(chat.getReply())) {
      ChatMessageEntity updateEntity = new ChatMessageEntity();
      updateEntity.setIsReplied(0);
      chatService.update(
          updateEntity, new QueryWrapper<ChatMessageEntity>().eq("user_id", chat.getUserId()));
      chat.setAdminId((Long) request.getSession().getAttribute("userId"));
    }
    chatService.save(chat);
    return R.ok();
  }

  /** 获取用户密保 */
  @RequestMapping("/security")
  @IgnoreAuth
  public R security(@RequestParam String username) {
    ChatMessageEntity chat =
        chatService.getOne(new QueryWrapper<ChatMessageEntity>().eq("", username));
    return R.ok().put("data", chat);
  }

  /** 修改 */
  @RequestMapping("/update")
  @Transactional
  @IgnoreAuth
  public R update(@RequestBody ChatMessageEntity chat, HttpServletRequest request) {
    // ValidatorUtils.validateEntity(chat);
    chatService.updateById(chat); // 全部更新
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    chatService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  private void applyLegacyParams(Map<String, Object> params, ChatMessageEntity chat) {
    if (params == null || chat == null) return;
    Object isreply = params.get("isreply");
    if (isreply != null && chat.getIsReplied() == null) {
      try {
        chat.setIsReplied(Integer.valueOf(isreply.toString()));
      } catch (NumberFormatException ignored) {
      }
      params.remove("isreply");
    }
    Object userid = params.get("userid");
    if (userid != null && chat.getUserId() == null) {
      try {
        chat.setUserId(Long.valueOf(userid.toString()));
      } catch (NumberFormatException ignored) {
      }
      params.remove("userid");
    }
  }

  /** 前端智能排序 */
  @IgnoreAuth
  @RequestMapping("/autoSort")
  public R autoSort(
      @RequestParam Map<String, Object> params,
      ChatMessageEntity chat,
      HttpServletRequest request,
      String pre) {
    QueryWrapper<ChatMessageEntity> ew = new QueryWrapper<ChatMessageEntity>();
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
        chatService.queryPage(
            params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chat), params), params));
    return R.ok().put("data", page);
  }
}
