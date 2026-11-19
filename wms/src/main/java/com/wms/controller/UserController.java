package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.AuthUtils;
import com.wms.common.IdSequence;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Menu;
import com.wms.entity.User;
import com.wms.service.MenuService;
import com.wms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wms
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private IdSequence idSequence;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/list")
    public List<User> list(){
        return userService.list();
    }
    @GetMapping("/findByNo")
    public Result findByNo(@RequestParam String no){
        List list = userService.lambdaQuery().eq(User::getNo,no).list();
        return list.size()>0?Result.suc(list):Result.fail();
    }
    //新增
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result save(@RequestBody User user){
        // 管理员不能新增超级管理员
        if (user.getRoleId() != null && user.getRoleId() == 0 && !isSuperAdmin()) {
            return Result.fail("只有超级管理员可以新增超级管理员");
        }
        if (StringUtils.isBlank(user.getPassword())) {
            return Result.fail("密码不能为空");
        }
        encodePasswordIfPresent(user);
        return userService.save(user)?Result.suc():Result.fail();
    }
    //更新
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result update(@RequestBody User user){
        Result denied = checkAdminTarget(user.getId());
        if (denied != null) {
            return denied;
        }
        // 管理员不能把别人提升为超级管理员
        if (user.getRoleId() != null && user.getRoleId() == 0 && !isSuperAdmin()) {
            return Result.fail("只有超级管理员可以设置超级管理员角色");
        }
        // 密码留空表示不修改；填写则加密后保存
        encodePasswordIfPresent(user);
        return userService.updateById(user)?Result.suc():Result.fail();
    }
    //删除
    @GetMapping("/del")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result del(@RequestParam String id){
        Result denied = checkAdminTarget(parseId(id));
        if (denied != null) {
            return denied;
        }
        boolean removed = userService.removeById(id);
        if (removed) {
            idSequence.reset("user");
        }
        return removed ? Result.suc() : Result.fail();
    }

    //登录
    @PostMapping("/login")
    public Result login(@RequestBody User user, HttpSession session){
        User dbUser = userService.lambdaQuery()
                .eq(User::getNo, user.getNo())
                .one();

        if(dbUser == null){
            return Result.fail("账号不存在");
        }
        // 校验账号是否被停用
        if(dbUser.getIsvalid() == null || !"Y".equals(dbUser.getIsvalid())){
            return Result.fail("该账号已被停用，请联系管理员");
        }
        // BCrypt 密码校验：兼容明文（旧数据）和 BCrypt（新数据）
        boolean passwordMatch;
        if(dbUser.getPassword().startsWith("$2a$") || dbUser.getPassword().startsWith("$2b$")){
            passwordMatch = passwordEncoder.matches(user.getPassword(), dbUser.getPassword());
        } else {
            // 旧明文密码，匹配后自动升级为 BCrypt
            passwordMatch = user.getPassword().equals(dbUser.getPassword());
            if(passwordMatch){
                dbUser.setPassword(passwordEncoder.encode(user.getPassword()));
                userService.updateById(dbUser);
            }
        }
        if(!passwordMatch){
            return Result.fail("账号或密码错误");
        }

        // 密码不返回给前端
        dbUser.setPassword(null);
        List menuList = menuService.lambdaQuery().like(Menu::getMenuright, dbUser.getRoleId()).list();

        // 将用户信息存入 Spring Security Context（这样后续请求才能通过鉴权）
        String roleName = "ROLE_" + dbUser.getRoleId();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                dbUser.getNo(), null,
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
        // 把用户 id 放进 details，业务代码取操作人时不再依赖前端传参
        auth.setDetails(dbUser.getId());
        SecurityContextHolder.getContext().setAuthentication(auth);
        // 同步到 Spring Session（Redis）
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        HashMap res = new HashMap();
        res.put("user", dbUser);
        res.put("menu", menuList);
        return Result.suc(res);
    }

    //登出
    @PostMapping("/logout")
    public Result logout(HttpSession session){
        session.invalidate();
        return Result.suc();
    }

    //修改（个人资料）：只允许改自己，且不允许改角色、启用状态，防止越权提权
    @PostMapping("/mod")
    public boolean mod(@RequestBody User user){
        Integer currentUserId = AuthUtils.currentUserId();
        if (currentUserId != null) {
            user.setId(currentUserId);
        }
        user.setRoleId(null);
        user.setIsvalid(null);
        encodePasswordIfPresent(user);
        return userService.updateById(user);
    }
    //新增或修改
    @PostMapping("/saveOrMod")
    @PreAuthorize("hasAnyRole('0','1')")
    public boolean saveOrMod(@RequestBody User user){
        if (user.getId() != null && checkAdminTarget(user.getId()) != null) {
            return false;
        }
        if (user.getRoleId() != null && user.getRoleId() == 0 && !isSuperAdmin()) {
            return false;
        }
        encodePasswordIfPresent(user);
        return userService.saveOrUpdate(user);
    }
    //删除
    @GetMapping("/delete")
    @PreAuthorize("hasAnyRole('0','1')")
    public boolean delete(Integer id){
        if (checkAdminTarget(id) != null) {
            return false;
        }
        boolean removed = userService.removeById(id);
        if (removed) {
            idSequence.reset("user");
        }
        return removed;
    }

    //停用/启用账号
    @GetMapping("/toggleValid")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result toggleValid(@RequestParam Integer id){
        User user = userService.getById(id);
        if(user == null){
            return Result.fail("用户不存在");
        }
        // 超级管理员不允许停用
        if(user.getRoleId() != null && user.getRoleId() == 0){
            return Result.fail("超级管理员不允许停用");
        }
        String newValid = "Y".equals(user.getIsvalid()) ? "N" : "Y";
        user.setIsvalid(newValid);
        userService.updateById(user);
        return Result.suc();
    }

    //查询（模糊、匹配）
    @PostMapping("/listP")
    public Result listP(@RequestBody User user){
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(StringUtils.isNotBlank(user.getName())){
            lambdaQueryWrapper.like(User::getName,user.getName());
        }

        return Result.suc(userService.list(lambdaQueryWrapper));
    }

    @PostMapping("/listPage")
