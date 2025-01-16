package com.px.sms.controller;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.px.common.annotation.Log;
import com.px.common.core.controller.BaseController;
import com.px.common.core.domain.AjaxResult;
import com.px.common.enums.BusinessType;
import com.px.sms.domain.SmsRecord;
import com.px.sms.service.ISmsRecordService;
import com.px.common.utils.poi.ExcelUtil;
import com.px.common.core.page.TableDataInfo;

/**
 * 短信记录Controller
 *
 * @author 品讯科技
 * @date 2024-08
 */
@Api(tags="短信记录")
@RestController
@RequestMapping("/sms/record")
public class SmsRecordController extends BaseController
{
    @Resource
    private ISmsRecordService smsRecordService;

    /**
     * 查询短信记录列表
     */
    @ApiOperation("查询短信记录列表")
    @PreAuthorize("@ss.hasPermi('sms:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(SmsRecord smsRecord)
    {
        startPage();
        List<SmsRecord> list = smsRecordService.selectSmsRecordList(smsRecord);
        return getDataTable(list);
    }

    /**
     * 导出短信记录列表
     */
    @ApiOperation("导出短信记录列表")
    @PreAuthorize("@ss.hasPermi('sms:record:export')")
    @Log(title = "短信记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SmsRecord smsRecord)
    {
        List<SmsRecord> list = smsRecordService.selectSmsRecordList(smsRecord);
        ExcelUtil<SmsRecord> util = new ExcelUtil<SmsRecord>(SmsRecord.class);
        util.exportExcel(response, list, "短信记录数据");
    }

    /**
     * 获取短信记录详细信息
     */
    @ApiOperation("获取短信记录详细信息")
    @PreAuthorize("@ss.hasPermi('sms:record:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(smsRecordService.selectSmsRecordByRecordId(recordId));
    }

    /**
     * 新增短信记录
     */
    @ApiOperation("新增短信记录")
    @PreAuthorize("@ss.hasPermi('sms:record:add')")
    @Log(title = "短信记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SmsRecord smsRecord)
    {
        return toAjax(smsRecordService.insertSmsRecord(smsRecord));
    }

    /**
     * 修改短信记录
     */
    @ApiOperation("修改短信记录")
    @PreAuthorize("@ss.hasPermi('sms:record:edit')")
    @Log(title = "短信记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SmsRecord smsRecord)
    {
        return toAjax(smsRecordService.updateSmsRecord(smsRecord));
    }

    /**
     * 删除短信记录
     */
    @ApiOperation("删除短信记录")
    @PreAuthorize("@ss.hasPermi('sms:record:remove')")
    @Log(title = "短信记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(smsRecordService.deleteSmsRecordByRecordIds(recordIds));
    }
}
