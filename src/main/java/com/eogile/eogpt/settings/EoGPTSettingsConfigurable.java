package com.eogile.eogpt.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EoGPTSettingsConfigurable implements Configurable {

  private EoGPTSettingsComponent settingsComponent;

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "EoGPT settings example";
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return settingsComponent.getPreferredFocusedComponent();
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    settingsComponent = new EoGPTSettingsComponent();
    return settingsComponent.getPanel();
  }

  @Override
  public boolean isModified() {
    var settings = EoGPTSettingsState.getInstance();
    boolean modified = !settingsComponent.getApiToken().equals(settings.apiToken);
    return modified;
  }

  @Override
  public void apply() {
    var settings = EoGPTSettingsState.getInstance();
    settings.apiToken = settingsComponent.getApiToken();
  }

  @Override
  public void reset() {
    var settings = EoGPTSettingsState.getInstance();
    settingsComponent.setApiToken("");
    settings.apiToken = "";
  }

  @Override
  public void disposeUIResources() {
    settingsComponent = null;
  }

}
