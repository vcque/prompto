package com.vcque.prompto.contexts;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.vcque.prompto.Prompts;
import org.jetbrains.annotations.NotNull;

public class MethodNameContext implements PromptoContext {

    @Override
    public String retrieveContext(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        var method = getMethod(element);
        return method == null ? null : method.getName();
    }

    @Override
    public ChatMessage toMessage(String contextValue) {
        return Prompts.methodNameContext(contextValue);
    }

    private PsiMethod getMethod(@NotNull PsiElement element) {
        return element instanceof PsiMethod ? (PsiMethod) element : PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    }
}
