package com.vcque.prompto;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * regroups chat messages. Might want to put that in settings in the future.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Prompts {

    /** Conversation initializer. */
    public static ChatMessage codingAssistant() {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        You are Prompto, an intelliJ coding assistant helping the user completing its programming tasks.
                        """.trim()
        );
    }

    public static ChatMessage editorContext(String file) {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        This is the content of the user's editor:
                        ```
                         %s
                        ```
                        """.formatted(file).trim()
        );
    }

    public static ChatMessage languageContext(String contextValue) {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        This is the programming language used: `%s`
                        """.formatted(contextValue)
        );
    }

    public static ChatMessage methodNameContext(String methodName) {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        This is the name of the method which has the user's focus: `%s`
                        """.formatted(methodName)
        );
    }

    public static ChatMessage selectionContext(String contextValue) {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        This is the user's current selection: `%s`
                        """.formatted(contextValue)
        );
    }

    public static ChatMessage methodOutput() {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        The user's next message will communicate instructions that you have to implement.
                        Always respond with a code snippet of a valid method. If you want to add additional context information, embed it in comments.
                        """.trim()
        );
    }

    public static ChatMessage shortAnswerOutput() {
        return new ChatMessage("system",
                """
                        The user's next message will ask you questions about the provided file.
                        Your task is to respond to him with the least words possible. Do not add code snippet.
                        """
        );
    }

    public static ChatMessage userInput(String userInput) {
        return new ChatMessage(ChatMessageRole.USER.value(), userInput);
    }
}
