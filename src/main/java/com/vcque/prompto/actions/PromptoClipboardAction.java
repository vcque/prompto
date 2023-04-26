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
import com.vcque.prompto.contexts.*;
import com.vcque.prompto.outputs.AnswerMeOutput;
import com.vcque.prompto.pipelines.PromptoRetrieverDefinition;
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
                .retrievers(List.of(
                        PromptoRetrieverDefinition.of(new LanguageRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new SettingsRetriever()),
                        PromptoRetrieverDefinition.of(new EditorContentRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new SelectionRetriever()),
                        PromptoRetrieverDefinition.ofOptional(new AvailableClassesRetriever(
                                new AvailableClassesRetriever.Config(
                                        1000,
                                        5
                                )
                        ))
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

        var result = PromptoManager.instance().buildManualPrompt(pipeline(), pipeline.retrieveContexts(scope), "");
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
