package com.rhtech.stser;

import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;


public class STSApp implements ApplicationComponent {


    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
        System.out.println("I am disposed!");
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "STSApp";
    }
}
