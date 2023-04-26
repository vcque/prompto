package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface PromptoUniqueRetriever extends PromptoRetriever {

    String retrieveUniqueContext(@NotNull Project project, Editor editor, @NotNull PsiElement element);

    PromptoContext.Type type();


    default Set<PromptoContext> retrieveContexts(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var value = retrieveUniqueContext(project, editor, element);
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Set.of(
                PromptoContext.builder()
                        .value(value)
                        .type(type())
                        .build()
        );
    }

}
