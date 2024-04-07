package com.github.open.courier.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class DateUtilsTest {

    @Test
    public void parseDate() {
        Assert.assertNotNull(DateUtils.parseDate(new Date(), "00:00:00"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseDate_incorrect_time() {
        DateUtils.parseDate(new Date(), "0:90:00");
    }

}