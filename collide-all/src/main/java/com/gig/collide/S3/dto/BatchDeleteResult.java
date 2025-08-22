package com.gig.collide.S3.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 批量删除结果DTO
 */
@Data
@Builder
public class BatchDeleteResult {
    
    /**
     * 是否删除成功
     */
    private boolean success;
    
    /**
     * 成功删除的文件数量
     */
    private int deletedCount;
    
    /**
     * 删除失败的文件数量
     */
    private int failedCount;
    
    /**
     * 成功删除的文件键名列表
     */
    private List<String> deletedKeys;
    
    /**
     * 删除失败的文件键名列表
     */
    private List<String> failedKeys;
    
    /**
     * 错误信息（删除失败时）
     */
    private String errorMessage;
}
