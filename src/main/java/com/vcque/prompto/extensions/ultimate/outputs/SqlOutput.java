package com.vcque.prompto.extensions.ultimate.outputs;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.Prompts;
import com.vcque.prompto.contexts.PromptoContext;
import com.vcque.prompto.outputs.PromptoOutput;

import java.util.List;

public class SqlOutput implements PromptoOutput<PromptoResponse> {

    @Override
    public List<ChatMessage> buildOutputFormattingMessages(Params params) {
        var language = params.contexts().stream()
                .filter(c -> c.getType() == PromptoContext.Type.LANGUAGE)
                .findFirst().orElseThrow();
        return List.of(
                Prompts.databaseQueryOutput(params.userInput(), language.getValue())
        );
    }

    @Override
    public PromptoResponse extractOutput(String assistantMessage, Params params) {
        return new PromptoResponse(assistantMessage);
    }
}
