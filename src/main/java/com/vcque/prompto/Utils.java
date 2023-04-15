package com.vcque.prompto;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {

    final static Pattern EDITOR_PATTERN = Pattern.compile("(?s)```(?:java\n)?(.*?)```");

    public static List<String> extractEditorContent(String text) {

        var matcher = EDITOR_PATTERN.matcher(text);

        List<String> contents = new ArrayList<>();
        while (matcher.find()) {
            contents.add(matcher.group(1));
        }
        return contents;
    }
}
