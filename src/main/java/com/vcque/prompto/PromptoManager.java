package com.vcque.prompto;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.DialogWrapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.vcque.prompto.contexts.PromptoContext;
import com.vcque.prompto.exceptions.MissingTokenException;
import com.vcque.prompto.outputs.PromptoOutput;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.settings.PromptoSettingsState;
import com.vcque.prompto.ui.PromptoQueryDialog;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

    public <T> void executePipeline(PromptoPipeline<T> pipeline, PromptoPipeline.Scope scope) {
        var maxToken = 3500; // To configure, this is ~ the number of token allowed for the chatGPT API (need also room for the response)

        var contextsByRetrievers = pipeline.getRetrievers().stream()
                .filter(r -> r.getRetriever().isAvailable(scope.project(), scope.editor(), scope.element()))
                .collect(Collectors.toMap(
                        x -> x,
                        r -> r.getRetriever().retrieveContexts(scope.project(), scope.editor(), scope.element()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        var dialog = new PromptoQueryDialog(pipeline, contextsByRetrievers, maxToken);
        dialog.show();

        var exitCode = dialog.getExitCode();
        if (exitCode == DialogWrapper.CANCEL_EXIT_CODE) {
            return;
        }

        var contexts = dialog.getSelectedContexts();
        var userInput = dialog.getUserInput();
        var outputParams = new PromptoOutput.Params(userInput, contexts, scope);

        var chatMessages = new ArrayList<ChatMessage>();
        chatMessages.add(Prompts.codingAssistant());
        chatMessages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), PromptoSettingsState.getInstance().projectContext));
        chatMessages.addAll(
                contexts.stream()
                        .map(Prompts::promptoContext)
                        .toList()
        );
        chatMessages.addAll(pipeline.getOutput().buildOutputFormattingMessages(outputParams));

        if (exitCode == DialogWrapper.OK_EXIT_CODE) {
            updateToken();
            ProgressManager.getInstance().run(new Task.Backgroundable(scope.project(), "Prompto " + pipeline.getName(), true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try {
                        callLLM(pipeline, contexts, scope, chatMessages, userInput);
                    } catch (MissingTokenException e) {
                        var notification = new Notification(
                                "Prompto",
                                "Missing OpenAI key",
                                "Add your open-ai key to Prompto settings to enable this feature.",
                                NotificationType.ERROR);
                        Notifications.Bus.notify(notification);
                    }
                }
            });
        } else if (exitCode == PromptoQueryDialog.CLIPBOARD_EXIT_CODE){
            var prompt = chatMessages.stream()
                    .map(ChatMessage::getContent)
                    .collect(Collectors.joining("\n"));

            var transferable = new StringSelection(prompt);
            CopyPasteManager.getInstance().setContents(transferable);

            var notification = new Notification(
                    "Prompto",
                    "Prompt copied",
                    "Your prompt and its context has been copied to the clipboard.",
                    NotificationType.INFORMATION);
            Notifications.Bus.notify(notification, scope.project());
        }
    }

    private <T> void callLLM(PromptoPipeline<T> pipeline, List<PromptoContext> contexts, PromptoPipeline.Scope scope, ArrayList<ChatMessage> chatMessages, String userInput) {
        // Send messages to OpenAI
        var result = openAI.createChatCompletion(
                ChatCompletionRequest.builder()
                        .temperature(TEMPERATURE)
                        .model(PromptoSettingsState.getInstance().languageModel)
                        .messages(chatMessages)
                        .stop(pipeline.getStopwords())
                        .stream(false)
                        .build()
        );

        // Retrieve the LLM response message
        var response = result.getChoices().get(0).getMessage().getContent();
        var outputParams = new PromptoOutput.Params(userInput, contexts, scope);
        var extractedResult = pipeline.getOutput().extractOutput(response, outputParams);
        // Execute the action
        pipeline.getExecution().execute(extractedResult, scope, contexts);
    }
}
