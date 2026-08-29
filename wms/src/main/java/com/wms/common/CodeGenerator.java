package com.wms.common;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;
import java.util.Scanner;

public class CodeGenerator {

    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotBlank(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir") + "/wms";

        FastAutoGenerator.create("jdbc:mysql://localhost:3306/wms02?useUnicode=true&characterEncoding=UTF8&useSSL=false&serverTimezone=GMT%2B8",
                        "root", "12345678")
                .globalConfig(builder -> {
                    builder.author("wms")
                            .enableSwagger()
                            .outputDir(projectPath + "/src/main/java")
                            .disableOpenDir();
                })
                .packageConfig(builder -> {
                    builder.parent("com.wms")
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(OutputFile.xml,
                                    projectPath + "/src/main/resources/mapper"));
                })
                .strategyConfig(builder -> {
                    builder.addInclude(scanner("表名，多个英文逗号分割").split(","))
                            .entityBuilder()
                            .enableLombok()
                            .naming(NamingStrategy.underline_to_camel)
                            .columnNaming(NamingStrategy.underline_to_camel)
                            .controllerBuilder()
                            .enableRestStyle()
                            .enableHyphenStyle()
                            .mapperBuilder()
                            .enableBaseResultMap()
                            .enableBaseColumnList()
                            .serviceBuilder()
                            .formatServiceFileName("%sService");
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}