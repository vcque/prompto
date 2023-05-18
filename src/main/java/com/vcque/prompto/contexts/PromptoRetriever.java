package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Common interface for defining code that captures information from the IDE.
 */
public interface PromptoRetriever {

    /**
     *
     * @param project The current project
     * @param editor The current editor
     * @param element The psi element where the caret is
     * @return true if this retriever can be used in this context
     */
    default boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var value = retrieveContexts(project, editor, element);
        return value != null && !value.isEmpty();
    }

    /**
     *
     * @param project The current project
     * @param editor The current editor
     * @param element The psi element where the caret is
     * @return List of retrieved prompto context, ordered by priority.
     */
    List<PromptoContext> retrieveContexts(@NotNull Project project, Editor editor, @NotNull PsiElement element);

    /**
     *
     * @return The user-facing name of this retriever.
     */
    String name();
}
