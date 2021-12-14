package com.yjxxt.crm.service;

import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.Permission;
import com.yjxxt.crm.mapper.PermissionMapper;
import com.yjxxt.crm.utils.AssertUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionService extends BaseService<Permission,Integer> {

    @Resource
    private PermissionMapper permissionMapper;

    public void addGrant(Integer roleId, Integer[] mids) {
        AssertUtil.isTrue(roleId==null,"请选择角色");
        AssertUtil.isTrue(mids==null || mids.length==0,"最少选择一个资源");

        List<Permission> plist = new ArrayList<Permission>();
        //遍历mids
        for(Integer mid:mids){
            //实例化对象
            Permission permission = new Permission();
            permission.setRoleId(roleId);
            permission.setModuleId(mids[0]);
            plist.add(permission);
        }

        AssertUtil.isTrue(permissionMapper.insertBatch(plist)!=plist.size(),"授权失败");
    }


    /**
     * 查询用户拥有的资源
     * @param userId
     * @return
     */
    public List<String> queryUserHasRoleModules(Integer userId){
        return permissionMapper.selectUserHasRolesHasPermissions(userId);

    }
}
