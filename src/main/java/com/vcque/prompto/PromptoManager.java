package com.vcque.prompto;

import com.vcque.prompto.settings.PromptoSettingsState;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PromptoManager {

    private static final PromptoManager INSTANCE = new PromptoManager();

    private static final String LLM_MODEL = "gpt-3.5-turbo";

    public static PromptoManager instance() {
        return INSTANCE;
    }

    private OpenAiService openAI = null;
    private String currentToken = null;

    public void updateToken() {
        var token = PromptoSettingsState.getInstance().apiToken;
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Please add your OpenAI key to the settings plugin.");
        }
        if (!token.equals(currentToken)) {
            openAI = new OpenAiService(token, Duration.ofMinutes(2));
            currentToken = token;
        }
    }

    public String queryEnhance(String classText, String methodName, String userInput) {
        updateToken();
        var result = openAI.createChatCompletion(
                ChatCompletionRequest.builder()
                        .temperature(0.3)
                        .model(LLM_MODEL)
                        .messages(List.of(
                                Prompts.seniorDeveloperPrompt(),
                                Prompts.fileAndMethodNameContextPrompt(classText, methodName),
                                new ChatMessage(
                                        ChatMessageRole.SYSTEM.value(),
                                        """
                                                The user's next message will communicate instructions.
                                                Your task is to enhance the context method solely based on the user instructions.
                                                """
                                ),
                                Prompts.methodOutputPrompt(),
                                new ChatMessage(
                                        ChatMessageRole.USER.value(),
                                        userInput
                                )
                        ))
                        .stream(false)
                        .build()
        );

        return extractCodeSnippet(result);
    }

    public String queryExplain(String classText, String userInput) {
        updateToken();
        var result = openAI.createChatCompletion(
                ChatCompletionRequest.builder()
                        .temperature(0.3)
                        .model(LLM_MODEL)
                        .messages(List.of(
                                Prompts.seniorDeveloperPrompt(),
                                Prompts.fileContextPrompt(classText),
                                new ChatMessage(
                                        ChatMessageRole.SYSTEM.value(),
                                        """
                                                The user's next message will ask you questions about the provided file.
                                                Your task is to respond to him with the least words possible. Do not add code snippet.
                                                """
                                ),
                                new ChatMessage(
                                        ChatMessageRole.USER.value(),
                                        userInput
                                )
                        ))
                        .stream(false)
                        .build()
        );

        return result.getChoices().get(0).getMessage().getContent();
    }

    public String buildPrompt(String text, String userInput) {
        return Stream.of(
                Prompts.seniorDeveloperPrompt(),
                Prompts.fileContextPrompt(text),
                new ChatMessage(
                        ChatMessageRole.SYSTEM.value(),
                        """
                                I will instruct you a task about the provided file.
                                Do answer truthfully. If you don't know how to do the task, say so and provide the reasons why.
                                """
                ),
                new ChatMessage(
                        ChatMessageRole.USER.value(),
                        userInput
                ))
                .map(ChatMessage::getContent)
                .collect(Collectors.joining("\n"));
    }

    public boolean hasToken() {
        var token = PromptoSettingsState.getInstance().apiToken;
        return token != null && !token.trim().isEmpty();
    }

    private static String extractCodeSnippet(ChatCompletionResult result) {

        var content = result.getChoices().get(0).getMessage().getContent();
        var editorContents = Utils.extractEditorContent(content);
        return editorContents.isEmpty() ? content.trim() : editorContents.get(0).trim();
    }
}
