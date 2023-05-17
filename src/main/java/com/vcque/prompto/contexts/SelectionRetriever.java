package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class SelectionRetriever implements PromptoUniqueRetriever {

    @Override
    public PromptoContext.Type type() {
        return PromptoContext.Type.SELECTION;
    }

    @Override
    public String retrieveUniqueContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return editor.getSelectionModel().getSelectedText();
    }

    @Override
    public String name() {
        return "Editor selection";
    }
}
