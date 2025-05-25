package com.myapp.server.controller;

import cn.hutool.Hutool;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myapp.server.dto.response;
import com.myapp.server.entity.User;
import com.myapp.server.service.IUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理接口")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUsersService usersService;

    private final String salt = "myappserver";

    /**
     * test接口
     */
    @Operation(summary = "test接口")
    @GetMapping("/test")
    public response test() {
        return response.ok("连接成功");
    }
    /**
     * 分页查询用户列表
     */
//    @Operation(summary = "分页查询用户列表")
//    @GetMapping("/list")
//    public response list(
//            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
//            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
//        Page<User> page = new Page<>(current, size);
//        Page<User> result = usersService.page(page);
//        if (result != null) {
//            return response.ok(result);
//        }
//        return response.error("查询失败");
//    }

    /**
     * 获取用户详情
     */
//    @Operation(summary = "获取用户详情")
//    @GetMapping("/{id}")
//    public response getById(@Parameter(description = "用户ID") @PathVariable Integer id) {
//        User result =  usersService.getById(id);
//        if (result!=null)
//            return response.ok(result);
//        return response.error("用户不存在");
//    }

    /**
     * 新增用户
     */
    @Operation(summary = "注册")
    @PostMapping("/register")
    public response save(@Parameter(description = "用户信息") @RequestBody User user) {
        if (usersService.getOne(new QueryWrapper<User>().eq("username", user.getUsername()))!=null)
            return response.error("用户名已存在");
        user.setPassword(SecureUtil.md5(user.getPassword()+salt));
        if(usersService.save(user)) {
            Integer userId = usersService.getOne(new QueryWrapper<User>().eq("username", user.getUsername())).getUserId();
            return response.ok(userId);
        }
        return response.error("注册失败");
    }

    /**
     * 修改用户
     */
//    @Operation(summary = "修改用户")
//    @PutMapping
//    public response update(@Parameter(description = "用户信息") @RequestBody User user) {
//        if(usersService.updateById(user))
//            return response.ok();
//        return response.error("修改失败");
//    }

    /**
     * 删除用户
     */
//    @Operation(summary = "删除用户")
//    @DeleteMapping("/{id}")
//    public response delete(@Parameter(description = "用户ID") @PathVariable Integer id) {
//        if(usersService.removeById(id))
//            return response.ok();
//        return response.error("删除失败");
//    }

    /**
     * 根据用户名查询用户
     */
//    @Operation(summary = "根据用户名查询用户")
//    @GetMapping("/search")
//    public List<User> searchByUsername(@Parameter(description = "用户名") @RequestParam String username) {
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.like("username", username);
//        return usersService.list(queryWrapper);
//    }
    /**
     * 根据用户密码查询用户
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public response login(@Parameter(description = "用户名") @RequestParam String username,
                      @Parameter(description = "密码") @RequestParam String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", SecureUtil.md5(password+salt));
        User user = usersService.getOne(queryWrapper);
        if (user!=null)
            return response.ok(user.getUserId());
        return response.error("用户名或密码错误");
    }

}
