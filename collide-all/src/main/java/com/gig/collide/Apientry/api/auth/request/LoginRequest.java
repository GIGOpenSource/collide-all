package com.gig.collide.Apientry.api.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 *
 * @author GIG Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录请求")
public class LoginRequest {

    /**
     * 用户名
     */
    @Schema(description = "用户名", required = true, example = "testuser")
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", required = true, example = "password123")
    @NotBlank(message = "密码不能为空")
    private String password;
}
