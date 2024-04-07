package com.github.open.courier.autoconfigure;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.PrintStream;
import java.util.Optional;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import com.github.open.courier.client.consumer.internal.CourierClientProperties;
import com.github.open.courier.core.support.CourierContext;

/**
 * courier启动banner
 */
public class CourierStarterBanner implements Banner, ApplicationRunner {

    /*
     *    ____    ____     _    _    _____    _____   ______   _____
     *   / ___|  / __ \   | |  | |  |  __ \  |_   _| |  ____| |  __ \
     *  | |     | |  | |  | |  | |  | |__) |   | |   | |__    | |__) |
     *  | |     | |  | |  | |  | |  |  _  /    | |   |  __|   |  _  /
     *  | |___  | |__| |  | |__| |  | | \ \   _| |_  | |____  | | \ \
     *   \____|  \____/    \____/   |_|  \_\ |_____| |______| |_|  \_\  v1.0.0
     */
    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {

        String version = Optional.ofNullable(getClass().getPackage()).map(Package::getImplementationVersion).map("v"::concat).orElse(EMPTY);

        String banner = "   ____    ____     _    _    _____    _____   ______   _____  \n" +
                "  / ___|  / __ \\   | |  | |  |  __ \\  |_   _| |  ____| |  __ \\ \n" +
                " | |     | |  | |  | |  | |  | |__) |   | |   | |__    | |__) |\n" +
                " | |     | |  | |  | |  | |  |  _  /    | |   |  __|   |  _  / \n" +
                " | |___  | |__| |  | |__| |  | | \\ \\   _| |_  | |____  | | \\ \\ \n" +
                "  \\____|  \\____/    \\____/   |_|  \\_\\ |_____| |______| |_|  \\_\\\t " + version + "\n";

        out.println(banner);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (CourierContext.getBean(CourierClientProperties.class).isBanner()) {
            printBanner(null, null, System.out); // NOSONAR, 不使用log记录日志, 在控制台打印一下即可
        }
    }
}
