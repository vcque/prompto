package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.vcque.prompto.settings.PromptoSettingsState;
import org.jetbrains.annotations.NotNull;

public class SettingsRetriever implements PromptoUniqueRetriever {

    @Override
    public String retrieveUniqueContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return PromptoSettingsState.getInstance().projectContext;
    }

    @Override
    public PromptoContext.Type type() {
        return PromptoContext.Type.SETTINGS;
    }

}
