package com.vcque.prompto.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.vcque.prompto.contexts.*;
import com.vcque.prompto.outputs.ShortAnswerOutput;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;
import com.vcque.prompto.pipelines.PromptoPipeline;

import java.util.List;

public class PromptoAskAction extends PromptoAction<String> {

    @Override
    public PromptoPipeline<String> pipeline() {
        return PromptoPipeline.<String>builder()
                .name("ask")
                .retrievers(List.of(
                        PromptoRetrieverDefinition.ofOptional(new SettingsRetriever()),
                        PromptoRetrieverDefinition.of(new LanguageRetriever()),
                        PromptoRetrieverDefinition.of(new EditorContentRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new SelectionRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new AvailableClassesRetriever())
                ))
                .defaultInput("What does this code do ?")
                .output(new ShortAnswerOutput())
                .execution((result, scope) -> {
                    ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(result, "Ask Prompto"));
                })
                .build();
    }
}
