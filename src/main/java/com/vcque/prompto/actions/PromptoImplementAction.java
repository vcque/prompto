package com.vcque.prompto.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.Utils;
import com.vcque.prompto.contexts.AvailableClassesRetriever;
import com.vcque.prompto.contexts.EditorContentRetriever;
import com.vcque.prompto.contexts.LanguageRetriever;
import com.vcque.prompto.contexts.MethodRetriever;
import com.vcque.prompto.contexts.SettingsRetriever;
import com.vcque.prompto.outputs.MethodOutput;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;

import java.util.List;

public class PromptoImplementAction extends PromptoAction<PromptoResponse> {

    @Override
    public PromptoPipeline<PromptoResponse> pipeline() {
        return PromptoPipeline.<PromptoResponse>builder()
                .name("implement")
                .retrievers(List.of(
                        PromptoRetrieverDefinition.ofOptional(new SettingsRetriever()),
                        PromptoRetrieverDefinition.of(new LanguageRetriever()),
                        PromptoRetrieverDefinition.of(new EditorContentRetriever()),
                        PromptoRetrieverDefinition.of(new MethodRetriever()),
                        PromptoRetrieverDefinition.of(new AvailableClassesRetriever())
                ))
                .defaultInput("Implement this method to the best of your abilities")
                .output(new MethodOutput())
                .execution(this::apply)
                .build();
    }

    private void apply(PromptoResponse result, PromptoPipeline.Scope scope) {
        var project = scope.project();
        if (result.getEditorBlocks().isEmpty()) {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(result.getRaw(), "Prompto"));
        } else {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                var hasChanged = insertCode(scope, result);
                if (!hasChanged) {
                    ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(result.getRaw(), "Prompto"));
                }
            });
        }
    }

    private boolean insertCode(PromptoPipeline.Scope scope, PromptoResponse response) {
        var methodHolder = findEditorBlockWithMethods(scope.project(), response);
        if (methodHolder == null) {
            return false;
        }
        var oldPsiClass = Utils.findParentOfType(scope.element(), PsiClass.class);
        var oldMethod = Utils.findParentOfType(scope.element(), PsiMethod.class);
        var newMethod = methodHolder.getMethods()[0];
        oldMethod.replace(newMethod);
        Utils.mergePsiClasses(oldPsiClass, methodHolder);
        return true;
    }

    private PsiClass findEditorBlockWithMethods(Project project, PromptoResponse response) {
        var editorBlocks = response.getEditorBlocks();
        for (var editorBlock : editorBlocks) {
            var code = editorBlock.code();
            var psiClass = Utils.asPsiClass(code, project);
            if (psiClass.getMethods().length > 0) {
                return psiClass;
            }
        }
        return null;
    }

}
