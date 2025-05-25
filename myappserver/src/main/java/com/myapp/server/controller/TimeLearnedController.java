package com.myapp.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myapp.server.dto.response;
import com.myapp.server.entity.TimeLearned;
import com.myapp.server.entity.User;
import com.myapp.server.service.ITimeLearnedService;
import com.myapp.server.service.IUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.sql.Date;
import java.util.List;

/**
 * 学习时间控制器
 */
@Tag(name = "学习时间接口")
@RestController
@RequestMapping("/time-learned")
public class TimeLearnedController {

    @Autowired
    private ITimeLearnedService timeLearnedService;

    @Autowired
    private IUsersService  usersService;

//    /**
//     * 分页查询学习时间记录
//     */
//    @Operation(summary = "分页查询学习时间记录")
//    @GetMapping("/list")
//    public response list(
//            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
//            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
//        Page<TimeLearned> page = new Page<>(current, size);
//        return response.ok(timeLearnedService.page(page));
//    }
//
//    /**
//     * 获取指定日期的学习时间记录
//     */
//    @Operation(summary = "获取指定日期的学习时间记录")
//    @GetMapping("/{date}")
//    public response getByDate(@Parameter(description = "日期") @PathVariable Date date) {
//        TimeLearned timeLearned = timeLearnedService.getById(date);
//        if (timeLearned != null) {
//            return response.ok(timeLearned);
//        } else {
//            return response.error("该日期的学习记录不存在");
//        }
//    }
//
//    /**
//     * 新增学习时间记录
//     */
//    @Operation(summary = "新增学习时间记录")
//    @PostMapping
//    public response save(@Parameter(description = "学习时间信息") @RequestBody TimeLearned timeLearned) {
//        boolean success = timeLearnedService.save(timeLearned);
//        if (success) {
//            return response.ok();
//        } else {
//            return response.error("新增学习时间记录失败");
//        }
//    }
//
//    /**
//     * 修改学习时间记录
//     */
//    @Operation(summary = "修改学习时间记录")
//    @PutMapping
//    public response update(@Parameter(description = "学习时间信息") @RequestBody TimeLearned timeLearned) {
//        boolean success = timeLearnedService.updateById(timeLearned);
//        if (success) {
//            return response.ok();
//        } else {
//            return response.error("修改学习时间记录失败");
//        }
//    }
//
//    /**
//     * 删除学习时间记录
//     */
//    @Operation(summary = "删除学习时间记录")
//    @DeleteMapping("/{date}")
//    public response delete(@Parameter(description = "日期") @PathVariable Date date) {
//        boolean success = timeLearnedService.removeById(date);
//        if (success) {
//            return response.ok();
//        } else {
//            return response.error("删除学习时间记录失败");
//        }
//    }
//
//    /**
//     * 获取日期范围内的学习时间记录
//     */
//    @Operation(summary = "获取日期范围内的学习时间记录")
//    @GetMapping("/range")
//    public response getByDateRange(
//            @Parameter(description = "开始日期") @RequestParam Date startDate,
//            @Parameter(description = "结束日期") @RequestParam Date endDate) {
//        QueryWrapper<TimeLearned> queryWrapper = new QueryWrapper<>();
//        queryWrapper.between("time_learned_date", startDate, endDate)
//                   .orderByAsc("time_learned_date");
//        List<TimeLearned> list = timeLearnedService.list(queryWrapper);
//        return response.ok(list);
//    }
//
//    /**
//     * 获取总学习时间
//     */
//    @Operation(summary = "获取总学习时间")
//    @GetMapping("/total")
//    public response getTotalTime() {
//        List<TimeLearned> timeLearnedList = timeLearnedService.list();
//        double totalTime = timeLearnedList.stream()
//                .mapToDouble(TimeLearned::getTimeLearned)
//                .sum();
//        return response.ok(totalTime);
//    }

    /**
     * 获取指定userid的所有学习时间记录
     */
    @Operation(summary = "获取指定userid的学习时间")
    @PostMapping("/user/time/get")
    public response getUserTime(@Parameter(description = "用户id") @RequestParam Integer userId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (usersService.getOne(queryWrapper)==null)
            return response.error("用户不存在");
        List<TimeLearned> timeLearnedList = timeLearnedService.list(new QueryWrapper<TimeLearned>().eq("user_id", userId));
        if (timeLearnedList.isEmpty())
            return response.error("无该用户对应数据");
        return response.ok(timeLearnedList);
    }

    /**
     * 删除指定userid的所有学习时间记录
     */
    @Operation(summary = "删除指定userid的所有学习时间记录")
    @PostMapping("/user/time/delete")
    public response deleteAllByUserId(@Parameter(description = "用户ID") @RequestParam Integer userId){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (usersService.getOne(queryWrapper)==null)
            return response.error("用户不存在");
        boolean success = timeLearnedService.remove(new QueryWrapper<TimeLearned>().eq("user_id", userId));
        if (success) {
            return response.ok();
        }
        return response.error("删除学习时间记录失败");
    }

    /**
     * 存入用户所有的学习时间记录
     */
    @Operation(summary = "存入用户所有的学习时间记录")
    @PostMapping("/user/time/save")
    public response saveAllByUserId(@Parameter(description = "用户学历记录list") @RequestBody List<TimeLearned> list){
        if (list.isEmpty())
            return response.error("列表为空");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", list.get(0).getUserId());
        if (usersService.getOne(queryWrapper)==null)
            return response.error("用户不存在");
        boolean success = timeLearnedService.saveOrUpdateBatch(list);
        if (success) {
            return response.ok();
        }
        return response.error("保存学习时间记录失败");
    }

}
