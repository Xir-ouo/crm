package com.yjxxt.crm.service;


import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.bean.UserRole;
import com.yjxxt.crm.mapper.UserMapper;
import com.yjxxt.crm.mapper.UserRoleMapper;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.utils.AssertUtil;
import com.yjxxt.crm.utils.Md5Util;
import com.yjxxt.crm.utils.PhoneUtil;
import com.yjxxt.crm.utils.UserIDBase64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService extends BaseService<User,Integer> {
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    public UserModel userLogin(String userName, String userPwd) {
        // 1. 验证参数
        checkLoginParams(userName, userPwd);
        // 2. 根据用户名，查询用户对象
        User user = userMapper.queryUserByUserName(userName);
        // 3. 判断用户是否存在 (用户对象为空，记录不存在，方法结束)
        AssertUtil.isTrue(null == user, "用户不存在或已注销！");
        // 4. 用户对象不为空（用户存在，校验密码。密码不正确，方法结束）
        checkLoginPwd(userPwd, user.getUserPwd());
        // 5. 密码正确（用户登录成功，返回用户的相关信息）
        return buildUserInfo(user);
    }
            /**
            * 构建返回的用户信息
            * @param user
            * @return
            */
    private UserModel buildUserInfo(User user) {
            //实例化目标对象
            UserModel userModel = new UserModel();
            // 设置用户信息
            //加密
            userModel.setUserIdStr(UserIDBase64.encoderUserID(user.getId()));
            userModel.setUserName(user.getUserName());
            userModel.setTrueName(user.getTrueName());
            return userModel;
            }
            /**
            * 验证登录密码
            * @param userPwd 前台传递的密码
            * @param upwd 数据库中查询到的密码
            * @return
            */
    private void checkLoginPwd(String userPwd, String upwd) {
                // 数据库中的密码是经过加密的，将前台传递的密码先加密，再与数据库中的密码作比较
                userPwd = Md5Util.encode(userPwd);
                // 比较密码
                AssertUtil.isTrue(!userPwd.equals(upwd), "用户密码不正确！");
                }
            /**
            * 验证用户登录参数
            * @param userName
            * @param userPwd
            */
    private void checkLoginParams(String userName, String userPwd) {
            // 判断姓名
            AssertUtil.isTrue(StringUtils.isBlank(userName), "用户姓名不能为空！");
            // 判断密码
            AssertUtil.isTrue(StringUtils.isBlank(userPwd), "用户密码不能为空！");
            }

    /**
     * 修改密码
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     */

    public void changeUserPwd(Integer userId,String oldPassword,String newPassword,String confirmPassword){
        //首先用户登录了 再修改,userId
        User user = userMapper.selectByPrimaryKey(userId);
        //密码验证
        checkPasswordParams(user,oldPassword,newPassword,confirmPassword);
        //修改密码
        user.setUserPwd(Md5Util.encode(newPassword));
        //确认密码是否修改成功
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user)<1,"修改失败了");
        }
    /**
     * 修改密码的校验
     * @param user
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     */
    private void checkPasswordParams(User user, String oldPassword, String newPassword, String confirmPassword) {
        AssertUtil.isTrue(user==null,"用户不存在或未登录,请重新输入^*^");
        //原始密码非空
        AssertUtil.isTrue(StringUtils.isBlank(oldPassword),"原始密码不能为空,请重新输入-.-");
        //原始密码是否正确
        AssertUtil.isTrue(!(user.getUserPwd().equals(Md5Util.encode(oldPassword))),"密码错误,请重新输入^-^");
        //新密码非空
        AssertUtil.isTrue(StringUtils.isBlank(newPassword),"新密码不能为空,请再次输入-.-");
        //新密码不能和原始密码一致
        AssertUtil.isTrue(newPassword.equals(oldPassword),"新密码和原始密码不能一致=.=");
        //确认新密码非空
        AssertUtil.isTrue(StringUtils.isBlank(confirmPassword),"密码不能为空+.+");
        //确认密码和新密码一致
        AssertUtil.isTrue(!newPassword.equals(confirmPassword),"确认密码和新密码保持一致^.^");
    }

    /*
    查询所有的销售人员
     */
    public List<Map<String ,Object>> querySales(){
        return userMapper.selectSales();
    }

    /**
     * 用户模块列表查询
     * 条件
     * @param userQuery 查询条件
     * @return
     */
    public Map<String,Object> findUserByParams(UserQuery userQuery){
        //实例化map
        Map<String,Object> map = new HashMap<String,Object>();
        //初始化分页单位
        PageHelper.startPage(userQuery.getPage(),userQuery.getLimit());
        //开始分页
        PageInfo<User> plist = new PageInfo<User>(selectByParams(userQuery));
        //准备数据
        map.put("code",0);
        map.put("msg","success");
        map.put("count",plist.getTotal());
        map.put("data",plist.getList());
        //返回目标map
        return map;
    }

    /**
     * 一.验证:
     *   1.用户非空 且唯一
     *   2.邮箱非空
     *   手机号非空 格式正确
     *二.设定默认值
     *   is_valid=1
     *   createDate 系统时间
     *   updateDate 系统时间
     * 密码:
     *   123456 加密
     * 三.添加是否成功
     * @param user
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addUser(User user){
        System.out.println(user);
        //验证
        checkUser(user.getUserName(),user.getEmail(),user.getPhone(),false);
        //用户名唯一
        User temp = userMapper.queryUserByUserName(user.getUserName());
        AssertUtil.isTrue(temp!=null,"用户名已经存在");
        //设定默认值
        user.setIsValid(1);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        //密码加密
        user.setUserPwd(Md5Util.encode("123456"));
        //验证是否成功
        AssertUtil.isTrue(insertHasKey(user)<1,"添加失败了=.=");

        relaionUserRose(user.getId(),user.getRoleIds());
    }


    /**
     * 操作中间表
     * @param userId 用户id
     * @param roleIds 角色id 1,2,4;
     *                 原来的角色数量
     *                没有角色
     *                添加新的角色
     *                有角色
     *                新增角色
     *                减少角色
     *                ...
     *                统计原来是否有角色
     *                删除
     *                新添加角色
     */
    private void relaionUserRose(Integer userId, String roleIds) {
        //准备集合存储对象
        List<UserRole> urlist = new ArrayList<>();
        //userId ,roleId
        AssertUtil.isTrue(StringUtils.isBlank(roleIds),"请选择角色信息");
        //统计当前用户有多少个角色
        int count = userRoleMapper.countUserRoleNum(userId);
        if(count>0){
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"用户角色删除失败");
        }
        //删除原来的角色
        String[] RoleStrId = roleIds.split(",");
        //遍历
        for(String rid:RoleStrId){
            //准备对象
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(Integer.parseInt(rid));
            userRole.setCreateDate(new Date());
            userRole.setUpdateDate(new Date());
            //存放到集合
            urlist.add(userRole);
        }
        //批量添加
        AssertUtil.isTrue(userRoleMapper.insertBatch(urlist)!=urlist.size(),"用户角色分配失败");
    }

    /**
     *
     * @param userName
     * @param email
     * @param phone
     */
    private void checkUser(String userName, String email, String phone,boolean flag) {
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空==");
        //用户名唯一
        User temp = userMapper.queryUserByUserName(userName);
        if (!flag){
            AssertUtil.isTrue(temp!=null,"用户名已经存在");
        }
        AssertUtil.isTrue(StringUtils.isBlank(email),"邮箱不能为空==");
        AssertUtil.isTrue(StringUtils.isBlank(phone),"手机号不能为空==");
        AssertUtil.isTrue(!PhoneUtil.isMobile(phone),"请输入合法的手机号=*=");
    }

    /**
     * 一.验证:
     *当前用户的id存在 否则不能修改
     *   1.用户非空 且唯一
     *   2.邮箱非空
     *   手机号非空 格式正确
     *二.设定默认值
     *   is_valid=1
     *   updateDate 系统时间
     * 三.添加是否成功
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void changeUser(User user){
        //根据id获取用户信息
        User temp = userMapper.selectByPrimaryKey(user.getId());
        //判断
        AssertUtil.isTrue(temp==null,"待修改的记录不存在");


        //验证参数
        checkUser(user.getUserName(),user.getEmail(),user.getPhone(),true);

        User temp2 = userMapper.queryUserByUserName(user.getUserName());

        //设定默认值
        user.setUpdateDate(new Date());
        //判断修改是否成功
        AssertUtil.isTrue(updateByPrimaryKeySelective(user)<1,"修改失败了");
        //
        relaionUserRose(user.getId(), user.getRoleIds());

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void removeUserIds(Integer[] ids){
        //验证
        AssertUtil.isTrue(ids==null || ids.length==0,"请选择删除数据" );
        //遍历对象
        for (Integer userId:ids){
            //统计当前用户有多少个角色
            int count = userRoleMapper.countUserRoleNum(userId);
            //删除当前用户的角色
            if(count>0){
                AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"用户角色删除失败");
            }
        }
        //判断删除成功与否
        AssertUtil.isTrue(userMapper.deleteBatch(ids)<1,"删除失败了");
    }
}




