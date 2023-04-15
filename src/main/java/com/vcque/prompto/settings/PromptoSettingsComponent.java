package com.vcque.prompto.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PromptoSettingsComponent {

  private final JPanel mainPanel;
  private final JBTextField apiToken = new JBTextField();

  public PromptoSettingsComponent() {
    mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel("Enter openAI token: "), apiToken, 1, false)
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

}
