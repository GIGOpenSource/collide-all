package com.gig.collide.Apientry.api.social.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户互动响应对象
 * 用于整合返回用户的点赞和评论数据
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserInteractionResponse implements Serializable {

    /**
     * 互动ID
     */
    private Long id;

    /**
     * 互动类型
     * LIKE: 点赞
     * COMMENT: 评论
     */
    private String interactionType;

    /**
     * 互动子类型
     * 点赞: CONTENT, COMMENT
     * 评论: CONTENT, DYNAMIC
     */
    private String subType;

    /**
     * 目标对象ID
     */
    private Long targetId;

    /**
     * 目标对象标题
     */
    private String title;

    /**
     * 目标对象作者ID
     */
    private Long authorId;

    /**
     * 目标对象作者昵称
     */
    private String authorNickname;

    /**
     * 目标对象作者头像
     */
    private String authorAvatar;

    /**
     * 是否关注该内容作者
     */
    private Boolean isFollowingAuthor;

    /**
     * 该内容被点赞总数量
     */
    private Long contentLikeCount;

    /**
     * 是否点赞该内容
     */
    private Boolean isLike;

    /**
     * 内容封面图片URL列表
     */
    private List<String> contentCoverUrl;

    /**
     * 内容描述
     */
    private String contentDescription;

    /**
     * 互动用户ID
     */
    private Long userId;

    /**
     * 互动用户昵称
     */
    private String userNickname;

    /**
     * 互动用户头像
     */
    private String userAvatar;

    /**
     * 评论内容（仅当interactionType为COMMENT时有值）
     */
    private String commentContent;

    /**
     * 父评论ID（仅当interactionType为COMMENT时有值）
     */
    private Long parentCommentId;

    /**
     * 回复目标用户ID（仅当interactionType为COMMENT时有值）
     */
    private Long replyToUserId;

    /**
     * 回复目标用户昵称（仅当interactionType为COMMENT时有值）
     */
    private String replyToUserNickname;

    /**
     * 点赞数（仅当interactionType为COMMENT时有值）
     */
    private Integer likeCount;

    /**
     * 回复数（仅当interactionType为COMMENT时有值）
     */
    private Integer replyCount;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 是否为点赞类型
     */
    public boolean isLikeType() {
        return "LIKE".equals(interactionType);
    }

    /**
     * 是否为评论类型
     */
    public boolean isCommentType() {
        return "COMMENT".equals(interactionType);
    }

    /**
     * 是否为内容相关
     */
    public boolean isContentRelated() {
        return "CONTENT".equals(subType);
    }

    /**
     * 是否为评论相关
     */
    public boolean isCommentRelated() {
        return "COMMENT".equals(subType);
    }

    /**
     * 是否为动态相关
     */
    public boolean isDynamicRelated() {
        return "DYNAMIC".equals(subType);
    }
}
