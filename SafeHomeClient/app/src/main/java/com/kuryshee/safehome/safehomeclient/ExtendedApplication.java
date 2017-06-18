package com.kuryshee.safehome.safehomeclient;

import 	android.app.Application;

public class ExtendedApplication extends Application {
    public String getRaspberryID() {
        return raspberryID;
    }

    public void setRaspberryID(String raspberryID) {
        this.raspberryID = raspberryID;
    }

    private String raspberryID;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    private String state;

}
