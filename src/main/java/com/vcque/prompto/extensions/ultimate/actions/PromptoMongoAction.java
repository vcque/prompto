package com.vcque.prompto.extensions.ultimate.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.Messages;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.actions.PromptoAction;
import com.vcque.prompto.contexts.EditorContentRetriever;
import com.vcque.prompto.contexts.LanguageRetriever;
import com.vcque.prompto.extensions.ultimate.contexts.MongoDbSchemaRetriever;
import com.vcque.prompto.extensions.ultimate.outputs.MongoOutput;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;

import java.util.List;

public class PromptoMongoAction extends PromptoAction<PromptoResponse> {

    @Override
    public PromptoPipeline<PromptoResponse> pipeline() {
        return PromptoPipeline.<PromptoResponse>builder()
                .name("mongo")
                .retrievers(List.of(
                        PromptoRetrieverDefinition.of(new MongoDbSchemaRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new LanguageRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new EditorContentRetriever())
                ))
                .defaultInput("")
                .stopwords(List.of(PromptoResponse.EDITOR_STOPWORD))
                .output(new MongoOutput())
                .execution((result, scope, contexts) -> {
                    var project = scope.project();
                    var editorBlock = result.firstBlock();
                    if (editorBlock.isEmpty()) {
                        ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(result.getRaw(), "Prompto"));
                    } else {
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            var document = scope.editor().getDocument();
                            document.insertString(document.getTextLength(), "\n" + editorBlock.get().code());
                        });
                    }
                })
                .build();
    }
}