package com.vcque.prompto;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.vcque.prompto.contexts.PromptoContext;
import com.vcque.prompto.exceptions.MissingTokenException;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.settings.PromptoSettingsState;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PromptoManager {

    private static final PromptoManager INSTANCE = new PromptoManager();

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

    public <T> void executePipeline(PromptoPipeline<T> pipeline, List<PromptoContext> contexts, String userInput, PromptoPipeline.Scope scope) {
        updateToken();
        var maxToken = 3500; // To configure, this is ~ the number of token allowed for the chatGPT API (need also room for the response)

        var chatMessages = new ArrayList<ChatMessage>();
        chatMessages.add(Prompts.codingAssistant());
        chatMessages.add(Prompts.promptoContextFormat());
        chatMessages.addAll(
                contexts.stream()
                        .takeWhile(new MaxTokenPredicate(maxToken))
                        .map(Prompts::promptoContext)
                        .toList()
        );
        chatMessages.add(pipeline.getOutput().chatMessage());
        chatMessages.add(Prompts.userInput(userInput));

        // Send messages to OpenAI
        var result = openAI.createChatCompletion(
                ChatCompletionRequest.builder()
                        .temperature(TEMPERATURE)
                        .model(PromptoSettingsState.getInstance().languageModel)
                        .messages(chatMessages)
                        .stream(false)
                        .build()
        );

        // Retrieve the LLM response message
        var response = result.getChoices().get(0).getMessage().getContent();
        var extractedResult = pipeline.getOutput().extractOutput(response);
        // Execute the action
        pipeline.getExecution().execute(extractedResult, scope, contexts);
    }

    public <T> String buildManualPrompt(PromptoPipeline<T> pipeline, List<PromptoContext> contexts, String userInput) {
        var maxToken = 2000; // To configure, this is ~ the number of token allowed in the chatGPT webapp

        var chatMessages = new ArrayList<ChatMessage>();
        chatMessages.add(Prompts.codingAssistant());


        chatMessages.addAll(
                contexts.stream()
                        .takeWhile(new MaxTokenPredicate(maxToken))
                        .map(Prompts::promptoContext)
                        .toList()
        );
        chatMessages.add(pipeline.getOutput().chatMessage());
        chatMessages.add(Prompts.userInput(userInput));

        return chatMessages.stream()
                .map(ChatMessage::getContent)
                .collect(Collectors.joining("\n"));
    }

    @RequiredArgsConstructor
    private static class MaxTokenPredicate implements Predicate<PromptoContext> {

        private final int maxToken;
        private int currentToken = 0;

        @Override
        public boolean test(PromptoContext context) {
            // Stateful predicate FTW
            currentToken += context.cost();
            return currentToken < maxToken;
        }
    }
}
