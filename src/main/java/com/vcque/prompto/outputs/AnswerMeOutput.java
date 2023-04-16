package com.vcque.prompto.outputs;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.Prompts;

/**
 * Special {@link PromptoOutput} used in prompt clipboard.
 * As the output is used (often) in the chatGPT webapp, we can't ask output as the system role so we make one as the user.
 */
public class AnswerMeOutput implements PromptoOutput<String> {

    @Override
    public ChatMessage chatMessage() {
        return Prompts.answermeOutput();
    }

    @Override
    public String extractOutput(String assistantMessage) {
        return assistantMessage;
    }

}
