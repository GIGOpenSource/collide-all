package com.gig.collide.Apientry.api.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户创建请求
 * 
 * <p>用于创建新用户的请求对象，包含用户的基本信息。</p>
 * <p>系统会自动设置以下默认值：</p>
 * <ul>
 *   <li>用户角色：默认为"user"（普通用户）</li>
 *   <li>VIP状态：默认为"N"（非VIP）</li>
 *   <li>用户状态：默认为"active"（活跃）</li>
 *   <li>创建时间：自动设置为当前时间</li>
 * </ul>
 * 
 * @author GIG Team
 * @version 1.0.0
 */
@Data
@Schema(description = "用户创建请求")
public class UserCreateRequest {

    @Schema(description = "用户名", required = true, example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "昵称", required = true, example = "张三")
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @Schema(description = "邮箱地址", required = false, example = "zhangsan@example.com")
    private String email;

    @Schema(description = "手机号码", required = false, example = "13800138000")
    private String phone;

    @Schema(description = "头像URL", required = false, example = "https://example.com/avatar.jpg")
    private String avatar;
} 