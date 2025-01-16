package com.px.biz.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.px.common.annotation.Excel;
import com.px.common.core.domain.BaseEntity;

/**
 * 目录模板对象 doc_dir_template
 *
 * @author 品讯科技
 * @date 2024-08
 */
@ApiModel(value = "DocDirTemplate", description = "目录模板")
public class DocDirTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    @ApiModelProperty("模板ID")
    @TableId
    private Long tmplId;

    /** 模板名称 */
    @ApiModelProperty("模板名称")
    @Excel(name = "模板名称")
    private String tmplName;

    /** 模板内容 */
    @ApiModelProperty("模板内容")
    private String tmplContent;

    public void setTmplId(Long tmplId)
    {
        this.tmplId = tmplId;
    }

    public Long getTmplId()
    {
        return tmplId;
    }
    public void setTmplName(String tmplName)
    {
        this.tmplName = tmplName;
    }

    public String getTmplName()
    {
        return tmplName;
    }
    public void setTmplContent(String tmplContent)
    {
        this.tmplContent = tmplContent;
    }

    public String getTmplContent()
    {
        return tmplContent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("tmplId", getTmplId())
            .append("tmplName", getTmplName())
            .append("tmplContent", getTmplContent())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
