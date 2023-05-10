package com.vcque.prompto.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.Messages;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.contexts.SqlSchemaRetriever;
import com.vcque.prompto.contexts.EditorContentRetriever;
import com.vcque.prompto.contexts.LanguageRetriever;
import com.vcque.prompto.contexts.SettingsRetriever;
import com.vcque.prompto.outputs.SqlOutput;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;

import java.util.List;

public class PromptoSqlAction extends PromptoAction<PromptoResponse> {

    @Override
    public PromptoPipeline<PromptoResponse> pipeline() {
        return PromptoPipeline.<PromptoResponse>builder()
                .name("sql")
                .retrievers(List.of(
                        PromptoRetrieverDefinition.of(new SqlSchemaRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new LanguageRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new SettingsRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new EditorContentRetriever())
                ))
                .defaultInput("")
                .output(new SqlOutput())
                .execution((result, scope) -> {
                    var project = scope.project();
                    var editorBlock = result.firstBlock();
                    if (editorBlock.isEmpty()) {
                        ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(result.getRaw(), "Prompto"));
                    } else {
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            var document = scope.editor().getDocument();
                            document.insertString(document.getTextLength(), editorBlock.get().code());
                        });
                    }
                })
                .build();
    }
}