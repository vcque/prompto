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

    /**
     * Conversation initializer.
     */
    public static ChatMessage codingAssistant() {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        You are Prompto, an intelliJ coding assistant helping the user completing its programming tasks.
                        """.trim()
        );
    }

    /**
     * Make it configurable on a per-project basis.
     */
    public static ChatMessage projectContext() {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                "Use modern langage feature, streams, lombok and assertj when applicable."
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
                        This is the user's current selection:
                        ```
                        %s
                        ```
                        """.formatted(contextValue)
        );
    }

    public static ChatMessage fileStructureContext(String contextValue) {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        This is the project's file structure:
                        ```
                        %s
                        ```
                        """.formatted(contextValue)
        );
    }

    public static ChatMessage typesContext(String contextValue) {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        Here are additional type definitions:
                        ```
                        %s
                        ```
                        """.formatted(contextValue)
        );
    }

    public static ChatMessage methodOutput() {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        Your task is to rewrite the context method based on the user's next input.
                        Always respond with the code snippet of a valid method.
                        """.trim()
        );
    }

    public static ChatMessage shortAnswerOutput() {
        return new ChatMessage("system",
                """
                        Your task is to answer the user's next question based on the provided context.
                        Answer with only the most relevant information and with the least words possible.
                        """
        );
    }

    public static ChatMessage addToExistingOutput() {
        return new ChatMessage(ChatMessageRole.SYSTEM.value(),
                """
                        Your task is to generate content based on the user's next input.
                        Respond with a code snippet that will be inserted in the current editor.
                        """
        );
    }

    public static ChatMessage answermeOutput() {
        return new ChatMessage(
                ChatMessageRole.USER.value(),
                """
                        I will instruct you a task about the provided file.
                        Do answer truthfully. If you don't know how to do the task, say so and provide the reasons why.
                        """
        );
    }

    public static ChatMessage userInput(String userInput) {
        return new ChatMessage(ChatMessageRole.USER.value(), userInput);
    }
}
