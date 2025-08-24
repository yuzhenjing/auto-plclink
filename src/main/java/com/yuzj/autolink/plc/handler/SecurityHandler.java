// 文件路径: src/main/java/com/yuzj/autolink/plc/security/SecurityManager.java
package com.yuzj.autolink.plc.handler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全管理控制类
 * 负责处理权限控制和退出功能
 *
 * @author yuzj002
 */
@Slf4j
public class SecurityHandler {

    // 单例实例
    private static SecurityHandler instance;

    // 用户权限定义
    @Getter
    public enum UserRole {
        ADMIN("admin", "系统管理员"),
        OPERATOR("operator", "操作员"),
        VIEWER("viewer", "查看员");

        private final String roleName;
        private final String description;

        UserRole(String roleName, String description) {
            this.roleName = roleName;
            this.description = description;
        }

    }

    // 用户凭证类
    public static class UserCredential {
        private final String username;
        private final String password;
        private final UserRole role;

        public UserCredential(String username, String password, UserRole role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public UserRole getRole() {
            return role;
        }
    }

    // 预定义用户账号密码和权限
    private final Map<String, UserCredential> users = new HashMap<>();

    // 当前登录用户
    private UserCredential currentUser;

    /**
     * 私有构造函数
     */
    private SecurityHandler() {
        initializeUsers();
    }

    /**
     * 获取安全管理器单例实例
     *
     * @return SecurityManager实例
     */
    public static synchronized SecurityHandler getInstance() {
        if (instance == null) {
            instance = new SecurityHandler();
        }
        return instance;
    }

    /**
     * 初始化预定义用户
     */
    private void initializeUsers() {
        // 管理员用户 - 拥有所有权限，包括退出程序
        users.put("admin", new UserCredential("admin", "admin123", UserRole.ADMIN));

        // 操作员用户 - 拥有基本操作权限，包括退出程序
        users.put("operator", new UserCredential("operator", "operator123", UserRole.OPERATOR));

        // 查看员用户 - 仅有查看权限，不能退出程序
        users.put("viewer", new UserCredential("viewer", "viewer123", UserRole.VIEWER));

        log.info("安全管理器初始化完成，预定义用户数量: {}", users.size());
    }

    /**
     * 用户登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @return 验证通过返回UserCredential对象，否则返回null
     */
    public UserCredential login(String username, String password) {
        UserCredential user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            log.info("用户 {} 登录成功，角色: {}", username, user.getRole().getDescription());
            return user;
        }
        log.warn("用户 {} 登录失败", username);
        return null;
    }

    /**
     * 用户登出
     */
    public void logout() {
        if (currentUser != null) {
            log.info("用户 {} 登出", currentUser.getUsername());
            currentUser = null;
        }
    }

    /**
     * 验证退出权限
     *
     * @param username 用户名
     * @param password 密码
     * @return 有退出权限返回UserCredential对象，否则返回null
     */
    public UserCredential validateExitPermission(String username, String password) {
        UserCredential user = login(username, password);
        if (user != null) {
            // 只有管理员和操作员有退出权限
            if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.OPERATOR) {
                log.info("用户 {} 验证退出权限通过", username);
                return user;
            } else {
                log.warn("用户 {} 没有退出权限，角色: {}", username, user.getRole().getDescription());
            }
        }
        return null;
    }

    /**
     * 检查用户是否具有指定权限
     *
     * @param requiredRole 所需权限级别
     * @return 有权限返回true，否则返回false
     */
    public boolean hasPermission(UserRole requiredRole) {
        if (currentUser == null) {
            return false;
        }

        // 权限级别检查：ADMIN > OPERATOR > VIEWER
        switch (requiredRole) {
            case ADMIN:
                return currentUser.getRole() == UserRole.ADMIN;
            case OPERATOR:
                return currentUser.getRole() == UserRole.ADMIN ||
                        currentUser.getRole() == UserRole.OPERATOR;
            case VIEWER:
                return true; // 所有登录用户都有查看权限
            default:
                return false;
        }
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前用户，未登录返回null
     */
    public UserCredential getCurrentUser() {
        return currentUser;
    }

    /**
     * 检查用户是否已登录
     *
     * @return 已登录返回true，否则返回false
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * 获取用户角色描述
     *
     * @param username 用户名
     * @return 角色描述，用户不存在返回null
     */
    public String getUserRoleDescription(String username) {
        UserCredential user = users.get(username);
        return user != null ? user.getRole().getDescription() : null;
    }
}
