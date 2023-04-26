package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface PromptoRetriever {

    default boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var value = retrieveContexts(project, editor, element);
        return value != null && !value.isEmpty();
    }

    Set<PromptoContext> retrieveContexts(@NotNull Project project, Editor editor, @NotNull PsiElement element);

}
