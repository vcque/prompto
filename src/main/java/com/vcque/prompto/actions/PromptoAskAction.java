package com.vcque.prompto.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.vcque.prompto.contexts.*;
import com.vcque.prompto.outputs.ShortAnswerOutput;
import com.vcque.prompto.pipelines.PromptoContextDefinition;
import com.vcque.prompto.pipelines.PromptoPipeline;

import java.util.List;

public class PromptoAskAction extends PromptoAction<String> {

    @Override
    public PromptoPipeline<String> pipeline() {
        return PromptoPipeline.<String>builder()
                .name("ask")
                .contexts(List.of(
                        PromptoContextDefinition.of(new FileContentContext()),
                        PromptoContextDefinition.of(new ProjectContext()),
                        PromptoContextDefinition.of(new LanguageContext()),
                        PromptoContextDefinition.ofOptional(new SelectionContext()),
                        PromptoContextDefinition.ofOptional(new AvailableClassesContext())
                ))
                .defaultInput("What does this code do ?")
                .output(new ShortAnswerOutput())
                .execution((result, scope) -> {
                    ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(result, "Ask Prompto"));
                })
                .build();
    }
}
