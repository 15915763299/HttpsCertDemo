package com.demo;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private static App app;

    public static App getApp() {
        return app;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        app = this;
    }
}
