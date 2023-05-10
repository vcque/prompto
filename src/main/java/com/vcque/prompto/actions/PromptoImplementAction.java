package com.vcque.prompto.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.vcque.prompto.PromptoResponse;
import com.vcque.prompto.Utils;
import com.vcque.prompto.contexts.*;
import com.vcque.prompto.outputs.MethodOutput;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;
import com.vcque.prompto.pipelines.PromptoPipeline;

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
                .execution((result, scope) -> {
                    var project = scope.project();
                    var editorBlock = result.firstBlock();
                    if (editorBlock.isEmpty()) {
                        ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(result.getRaw(), "Prompto"));
                    } else {
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            var code = editorBlock.get().code();
                            var newPsiClass = Utils.asPsiClass(code, project);
                            var oldPsiClass = Utils.findParentOfType(scope.element(), PsiClass.class);
                            var oldMethod = Utils.findParentOfType(scope.element(), PsiMethod.class);
                            if (newPsiClass.getMethods().length > 0) {
                                var newMethod = newPsiClass.getMethods()[0];
                                oldMethod.replace(newMethod);
                            }
                            Utils.mergePsiClasses(oldPsiClass, newPsiClass);
                        });
                    }
                })
                .build();
    }

}