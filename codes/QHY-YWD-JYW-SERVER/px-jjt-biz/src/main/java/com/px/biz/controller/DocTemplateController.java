package com.px.biz.controller;

import com.px.biz.domain.DocTemplate;
import com.px.biz.service.IDocTemplateService;
import com.px.biz.service.IMongoService;
import com.px.biz.vo.DocumentVO;
import com.px.biz.vo.UserVO;
import com.px.common.annotation.Log;
import com.px.common.constant.Constants;
import com.px.common.core.controller.BaseController;
import com.px.common.core.domain.AjaxResult;
import com.px.common.core.domain.entity.SysUser;
import com.px.common.core.page.TableDataInfo;
import com.px.common.enums.BusinessType;
import com.px.common.enums.EventLevel;
import com.px.common.utils.file.FileUtils;
import com.px.common.utils.poi.ExcelUtil;
import com.px.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档模板Controller
 *
 * @author 品讯科技
 * @date 2024-08
 */
@Api(tags = "文档模板")
@RestController
@RequestMapping("/biz/template")
@Slf4j
public class DocTemplateController extends BaseController {
    @Resource
    private IDocTemplateService docTemplateService;
    @Resource
    private IMongoService mongoService;
    @Resource
    private ISysUserService userService;

    /**
     * 查询文档模板列表
     */
    @ApiOperation("查询文档模板列表")
//    @PreAuthorize("@ss.hasPermi('biz:template:list')")
    @GetMapping("/list")
    public TableDataInfo list(DocTemplate docTemplate) {
        startPage();
        List<DocTemplate> list = docTemplateService.selectDocTemplateList(docTemplate);
        return getDataTable(list);
    }

    /**
     * 导出文档模板列表
     */
    @ApiOperation("导出文档模板列表")
    @PreAuthorize("@ss.hasPermi('biz:template:export')")
    @Log(title = "文档模板", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DocTemplate docTemplate) {
        List<DocTemplate> list = docTemplateService.selectDocTemplateList(docTemplate);
        ExcelUtil<DocTemplate> util = new ExcelUtil<DocTemplate>(DocTemplate.class);
        util.exportExcel(response, list, "文档模板数据");
    }

    /**
     * 获取文档模板详细信息
     */
    @ApiOperation("获取文档模板详细信息")
//    @PreAuthorize("@ss.hasPermi('biz:template:query')")
    @GetMapping(value = "/{tmplId}")
    public AjaxResult getInfo(@PathVariable("tmplId") Long tmplId) {
        return success(docTemplateService.selectDocTemplateByTmplId(tmplId));
    }

    /**
     * 新增文档模板
     */
    @ApiOperation("新增文档模板")
    @PreAuthorize("@ss.hasPermi('biz:template:add')")
    @Log(title = "文档模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DocTemplate docTemplate) {
        try {
            String filePath = "";
            String fileName = "";
            switch (docTemplate.getTmplType()) {
                case "word":
                    filePath = "file/new.docx";
                    fileName = docTemplate.getTmplName() + ".docx";
                    break;
                case "excel":
                    filePath = "file/new.xlsx";
                    fileName = docTemplate.getTmplName() + ".xlsx";
                    break;
                case "ppt":
                    filePath = "file/new.pptx";
                    fileName = docTemplate.getTmplName() + ".pptx";
                    break;
                default:
            }

            InputStream is = this.getClass().getResourceAsStream("/" + filePath);
            MultipartFile multipartFile = FileUtils.getMultipartFile(is, fileName);
            DocumentVO vo = mongoService.uploadFile(multipartFile);
            docTemplate.setFileId(vo.getFileId());
            return toAjax(docTemplateService.insertDocTemplate(docTemplate));
        } catch (Exception e) {
            log.error("新建文件出错啦：{}", e.getMessage());
            return error("新建文件出错");
        }
    }

    /**
     * 修改文档模板
     */
    @ApiOperation("修改文档模板")
    @PreAuthorize("@ss.hasPermi('biz:template:edit')")
    @Log(title = "文档模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DocTemplate docTemplate) {
        return toAjax(docTemplateService.updateDocTemplate(docTemplate));
    }

    /**
     * 删除文档模板
     */
    @ApiOperation("删除文档模板")
    @PreAuthorize("@ss.hasPermi('biz:template:remove')")
    @Log(title = "文档模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tmplIds}")
    public AjaxResult remove(@PathVariable Long[] tmplIds) {
        return toAjax(docTemplateService.deleteDocTemplateByTmplIds(tmplIds));
    }

    @ApiOperation("使用该文档的人员")
    @GetMapping("/users")
    public AjaxResult users() {
        List<UserVO> result = new ArrayList<>();
        userService.selectUserList(new SysUser()).forEach(u -> {
            if (u.getUserId() > 1) {
                UserVO vo = new UserVO();
                vo.setUserId(u.getUserId());
                vo.setUserName(u.getNickName());
                if (u.getDept() != null) {
                    vo.setDeptName(u.getDept().getDeptName());
                }
                result.add(vo);
            }
        });
        return success(result);
    }

    /**
     * 文件上传
     *
     * @param file 文件
     * @return 上传结果
     */
    @ApiOperation("模板文件上传-单文件")
    @Log(title = "文件基本信息表", businessType = BusinessType.INSERT, eventLevel = EventLevel.MIDDLE)
    @PostMapping("/upload")
    public AjaxResult uploadFile(@ApiParam(value = "文件", required = true) @RequestPart(value = "file") MultipartFile file) {
        try {
            DocumentVO vo = mongoService.uploadFile(file);
            return upload(vo);
        } catch (Exception e) {
            log.error("文件上传失败：", e);
            return error(e.getMessage());
        }
    }

    /**
     * 多文件上传
     *
     * @param files 文件列表
     * @return 返回
     */
    @ApiOperation("文件上传-多文件")
    @PostMapping("/uploadFiles")
    @Log(title = "文件基本信息表", businessType = BusinessType.INSERT, eventLevel = EventLevel.MIDDLE)
    public AjaxResult uploadFile(@ApiParam(value = "文件", required = true) @RequestPart(value = "files") List<MultipartFile> files) {
        try {
            mongoService.uploadFiles(files).forEach(vo -> {
                upload(vo);
            });
            return success();
        } catch (Exception e) {
            e.printStackTrace();
            return error(e.getMessage());
        }
    }

    private AjaxResult upload(DocumentVO vo) {
        DocTemplate docTemplate = new DocTemplate();
        docTemplate.setTmplName(vo.getFileName());
        if (Constants.WORD_EXTENSION.contains(vo.getSuffix())) {
            docTemplate.setTmplType("word");
        }
        if (Constants.EXCEL_EXTENSION.contains(vo.getSuffix())) {
            docTemplate.setTmplType("excel");
        }
        if (Constants.PPT_EXTENSION.contains(vo.getSuffix())) {
            docTemplate.setTmplType("ppt");
        }
        docTemplate.setFileId(vo.getFileId());
        return toAjax(docTemplateService.insertDocTemplate(docTemplate));
    }
}
