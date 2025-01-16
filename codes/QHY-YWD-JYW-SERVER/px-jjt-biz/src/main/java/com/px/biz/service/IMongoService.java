package com.px.biz.service;

import com.px.biz.domain.DocumentFile;
import com.px.biz.vo.DocumentVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Mongo存储Service接口
 *
 * @author 品讯科技
 * @date 2024-08
 */
public interface IMongoService {
    /**
     * 文件上传
     *
     * @param file 文件
     * @return 结果
     * @throws Exception
     */
    DocumentVO uploadFile(MultipartFile file) throws Exception;

    /**
     * 多文件上传
     *
     * @param files 文件
     * @return 结果
     */
    List<DocumentVO> uploadFiles(List<MultipartFile> files);

    /**
     * 文件下载
     *
     * @param fileId 文件ID
     * @return 结果
     */
    DocumentVO downloadFile(String fileId);

    /**
     * 前端文件下载
     *
     * @param fileId 文件ID
     * @param isDown 是否下载
     * @return 下载流
     */
    ResponseEntity<Object> download(String fileId, boolean isDown);

    /**
     * 文件删除
     *
     * @param fileId 文件ID
     */
    void removeFile(String fileId);

    /**
     * 查询并复制mongo文件
     *
     * @param fileId 文件id
     * @return 结果
     */
    DocumentVO copy(String fileId);


    /**
     * 根据文件ID获取文档内容
     *
     * @param fileId 文件ID
     * @return documentFile
     */
    public Optional<DocumentFile> findByFileId(String fileId);
}
