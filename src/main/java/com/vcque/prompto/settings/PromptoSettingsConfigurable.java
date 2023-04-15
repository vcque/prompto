package com.vcque.prompto.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PromptoSettingsConfigurable implements Configurable {

  private PromptoSettingsComponent settingsComponent;

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "Prompto settings";
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return settingsComponent.getPreferredFocusedComponent();
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    settingsComponent = new PromptoSettingsComponent();
    return settingsComponent.getPanel();
  }

  @Override
  public boolean isModified() {
    var settings = PromptoSettingsState.getInstance();
    boolean modified = !settingsComponent.getApiToken().equals(settings.apiToken);
    return modified;
  }

  @Override
  public void apply() {
    var settings = PromptoSettingsState.getInstance();
    settings.apiToken = settingsComponent.getApiToken();
  }

  @Override
  public void reset() {
    var settings = PromptoSettingsState.getInstance();
    settingsComponent.setApiToken("");
    settings.apiToken = "";
  }

  @Override
  public void disposeUIResources() {
    settingsComponent = null;
  }

}
