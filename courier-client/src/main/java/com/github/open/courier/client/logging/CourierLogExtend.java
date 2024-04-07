package com.github.open.courier.client.logging;

import com.github.open.casslog.core.logging.AbstractLogExtend;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/16
 */
@Component
public class CourierLogExtend extends AbstractLogExtend {
    @Override
    public String logConfig() {
        return "classpath:com/github/open/courier/client/logging/courier-log4j2.xml";
    }
}
