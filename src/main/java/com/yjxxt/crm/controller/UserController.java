package com.yjxxt.crm.controller;

import com.yjxxt.crm.base.BaseController;
import com.yjxxt.crm.base.ResultInfo;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.exceptions.ParamsException;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.service.UserService;

import com.yjxxt.crm.utils.LoginUserUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("user")
public class UserController extends BaseController {
    @Resource
    private UserService userService;
    @PostMapping("login")
    @ResponseBody
    public ResultInfo userLogin (String userName, String userPwd) {
        /**
         * 登录成功后，有两种处理：
         * 1. 将用户的登录信息存入 Session （ 问题：重启服务器，Session 失效，客户端
         需要重复登录 ）
         * 2. 将用户信息返回给客户端，由客户端（Cookie）保存
         */
        ResultInfo resultInfo = new ResultInfo();
        // 通过 try catch 捕获 Service 层抛出的异常
        // 调用Service层的登录方法，得到返回的用户对象
        UserModel userModel = userService.userLogin(userName, userPwd);
        // 将返回的UserModel对象设置到 ResultInfo 对象中
        resultInfo.setResult(userModel);
        return resultInfo;
    }
    @PostMapping("updatePwd")
    @ResponseBody
    public ResultInfo updatePwd(HttpServletRequest req,String oldPassword,String newPassword,String confirmPwd){
        ResultInfo resultInfo = new ResultInfo();
        //获取Cookie中的userId
        int userId = LoginUserUtil.releaseUserIdFromCookie(req);
        //修改密码操作
        userService.changeUserPwd(userId,oldPassword,newPassword,confirmPwd);
        return resultInfo;
    }

    @RequestMapping("toPasswordPage")
    public String updatePassword(){
        return "user/password";
    }

    @RequestMapping("index")
    public String index(){
        return "user/user";
    }

    @RequestMapping("addOrUpdatePage")
    public String addOrUpdatePage(Integer id, Model model){
        if(id!=null){
            User user = userService.selectByPrimaryKey(id);
            model.addAttribute("user",user);
        }
        return "user/add_update";
    }

    @RequestMapping("toSettingPage")
    public String setting(HttpServletRequest req){
        //获取用户的id
        int userId = LoginUserUtil.releaseUserIdFromCookie(req);
        //调用方法
        User user = userService.selectByPrimaryKey(userId);
        //存储
        req.setAttribute("user",user);
        //转发
        return "user/setting";

    }

    @RequestMapping("setting")
    @ResponseBody
    public ResultInfo sayUpdate(User user){
        ResultInfo resultInfo = new ResultInfo();
        //修改信息
        userService.updateByPrimaryKeySelective(user);
        //返回目标数据对象
        return resultInfo;
    }

    @RequestMapping("save")
    @ResponseBody
    public ResultInfo save(User user){
        System.out.println(user);
        //用户的添加
        userService.addUser(user);
        //返回目标数据对象
        return success("用户添加成功!");
    }

    @RequestMapping("update")
    @ResponseBody
    public ResultInfo update(User user){
        //用户的修改
        userService.changeUser(user);
        //返回目标数据对象
        return success("用户修改成功!");
    }


    @RequestMapping("delete")
    @ResponseBody
    public ResultInfo delete(Integer[] ids){
        //用户的修改
        userService.removeUserIds(ids);
        //返回目标数据对象
        return success("批量删除用户成功!");
    }

    @RequestMapping("sales")
    @ResponseBody
    public List<Map<String,Object>> findSales() {
        List<Map<String ,Object>> list = userService.querySales();
        return list;
    }


    @RequestMapping("list")
    @ResponseBody
    public Map<String,Object> list(UserQuery userQuery) {
        return userService.findUserByParams(userQuery);
    }


}
