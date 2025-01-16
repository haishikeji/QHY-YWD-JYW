package com.px.framework.web.domain.server;

import lombok.Data;

/**
 * 硬盘相关信息
 *
 * @author 品讯科技
 */
@Data
public class Hdd {

    /**
     * HDD总容量
     */
    private double total;


    /**
     * HDD用户使用率
     */
    private double used;
    /**
     * HDD剩余容量
     */

    private String free;

}
