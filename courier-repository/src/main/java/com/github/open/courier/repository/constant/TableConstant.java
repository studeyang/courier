package com.github.open.courier.repository.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 消息总线表
 *
 * @author yanglulu
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TableConstant {

    public static final String EC_COURIER = "ec_courier";

    public static final String COURIER_MESSAGE = "courier_message";

    public static final String COURIER_CONSUME_RECORD = "courier_consume_record";

    public static final String COURIER_DELAY_MESSAGE = "courier_delay_message";

}
