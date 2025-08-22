package com.gig.collide;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Collide 单体应用启动类
 *
 * @author Collide Team
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration.class
})
@EnableTransactionManagement
@EnableScheduling
@MapperScan("com.gig.collide.mapper")
public class CollideAllApplication {

    public static void main(String[] args) {
        try {
            log.info("正在启动 Collide All Application...");
            SpringApplication.run(CollideAllApplication.class, args);
            log.info("=================================");
            log.info("Collide All Application Started!");
            log.info("=================================");
        } catch (Exception e) {
            log.error("应用启动失败!", e);
            System.err.println("=================================");
            System.err.println("应用启动失败!");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("=================================");
            System.exit(1);
        }
    }
}
