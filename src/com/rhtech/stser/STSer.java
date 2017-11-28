package com.rhtech.stser;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;

public class STSer extends AnAction
{

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        STSConfigService stsConfigService = ServiceManager.getService(STSConfigService.class);
        stsConfigService.processFile();

        // STSSettingsDialog dialog = new STSSettingsDialog(e.getProject(), true);
        // dialog.show();
    }
}
