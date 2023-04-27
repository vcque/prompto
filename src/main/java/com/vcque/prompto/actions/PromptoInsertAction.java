package com.vcque.prompto.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.Utils;
import com.vcque.prompto.contexts.AvailableClassesRetriever;
import com.vcque.prompto.contexts.EditorContentRetriever;
import com.vcque.prompto.contexts.LanguageRetriever;
import com.vcque.prompto.contexts.SettingsRetriever;
import com.vcque.prompto.outputs.InsertOutput;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PromptoInsertAction extends PromptoAction<PromptoResponse> {

    /** Only insert on the class level. */
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var psiMethod = Utils.findParentOfType(element, PsiMethod.class);
        var psiClass = Utils.findParentOfType(element, PsiClass.class);
        return psiMethod == null && psiClass != null;
    }

    @Override
    public PromptoPipeline<PromptoResponse> pipeline() {
        return PromptoPipeline.<PromptoResponse>builder()
                .name("insert")
                .retrievers(List.of(
                        PromptoRetrieverDefinition.ofOptional(new SettingsRetriever()),
                        PromptoRetrieverDefinition.of(new LanguageRetriever()),
                        PromptoRetrieverDefinition.of(new EditorContentRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new AvailableClassesRetriever())
                ))
                .defaultInput("")
                .output(new InsertOutput())
                .execution((result, scope) -> {
                    var project = scope.project();
                    var editorBlock = result.firstBlock();
                    if (editorBlock.isEmpty()) {
                        ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(result.getRaw(), "Prompto"));
                    } else {
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            var oldPsiClass = Utils.findParentOfType(scope.element(), PsiClass.class);
                            for(var block : result.getEditorBlocks()) {
                                var newPsiClass = Utils.asPsiClass(block.code(), project);
                                Utils.mergePsiClasses(oldPsiClass, newPsiClass);
                            }
                        });
                    }
                })
                .build();
    }
}