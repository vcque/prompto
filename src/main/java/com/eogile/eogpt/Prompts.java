package com.eogile.eogpt;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

public class Prompts {

    public static ChatMessage seniorDeveloperPrompt() {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        You are an expert JAVA developer, versed in development best-practices.
                        The user your are interacting with is a JAVA senior developer.
                        """.trim()
        );
    }

    public static ChatMessage methodOutputPrompt() {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        Always respond with a code snippet of a valid JAVA method. If you want to add additional context information, embed it in comments.
                        Use `var`, lombok builders and `assertThat` when applicable.
                        """.trim()
        );
    }

    public static ChatMessage fileAndMethodNameContextPrompt(String file, String methodName) {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        To help you in you task, this is the provided context:
                        
                        File content:
                        ```
                        %s
                        ```
                        
                        Method to focus your task on: `%s`
                        
                        
                        """.formatted(file, methodName).trim()
        );
    }

    public static ChatMessage fileContextPrompt(String file) {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                """
                        To help you in you task, this is the provided context:
                        
                        File content:
                        ```
                        %s
                        ```
                        
                        
                        """.formatted(file).trim()
        );
    }
}
