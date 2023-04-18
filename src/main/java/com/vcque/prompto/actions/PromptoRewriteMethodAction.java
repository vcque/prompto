package com.vcque.prompto.actions;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.vcque.prompto.contexts.FileContentContext;
import com.vcque.prompto.contexts.LanguageContext;
import com.vcque.prompto.contexts.MethodNameContext;
import com.vcque.prompto.contexts.SelectionContext;
import com.vcque.prompto.outputs.MethodOutput;
import com.vcque.prompto.pipelines.PromptoContextDefinition;
import com.vcque.prompto.pipelines.PromptoPipeline;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PromptoRewriteMethodAction extends PromptoAction<String> {

    @Override
    public PromptoPipeline<String> pipeline() {
        return PromptoPipeline.<String>builder()
                .name("rewrite method")
                .contexts(List.of(
                        PromptoContextDefinition.of(new FileContentContext()),
                        PromptoContextDefinition.of(new LanguageContext()),
                        PromptoContextDefinition.of(new MethodNameContext()),
                        PromptoContextDefinition.ofOptional(new SelectionContext())
                ))
                .defaultInput("Add documentation")
                .output(new MethodOutput())
                .execution((result, scope) -> {
                    var project = scope.project();
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        var oldMethod = getMethod(scope.element());
                        var newMethod = PsiElementFactory.getInstance(project).createMethodFromText(result, oldMethod.getContext());
                        oldMethod.replace(newMethod);
                    });
                })
                .build();
    }

    private PsiMethod getMethod(@NotNull PsiElement element) {
        return element instanceof PsiMethod ? (PsiMethod) element : PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    }
}
