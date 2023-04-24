package com.vcque.prompto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Wraps a chat response and extract useful information from it.
 */
@Getter
public class PromptoResponse {


    static final Pattern EDITOR_PATTERN = Pattern.compile("(?s)```(\\w*)\n(.*)```");

    public static List<EditorBlock> extractEditorBlocks(String text) {

        var matcher = EDITOR_PATTERN.matcher(text);

        var contents = new ArrayList<EditorBlock>();
        while (matcher.find()) {
            contents.add(new EditorBlock(matcher.group(1), matcher.group(2)));
        }
        return contents;
    }

    private final String raw;
    private final List<EditorBlock> editorBlocks;

    public PromptoResponse(String text) {
        this.raw = text;
        this.editorBlocks = extractEditorBlocks(text);
    }

    public Optional<EditorBlock> firstBlock() {
        return editorBlocks.stream().findFirst();
    }

    public record EditorBlock(String lang, String code) {
    }
}
