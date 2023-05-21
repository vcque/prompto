package com.vcque.prompto.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.vcque.prompto.contexts.*;
import com.vcque.prompto.outputs.ShortAnswerOutput;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.ui.PromptoAnswerDialog;

import java.util.List;

public class PromptoAskAction extends PromptoAction<String> {

    @Override
    public PromptoPipeline<String> pipeline() {
        return PromptoPipeline.<String>builder()
                .name("ask")
                .retrievers(List.of(
                        PromptoRetrieverDefinition.of(new EditorContentRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new LanguageRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new SelectionRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new AvailableClassesRetriever())
                ))
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
