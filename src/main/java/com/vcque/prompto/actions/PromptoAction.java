package com.vcque.prompto.actions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.vcque.prompto.PromptoManager;
import com.vcque.prompto.pipelines.PromptoPipeline;
import org.jetbrains.annotations.NotNull;

public abstract class PromptoAction<T> extends PsiElementBaseIntentionAction implements IntentionAction {

    public abstract PromptoPipeline<T> pipeline();

    @NotNull
    @Override
    public String getFamilyName() {
        return "prompto";
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "prompto " + pipeline().getName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return pipeline().isAvailable(new PromptoPipeline.Scope(project, editor, element));
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        var scope = new PromptoPipeline.Scope(project, editor, element);
        PromptoManager.instance().executePipeline(pipeline(), scope);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
