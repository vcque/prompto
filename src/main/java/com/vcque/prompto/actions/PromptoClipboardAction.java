package com.vcque.prompto.actions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.vcque.prompto.PromptoManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;

/**
 * Action that builds a prompt and put it in the user's clipboard.
 */
public class PromptoClipboardAction extends PsiElementBaseIntentionAction implements IntentionAction {

    @NotNull
    @Override
    public String getText() {
        return "prompto clipboard";
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
        var text = editor.getDocument().getText();

        var title = "Prompto clipboard";
        var message = "What do you want ?";
        var initialValue = "Can you do a code review of this ?";

        var userInput = Messages.showInputDialog(project, message, title, Messages.getQuestionIcon(), initialValue, null);
        if (userInput == null || userInput.trim().isEmpty()) {
            return;
        }

        var result = PromptoManager.instance().buildPrompt(text, userInput);
        var transferable = new StringSelection(result);
        CopyPasteManager.getInstance().setContents(transferable);

        var notification = new Notification(
                "Prompto",
                "Prompt copied",
                "Your prompt and its context has been copied to your clipboard.",
                NotificationType.INFORMATION);
        Notifications.Bus.notify(notification, project);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
