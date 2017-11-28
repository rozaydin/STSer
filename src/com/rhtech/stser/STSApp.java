package com.rhtech.stser;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class STSApp implements ApplicationComponent
{
    private WatchService watcher;
    private WatchKey watchKey;
    private volatile boolean listening = true;

    @Override
    public void initComponent()
    {
        try
        {
            watcher = FileSystems.getDefault().newWatchService();
            STSConfigService stsConfigService = ServiceManager.getService(STSConfigService.class);
            // Watcher events
            watchKey = stsConfigService.getSTSDirectory().register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
            // register listener
            Thread watcherThread = new Thread(() -> {
                while (listening)
                {
                    stsConfigService.listenForChanges(watcher);
                }
            });

            watcherThread.setDaemon(true);
            watcherThread.start();
        }
        catch (Exception exc)
        {
            throw new RuntimeException(exc.getCause());
        }
    }

    @Override
    public void disposeComponent()
    {
        try
        {
            listening = false;
            watcher.close();
            watchKey.cancel();
        }
        catch (Exception exc)
        {
            throw new RuntimeException(exc.getCause());
        }
    }

    @Override
    @NotNull
    public String getComponentName()
    {
        return "STSer";
    }
}
