package com.vcque.prompto.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.Utils;
import com.vcque.prompto.contexts.AvailableClassesRetriever;
import com.vcque.prompto.contexts.EditorContentRetriever;
import com.vcque.prompto.contexts.ErrorRetriever;
import com.vcque.prompto.contexts.LanguageRetriever;
import com.vcque.prompto.contexts.MethodRetriever;
import com.vcque.prompto.contexts.PromptoContext;
import com.vcque.prompto.contexts.SettingsRetriever;
import com.vcque.prompto.lang.java.PromptoJavaUtils;
import com.vcque.prompto.outputs.MethodOutput;
import com.vcque.prompto.pipelines.PromptoPipeline;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;
import com.vcque.prompto.ui.PromptoAnswerDialog;

import java.util.List;

public class PromptoImplementAction extends PromptoAction<PromptoResponse> {

    @Override
    public PromptoPipeline<PromptoResponse> pipeline() {
        return PromptoPipeline.<PromptoResponse>builder()
                .name("implement")
                .retrievers(List.of(
                        PromptoRetrieverDefinition.of(new LanguageRetriever()),
                        PromptoRetrieverDefinition.of(new MethodRetriever()),
                        PromptoRetrieverDefinition.of(new EditorContentRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new SettingsRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new ErrorRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new AvailableClassesRetriever())
                ))
                .defaultInput("Go")
                .output(new MethodOutput())
                .execution(this::apply)
                .build();
    }

    private void apply(PromptoResponse result, PromptoPipeline.Scope scope, List<PromptoContext> contexts) {
        var project = scope.project();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            var hasChanged = insertCode(scope, result);
            if (!hasChanged) {
                ApplicationManager.getApplication().invokeLater(() -> new PromptoAnswerDialog(scope.project(), result.getRaw()).show());
            }
        });
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
            var psiClass = PromptoJavaUtils.asPsiClass(project, code);
            if (psiClass != null && psiClass.getMethods().length > 0) {
                return psiClass;
            }
        }
        return null;
    }

}
