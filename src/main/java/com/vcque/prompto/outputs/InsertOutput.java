package com.vcque.prompto.outputs;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.Prompts;

import java.util.List;

public class InsertOutput implements PromptoOutput<PromptoResponse> {

    @Override
    public List<ChatMessage> buildOutputFormattingMessages(Params params) {
        return List.of(
                Prompts.insertOutput(),
                Prompts.userInput(params.userInput())
        );
    }

    @Override
    public PromptoResponse extractOutput(String assistantMessage, Params params) {
        return new PromptoResponse(assistantMessage);
    }
}
