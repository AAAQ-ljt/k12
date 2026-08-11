package com.smart.campus.admin.biz;

import com.smart.campus.admin.entity.dto.RolePermissionSaveDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.StatusEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.SystemMenu;
import com.smart.campus.entity.po.SystemRoleMenu;
import com.smart.campus.entity.query.SystemMenuQuery;
import com.smart.campus.entity.query.SystemRoleMenuQuery;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.admin.entity.vo.RolePermissionVO;
import com.smart.campus.admin.entity.vo.RoleVO;
import com.smart.campus.entity.vo.SystemMenuVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.SystemMenuService;
import com.smart.campus.service.SystemRoleMenuService;
import com.smart.campus.utils.LoginUserContextHolder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminPermissionBiz {

    @Resource
    private SystemMenuService systemMenuService;

    @Resource
    private SystemRoleMenuService systemRoleMenuService;

    public List<RoleVO> loadRoleList() {
        List<RoleVO> roles = new ArrayList<>();
        roles.add(buildRole(UserRoleTypeEnum.ADMIN, false, "管理员默认拥有全部后台权限"));
        roles.add(buildRole(UserRoleTypeEnum.TEACHER, true, "可分配管理后台菜单权限"));
        roles.add(buildRole(UserRoleTypeEnum.STUDENT, false, "学生仅允许登录用户端"));
        return roles;
    }

    private RoleVO buildRole(UserRoleTypeEnum roleTypeEnum, Boolean configurable, String remark) {
        RoleVO role = new RoleVO();
        role.setRoleType(roleTypeEnum.getCode());
        role.setRoleName(roleTypeEnum.getDesc());
        role.setConfigurable(configurable);
        role.setRemark(remark);
        return role;
    }

    public List<SystemMenuVO> loadMenuTree() {
        return buildMenuTree(loadEnabledMenus());
    }

    public RolePermissionVO getRolePermission(Integer roleType) {
        UserRoleTypeEnum roleTypeEnum = getRoleTypeEnum(roleType);
        RolePermissionVO result = new RolePermissionVO();
        result.setRoleType(roleType);
        result.setRoleName(roleTypeEnum.getDesc());
        result.setMenuCodes(loadMenuCodesByRole(roleType));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveRolePermission(RolePermissionSaveDTO dto) {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null || !UserRoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "无操作权限");
        }
        Integer roleType = dto.getRoleType();
        if (UserRoleTypeEnum.ADMIN.getCode().equals(roleType)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "管理员默认拥有全部后台权限，无需配置");
        }
        if (UserRoleTypeEnum.STUDENT.getCode().equals(roleType)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学生仅允许登录用户端，不能配置后台菜单");
        }
        if (!UserRoleTypeEnum.TEACHER.getCode().equals(roleType)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "角色不存在");
        }

        List<String> menuCodes = dto.getMenuCodes() == null ? List.of() : dto.getMenuCodes().stream().distinct().toList();
        Set<String> enabledMenuCodes = new HashSet<>(loadAllEnabledMenuCodes());
        for (String menuCode : menuCodes) {
            if (!enabledMenuCodes.contains(menuCode)) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "菜单权限不存在或已停用");
            }
        }

        SystemRoleMenuQuery deleteQuery = new SystemRoleMenuQuery();
        deleteQuery.setRoleType(roleType);
        systemRoleMenuService.deleteByParam(deleteQuery);

        List<SystemRoleMenu> roleMenus = new ArrayList<>();
        Date now = new Date();
        for (String menuCode : menuCodes) {
            SystemRoleMenu roleMenu = new SystemRoleMenu();
            roleMenu.setRoleType(roleType);
            roleMenu.setMenuCode(menuCode);
            roleMenu.setCreateTime(now);
            roleMenus.add(roleMenu);
        }
        systemRoleMenuService.addBatch(roleMenus);
    }

    public List<SystemMenuVO> getCurrentMenuList() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        return getMenuListByRole(loginUser.getRoleType());
    }

    public List<SystemMenuVO> getMenuListByRole(Integer roleType) {
        return buildMenuTree(filterMenusByRole(loadEnabledMenus(), roleType));
    }

    public List<String> getCurrentMenuCodes() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            return List.of();
        }
        return getMenuCodesByRole(loginUser.getRoleType());
    }

    public List<String> getMenuCodesByRole(Integer roleType) {
        return loadMenuCodesByRole(roleType);
    }

    public boolean hasPermission(LoginUserVO loginUser, String menuCode) {
        if (loginUser == null) {
            return false;
        }
        Integer roleType = loginUser.getRoleType();
        if (UserRoleTypeEnum.ADMIN.getCode().equals(roleType)) {
            return true;
        }
        if (!UserRoleTypeEnum.TEACHER.getCode().equals(roleType)) {
            return false;
        }
        return loadMenuCodesByRole(roleType).contains(menuCode);
    }

    private List<SystemMenu> loadEnabledMenus() {
        SystemMenuQuery query = new SystemMenuQuery();
        query.setStatus(StatusEnum.ENABLED.getCode());
        query.setOrderBy("s.sort_order asc,s.menu_id asc");
        return systemMenuService.findListByParam(query);
    }

    private List<String> loadAllEnabledMenuCodes() {
        return loadEnabledMenus().stream().map(SystemMenu::getMenuCode).toList();
    }

    private List<String> loadMenuCodesByRole(Integer roleType) {
        if (UserRoleTypeEnum.ADMIN.getCode().equals(roleType)) {
            return loadAllEnabledMenuCodes();
        }
        if (!UserRoleTypeEnum.TEACHER.getCode().equals(roleType)) {
            return List.of();
        }
        SystemRoleMenuQuery query = new SystemRoleMenuQuery();
        query.setRoleType(roleType);
        return systemRoleMenuService.findListByParam(query).stream()
                .map(SystemRoleMenu::getMenuCode)
                .distinct()
                .toList();
    }

    private List<SystemMenu> filterMenusByRole(List<SystemMenu> menus, Integer roleType) {
        if (UserRoleTypeEnum.ADMIN.getCode().equals(roleType)) {
            return menus;
        }
        Set<String> menuCodes = new HashSet<>(loadMenuCodesByRole(roleType));
        return menus.stream().filter(item -> menuCodes.contains(item.getMenuCode())).toList();
    }

    private List<SystemMenuVO> buildMenuTree(List<SystemMenu> menus) {
        Map<String, SystemMenuVO> menuMap = new LinkedHashMap<>();
        Map<String, List<SystemMenuVO>> childrenMap = new HashMap<>();
        List<SystemMenuVO> roots = new ArrayList<>();
        for (SystemMenu menu : menus) {
            SystemMenuVO vo = convertMenu(menu);
            menuMap.put(vo.getMenuCode(), vo);
            childrenMap.computeIfAbsent(vo.getParentCode(), key -> new ArrayList<>()).add(vo);
        }
        for (SystemMenuVO vo : menuMap.values()) {
            List<SystemMenuVO> children = childrenMap.get(vo.getMenuCode());
            if (children != null) {
                vo.setChildren(children);
            }
            if (vo.getParentCode() == null || !menuMap.containsKey(vo.getParentCode())) {
                roots.add(vo);
            }
        }
        return roots;
    }

    private SystemMenuVO convertMenu(SystemMenu menu) {
        SystemMenuVO vo = new SystemMenuVO();
        vo.setMenuCode(menu.getMenuCode());
        vo.setMenuName(menu.getMenuName());
        vo.setParentCode(menu.getParentCode());
        vo.setMenuPath(menu.getMenuPath());
        vo.setRouteName(menu.getRouteName());
        vo.setMenuType(menu.getMenuType());
        vo.setSortOrder(menu.getSortOrder());
        return vo;
    }

    private UserRoleTypeEnum getRoleTypeEnum(Integer roleType) {
        for (UserRoleTypeEnum item : UserRoleTypeEnum.values()) {
            if (item.getCode().equals(roleType)) {
                return item;
            }
        }
        throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "角色不存在");
    }
}
