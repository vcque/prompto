package com.vcque.prompto.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.vcque.prompto.PromptoManager;

public class ActionHelper {

    private ActionHelper() {
    }

    /**
     * Check if openAI is available. Adds an error notification if not.
     *
     * @return the openAI status.
     */
    public static boolean isOpenAIAvailable() {
        var hasToken = PromptoManager.instance().hasToken();
        if (!hasToken) {
            var notification = new Notification(
                    "Prompto",
                    "Missing OpenAI key",
                    "Add your open-ai key to Prompto settings to enable this feature.",
                    NotificationType.ERROR);
            Notifications.Bus.notify(notification);
        }
        return hasToken;
    }
}
