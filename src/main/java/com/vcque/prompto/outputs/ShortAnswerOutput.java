package com.vcque.prompto.outputs;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.Prompts;

import java.util.List;

public class ShortAnswerOutput implements PromptoOutput<String> {

    @Override
    public List<ChatMessage> buildOutputFormattingMessages(Params params) {
        return List.of(
                Prompts.shortAnswerOutput(),
                Prompts.userInput(params.userInput())
        );
    }

    @Override
    public String extractOutput(String assistantMessage, Params params) {
        return assistantMessage;
    }

}
