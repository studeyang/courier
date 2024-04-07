package com.github.open.courier.agent.restful;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 1.0 2022/8/9
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeadersFilter {

    /**
     * 只传递 Courier- 开头的
     * <p>
     * 传递所有的 gateway-api 接收不了，不知为何
     */
    public static HttpHeaders doFilter(HttpHeaders headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach((key, value) -> {
            if (key.startsWith("Courier-")) {
                httpHeaders.addAll(key, value);
            }
        });
        return httpHeaders;
    }

}
