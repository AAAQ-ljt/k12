package com.nexora.admin.controller;

import com.nexora.admin.vo.SystemMenuVO;
import com.nexora.entity.po.SystemMenu;
import com.nexora.entity.query.SystemMenuQuery;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.service.SystemMenuService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统菜单控制器 — 获取菜单树
 */
@RestController
@RequestMapping("/systemMenu")
public class SystemMenuController {

    @Resource
    private SystemMenuService systemMenuService;

    /**
     * 获取菜单树形结构
     */
    @GetMapping("/getMenuTree")
    public ResponseVO<List<SystemMenuVO>> getMenuTree() {
        // 查询全部菜单
        List<SystemMenu> allMenus = systemMenuService.findListByParam(new SystemMenuQuery());

        // 转换为 VO
        List<SystemMenuVO> voList = allMenus.stream().map(this::convertToVO).collect(Collectors.toList());

        // 按 parentId 分组
        Map<Integer, List<SystemMenuVO>> parentIdMap = voList.stream()
                .collect(Collectors.groupingBy(vo -> vo.getParentId() == null ? 0 : vo.getParentId()));

        // 递归构建树
        List<SystemMenuVO> tree = buildTree(parentIdMap, 0);
        return ResponseVO.success(tree);
    }

    /**
     * 递归构建菜单树
     */
    private List<SystemMenuVO> buildTree(Map<Integer, List<SystemMenuVO>> parentIdMap, Integer parentId) {
        List<SystemMenuVO> children = parentIdMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        // 按 sort 排序
        children.sort(Comparator.comparing(
                SystemMenuVO::getSort,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        for (SystemMenuVO child : children) {
            child.setChildren(buildTree(parentIdMap, child.getMenuId()));
        }
        return children;
    }

    /**
     * PO 转 VO
     */
    private SystemMenuVO convertToVO(SystemMenu menu) {
        SystemMenuVO vo = new SystemMenuVO();
        vo.setMenuId(menu.getMenuId());
        vo.setParentId(menu.getParentId());
        vo.setMenuName(menu.getMenuName());
        vo.setMenuCode(menu.getMenuCode());
        vo.setMenuType(menu.getMenuType());
        vo.setPath(menu.getPath());
        vo.setIcon(menu.getIcon());
        vo.setSort(menu.getSort());
        vo.setStatus(menu.getStatus());
        vo.setCreateTime(menu.getCreateTime());
        vo.setUpdateTime(menu.getUpdateTime());
        return vo;
    }
}
