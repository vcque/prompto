package com.vcque.prompto.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.LightVirtualFile;
import org.intellij.plugins.markdown.ui.preview.html.MarkdownUtil;
import org.intellij.plugins.markdown.ui.preview.jcef.MarkdownJCEFHtmlPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PromptoAnswerDialog extends DialogWrapper {
    private final String markdown;
    private final Project project;

    public PromptoAnswerDialog(Project project, String markdown) {
        super(true); // use current window as parent
        this.project = project;
        this.markdown = markdown;
        init();
        setTitle("Prompto");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        var file = new LightVirtualFile("answer.md", markdown);
        var panel = new MarkdownJCEFHtmlPanel(project, file);
        Disposer.register(this.myDisposable, panel);
        var html = MarkdownUtil.INSTANCE.generateMarkdownHtml(file, markdown, project);
        panel.setHtml(html, 0);
        return panel.getComponent();
    }
}