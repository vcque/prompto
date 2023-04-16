package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilBase;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.Prompts;
import org.jetbrains.annotations.NotNull;

public class LanguageContext implements PromptoContext {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return true;
    }

    @Override
    public String retrieveContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        return psiFile == null ? null : psiFile.getLanguage().getDisplayName();
    }

    @Override
    public ChatMessage toMessage(String contextValue) {
        return Prompts.languageContext(contextValue);
    }
}