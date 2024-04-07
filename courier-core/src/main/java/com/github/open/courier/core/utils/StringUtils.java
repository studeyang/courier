package com.github.open.courier.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author yanglulu
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtils {

    private static final String BLANK = " ";

    /**
     * 截取一个多行字符串的前几行，剩下的用 ... 表示
     * eg:
     *     abc.def      abc.def
     *       abc   -3->   abc
     *       def          def
     *       asd          ...
     *
     * @param multiLinesString 一个多行字符串
     * @param line             需要截取的行数
     * @return 截取结果
     */
    public static String cutLines(String multiLinesString, int line) {

        List<String> lines;
        try {
            InputStream is = IOUtils.toInputStream(multiLinesString, Charset.defaultCharset());
            lines = IOUtils.readLines(is, Charset.defaultCharset());
        } catch (IOException e) {
            return multiLinesString;
        }

        if (lines.size() <= line) {
            return multiLinesString;
        }

        StringBuilder lineBuilder = new StringBuilder();
        for (int i = 0; i < line; i++) {
            String aLine = lines.get(i);
            lineBuilder.append(aLine).append("\n");
            if (i == line - 1) {
                String indents = getIndents(aLine);
                lineBuilder.append(indents);
            }
        }
        lineBuilder.append("...");

        return lineBuilder.toString();

    }

    /**
     * <p>获取字符的 tap 空格数</p>
     * eg:
     * __abc -> __
     * ____abc -> ____
     *
     * （_表示空格）
     *
     * @param string 字符串
     * @return 空格
     */
    public static String getIndents(String string) {

        if (string == null || string.isEmpty()) {
            return "";
        }

        StringBuilder indentsBuilder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c != ' ') {
                break;
            }
            indentsBuilder.append(BLANK);
        }

        return indentsBuilder.toString();
    }

}
