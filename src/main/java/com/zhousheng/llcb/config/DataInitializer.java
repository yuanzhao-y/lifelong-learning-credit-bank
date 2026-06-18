package com.zhousheng.llcb.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.entity.*;
import com.zhousheng.llcb.mapper.*;
import com.zhousheng.llcb.service.CreditService;
import com.zhousheng.llcb.service.RoleService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements ApplicationRunner {

    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysDictMapper dictMapper;
    private final SysDictItemMapper dictItemMapper;
    private final SysMenuMapper menuMapper;
    private final SysApiPermissionMapper apiPermissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final CreditService creditService;
    private final SeedProperties seedProperties;

    public DataInitializer(SysRoleMapper roleMapper,
                           SysUserMapper userMapper,
                           SysDictMapper dictMapper,
                           SysDictItemMapper dictItemMapper,
                           SysMenuMapper menuMapper,
                           SysApiPermissionMapper apiPermissionMapper,
                           PasswordEncoder passwordEncoder,
                           RoleService roleService,
                           CreditService creditService,
                           SeedProperties seedProperties) {
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
        this.dictMapper = dictMapper;
        this.dictItemMapper = dictItemMapper;
        this.menuMapper = menuMapper;
        this.apiPermissionMapper = apiPermissionMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.creditService = creditService;
        this.seedProperties = seedProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedAdmin();
        seedDictionaries();
        seedMenusAndApis();
    }

    private void seedRoles() {
        upsertRole("learner", "学习者", 10);
        upsertRole("auditor", "审核员", 20);
        upsertRole("expert", "专家", 30);
        upsertRole("admin", "管理员", 40);
    }

    private void upsertRole(String code, String name, int sortNo) {
        if (roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, code)) > 0) {
            return;
        }
        SysRole role = new SysRole();
        role.setRoleCode(code);
        role.setRoleName(name);
        role.setStatus(Constants.STATUS_ENABLED);
        role.setSortNo(sortNo);
        roleMapper.insert(role);
    }

    private void seedAdmin() {
        SysUser admin = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, seedProperties.adminUsername())
                .last("limit 1"));
        if (admin == null) {
            admin = new SysUser();
            admin.setUsername(seedProperties.adminUsername());
            admin.setPasswordHash(passwordEncoder.encode(seedProperties.adminPassword()));
            admin.setRealName("系统管理员");
            admin.setStatus(Constants.STATUS_ENABLED);
            admin.setPasswordUpdatedAt(LocalDateTime.now());
            userMapper.insert(admin);
            creditService.getOrCreateAccount(admin.getId());
        }
        SysRole adminRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, Constants.ROLE_ADMIN)
                .last("limit 1"));
        if (adminRole != null) {
            roleService.assignUserRoles(admin.getId(), List.of(adminRole.getId()));
        }
    }

    private void seedDictionaries() {
        Map<String, List<String[]>> dictionaries = Map.of(
                "user_status", List.of(pair("enabled", "启用"), pair("disabled", "禁用"), pair("frozen", "冻结")),
                "outcome_type", List.of(pair("course_cert", "课程证书"), pair("diploma_cert", "毕业证书"), pair("degree_cert", "学位证书"),
                        pair("vocational_qualification", "职业资格证书"), pair("skill_level", "技能等级证书"), pair("training_cert", "培训证书")),
                "application_status", List.of(pair("pending", "待审核"), pair("approved", "已通过"), pair("rejected", "已驳回"), pair("withdrawn", "已撤回")),
                "credit_change_type", List.of(pair("earn", "获取"), pair("deduct", "扣减"), pair("freeze", "冻结"), pair("unfreeze", "解冻")),
                "conversion_rule_status", List.of(pair("draft", "草稿"), pair("reviewing", "评审中"), pair("effective", "生效"), pair("abolished", "废止"))
        );
        dictionaries.forEach((code, items) -> {
            SysDict dict = dictMapper.selectOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode, code).last("limit 1"));
            if (dict == null) {
                dict = new SysDict();
                dict.setDictCode(code);
                dict.setDictName(code);
                dict.setStatus(Constants.STATUS_ENABLED);
                dict.setSortNo(0);
                dictMapper.insert(dict);
            }
            int sort = 1;
            for (String[] item : items) {
                if (dictItemMapper.selectCount(new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getDictId, dict.getId())
                        .eq(SysDictItem::getItemCode, item[0])) == 0) {
                    SysDictItem dictItem = new SysDictItem();
                    dictItem.setDictId(dict.getId());
                    dictItem.setItemCode(item[0]);
                    dictItem.setItemName(item[1]);
                    dictItem.setStatus(Constants.STATUS_ENABLED);
                    dictItem.setSortNo(sort);
                    dictItemMapper.insert(dictItem);
                }
                sort++;
            }
        });
    }

    private void seedMenusAndApis() {
        upsertMenu("dashboard", "首页", "/dashboard", 10);
        upsertMenu("outcome_catalog", "学习成果目录", "/outcomes", 20);
        upsertMenu("cert_application", "成果认证", "/certifications", 30);
        upsertMenu("credit_account", "学分账户", "/credits", 40);
        upsertMenu("conversion", "成果转换", "/conversions", 50);
        upsertMenu("system_admin", "系统管理", "/admin", 90);
        upsertApi("auth_login", "登录", "认证", "POST", "/auth/**");
        upsertApi("admin_all", "管理接口", "系统管理", "*", "/admin/**");
        upsertApi("cert_audit", "认证审核", "成果认证", "*", "/certifications/audit/**");
        upsertApi("conversion_audit", "转换审核", "成果转换", "*", "/conversions/audit/**");
    }

    private void upsertMenu(String code, String name, String path, int sortNo) {
        if (menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getMenuCode, code)) > 0) {
            return;
        }
        SysMenu menu = new SysMenu();
        menu.setMenuCode(code);
        menu.setMenuName(name);
        menu.setMenuType("menu");
        menu.setRoutePath(path);
        menu.setVisible(1);
        menu.setStatus(Constants.STATUS_ENABLED);
        menu.setSortNo(sortNo);
        menuMapper.insert(menu);
    }

    private void upsertApi(String code, String name, String module, String method, String path) {
        if (apiPermissionMapper.selectCount(new LambdaQueryWrapper<SysApiPermission>().eq(SysApiPermission::getApiCode, code)) > 0) {
            return;
        }
        SysApiPermission api = new SysApiPermission();
        api.setApiCode(code);
        api.setApiName(name);
        api.setModule(module);
        api.setHttpMethod(method);
        api.setPathPattern(path);
        api.setStatus(Constants.STATUS_ENABLED);
        apiPermissionMapper.insert(api);
    }

    private String[] pair(String code, String name) {
        return new String[]{code, name};
    }
}
