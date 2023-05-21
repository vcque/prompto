package com.vcque.prompto.outputs;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.Prompts;
import com.vcque.prompto.contexts.PromptoContext;

import java.util.List;

public class MethodOutput implements PromptoOutput<PromptoResponse> {

    @Override
    public List<ChatMessage> buildOutputFormattingMessages(Params params) {
        var method = params.contexts().stream()
                .filter(c -> c.getType() == PromptoContext.Type.METHOD)
                .findFirst().orElseThrow();
        var language = params.contexts().stream()
                .filter(c -> c.getType() == PromptoContext.Type.LANGUAGE)
                .findFirst().orElseThrow();
        return List.of(
                Prompts.implementMethodOutput(params.userInput(), language.getValue(), method.getValue())
        );
    }

    @Override
    public PromptoResponse extractOutput(String assistantMessage, Params params) {
        return new PromptoResponse(assistantMessage);
    }
}
