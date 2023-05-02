package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.vcque.prompto.lang.PromptoLangs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

public class EditorContentRetriever implements PromptoUniqueRetriever {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Config {
        /**
         * Max token cost of this context.
         */
        private int maxCost = 1200;
    }

    private final EditorContentRetriever.Config config;

    /**
     * With default config.
     */
    public EditorContentRetriever() {
        this.config = new EditorContentRetriever.Config();
    }

    public EditorContentRetriever(EditorContentRetriever.Config config) {
        this.config = config;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return true;
    }

    @Override
    public String retrieveUniqueContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return PromptoLangs.getInstance().shrinkEditor(editor, config.maxCost);
    }

    @Override
    public PromptoContext.Type type() {
        return PromptoContext.Type.EDITOR;
    }
}
