package com.yjxxt.crm.controller;

import com.yjxxt.crm.base.BaseController;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.service.PermissionService;
import com.yjxxt.crm.service.UserService;
import com.yjxxt.crm.utils.LoginUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController extends BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private PermissionService permissionService;
    @RequestMapping("/index")
    public String index(){
        return "index";
    }


    @RequestMapping("/")
    public String hello(){
        return "index";
    }

    @RequestMapping("welcome")
    public String welcome(){
        return "welcome";
    }

/**
 * 后台资源页面
 *
 * @return
 */
    @RequestMapping("main")
    public String main(HttpServletRequest request) {
    // 通过工具类，从cookie中获取userId
    Integer userId = LoginUserUtil.releaseUserIdFromCookie(request);
    // 调用对应Service层的方法，通过userId主键查询用户对象
    User user = userService.selectByPrimaryKey(userId);
    // 将用户对象设置到request作用域中
    request.setAttribute("user", user);
    //将用户的权限码存在Session
    List<String > permission = permissionService.queryUserHasRoleModules(userId);

    for(String code:permission){
        System.out.println(code+"权限码");
    }
        System.out.println(permission);
    //将用户的权限码存在Session作用域
    request.getSession().setAttribute("permission",permission);
    //转发
    return "main";
}

}
