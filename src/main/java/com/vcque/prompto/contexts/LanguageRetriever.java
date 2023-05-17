package com.vcque.prompto.contexts;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LanguageRetriever implements PromptoUniqueRetriever {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return true;
    }

    @Override
    public String name() {
        return "Editor language";
    }

    @Override
    public String retrieveUniqueContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        return Optional.ofNullable(psiFile)
                .map(PsiElement::getLanguage)
                .map(Language::getDisplayName)
                .orElse(null);
    }

    @Override
    public PromptoContext.Type type() {
        return PromptoContext.Type.LANGUAGE;
    }
}