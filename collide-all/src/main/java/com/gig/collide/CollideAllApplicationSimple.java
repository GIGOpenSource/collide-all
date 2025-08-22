package com.gig.collide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Collide 单体应用启动类 - 简化版本
 *
 * @author Collide Team
 * @since 1.0.0
 */
@SpringBootApplication
public class CollideAllApplicationSimple {

    public static void main(String[] args) {
        SpringApplication.run(CollideAllApplicationSimple.class, args);
        System.out.println("=================================");
        System.out.println("Collide All Application Started!");
        System.out.println("=================================");
    }
}
