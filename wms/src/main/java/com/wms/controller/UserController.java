package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Menu;
import com.wms.entity.User;
import com.wms.service.MenuService;
import com.wms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    public Result save(@RequestBody User user){
        return userService.save(user)?Result.suc():Result.fail();
    }
    //更新
    @PostMapping("/update")
    public Result update(@RequestBody User user){
        return userService.updateById(user)?Result.suc():Result.fail();
    }
    //删除
    @GetMapping("/del")
    public Result del(@RequestParam String id){
        return userService.removeById(id)?Result.suc():Result.fail();
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

    //修改
    @PostMapping("/mod")
    public boolean mod(@RequestBody User user){
        return userService.updateById(user);
    }
    //新增或修改
    @PostMapping("/saveOrMod")
    public boolean saveOrMod(@RequestBody User user){
        return userService.saveOrUpdate(user);
    }
    //删除
    @GetMapping("/delete")
    public boolean delete(Integer id){
        return userService.removeById(id);
    }

    //停用/启用账号
    @GetMapping("/toggleValid")
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
}
