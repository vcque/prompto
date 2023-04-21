package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.vcque.prompto.settings.PromptoSettingsState;
import org.jetbrains.annotations.NotNull;

public class ProjectContext implements PromptoContext {

    @Override
    public String retrieveContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return PromptoSettingsState.getInstance().projectContext;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var val = retrieveContext(project, editor, element);
        return val != null && !val.trim().isEmpty();
    }

    @Override
    public ChatMessage toMessage(String contextValue) {
        return new ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                PromptoSettingsState.getInstance().projectContext
        );
    }

}
