package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.Prompts;
import org.jetbrains.annotations.NotNull;

public class FileContentContext implements PromptoContext {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return true;
    }

    @Override
    public String retrieveContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return editor.getDocument().getText();
    }

    @Override
    public ChatMessage toMessage(String contextValue) {
        return Prompts.editorContext(contextValue);
    }
}
