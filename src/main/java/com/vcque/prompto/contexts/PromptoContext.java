package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PromptoContext {

    default boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var value = retrieveContext(project, editor, element);
        return value != null && !value.trim().isEmpty();
    }

    String retrieveContext(@NotNull Project project, Editor editor, @NotNull PsiElement element);

    ChatMessage toMessage(String contextValue);

    default Optional<ChatMessage> messageFromContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var contextValue = retrieveContext(project, editor, element);
        if (contextValue == null || contextValue.trim().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toMessage(contextValue));
        }
    }
}
