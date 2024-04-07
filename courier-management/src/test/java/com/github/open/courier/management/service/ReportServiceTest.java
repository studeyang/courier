package com.github.open.courier.management.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author yanglulu
 * @date 2021/7/21
 */
public class ReportServiceTest {

    @Test
    public void test_split() {

        String type = "com.xxx.ec.cloud.events.order.OrderApproved";
        assertEquals("OrderApproved", StringUtils.substringAfterLast(type, "."));

    }


}