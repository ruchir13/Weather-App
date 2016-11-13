package com.weatherapp.model;

/**
 * Created by Ruchir on 11-11-2016.
 */

public class Sys {
    private String pod;

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    @Override
    public String toString() {
        return "ClassPojo [pod = " + pod + "]";
    }
}
