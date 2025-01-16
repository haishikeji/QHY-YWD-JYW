package com.px.biz.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * OCR服务
 *
 * @author 品讯科技
 */
public interface IOcrService {
    /**
     * 文字识别
     *
     * @param fileId 文件ID
     * @return 识别结果
     * @throws Exception 异常
     */
    Map<String,Object> recognition(String fileId) throws Exception;

    /**
     * 文字识别
     *
     * @param file 文件
     * @return 识别结果
     * @throws Exception 异常
     */
    Map<String,Object> recognition(MultipartFile file) throws Exception;
}
