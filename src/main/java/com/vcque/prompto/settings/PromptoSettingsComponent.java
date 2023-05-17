package com.vcque.prompto.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PromptoSettingsComponent {

    private final JPanel mainPanel;
    private final JBTextField apiToken = new JBTextField();

    private final ComboBox<String> languageModel = new ComboBox<>();
    private final JBTextArea projectContext = new JBTextArea(
            "Additional information provided to Prompto.",
            4,
            80
    );

    public PromptoSettingsComponent() {
        var languageModelModel = new DefaultComboBoxModel<String>();
        languageModelModel.addElement("gpt-3.5-turbo");
        languageModelModel.addElement("gpt-4");
        languageModel.setModel(languageModelModel);

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Enter openAI token: "), apiToken, 1, false)
                .addVerticalGap(8)
                .addLabeledComponent(new JBLabel("GPT version: "), languageModel, 1, false)
                .addVerticalGap(8)
                .addLabeledComponent(new JBLabel("Global context information: "), projectContext, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return apiToken;
    }

    @NotNull
    public String getApiToken() {
        return apiToken.getText();
    }

    public void setApiToken(@NotNull String newText) {
        apiToken.setText(newText);
    }

    @NotNull
    public String getlanguageModel() {
        return languageModel.getItem();
    }

    public void setLanguageModel(@NotNull String languageModel) {
        this.languageModel.setSelectedItem(languageModel);
    }

    public String getProjectContext() {
        return projectContext.getText();
    }

    public void setProjectContext(String text) {
        projectContext.setText(text);
    }


}
