package com.px.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.px.biz.domain.DocActor;
import com.px.biz.domain.DocInfo;

import java.util.List;

/**
 * 文档协作Mapper接口
 *
 * @author 品讯科技
 * @date 2024-08
 */
public interface DocActorMapper extends BaseMapper<DocActor> {
    /**
     * 查询文档协作
     *
     * @param docId 文档协作主键
     * @return 文档协作
     */
    public DocActor selectDocActorByDocId(Long docId);

    /**
     * 查询文档协作列表
     *
     * @param docActor 文档协作
     * @return 文档协作集合
     */
    public List<DocActor> selectDocActorList(DocActor docActor);

    /**
     * 新增文档协作
     *
     * @param docActor 文档协作
     * @return 结果
     */
    public int insertDocActor(DocActor docActor);

    /**
     * 修改文档协作
     *
     * @param docActor 文档协作
     * @return 结果
     */
    public int updateDocActor(DocActor docActor);

    /**
     * 删除文档协作
     *
     * @param docId 文档协作主键
     * @return 结果
     */
    public int deleteDocActorByDocId(Long docId);

    /**
     * 批量删除文档协作
     *
     * @param docIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDocActorByDocIds(Long[] docIds);

    /**
     * 我发起的
     * @param userId 用户ID
     * @return 结果
     */
    List<DocInfo> initiationList(Long userId);

    /**
     * 我参与的
     * @param userId 用户ID
     * @return 结果
     */
    List<DocInfo> participationList(Long userId);
}
