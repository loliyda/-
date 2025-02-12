package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserMapper userMapper;

    @PostMapping
    public Result<?> save(@RequestBody User user){
        if(user.getPassword()==null){
            user.setPassword("123456");
        }
        userMapper.insert(user);
        return Result.success();
    }

    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search){
        LambdaQueryWrapper<User>wrapper = Wrappers.<User>lambdaQuery();
        if (StrUtil.isNotBlank(search)){
            wrapper.like(User::getUsername,search);
        }
        Page <User> userPage = userMapper.selectPage(new Page<> (pageNum,pageSize), wrapper);
        return Result.success(userPage);
    }

    @PutMapping
    public Result<?> update(@RequestBody User user){
        userMapper.updateById(user);
        return Result.success();
    }

    @PutMapping("/revise")
    public Result<?> revise(@RequestBody User user){
        LambdaQueryWrapper<User>wrapper = Wrappers.<User>lambdaQuery();
        userMapper.update(user,wrapper.eq(User::getUsername,user.getUsername()));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id){
        userMapper.deleteById(id);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody User user){
        User res=userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername,user.getUsername()).eq(User::getPassword,user.getPassword()));
        if(res==null){
            return Result.error("-1","用户名或密码错误");
        }
        return Result.success(res);
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user){
        User res=userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername,user.getUsername()));
        if(res!=null){
            return Result.error("-1","用户名已存在");
        }
        userMapper.insert(user);
        return Result.success();
    }


}
