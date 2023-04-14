package com.eogile.eogpt.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(name = "EoGPTSettings", storages = {@Storage("EoGPTSettings.xml")})
@Service
public final class EoGPTSettingsState implements PersistentStateComponent<EoGPTSettingsState> {

    public String apiToken = "";

    public static EoGPTSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(EoGPTSettingsState.class);
    }

    @Override
    public EoGPTSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull EoGPTSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}