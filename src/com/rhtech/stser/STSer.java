package com.rhtech.stser;

import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.RunManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.ide.ui.EditorOptionsTopHitProvider;
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
import java.io.FileReader;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class STSer extends AnAction {
    final String STS_CONFIG_NAME = "sts.txt";
    final String STS_CONFIG_DIR = "STS";

    enum TOKEN {
        AWS_ACCESS_KEY_ID,
        AWS_SECRET_ACCESS_KEY,
        AWS_SECURITY_TOKEN,
        AWS_SESSION_TOKEN
    }

    private Optional<Map<TOKEN, String>> parseFile() {

        String homeDirectory = System.getProperty("user.home");
        Path path = Paths.get(homeDirectory, STS_CONFIG_DIR, STS_CONFIG_NAME);

        File configFile = path.toFile();
        Map<TOKEN, String> AWS_ENV_VARS = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {

            int tokenSearchIndex = 0;
            String line;
            while ((line = reader.readLine()) != null) {

                TOKEN currToken = TOKEN.values()[tokenSearchIndex];
                String searchExpression = currToken + "=";
                int searchExpIndex = line.indexOf(searchExpression);

                if (searchExpIndex != -1) {
                    AWS_ENV_VARS.put(currToken, line.substring(searchExpIndex + searchExpression.length()));
                    if ((++tokenSearchIndex) >= TOKEN.values().length) {
                        break;
                    }
                }
            }

            // if collected token count is less than expected
            if (AWS_ENV_VARS.size() < TOKEN.values().length) {
                throw new Exception("AWS Environment variables are missing (expected 4 but found less)");
            }

        } catch (Exception exc) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                Notifications.Bus.notify(new Notification("STSer", "STS Parsing Error ", "Unable to Parse STS token file exc: " + exc.getMessage(),
                        NotificationType.ERROR));
            });
        }

        // AWS_ENV_VARS
        return AWS_ENV_VARS.size() == TOKEN.values().length ? Optional.of(AWS_ENV_VARS) : Optional.empty();
    }

    private void modifyEnvVarSettings(Object target, Map<TOKEN, String> awsTokens) throws Exception {

        // if configuration type does not have getEnvs method this call will fail
        Method method = target.getClass().getMethod("getEnvs");
        Map<String, String> envVars = (Map<String, String>) method.invoke(target);

        awsTokens.entrySet().stream().forEach((entry) -> {
            envVars.put(entry.getKey().name(), entry.getValue());
        });
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        parseFile().ifPresent((awsTokens) -> {

            Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

            Arrays.stream(openProjects).forEach((project) -> {
                RunManager runManager = RunManager.getInstance(ProjectManager.getInstance().getOpenProjects()[0]);
                if (runManager.getSelectedConfiguration() != null) {
                    RunConfiguration runConfig = runManager.getSelectedConfiguration().getConfiguration();
                    try {
                        modifyEnvVarSettings(runConfig, awsTokens);

                        // display notification
                        ApplicationManager.getApplication().executeOnPooledThread(() -> {
                            Notifications.Bus.notify(new Notification("STSer", "Run Configuration " + runConfig.getName() + " updated",
                                    "STS variables set to env variables", NotificationType.INFORMATION));
                        });

                    } catch (Exception exc) {
                        // display notification
                        ApplicationManager.getApplication().executeOnPooledThread(() -> {
                            Notifications.Bus.notify(new Notification("STSer", "Run Configuration type:" + runConfig.getType().getDisplayName() + " is not supported",
                                    "Configuration Type is not supported", NotificationType.INFORMATION));
                        });
                    }
                }
            });
        });
    }
}
