package com.github.open.courier.client.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/16
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CourierMetricsLog {

    public static final Logger logger = LoggerFactory.getLogger(CourierMetricsLog.class);

    public static Logger getLogger() {
        return logger;
    }

    public static void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

}
