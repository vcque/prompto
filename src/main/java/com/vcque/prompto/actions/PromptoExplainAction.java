package com.vcque.prompto.actions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.vcque.prompto.PromptoManager;
import org.jetbrains.annotations.NotNull;

public class PromptoExplainAction extends PsiElementBaseIntentionAction implements IntentionAction {

    @NotNull
    @Override
    public String getText() {
        return "prompto explain";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "prompto";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        if (!ActionHelper.isOpenAIAvailable()) {
            return;
        }

        var text = editor.getDocument().getText();
        var method = getMethod(element);

        var title = "prompto explain";
        var message = "What do you want to know ?";
        var initialValue = method != null
                ? "What is the purpose of %s?".formatted(method.getName())
                : "What does this class do ?";

        var userInput = Messages.showInputDialog(project, message, title, Messages.getQuestionIcon(), initialValue, null);
        if (userInput == null || userInput.trim().isEmpty()) {
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "prompto explain", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                var result = PromptoManager.instance().queryExplain(text, userInput);
                ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(result, userInput));
            }
        });
    }

    private PsiMethod getMethod(@NotNull PsiElement element) {
        return element instanceof PsiMethod ? (PsiMethod) element : PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