//    public List<User> listPage(@RequestBody HashMap map){
    public List<User> listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        System.out.println("name==="+(String)param.get("name"));
        /*System.out.println("no==="+(String)param.get("no"));*/
        /*LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(User::getName,user.getName());

        return userService.list(lambdaQueryWrapper);*/

        Page<User> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.like(User::getName,name);


        IPage result = userService.page(page,lambdaQueryWrapper);

        System.out.println("total=="+result.getTotal());

        return result.getRecords();
    }

    @PostMapping("/listPageC")
    public List<User> listPageC(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        System.out.println("name==="+(String)param.get("name"));



        Page<User> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.like(User::getName,name);


        //IPage result = userService.pageC(page);
        IPage result = userService.pageCC(page,lambdaQueryWrapper);

        System.out.println("total=="+result.getTotal());

        return result.getRecords();
    }

    @PostMapping("/listPageC1")
    public Result listPageC1(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        String sex = (String)param.get("sex");
        String roleId = (String)param.get("roleId");

        Page<User> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            lambdaQueryWrapper.like(User::getName,name);
        }
        if(StringUtils.isNotBlank(sex)){
            lambdaQueryWrapper.eq(User::getSex,sex);
        }
        if(StringUtils.isNotBlank(roleId)){
            lambdaQueryWrapper.eq(User::getRoleId,roleId);
        }

        //IPage result = userService.pageC(page);
        IPage result = userService.pageCC(page,lambdaQueryWrapper);

        System.out.println("total=="+result.getTotal());

        return Result.suc(result.getRecords(),result.getTotal());
    }

    //修改自己的密码（校验原密码）
    @PostMapping("/changePassword")
    public Result changePassword(@RequestBody HashMap<String, String> body){
        Integer uid = AuthUtils.currentUserId();
        if (uid == null) {
            return Result.fail("登录状态已失效，请重新登录");
        }
        User dbUser = userService.getById(uid);
        if (dbUser == null) {
            return Result.fail("用户不存在");
        }
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        if (StringUtils.isBlank(oldPwd) || StringUtils.isBlank(newPwd)) {
            return Result.fail("原密码和新密码不能为空");
        }
        if (!passwordMatches(oldPwd, dbUser.getPassword())) {
            return Result.fail("原密码不正确");
        }
        String trimmed = newPwd.trim();
        if (trimmed.length() < 3 || trimmed.length() > 20) {
            return Result.fail("新密码长度需在 3 到 20 个字符之间");
        }
        if (passwordMatches(trimmed, dbUser.getPassword())) {
            return Result.fail("新密码不能与原密码相同");
        }
        User update = new User();
        update.setId(uid);
        update.setPassword(passwordEncoder.encode(trimmed));
        return userService.updateById(update) ? Result.suc() : Result.fail("修改失败，请稍后重试");
    }

    //管理员重置他人密码
    @PostMapping("/resetPassword")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result resetPassword(@RequestBody HashMap<String, Object> body){
        Integer targetId = parseId(String.valueOf(body.get("id")));
        if (targetId == null) {
            return Result.fail("参数错误");
        }
        Result denied = checkAdminTarget(targetId);
        if (denied != null) {
            return denied;
        }
        Integer currentId = AuthUtils.currentUserId();
        if (currentId != null && currentId.equals(targetId)) {
            return Result.fail("请使用“修改密码”修改自己的密码");
        }
        String newPwd = body.get("password") == null ? null : String.valueOf(body.get("password"));
        if (StringUtils.isBlank(newPwd)) {
            return Result.fail("新密码不能为空");
        }
        String trimmed = newPwd.trim();
        if (trimmed.length() < 3 || trimmed.length() > 20) {
            return Result.fail("密码长度需在 3 到 20 个字符之间");
        }
        User update = new User();
        update.setId(targetId);
        update.setPassword(passwordEncoder.encode(trimmed));
        return userService.updateById(update) ? Result.suc() : Result.fail("重置失败，请稍后重试");
    }

    /**
     * 密码为空时保持原值（updateById 忽略 null），否则用 BCrypt 加密后保存
     */
    private void encodePasswordIfPresent(User user) {
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword(null);
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
    }

    /**
     * 兼容历史明文密码与 BCrypt 密码
     */
    private boolean passwordMatches(String raw, String stored) {
        if (raw == null || stored == null) {
            return false;
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(raw, stored);
        }
        return raw.equals(stored);
    }

    /**
     * 保护超级管理员账号：目标是超级管理员时，只有超级管理员角色可以操作
     */
    private Result checkAdminTarget(Integer targetId) {
        if (targetId == null) {
            return null;
        }
        User target = userService.getById(targetId);
        if (target == null) {
            return Result.fail("用户不存在");
        }
        if (target.getRoleId() != null && target.getRoleId() == 0 && !isSuperAdmin()) {
            return Result.fail("只有超级管理员可以修改超级管理员");
        }
        return null;
    }

    private boolean isSuperAdmin() {
        return Integer.valueOf(0).equals(AuthUtils.currentRole());
    }

    private Integer parseId(String id) {
        try {
            return Integer.valueOf(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
