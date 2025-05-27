package com.myapp.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myapp.server.dto.response;
import com.myapp.server.entity.NetemLearnedDetail;
import com.myapp.server.entity.User;
import com.myapp.server.service.INetemLearnedDetailService;
import com.myapp.server.service.IUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.sql.Date;
import java.util.List;

/**
 * Netem学习详情控制器
 */
@Tag(name = "Netem学习详情接口")
@RestController
@RequestMapping("/netem-learned-detail")
public class NetemLearnedDetailController {

    @Autowired
    private INetemLearnedDetailService netemLearnedDetailService;

    @Autowired
    private IUsersService usersService;

//    /**
//     * 分页查询学习详情
//     */
//    @Operation(summary = "分页查询学习详情")
//    @GetMapping("/list")
//    public response list(
//            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
//            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
//        Page<NetemLearnedDetail> page = new Page<>(current, size);
//        return response.ok(netemLearnedDetailService.page(page));
//    }
//
//    /**
//     * 获取学习详情
//     */
//    @Operation(summary = "获取学习详情")
//    @GetMapping("/{userId}")
//    public response getById(@Parameter(description = "用户ID") @PathVariable Integer userId) {
//        NetemLearnedDetail detail = netemLearnedDetailService.getById(userId);
//        if (detail != null) {
//            return response.ok(detail);
//        } else {
//            return response.error("学习详情不存在");
//        }
//    }
//
//    /**
//     * 新增学习详情
//     */
//    @Operation(summary = "新增学习详情")
//    @PostMapping
//    public response save(@Parameter(description = "学习详情信息") @RequestBody NetemLearnedDetail netemLearnedDetail) {
//        boolean success = netemLearnedDetailService.save(netemLearnedDetail);
//        if (success) {
//            return response.ok();
//        } else {
//            return response.error("新增学习详情失败");
//        }
//    }
//
//    /**
//     * 修改学习详情
//     */
//    @Operation(summary = "修改学习详情")
//    @PutMapping
//    public response update(@Parameter(description = "学习详情信息") @RequestBody NetemLearnedDetail netemLearnedDetail) {
//        boolean success = netemLearnedDetailService.updateById(netemLearnedDetail);
//        if (success) {
//            return response.ok();
//        } else {
//            return response.error("修改学习详情失败");
//        }
//    }
//
//    /**
//     * 删除学习详情
//     */
//    @Operation(summary = "删除学习详情")
//    @DeleteMapping("/{userId}")
//    public response delete(@Parameter(description = "用户ID") @PathVariable Integer userId) {
//        boolean success = netemLearnedDetailService.removeById(userId);
//        if (success) {
//            return response.ok();
//        } else {
//            return response.error("删除学习详情失败");
//        }
//    }
//
//    /**
//     * 根据用户ID和日期查询学习详情
//     */
//    @Operation(summary = "根据用户ID和日期查询学习详情")
//    @GetMapping("/user/{userId}/date/{date}")
//    public response getByUserIdAndDate(
//            @Parameter(description = "用户ID") @PathVariable Integer userId,
//            @Parameter(description = "学习日期") @PathVariable Date date) {
//        QueryWrapper<NetemLearnedDetail> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("user_id", userId)
//                   .eq("netem_learned_date", date);
//        List<NetemLearnedDetail> list = netemLearnedDetailService.list(queryWrapper);
//        return response.ok(list);
//    }

    /**
     * 获取用户的所有学习详情
     */
//    @Operation(summary = "获取用户的所有学习详情")
//    @GetMapping("/user/{userId}/all")
//    public response getAllByUserId(@Parameter(description = "用户ID") @PathVariable Integer userId) {
//        QueryWrapper<NetemLearnedDetail> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("user_id", userId)
//                   .orderByDesc("netem_learned_date");
//        List<NetemLearnedDetail> list = netemLearnedDetailService.list(queryWrapper);
//        return response.ok(list);
//    }
    /**
     * 获取用户的所有学习详情
     */
    @Operation(summary = "获取用户的所有学习详情")
    @PostMapping("/user/word/get")
    public response getAllByUserId(@Parameter(description = "用户ID") @RequestParam Integer userId) {
        if (usersService.getOne(new  QueryWrapper<User>().eq("user_id", userId))==null)
            return response.error("用户不存在");
        QueryWrapper<NetemLearnedDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("netem_learned_date");
        List<NetemLearnedDetail> list = netemLearnedDetailService.list(queryWrapper);
        if (list.isEmpty())
            return response.error("无该用户对应数据");
        return response.ok(list);
    }

    /**
     * 删除用户所有的学习详情
     */
    @Operation(summary = "删除用户所有的学习详情")
    @PostMapping("/user/word/delete")
    public response deleteAllByUserId(@Parameter(description = "用户ID") @RequestParam Integer userId) {
        if (usersService.getOne(new  QueryWrapper<User>().eq("user_id", userId))==null)
            return response.error("用户不存在");
        QueryWrapper<NetemLearnedDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        boolean success = netemLearnedDetailService.remove(queryWrapper);
        if (success) {
            return response.ok();
        } else {
            return response.error("删除用户所有的学习详情失败");
        }
    }

    /**
     * 存入用户的所有学习详情
     */
    @Operation(summary = "存入用户所有的学习详情")
    @PostMapping("/user/word/save")
    public response saveAllByUserId(@Parameter(description = "用户ID") @RequestBody List<NetemLearnedDetail> list) {
        if (list == null || list.isEmpty())
            return response.error("列表为空");
        if (usersService.getOne(new  QueryWrapper<User>().eq("user_id", list.get(0).getUserId()))==null)
            return response.error("用户不存在");
        boolean success = netemLearnedDetailService.saveBatch(list);
        if (success)
            return response.ok();
        return response.error("存入失败");
    }
}
