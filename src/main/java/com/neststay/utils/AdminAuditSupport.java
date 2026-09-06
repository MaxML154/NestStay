package com.neststay.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 写入 admin_audit_log，失败不影响主流程。 */
@Component
public class AdminAuditSupport {
  @Autowired private DataSource dataSource;

  public void log(
      HttpServletRequest request, String action, String targetType, String targetId, String detail) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO admin_audit_log(operator_role, operator_account, action, target_type, target_id, detail) VALUES (?,?,?,?,?,?)")) {
      statement.setString(1, String.valueOf(request.getSession().getAttribute("role")));
      statement.setString(2, StringUtils.defaultString(AuthSupport.username(request)));
      statement.setString(3, action);
      statement.setString(4, targetType);
      statement.setString(5, targetId);
      statement.setString(6, detail);
      statement.executeUpdate();
    } catch (Exception ignored) {
      // 审计表未迁移时不阻断业务
    }
  }
}
