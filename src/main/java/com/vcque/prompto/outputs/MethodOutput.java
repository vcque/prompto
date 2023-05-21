package com.vcque.prompto.outputs;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.Prompts;

import java.util.List;

public class MethodOutput implements PromptoOutput<PromptoResponse> {

    @Override
    public List<ChatMessage> buildOutputFormattingMessages(Params params) {
        return List.of(
                Prompts.implementMethodOutput(),
                Prompts.userInput(params.userInput())
        );
    }

    @Override
    public PromptoResponse extractOutput(String assistantMessage, Params params) {
        return new PromptoResponse(assistantMessage);
    }
}
