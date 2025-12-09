package com.li.socialplatform.controller;

import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.IFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author e69d8e
 * @since 2025/12/8 23:00
 */
@Slf4j
@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final IFollowService followService;
    // 关注用户
    @PostMapping("/{id}")
    public Result follow(@PathVariable Long id) {
        return followService.follow(id);
    }
    // 取消关注
    @DeleteMapping("/{id}")
    public Result cancelFollow(@PathVariable Long id) {
        return followService.cancelFollow(id);
    }
    // 获取某个用户粉丝列表
    @GetMapping("/list/{id}")
    public Result getFollowerList(@PathVariable Long id,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return followService.getFollowerList(id, pageNum, pageSize);
    }
    // 获取某个用户关注列表
    @GetMapping("/followee/{id}")
    public Result getFolloweeList(@PathVariable Long id,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return followService.getFolloweeList(id, pageNum, pageSize);
    }
    // 获取当前用户的粉丝列表
    @GetMapping("/list")
    public Result getFollowerList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return followService.getFollowerList(null, pageNum, pageSize);
    }
    @GetMapping("/followee")
    public Result getFolloweeList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return followService.getFolloweeList(null, pageNum, pageSize);
    }
}
