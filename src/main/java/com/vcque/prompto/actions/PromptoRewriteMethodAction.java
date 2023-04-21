package com.vcque.prompto.actions;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.vcque.prompto.Utils;
import com.vcque.prompto.contexts.*;
import com.vcque.prompto.outputs.MethodOutput;
import com.vcque.prompto.pipelines.PromptoContextDefinition;
import com.vcque.prompto.pipelines.PromptoPipeline;

import java.util.List;

public class PromptoRewriteMethodAction extends PromptoAction<String> {

    @Override
    public PromptoPipeline<String> pipeline() {
        return PromptoPipeline.<String>builder()
                .name("rewrite method")
                .contexts(List.of(
                        PromptoContextDefinition.ofOptional(new ProjectContext()),
                        PromptoContextDefinition.of(new LanguageContext()),
                        PromptoContextDefinition.of(new FileContentContext()),
                        PromptoContextDefinition.of(new MethodNameContext()),
                        PromptoContextDefinition.of(new AvailableClassesContext())
                ))
                .defaultInput("Add documentation")
                .output(new MethodOutput())
                .execution((result, scope) -> {
                    var project = scope.project();
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        var oldMethod = Utils.findParentOfType(scope.element(), PsiMethod.class);
                        var newMethod = PsiElementFactory.getInstance(project).createMethodFromText(result, oldMethod.getContext());
                        oldMethod.replace(newMethod);
                    });
                })
                .build();
    }

}
