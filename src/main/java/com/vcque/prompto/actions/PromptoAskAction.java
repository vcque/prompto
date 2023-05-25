package com.vcque.prompto.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.vcque.prompto.contexts.AvailableClassesRetriever;
import com.vcque.prompto.contexts.EditorContentRetriever;
import com.vcque.prompto.contexts.LanguageRetriever;
import com.vcque.prompto.contexts.PromptoRetriever;
import com.vcque.prompto.contexts.SelectionRetriever;
import com.vcque.prompto.outputs.ShortAnswerOutput;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;
import com.vcque.prompto.ui.PromptoAnswerDialog;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Contextual action for asking a question to the assistant and showing a markdown response.
 */
public class PromptoAskAction extends PromptoAction<String> {

    private static final ExtensionPointName<PromptoRetriever> EXTENSION_RETRIEVERS = new ExtensionPointName<>("com.vcque.prompto.promptoRetriever");

    @Override
    public PromptoPipeline<String> pipeline() {

        return PromptoPipeline.<String>builder()
                .name("ask")
                .retrievers(
                        Stream.concat(
                                        Stream.of(
                                                PromptoRetrieverDefinition.of(new EditorContentRetriever()),
                                                PromptoRetrieverDefinition.ofOptional(new LanguageRetriever()),
                                                PromptoRetrieverDefinition.ofOptional(new SelectionRetriever()),
                                                PromptoRetrieverDefinition.ofOptional(new AvailableClassesRetriever())
                                        ),
                                        Arrays.stream(EXTENSION_RETRIEVERS.getExtensions()).map(PromptoRetrieverDefinition::ofOptional))
                                .toList())
                .defaultInput("What does this code do ?")
                .output(new ShortAnswerOutput())
                .execution((result, scope, contexts) -> {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        new PromptoAnswerDialog(scope.project(), result).show();
                    });
                })
                .build();
    }
}
