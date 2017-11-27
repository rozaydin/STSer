package com.rhtech.stser;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class STSSettingsDialog extends DialogWrapper
{

    public STSSettingsDialog(@Nullable Project project, boolean canBeParent)
    {
        super(project, canBeParent);
        init();
        setTitle("STSer Configuration");
    }

    @Nullable @Override
    protected JComponent createCenterPanel()
    {
        JPanel panel = new JPanel();

        JLabel label = new JLabel("STS Directory:");
        JTextField setting = new JTextField();
        setting.setMinimumSize(new Dimension(200, 20));
        JButton selector = new JButton("+");

        panel.add("label", label);
        panel.add("setting", setting);
        panel.add("selector", selector);

        return panel;
    }
}
