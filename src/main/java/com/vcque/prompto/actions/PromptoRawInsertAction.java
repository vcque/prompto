package com.vcque.prompto.actions;

import com.intellij.openapi.command.WriteCommandAction;
import com.vcque.prompto.contexts.FileContentContext;
import com.vcque.prompto.contexts.LanguageContext;
import com.vcque.prompto.outputs.AddToExistingOutput;
import com.vcque.prompto.pipelines.PromptoContextDefinition;
import com.vcque.prompto.pipelines.PromptoPipeline;

import java.util.List;

/**
 * An action that adds new content to the current file.
 */
public class PromptoRawInsertAction extends PromptoAction<String> {

    @Override
    public PromptoPipeline<String> pipeline() {
        return PromptoPipeline.<String>builder()
                .name("raw insert")
                .contexts(List.of(
                        PromptoContextDefinition.of(new FileContentContext()),
                        PromptoContextDefinition.of(new LanguageContext())
                ))
                .defaultInput("Add something you think is useful")
                .output(new AddToExistingOutput())
                .execution((result, scope) -> {
                    WriteCommandAction.runWriteCommandAction(scope.project(), () -> {
                        var document = scope.editor().getDocument();
                        var caretModel = scope.editor().getCaretModel();
                        int offset = caretModel.getOffset();
                        document.insertString(offset, result);
                    });
                })
                .build();
    }


}
