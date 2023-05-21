package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.vcque.prompto.Utils;
import com.vcque.prompto.lang.java.PromptoJavaUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MethodRetriever implements PromptoUniqueRetriever {

    @Override
    public String retrieveUniqueContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var method = Utils.findParentOfType(element, PsiMethod.class);
        return Optional.ofNullable(method)
                .map(m -> {
                    var copy = (PsiMethod) method.copy();
                    Optional.ofNullable(copy.getDocComment()).ifPresent(PsiElement::delete);
                    PromptoJavaUtils.elideBody(copy);
                    return copy.getText();
                })
                .orElse(null);
    }

    @Override
    public PromptoContext.Type type() {
        return PromptoContext.Type.METHOD;
    }

    @Override
    public String name() {
        return "Selected method";
    }
}
