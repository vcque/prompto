package com.vcque.prompto;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.vcque.prompto.contexts.PromptoContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * regroups chat messages. Might want to put that in settings in the future.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Prompts {

    /**
     * Conversation initializer.
     */
    public static ChatMessage codingAssistant() {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        You are Prompto, a coding assistant integrated into the intellij IDE that helps the user completing its programming tasks.
                        """.trim()
        );
    }

    public static ChatMessage implementMethodOutput() {
        return new ChatMessage(
                ChatMessageRole.USER.value(),
                """
                        Your task is to implement or rewrite the currently focused method based on the my next input.
                        If I do not provide specific directives and the method is not implemented, do an informed guess and implement the method based on this guess.
                        Ensure that the code you provide is efficient, well-structured, and adheres to best practices.
                        Always answer with one or multiple methods between triple back quotes in the queried language.
                        """.trim()
        );
    }

    public static ChatMessage insertOutput() {
        return new ChatMessage(
                ChatMessageRole.USER.value(),
                """
                        Your task is insert new code based on my next input.
                        The first code block you provide will be inserted in the user's editor.
                        """.trim()
        );
    }

    public static ChatMessage shortAnswerOutput() {
        return new ChatMessage(ChatMessageRole.USER.value(),
                """
                        Your task is to answer my next question based on the provided context.
                        Answer with only the most relevant information and with the least words possible.
                        """
        );
    }

    public static ChatMessage sqlOutput() {
        return new ChatMessage(ChatMessageRole.USER.value(), """
                Your task is to provide a high-quality SQL script based on my next input.
                Ensure that the SQL code you provide is efficient, well-structured, and adheres to best practices.
                Always return the SQL script between triple back quotes.
                """.trim());
    }

    public static ChatMessage userInput(String userInput) {
        return new ChatMessage(ChatMessageRole.USER.value(), userInput);
    }

    public static ChatMessage promptoContext(PromptoContext state) {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        `%s-%s`: This is %s
                        ```
                        %s
                        ```
                        """.formatted(
                        state.getType().name(),
                        state.getId(),
                        state.getType().description,
                        state.getValue()
                )
        );
    }

    public static ChatMessage promptoContextFormat() {
        var exampleState = PromptoContext.builder()
                .id("state_id")
                .type(PromptoContext.Type.EXAMPLE)
                .value("$state_value")
                .build();
        var exampleFormat = promptoContext(exampleState).getContent();

        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        In the next messages, you will receive context information useful to your task. It will have the following format:
                        %s
                        """.formatted(exampleFormat)
        );
    }

}
