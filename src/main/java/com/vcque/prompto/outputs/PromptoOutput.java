package com.vcque.prompto.outputs;

import com.theokanning.openai.completion.chat.ChatMessage;

public interface PromptoOutput<T> {

    ChatMessage chatMessage();

    T extractOutput(String assistantMessage);

}
