package com.rhtech.stser;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class STSFileParser
{
    static Optional<Map<STSConfigService.TOKEN, String>> parseFile(Path directory, String fileName)
    {
        Path path = directory.resolve(fileName);

        File configFile = path.toFile();
        Map<STSConfigService.TOKEN, String> AWS_ENV_VARS = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile)))
        {
            int tokenSearchIndex = 0;
            String line;
            while ((line = reader.readLine()) != null)
            {
                STSConfigService.TOKEN currToken = STSConfigService.TOKEN.values()[tokenSearchIndex];
                String searchExpression = currToken + "=";
                int searchExpIndex = line.indexOf(searchExpression);

                if (searchExpIndex != -1)
                {
                    AWS_ENV_VARS.put(currToken, line.substring(searchExpIndex + searchExpression.length()));
                    if ((++tokenSearchIndex) >= STSConfigService.TOKEN.values().length)
                    {
                        break;
                    }
                }
            }

            // if collected token count is less than expected
            if (AWS_ENV_VARS.size() < STSConfigService.TOKEN.values().length)
            {
                throw new Exception("AWS Environment variables are missing (expected 4 but found less)");
            }

        }
        catch (Exception exc)
        {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                Notifications.Bus.notify(new Notification("STSer", "STS Parsing Error ", "Unable to Parse STS token file exc: " + exc.getMessage(),
                        NotificationType.ERROR));
            });
        }

        // AWS_ENV_VARS
        return AWS_ENV_VARS.size() == STSConfigService.TOKEN.values().length ? Optional.of(AWS_ENV_VARS) : Optional.empty();
    }

}
