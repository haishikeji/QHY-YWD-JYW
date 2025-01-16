package com.px.biz.service.impl;

import com.px.biz.domain.DocSpace;
import com.px.biz.mapper.DocSpaceMapper;
import com.px.biz.service.IDocDirService;
import com.px.biz.service.IDocSpaceService;
import com.px.common.core.domain.entity.SysDept;
import com.px.common.core.domain.entity.SysUser;
import com.px.common.enums.*;
import com.px.common.utils.DateUtils;
import com.px.common.utils.SecurityUtils;
import com.px.common.utils.bean.BeanUtils;
import com.px.system.domain.SysOperLog;
import com.px.system.service.ISysConfigService;
import com.px.system.service.ISysOperLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 文档空间管理Service业务层处理
 *
 * @author 品讯科技
 * @date 2024-08
 */
@Service
public class DocSpaceServiceImpl implements IDocSpaceService {
    @Resource
    private DocSpaceMapper docSpaceMapper;
    @Resource
    private ISysConfigService configService;
    @Resource
    private IDocDirService dirService;
    @Resource
    private ISysOperLogService operLogService;

    /**
     * 查询文档空间管理
     *
     * @param spaceId 文档空间管理主键
     * @return 文档空间管理
     */
    @Override
    public DocSpace selectDocSpaceBySpaceId(Long spaceId) {
        return docSpaceMapper.selectDocSpaceBySpaceId(spaceId);
    }

    /**
     * /**
     * 更新空间使用情况，做个冗余
     */
    @Override
    public void updateUsedCap(Long spaceId) {
        docSpaceMapper.updateUsedCap(spaceId);
    }

    /**
     * 查询文档空间管理列表
     *
     * @param docSpace 文档空间管理
     * @return 文档空间管理
     */
    @Override
    public List<DocSpace> selectDocSpaceList(DocSpace docSpace) {
        return docSpaceMapper.selectDocSpaceList(docSpace);
    }

    /**
     * 根据空间类型查询目录
     *
     * @param type 空间类型
     * @return 文档空间管理集合
     */
    @Override
    public DocSpace selectDocSpaceListByType(String type) {
        List<DocSpace> spaceList = getSpaceByUser(SecurityUtils.getUserId());
        DocSpace space = new DocSpace();
        spaceList.forEach(s -> {
            if (type.equals(s.getSpaceType())) {
                BeanUtils.copyProperties(s, space);
            }
        });
        return space;
    }

    /**
     * 通过用户ID获取公共空间、部门空间和个人空间
     *
     * @param userId 用户ID
     * @return 结果集
     */
    @Override
    public List<DocSpace> getSpaceByUser(Long userId) {
        return docSpaceMapper.selectDocSpaceByUser(userId);
    }

    /**
     * 新增文档空间管理
     *
     * @param docSpace 文档空间管理
     * @return 结果
     */
    @Override
    public int insertDocSpace(DocSpace docSpace) {
        docSpace.setCreateTime(DateUtils.getNowDate());
        return docSpaceMapper.insertDocSpace(docSpace);
    }

    /**
     * 修改文档空间管理
     *
     * @param docSpace 文档空间管理
     * @return 结果
     */
    @Override
    public int updateDocSpace(DocSpace docSpace) {
        docSpace.setUpdateTime(DateUtils.getNowDate());
        return docSpaceMapper.updateDocSpace(docSpace);
    }

    /**
     * 批量删除文档空间管理
     *
     * @param spaceIds 需要删除的文档空间管理主键
     * @return 结果
     */
    @Override
    public int deleteDocSpaceBySpaceIds(Long[] spaceIds) {
        return docSpaceMapper.deleteDocSpaceBySpaceIds(spaceIds);
    }

    /**
     * 删除文档空间管理信息
     *
     * @param spaceId 文档空间管理主键
     * @return 结果
     */
    @Override
    public int deleteDocSpaceBySpaceId(Long spaceId) {
        return docSpaceMapper.deleteDocSpaceBySpaceId(spaceId);
    }

