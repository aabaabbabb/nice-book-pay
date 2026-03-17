package com.nicebook.nicebookpay.utils;

import java.util.regex.Pattern;

public class HotelNameCleaner {
    // () （） [] 【】 内内容
    private static final Pattern BRACKET_PATTERN =
            Pattern.compile("[\\(（\\[【].*?[\\)）\\]】]");

    // - 后面的分店
    private static final Pattern DASH_PATTERN =
            Pattern.compile("[-－].*");

    // 多余空格
    private static final Pattern SPACE_PATTERN =
            Pattern.compile("\\s+");

    /**
     * 清洗酒店名称
     */
    public static String clean(String name) {

        if (name == null || name.isEmpty()) {
            return name;
        }

        // 1 去掉括号内容
        name = BRACKET_PATTERN.matcher(name).replaceAll("");

        // 2 去掉 - 分店
        name = DASH_PATTERN.matcher(name).replaceAll("");

        // 3 去空格
        name = SPACE_PATTERN.matcher(name).replaceAll("");

        // 4 去特殊符号
        name = name.replace("·", "")
                .replace(".", "")
                .replace("店", "店");

        return name.trim();
    }
}
