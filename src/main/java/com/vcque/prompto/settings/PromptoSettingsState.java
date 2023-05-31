package com.vcque.prompto.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(name = "PromptoSettings", storages = {@Storage("PromptoSettings.xml")})
@Service
public final class PromptoSettingsState implements PersistentStateComponent<PromptoSettingsState> {

    public String apiToken = "";
    public String languageModel = "gpt-3.5-turbo";
    public String projectContext = "The user is an experienced developer familiar with the code base. \nBe very concise and do not waste his time with trivial or obvious considerations.";

    public static PromptoSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(PromptoSettingsState.class);
    }

    @Override
    public PromptoSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PromptoSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}