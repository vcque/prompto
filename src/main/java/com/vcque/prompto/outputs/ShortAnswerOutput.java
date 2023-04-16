package com.vcque.prompto.outputs;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.Prompts;

public class ShortAnswerOutput implements PromptoOutput<String> {

    @Override
    public ChatMessage chatMessage() {
        return Prompts.shortAnswerOutput();
    }

    @Override
    public String extractOutput(String assistantMessage) {
        return assistantMessage;
    }

}
