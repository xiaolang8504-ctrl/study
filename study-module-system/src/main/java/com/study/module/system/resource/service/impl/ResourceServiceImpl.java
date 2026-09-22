package com.study.module.system.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.entity.MenuResource;
import com.study.module.system.menu.service.MenuResourceService;
import com.study.module.system.menu.service.MenuService;
import com.study.module.system.role.service.RoleMenuService;
import com.study.module.system.role.service.RoleService;
import com.study.module.system.role.entity.Role;
import com.study.module.system.resource.entity.Resource;
import com.study.module.system.resource.mapper.ResourceMapper;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.resource.service.ResourceService;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserRoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 系统资源表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    private static final Pattern PRE_AUTHORIZE_PATTERN = Pattern.compile("hasAuthority\\('([^']+)'\\)");

    private static final String CONTROLLER_BASE_PACKAGE = "com.study.module.system";

    private static final Pattern API_OPERATION_PATTERN = Pattern.compile("@ApiOperation\\(\"([^\"]+)\"\\)");

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuResourceService menuResourceService;

    @Autowired
    private RoleMenuService roleMenuService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserAuthorityCacheService userAuthorityCacheService;

    /**
     * 检测指定ID是否有子
     */
    @Override
    public boolean checkSonByPid(Integer pid) {
        if (this.getOne(new LambdaQueryWrapper<Resource>().eq(Resource::getPid, pid),false) !=null){
            return true;
        }
        return false;
    }

    /**
     * 获取指定code信息
     */
    @Override
    public Resource getResourceCode(String code) {
        return this.getOne(new LambdaQueryWrapper<Resource>().eq(Resource::getCode, code));
    }

    /**
     * 检测指定code存在
     */
    @Override
    public boolean checkResourceCode(String code){
        if (getResourceCode(code) != null){
            return true;
        }
        return false;
    }

    /**
     * 检测指定code和ID存在
     */
    @Override
    public boolean checkResourceCodeById(Integer id,String code) {
        if ((this.getOne(new LambdaQueryWrapper<Resource>().eq(Resource::getCode, code).ne(Resource::getId,id)) != null)){
            return true;
        }
        return false;
    }

    /**
     * 检测指定ID存在
     */
    @Override
    public void checkResourceById(Integer id) {
        if (this.getOne(new LambdaQueryWrapper<Resource>().eq(Resource::getId, id)) == null){
            throw new LogicException(ErrorCodeConstants.RES_NOT_EXIST);
        }
    }

    /**
     * 从Controller权限注解生成资源
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Resource> createPre() {
        Map<String, String> resourceMap = readPreAuthorizeResources();
        List<Resource> resourceList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        resourceMap.forEach((code, resourceName) -> {
            if (checkResourceCode(code)) {
                return;
            }
            Integer pid = getPidByPreCode(code);
            if (pid == null) {
                return;
            }
            Resource resource = new Resource();
            resource.setCode(code);
            resource.setResourceName(resourceName);
            resource.setPid(pid);
            resource.setCreateTime(now);
            resourceList.add(resource);
        });
        if (!resourceList.isEmpty()) {
            if (!this.saveBatch(resourceList)) {
                throw new LogicException(ErrorCodeConstants.CREATE_RES_FAIL);
            }
            resourceList.forEach(resource -> {
                Resource updateResource = new Resource();
                updateResource.setId(resource.getId());
                updateResource.setSort(resource.getId());
                this.updateById(updateResource);
                resource.setSort(resource.getId());
            });
        }
        refreshUserAuthoritiesByMenuIds(syncMenuResources(resourceMap.keySet()));
        return resourceList;
    }

    /**
     * 将资源绑定到同名业务菜单，并刷新菜单中的冗余资源字段。
     */
    private List<Integer> syncMenuResources(Iterable<String> resourceCodes) {
        List<String> codes = new ArrayList<>();
        resourceCodes.forEach(codes::add);
        if (codes.isEmpty()) {
            return new ArrayList<>();
        }
        List<Resource> resources = this.list(new LambdaQueryWrapper<Resource>().in(Resource::getCode, codes));
        if (resources.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> menuCodes = resources.stream().map(this::getMenuCode).filter(item -> item != null)
                .distinct().collect(Collectors.toList());
        if (menuCodes.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, Menu> menuMap = menuService.list(new LambdaQueryWrapper<Menu>()
                        .in(Menu::getCode, menuCodes)).stream()
                .collect(Collectors.toMap(Menu::getCode, item -> item, (first, second) -> first));
        List<Integer> menuIds = menuMap.values().stream().map(Menu::getId).collect(Collectors.toList());
        List<MenuResource> existingRelations = menuResourceService.list(new LambdaQueryWrapper<MenuResource>()
                .in(MenuResource::getMenuId, menuIds));
        HashSet<String> relationKeys = existingRelations.stream()
                .map(item -> item.getMenuId() + ":" + item.getResourceId()).collect(Collectors.toCollection(HashSet::new));
        List<MenuResource> newRelations = new ArrayList<>();
        Set<Integer> changedMenuIds = new HashSet<>();
        for (Resource resource : resources) {
            Menu menu = menuMap.get(getMenuCode(resource));
            if (menu == null || !relationKeys.add(menu.getId() + ":" + resource.getId())) {
                continue;
            }
            MenuResource relation = new MenuResource();
            relation.setMenuId(menu.getId());
            relation.setResourceId(resource.getId());
            relation.setResourceLevel(resource.getPid() + "-" + resource.getId());
            relation.setCreateTime(LocalDateTime.now());
            newRelations.add(relation);
            changedMenuIds.add(menu.getId());
        }
        if (!newRelations.isEmpty() && !menuResourceService.saveBatch(newRelations)) {
            throw new LogicException(ErrorCodeConstants.CREATE_RES_FAIL);
        }
        if (!changedMenuIds.isEmpty()) {
            refreshMenuResourceFields(menuMap.values().stream()
                    .filter(menu -> changedMenuIds.contains(menu.getId())).collect(Collectors.toList()));
        }
        return new ArrayList<>(changedMenuIds);
    }

    /**
     * 根据资源编码获取对应的业务菜单编码。
     */
    private String getMenuCode(Resource resource) {
        String[] codeParts = resource.getCode().split(":");
        return codeParts.length < 3 ? null : codeParts[1];
    }

    /**
     * 刷新菜单表中冗余存储的资源 ID 与层级信息。
     */
    private void refreshMenuResourceFields(Iterable<Menu> menus) {
        for (Menu menu : menus) {
            List<MenuResource> relations = menuResourceService.list(new LambdaQueryWrapper<MenuResource>()
                    .eq(MenuResource::getMenuId, menu.getId()));
            List<String> resourceIds = relations.stream().map(MenuResource::getResourceId).sorted()
                    .map(String::valueOf).collect(Collectors.toList());
            List<String> resourceLevels = relations.stream().sorted(Comparator.comparing(MenuResource::getResourceId))
                    .map(MenuResource::getResourceLevel).collect(Collectors.toList());
            menu.setResourceIds(String.join(",", resourceIds));
            menu.setResourceLevel(String.join(",", resourceLevels));
            if (!menuService.updateById(menu)) {
                throw new LogicException(ErrorCodeConstants.CREATE_RES_FAIL);
            }
        }
    }

    /**
     * 根据变更菜单定位角色与账号，并在事务提交后刷新其权限缓存。
     */
    private void refreshUserAuthoritiesByMenuIds(List<Integer> menuIds) {
        if (menuIds.isEmpty()) {
            return;
        }
        Set<Integer> roleIds = new HashSet<>(roleMenuService.roleIdsByMenuId(menuIds));
        roleService.list(new LambdaQueryWrapper<Role>().eq(Role::getIsSystem, 1))
                .forEach(role -> roleIds.add(role.getId()));
        userAuthorityCacheService.refreshUserAuthorityCachesAfterCommit(
                userRoleService.listUserIdsByRoleIds(new ArrayList<>(roleIds)));
    }

    /**
     * 从运行时类路径扫描控制器，汇总权限编码和资源名称。
     */
    private Map<String, String> readPreAuthorizeResources() {
        Map<String, String> resourceMap = new LinkedHashMap<>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*Controller$")));
        scanner.findCandidateComponents(CONTROLLER_BASE_PACKAGE).stream()
                .map(candidate -> candidate.getBeanClassName())
                .sorted()
                .forEach(className -> parseControllerClass(className, resourceMap));
        return resourceMap;
    }

    /**
     * 解析控制器方法上的权限与 Swagger 描述；已编译类可随 JAR 一同部署。
     */
    private void parseControllerClass(String className, Map<String, String> resourceMap) {
        try {
            Class<?> controllerClass = Class.forName(className);
            for (Method method : controllerClass.getDeclaredMethods()) {
                PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
                if (preAuthorize == null) {
                    continue;
                }
                String code = getAuthorityCode(preAuthorize.value());
                if (code == null) {
                    continue;
                }
                ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
                String resourceName = apiOperation == null || apiOperation.value().trim().isEmpty()
                        ? method.getName() : apiOperation.value().trim();
                resourceMap.putIfAbsent(code, resourceName);
            }
        } catch (ClassNotFoundException e) {
            throw new LogicException(ErrorCodeConstants.CREATE_RES_FAIL);
        }
    }

    /**
     * 从权限表达式中提取 hasAuthority 的权限编码。
     */
    private String getAuthorityCode(String expression) {
        java.util.regex.Matcher matcher = PRE_AUTHORIZE_PATTERN.matcher(expression);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * 根据权限编码前两段查询父级资源ID
     */
    private Integer getPidByPreCode(String code) {
        String[] codeArray = code.split(":");
        if (codeArray.length < 3) {
            return null;
        }
        Resource parentResource = getResourceCode(codeArray[0] + ":" + codeArray[1]);
        return parentResource == null ? null : parentResource.getId();
    }
}