    /**
     * 初始化用户空间
     *
     * @param user 用户实体
     */
    @Override
    public void initUserSpace(SysUser user) {
        String spaceName = "\"" + user.getNickName() + "\"的个人空间";
        String defaultCap = configService.selectConfigByKey("user.default.cap");

        initSpace(SpaceType.PERSONAL, user.getUserId(), spaceName, defaultCap);
    }

    /**
     * 初始化部门空间
     *
     * @param dept 部门实体
     */
    @Override
    public void initDeptSpace(SysDept dept) {
        String spaceName = "\"" + dept.getDeptName() + "\"的部门空间";
        String defaultCap = configService.selectConfigByKey("dept.default.cap");

        initSpace(SpaceType.DEPT, dept.getDeptId(), spaceName, defaultCap);
    }

    /**
     * 增加容量
     *
     * @param spaceId   空间ID
     * @param expandCap 容量大小
     */
    @Override
    public DocSpace addCap(Long spaceId, Long expandCap) {
        docSpaceMapper.addCap(spaceId, expandCap);
        return selectDocSpaceBySpaceId(spaceId);
    }

    /**
     * 改变已使用容量
     *
     * @param spaceId 空间ID
     * @param size    容量大小
     * @return 空间对象
     */
    @Override
    public DocSpace changeUsedCap(Long spaceId, Long size) {
        DocSpace space = selectDocSpaceBySpaceId(spaceId);
        String publicSpace = "1";
        if (publicSpace.equals(space.getSpaceType())) {
            //如果是公共空间，则不处理
            return space;
        }
        BigDecimal used = space.getUsedCap();
        if (used == null) {
            used = BigDecimal.ZERO;
        }
        BigDecimal cap = space.getSpaceCap();
        long convert = 1024 * 1024 * 1024;
        BigDecimal addSize = BigDecimal.valueOf(size).divide(BigDecimal.valueOf(convert));
        used = used.add(addSize);
        BigDecimal free = cap.subtract(used);
        space.setFreeCap(free);
        space.setUsedCap(used);
        updateDocSpace(space);
        return space;
    }

    /**
     * 初始化空间
     *
     * @param spaceType  空间类型
     * @param owner      所有人
     * @param spaceName  空间名称
     * @param defaultCap 空间大小
     */
    private void initSpace(SpaceType spaceType, Long owner, String spaceName, String defaultCap) {
        long start = System.currentTimeMillis();
        //初始化部门空间
        DocSpace space = new DocSpace();
        space.setSpaceType(spaceType.getValue());
        space.setOwner(owner);

        List<DocSpace> list = selectDocSpaceList(space);
        space.setSpaceName(spaceName);
        if (list.size() > 0) {
            //如果有记录，则更新空间名称
            space.setSpaceId(list.get(0).getSpaceId());
            space.setUpdateBy(SecurityUtils.getUsername());
            updateDocSpace(space);
        } else {
            //如果木有记录，则插入
            space.setSpaceCap(new BigDecimal(defaultCap));
            space.setCreateBy(SecurityUtils.getUsername());
            insertDocSpace(space);

            //设置操作日志
            SysOperLog log = new SysOperLog();
            // 设置action动作
            log.setBusinessType(BusinessType.INSERT.ordinal());
            // 设置标题
            log.setTitle(spaceType.getName() + "空间");
            // 设置事件类型
            log.setEventType(EventType.SYSTEM.ordinal());
            // 设置事件级别
            log.setEventLevel(EventLevel.HIGH.ordinal());
            // 设置操作人类别
            log.setOperatorType(OperatorType.MANAGE.ordinal());
            log.setOperParam(spaceName);
            log.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 设置消耗时间
            log.setCostTime(System.currentTimeMillis() - start);

            operLogService.insertOperlog(log);
        }
        //初始化顶层目录
        dirService.initTopDir(space);
    }
}
