package com.myapp.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myapp.server.dto.response;
import com.myapp.server.entity.NetemFullList;
import com.myapp.server.service.INetemFullListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

/**
 * Netem完整列表控制器
 */
@Tag(name = "Netem完整列表接口")
@RestController
@RequestMapping("/netem-full-list")
public class NetemFullListController {

    @Autowired
    private INetemFullListService netemFullListService;

//    /**
//     * 分页查询Netem列表
//     */
//    @Operation(summary = "分页查询Netem列表")
//    @GetMapping("/list")
//    public response list(
//            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
//            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
//        Page<NetemFullList> page = new Page<>(current, size);
//        return response.ok(netemFullListService.page(page));
//    }
//
//    /**
//     * 获取Netem详情
//     */
//    @Operation(summary = "获取Netem详情")
//    @GetMapping("/{id}")
//    public response getById(@Parameter(description = "ID") @PathVariable Integer id) {
//        NetemFullList netemFullList = netemFullListService.getById(id);
//        if (netemFullList != null) {
//            return response.ok(netemFullList);
//        } else {
//            return response.error("记录不存在");
//        }
//    }
//
//    /**
//     * 新增Netem
//     */
//    @Operation(summary = "新增Netem")
//    @PostMapping
//    public response save(@Parameter(description = "Netem信息") @RequestBody NetemFullList netemFullList) {
//        boolean success = netemFullListService.save(netemFullList);
//        if (success) {
//            return response.ok();
//        } else {
//            return response.error("新增记录失败");
//        }
//    }
//
//    /**
//     * 修改Netem
//     */
//    @Operation(summary = "修改Netem")
//    @PutMapping
//    public response update(@Parameter(description = "Netem信息") @RequestBody NetemFullList netemFullList) {
//        boolean success = netemFullListService.updateById(netemFullList);
//        if (success) {
//            return response.ok();
//        } else {
//            return response.error("修改记录失败");
//        }
//    }
//
//    /**
//     * 删除Netem
//     */
//    @Operation(summary = "删除Netem")
//    @DeleteMapping("/{id}")
//    public response delete(@Parameter(description = "ID") @PathVariable Integer id) {
//        boolean success = netemFullListService.removeById(id);
//        if (success) {
//            return response.ok();
//        } else {
//            return response.error("删除记录失败");
//        }
//    }
//
//    /**
//     * 根据单词查询
//     */
//    @Operation(summary = "根据单词查询")
//    @GetMapping("/search")
//    public response searchByWord(@Parameter(description = "单词") @RequestParam String word) {
//        QueryWrapper<NetemFullList> queryWrapper = new QueryWrapper<>();
//        queryWrapper.like("word", word);
//        List<NetemFullList> list = netemFullListService.list(queryWrapper);
//        return response.ok(list);
//    }
//
//    /**
//     * 根据主题查询
//     */
//    @Operation(summary = "根据主题查询")
//    @GetMapping("/topic/{topic}")
//    public response getByTopic(@Parameter(description = "主题") @PathVariable String topic) {
//        QueryWrapper<NetemFullList> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("topic", topic);
//        List<NetemFullList> list = netemFullListService.list(queryWrapper);
//        return response.ok(list);
//    }
}
