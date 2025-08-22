package com.gig.collide.Apientry.api.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户更新请求
 * 
 * <p>用于更新用户信息的请求对象，支持部分字段更新。</p>
 * <p>更新规则：</p>
 * <ul>
 *   <li>只更新请求中提供的字段</li>
 *   <li>未提供的字段保持原值不变</li>
 *   <li>支持更新：昵称、邮箱、手机号、头像、VIP状态</li>
 *   <li>更新时间会自动设置为当前时间</li>
 * </ul>
 * 
 * @author GIG Team
 * @version 2.0.0
 */
@Data
@Schema(description = "用户更新请求")
public class UserUpdateRequest {

    @Schema(description = "用户ID", required = true, example = "123")
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Schema(description = "昵称", required = false, example = "新昵称")
    private String nickname;

    @Schema(description = "邮箱地址", required = false, example = "newemail@example.com")
    private String email;

    @Schema(description = "手机号码", required = false, example = "13900139000")
    private String phone;

    @Schema(description = "头像URL", required = false, example = "https://example.com/new-avatar.jpg")
    private String avatar;

    @Schema(description = "VIP状态：Y-是VIP，N-非VIP", required = false, example = "Y")
    @Pattern(regexp = "^(Y|N)$", message = "VIP状态只能是Y或N")
    private String isVip;
} 