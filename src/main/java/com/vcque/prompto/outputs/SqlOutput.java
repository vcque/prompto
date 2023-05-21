package com.vcque.prompto.outputs;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.Prompts;

import java.util.List;

public class SqlOutput implements PromptoOutput<PromptoResponse> {

    @Override
    public List<ChatMessage> buildOutputFormattingMessages(Params params) {
        return List.of(
                Prompts.sqlOutput(),
                Prompts.userInput(params.userInput())
        );
    }

    @Override
    public PromptoResponse extractOutput(String assistantMessage, Params params) {
        return new PromptoResponse(assistantMessage);
    }
}
