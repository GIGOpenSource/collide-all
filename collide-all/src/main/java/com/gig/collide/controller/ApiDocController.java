package com.gig.collide.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * API文档控制器
 * 提供模块化API文档的访问入口
 *
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-31
 */
@Slf4j
@Controller
@RequestMapping("/api-docs")
@Hidden // 在Swagger文档中隐藏此控制器
public class ApiDocController {

    /**
     * API文档首页
     * 展示模块化的API文档导航页面
     */
    @GetMapping({"", "/", "/index"})
    public String apiDocsIndex(Model model) {
        log.info("访问API文档首页");
        
        // 添加一些统计信息到模型中
        model.addAttribute("totalModules", 8);
        model.addAttribute("totalApis", "50+");
        model.addAttribute("totalControllers", "25+");
        model.addAttribute("coverage", "100%");
        
        // 重定向到静态HTML页面
        return "redirect:/api-docs.html";
    }

    /**
     * 跳转到Knife4j文档
     */
    @GetMapping("/knife4j")
    public String redirectToKnife4j() {
        log.info("跳转到Knife4j文档");
        return "redirect:/doc.html";
    }

    /**
     * 跳转到指定模块的API文档
     */
    @GetMapping("/module/{moduleName}")
    public String redirectToModuleDoc(String moduleName) {
        log.info("跳转到模块API文档: {}", moduleName);
        return "redirect:/doc.html#/" + moduleName;
    }
}
