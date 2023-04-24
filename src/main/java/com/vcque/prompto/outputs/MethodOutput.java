package com.vcque.prompto.outputs;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.Prompts;

public class MethodOutput implements PromptoOutput<PromptoResponse> {

    @Override
    public ChatMessage chatMessage() {
        return Prompts.rewriteMethodOutput();
    }

    @Override
    public PromptoResponse extractOutput(String assistantMessage) {
        return new PromptoResponse(assistantMessage);
    }
}
