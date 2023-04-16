package com.vcque.prompto.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.vcque.prompto.PromptoManager;
import com.vcque.prompto.contexts.FileContentContext;
import com.vcque.prompto.contexts.LanguageContext;
import com.vcque.prompto.contexts.SelectionContext;
import com.vcque.prompto.outputs.AnswerMeOutput;
import com.vcque.prompto.pipelines.PromptoContextDefinition;
import com.vcque.prompto.pipelines.PromptoPipeline;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.List;

/**
 * Action that builds a prompt and put it in the user's clipboard.
 * It overrides the default "send to OpenAI" action and just output the prompt instead.
 */
public class PromptoClipboardAction extends PromptoAction<String> {

    @Override
    public PromptoPipeline<String> pipeline() {
        return PromptoPipeline.<String>builder()
                .name("clipboard")
                .contexts(List.of(
                        PromptoContextDefinition.of(new FileContentContext()),
                        PromptoContextDefinition.of(new LanguageContext()),
                        PromptoContextDefinition.ofOptional(new SelectionContext())
                ))
                .defaultInput("What does this code do ?")
                .output(new AnswerMeOutput())
                .execution((result, scope) -> {
                    ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage(result, "Explanation"));
                })
                .build();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        var pipeline = pipeline();
        var scope = new PromptoPipeline.Scope(project, editor, element);

        var title = "Prompto clipboard";
        var message = "What do you want ?";
        var initialValue = "Can you do a code review of this ?";

        var userInput = Messages.showInputDialog(project, message, title, Messages.getQuestionIcon(), initialValue, null);
        if (userInput == null || userInput.trim().isEmpty()) {
            return;
        }

        var result = PromptoManager.instance().buildManualPrompt(pipeline(), pipeline.contextMessages(scope), userInput);
        var transferable = new StringSelection(result);
        CopyPasteManager.getInstance().setContents(transferable);

        var notification = new Notification(
                "Prompto",
                "Prompt copied",
                "Your prompt and its context has been copied to your clipboard.",
                NotificationType.INFORMATION);
        Notifications.Bus.notify(notification, project);
    }
}
