package com.vcque.prompto.actions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.vcque.prompto.PromptoManager;
import org.jetbrains.annotations.NotNull;

public class PromptoEnhanceAction extends PsiElementBaseIntentionAction implements IntentionAction {

    @NotNull
    @Override
    public String getText() {
        return "prompto enhance";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "prompto";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return getMethod(element) != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        var text = editor.getDocument().getText();
        var method = getMethod(element);
        if (method == null) {
            return;
        }

        String title = "prompto enhance";
        String message = "What do you want ?";
        String initialValue = "Make it better";

        String userInput = Messages.showInputDialog(project, message, title, Messages.getQuestionIcon(), initialValue, null);
        if (userInput == null || userInput.trim().isEmpty()) {
            return;
        }

        var methodName = method.getName();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "prompto enhance", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                var result = PromptoManager.instance().queryEnhance(text, methodName, userInput);
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    var newMethod = PsiElementFactory.getInstance(project).createMethodFromText(result, method.getContext());
                    method.replace(newMethod);
                });
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
