package com.github.open.courier.core.converter;

import org.apache.commons.lang3.BooleanUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author yanglulu
 * @date 2021/7/20
 */
public class AlarmConverterTest {

    @Test
    public void test_boolean() {

        assertTrue(BooleanUtils.toBooleanDefaultIfNull(null, true));
        assertTrue(BooleanUtils.toBooleanDefaultIfNull(true, true));
        assertFalse(BooleanUtils.toBooleanDefaultIfNull(false, true));
    }

}