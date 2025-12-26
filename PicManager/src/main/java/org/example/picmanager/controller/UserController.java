package org.example.picmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.example.picmanager.entity.User;
import org.example.picmanager.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // 注册接口
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();

        String username = data.get("username");
        String email = data.get("email");
        String password = data.get("password");

        // 检查用户名是否存在
        QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("name", username);
        if (userMapper.selectCount(usernameWrapper) > 0) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }

        // 检查邮箱是否存在
        QueryWrapper<User> emailWrapper = new QueryWrapper<>();
        emailWrapper.eq("email", email);
        if (userMapper.selectCount(emailWrapper) > 0) {
            result.put("success", false);
            result.put("message", "邮箱已被注册");
            return result;
        }

        // 创建并保存用户
        User user = new User();
        user.setName(username);
        user.setEmail(email);
        user.setPassword(password);

        if (userMapper.insert(user) == 1) {
            result.put("success", true);
            result.put("message", "注册成功");

            // 返回用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getName());
            userInfo.put("email", user.getEmail());
            result.put("data", userInfo);
        } else {
            result.put("success", false);
            result.put("message", "注册失败");
        }

        return result;
    }

    // 登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();

        String username = data.get("username");
        String password = data.get("password");

        // 查询用户（支持用户名或邮箱）
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", username).or().eq("email", username);

        User user = userMapper.selectOne(wrapper);

        if (user != null && password.equals(user.getPassword())) {
            result.put("success", true);
            result.put("message", "登录成功");

            // 返回用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getName());
            userInfo.put("email", user.getEmail());
            result.put("data", userInfo);
        } else {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
        }

        return result;
    }

    // 检查用户名接口
    @GetMapping("/checkUsername")
    public Map<String, Object> checkUsername(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", username);
        boolean exists = userMapper.selectCount(wrapper) > 0;

        result.put("exists", exists);
        result.put("message", exists ? "用户名已存在" : "用户名可用");
        return result;
    }

    // 检查邮箱接口
    @GetMapping("/checkEmail")
    public Map<String, Object> checkEmail(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        boolean exists = userMapper.selectCount(wrapper) > 0;

        result.put("exists", exists);
        result.put("message", exists ? "邮箱已被注册" : "邮箱可用");
        return result;
    }

    // 更新个人资料
    @PostMapping("/update-profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取参数
            Object userIdObj = data.get("userId");
            String username = (String) data.get("username");
            String email = (String) data.get("email");

            // 验证参数
            if (userIdObj == null) {
                result.put("success", false);
                result.put("message", "用户ID不能为空");
                return result;
            }

            if (username == null || username.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "用户名不能为空");
                return result;
            }

            if (email == null || email.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "邮箱不能为空");
                return result;
            }

            // 验证邮箱格式
            if (!isValidEmail(email)) {
                result.put("success", false);
                result.put("message", "邮箱格式不正确");
                return result;
            }

            Long userId;
            try {
                if (userIdObj instanceof Integer) {
                    userId = ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                } else if (userIdObj instanceof Long) {
                    userId = (Long) userIdObj;
                } else {
                    result.put("success", false);
                    result.put("message", "用户ID格式错误");
                    return result;
                }
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", "用户ID格式错误");
                return result;
            }

            // 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            // 检查用户名是否已被其他人使用
            if (!username.equals(user.getName())) {
                QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
                usernameWrapper.eq("name", username).ne("id", userId);
                if (userMapper.selectCount(usernameWrapper) > 0) {
                    result.put("success", false);
                    result.put("message", "用户名已被其他人使用");
                    return result;
                }
            }

            // 检查邮箱是否已被其他人使用
            if (!email.equals(user.getEmail())) {
                QueryWrapper<User> emailWrapper = new QueryWrapper<>();
                emailWrapper.eq("email", email).ne("id", userId);
                if (userMapper.selectCount(emailWrapper) > 0) {
                    result.put("success", false);
                    result.put("message", "邮箱已被其他人注册");
                    return result;
                }
            }

            // 更新用户信息
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", userId)
                    .set("name", username)
                    .set("email", email);

            int updated = userMapper.update(null, updateWrapper);

            if (updated > 0) {
                result.put("success", true);
                result.put("message", "个人资料更新成功");

                // 返回更新后的用户信息
                User updatedUser = userMapper.selectById(userId);
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", updatedUser.getId());
                userData.put("username", updatedUser.getName());
                userData.put("email", updatedUser.getEmail());
                result.put("data", userData);
            } else {
                result.put("success", false);
                result.put("message", "个人资料更新失败");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }

        return result;
    }

    // 修改密码
    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取参数
            Object userIdObj = data.get("userId");
            String oldPassword = (String) data.get("oldPassword");
            String newPassword = (String) data.get("newPassword");
            String confirmPassword = (String) data.get("confirmPassword");

            // 验证参数
            if (userIdObj == null) {
                result.put("success", false);
                result.put("message", "用户ID不能为空");
                return result;
            }

            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "原密码不能为空");
                return result;
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "新密码不能为空");
                return result;
            }

            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "确认密码不能为空");
                return result;
            }

            long userId;
            try {
                if (userIdObj instanceof Integer) {
                    userId = ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                } else if (userIdObj instanceof Long) {
                    userId = (Long) userIdObj;
                } else {
                    result.put("success", false);
                    result.put("message", "用户ID格式错误");
                    return result;
                }
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", "用户ID格式错误");
                return result;
            }

            // 验证旧密码是否正确
            User user = userMapper.selectById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            if (!oldPassword.equals(user.getPassword())) {
                result.put("success", false);
                result.put("message", "原密码错误");
                return result;
            }

            // 验证新密码不能和旧密码相同
            if (newPassword.equals(oldPassword)) {
                result.put("success", false);
                result.put("message", "新密码不能和原密码相同");
                return result;
            }

            // 更新密码
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", userId)
                    .set("password", newPassword);

            int updated = userMapper.update(null, updateWrapper);

            if (updated > 0) {
                result.put("success", true);
                result.put("message", "密码修改成功");
            } else {
                result.put("success", false);
                result.put("message", "密码修改失败");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }

        return result;
    }

    // 注销账号
    @PostMapping("/delete-account")
    public Map<String, Object> deleteAccount(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取参数
            Object userIdObj = data.get("userId");
            String password = (String) data.get("password");

            // 验证参数
            if (userIdObj == null) {
                result.put("success", false);
                result.put("message", "用户ID不能为空");
                return result;
            }

            if (password == null || password.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "密码不能为空");
                return result;
            }

            long userId;
            try {
                if (userIdObj instanceof Integer) {
                    userId = ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                } else if (userIdObj instanceof Long) {
                    userId = (Long) userIdObj;
                } else {
                    result.put("success", false);
                    result.put("message", "用户ID格式错误");
                    return result;
                }
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", "用户ID格式错误");
                return result;
            }

            // 验证密码
            User user = userMapper.selectById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            if (!password.equals(user.getPassword())) {
                result.put("success", false);
                result.put("message", "密码错误");
                return result;
            }

            // 删除用户
            int deleted = userMapper.deleteById(userId);

            if (deleted > 0) {
                result.put("success", true);
                result.put("message", "账号已成功注销");
            } else {
                result.put("success", false);
                result.put("message", "账号注销失败");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }

        return result;
    }

    // 验证邮箱格式
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String regex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
        return email.matches(regex);
    }
}