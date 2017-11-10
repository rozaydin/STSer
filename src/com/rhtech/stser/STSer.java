package com.rhtech.stser;

import com.intellij.execution.RunManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class STSer extends AnAction
{
    final String path = "C:\\STS\\sts.txt";

    enum TOKEN
    {
        AWS_ACCESS_KEY_ID,
        AWS_SECRET_ACCESS_KEY,
        AWS_SECURITY_TOKEN
    }

    private Map<TOKEN, String> parseFile()
    {

        File configFile = new File(path);
        Map<TOKEN, String> AWS_ENV_VARS = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile)))
        {

            int tokenSearchIndex = 0;
            String line;
            while ((line = reader.readLine()) != null)
            {

                TOKEN currToken = TOKEN.values()[tokenSearchIndex];
                String searchExpression = currToken + "=";
                int searchExpIndex = line.indexOf(searchExpression);

                if (searchExpIndex != -1)
                {
                    AWS_ENV_VARS.put(currToken, line.substring(searchExpIndex + searchExpression.length()));
                    if ( (++tokenSearchIndex) >= TOKEN.values().length ) {
                        break;
                    }
                }
            }

        }
        catch (FileNotFoundException exc)
        {
            System.out.println(exc);
        }
        catch (IOException exc)
        {
            System.out.println(exc);
        }

        // AWS_ENV_VARS
        return AWS_ENV_VARS;
    }

    @Override
    public void actionPerformed(AnActionEvent e)
    {

        Map<TOKEN, String> awsTokens = parseFile();
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

        Arrays.stream(openProjects).forEach((project) -> {
            RunManager runManager = RunManager.getInstance(ProjectManager.getInstance().getOpenProjects()[0]);
            if (runManager.getSelectedConfiguration() != null) {
                ApplicationConfiguration config = (ApplicationConfiguration) runManager.getSelectedConfiguration().getConfiguration();
                awsTokens.entrySet().stream().forEach((entry) -> {
                    config.getEnvs().put(entry.getKey().name(), entry.getValue());
                });
                // display notification
                // display notification
                ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                    @Override
                    public void run() {
                        Notifications.Bus.notify(new Notification("STSer", "Run Configuration " + config.getName() + " updated", "STS variables set to env variables", NotificationType.INFORMATION));
                    }
                });
            }
        });
    }
}
