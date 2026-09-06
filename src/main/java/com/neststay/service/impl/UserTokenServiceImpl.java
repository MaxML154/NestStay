package com.neststay.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neststay.dao.UserTokenDao;
import com.neststay.entity.UserTokenEntity;
import com.neststay.service.UserTokenService;
import com.neststay.utils.CommonUtil;
import com.neststay.utils.PageUtils;
import com.neststay.utils.Query;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** token */
@Service("tokenService")
public class UserTokenServiceImpl extends ServiceImpl<UserTokenDao, UserTokenEntity>
    implements UserTokenService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    Page<UserTokenEntity> pageResult =
        this.page(
            new Query<UserTokenEntity>(params).getPage(), new QueryWrapper<UserTokenEntity>());
    return new PageUtils(pageResult);
  }

  @Override
  public List<UserTokenEntity> selectListView(Wrapper<UserTokenEntity> wrapper) {
    return baseMapper.selectListView(wrapper);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, Wrapper<UserTokenEntity> wrapper) {
    Page<UserTokenEntity> page = new Query<UserTokenEntity>(params).getPage();
    page.setRecords(baseMapper.selectListView(page, wrapper));
    PageUtils pageUtil = new PageUtils(page);
    return pageUtil;
  }

  @Override
  public String generateToken(Long userid, String username, String tableName, String role) {
    UserTokenEntity tokenEntity =
        this.getOne(new QueryWrapper<UserTokenEntity>().eq("user_id", userid).eq("role", role));
    String token = CommonUtil.getRandomString(32);
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.HOUR_OF_DAY, 1);
    if (tokenEntity != null) {
      tokenEntity.setToken(token);
      tokenEntity.setExpireTime(cal.getTime());
      this.updateById(tokenEntity);
    } else {
      this.save(new UserTokenEntity(userid, username, tableName, role, token, cal.getTime()));
    }
    return token;
  }

  @Override
  public UserTokenEntity getTokenEntity(String token) {
    UserTokenEntity tokenEntity =
        this.getOne(new QueryWrapper<UserTokenEntity>().eq("user_token", token));
    if (tokenEntity == null || tokenEntity.getExpireTime().getTime() < new Date().getTime()) {
      return null;
    }
    return tokenEntity;
  }
}
