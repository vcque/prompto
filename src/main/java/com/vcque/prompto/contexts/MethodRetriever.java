package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.vcque.prompto.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MethodRetriever implements PromptoUniqueRetriever {

    @Override
    public String retrieveUniqueContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var method = Utils.findParentOfType(element, PsiMethod.class);
        return Optional.ofNullable(method)
                .map(PsiMethod::getNameIdentifier)
                .map(PsiElement::getText)
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
