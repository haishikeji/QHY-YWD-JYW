package com.px.system.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.px.system.domain.SysAlarm;

/**
 * 系统告警Mapper接口
 * 
 * @author 品讯科技
 * @date 2024-08
 */
public interface SysAlarmMapper extends BaseMapper<SysAlarm>
{
    /**
     * 查询系统告警
     * 
     * @param alarmId 系统告警主键
     * @return 系统告警
     */
    public SysAlarm selectSysAlarmByAlarmId(Long alarmId);

    /**
     * 查询系统告警列表
     * 
     * @param sysAlarm 系统告警
     * @return 系统告警集合
     */
    public List<SysAlarm> selectSysAlarmList(SysAlarm sysAlarm);

    /**
     * 新增系统告警
     * 
     * @param sysAlarm 系统告警
     * @return 结果
     */
    public int insertSysAlarm(SysAlarm sysAlarm);

    /**
     * 修改系统告警
     * 
     * @param sysAlarm 系统告警
     * @return 结果
     */
    public int updateSysAlarm(SysAlarm sysAlarm);

    /**
     * 删除系统告警
     * 
     * @param alarmId 系统告警主键
     * @return 结果
     */
    public int deleteSysAlarmByAlarmId(Long alarmId);

    /**
     * 批量删除系统告警
     * 
     * @param alarmIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysAlarmByAlarmIds(Long[] alarmIds);
}
