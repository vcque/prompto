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
        return "Prompto";
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
        return !settingsComponent.getApiToken().equals(settings.apiToken)
                || !settingsComponent.getProjectContext().equals(settings.projectContext)
                || !settingsComponent.getlanguageModel().equals(settings.languageModel);
    }

    @Override
    public void apply() {
        var settings = PromptoSettingsState.getInstance();
        settings.apiToken = settingsComponent.getApiToken();
        settings.projectContext = settingsComponent.getProjectContext();
        settings.languageModel = settingsComponent.getlanguageModel();
    }

    @Override
    public void reset() {
        settingsComponent.setApiToken(PromptoSettingsState.getInstance().apiToken);
        settingsComponent.setProjectContext(PromptoSettingsState.getInstance().projectContext);
        settingsComponent.setLanguageModel(PromptoSettingsState.getInstance().languageModel);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }

}
