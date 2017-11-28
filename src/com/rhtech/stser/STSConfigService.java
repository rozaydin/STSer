package com.rhtech.stser;

import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.ProjectManager;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.rhtech.stser.STSFileParser.parseFile;

public class STSConfigService
{
    private final Pattern configPattern = Pattern.compile("sts\\s*(\\([0-9]+\\))*.txt");
    private final String STS_CONFIG_DIR = "Downloads";
    private final Path stsDirectory;

    private volatile String mostRecentSTSConfigFile;

    public STSConfigService()
    {
        stsDirectory = Paths.get(System.getProperty("user.home"), STS_CONFIG_DIR);
    }

    enum TOKEN
    {
        AWS_ACCESS_KEY_ID,
        AWS_SECRET_ACCESS_KEY,
        AWS_SECURITY_TOKEN,
        AWS_SESSION_TOKEN
    }

    public void processFile()
    {
        if (mostRecentSTSConfigFile != null)
        {
            processFile(mostRecentSTSConfigFile);
        }
        else
        {
            // determine most recent
            File folder = stsDirectory.toFile();
            File[] files = folder.listFiles();
            if (files != null)
            {
                Arrays.stream(files).filter((file) -> configPattern.matcher(file.getName()).matches()).sorted(new Comparator<File>()
                {
                    @Override
                    public int compare(File o1, File o2)
                    {
                        return (int) (o1.lastModified() - o2.lastModified());
                    }
                }).findFirst().ifPresent((newestConfig) -> {
                    processFile(newestConfig.getName());
                });
            }
        }
    }

    private void processFile(String fileName)
    {
        parseFile(stsDirectory, fileName).ifPresent((awsTokens) ->
                Arrays.stream(ProjectManager.getInstance().getOpenProjects())
                        .forEach((project) -> {

                            RunManager runManager = RunManager.getInstance(project);
                            if (runManager.getSelectedConfiguration() != null)
                            {
                                RunConfiguration runConfig = runManager.getSelectedConfiguration().getConfiguration();
                                try
                                {
                                    modifyEnvVarSettings(runConfig, awsTokens);
                                    // display notification
                                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                                        Notifications.Bus.notify(new Notification("STSer", "Run Configuration " + runConfig.getName() + " updated",
                                                "STS variables set to env variables", NotificationType.INFORMATION));
                                    });
                                }
                                catch (Exception exc)
                                {
                                    // display notification
                                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                                        Notifications.Bus.notify(new Notification("STSer",
                                                "Run Configuration type:" + runConfig.getType().getDisplayName() + " is not supported",
                                                "Configuration Type is not supported", NotificationType.INFORMATION));
                                    });
                                }
                            }
                        })
        );
    }

    private void modifyEnvVarSettings(Object target, Map<TOKEN, String> awsTokens) throws Exception
    {
        // if configuration type does not have getEnvs method this call will fail
        Method method = target.getClass().getMethod("getEnvs");
        Map<String, String> envVars = (Map<String, String>) method.invoke(target);
        awsTokens.forEach((key, value) -> envVars.put(key.name(), value));
    }

    public Path getSTSDirectory()
    {
        return stsDirectory;
    }

    void listenForChanges(WatchService watchService)
    {
        WatchKey watchKey = watchService.poll();
        if (watchKey != null && watchKey.isValid())
        {
            List<WatchEvent<?>> events = watchKey.pollEvents();
            // process events
            events.forEach((event) -> {
                // CREATE
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE)
                {
                    String fileName = event.context().toString();
                    // process filename and update run configurations
                    if (configPattern.matcher(fileName).matches())
                    {
                        processFile(fileName);
                        // store latest file name
                        mostRecentSTSConfigFile = fileName;
                    }
                }
            });
            // resets ket state back to ready
            watchKey.reset();
        }
    }

}