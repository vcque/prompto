package com.vcque.prompto.outputs;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.contexts.PromptoContext;
import com.vcque.prompto.pipelines.PromptoPipeline;

import java.util.List;

public interface PromptoOutput<T> {

    List<ChatMessage> buildOutputFormattingMessages(Params params);

    T extractOutput(String response, Params params);

    record Params(String userInput, List<PromptoContext> contexts, PromptoPipeline.Scope scope) {
    }
}
