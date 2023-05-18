package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PromptoUniqueRetriever extends PromptoRetriever {

    String retrieveUniqueContext(@NotNull Project project, Editor editor, @NotNull PsiElement element);

    PromptoContext.Type type();


    default List<PromptoContext> retrieveContexts(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var value = retrieveUniqueContext(project, editor, element);
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return List.of(
                PromptoContext.builder()
                        .value(value)
                        .type(type())
                        .build()
        );
    }

}
