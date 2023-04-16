package com.vcque.prompto;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.vcque.prompto.exceptions.MissingTokenException;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.settings.PromptoSettingsState;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PromptoManager {

    private static final PromptoManager INSTANCE = new PromptoManager();

    private static final String LLM_MODEL = "gpt-3.5-turbo";

    private static final double TEMPERATURE = 0.3;

    public static PromptoManager instance() {
        return INSTANCE;
    }

    private OpenAiService openAI = null;
    private String currentToken = null;

    public void updateToken() {
        var token = PromptoSettingsState.getInstance().apiToken;
        if (token == null || token.isEmpty()) {
            throw new MissingTokenException();
        }
        if (!token.equals(currentToken)) {
            openAI = new OpenAiService(token, Duration.ofMinutes(2));
            currentToken = token;
        }
    }

    public <T> void executePipeline(PromptoPipeline<T> pipeline, List<ChatMessage> contextMessages, String userInput, PromptoPipeline.Scope scope) {
        updateToken();

        var chatMessages = new ArrayList<ChatMessage>();
        chatMessages.add(Prompts.codingAssistant());
        chatMessages.addAll(contextMessages);
        chatMessages.add(pipeline.getOutput().chatMessage());
        chatMessages.add(Prompts.userInput(userInput));

        // Send messages to OpenAI
        var result = openAI.createChatCompletion(
                ChatCompletionRequest.builder()
                        .temperature(TEMPERATURE)
                        .model(LLM_MODEL)
                        .messages(chatMessages)
                        .stream(false)
                        .build()
        );

        // Call pipeline.output with the results
        var extractedResult = pipeline.getOutput().extractOutput(result.getChoices().get(0).getMessage().getContent());
        pipeline.getExecution().accept(extractedResult, scope);
    }

    public <T> String buildManualPrompt(PromptoPipeline<T> pipeline, List<ChatMessage> contextMessages, String userInput) {
        var chatMessages = new ArrayList<ChatMessage>();
        chatMessages.add(Prompts.codingAssistant());
        chatMessages.addAll(contextMessages);
        chatMessages.add(pipeline.getOutput().chatMessage());
        chatMessages.add(Prompts.userInput(userInput));

        return chatMessages.stream()
                .map(ChatMessage::getContent)
                .collect(Collectors.joining("\n"));
    }
}
