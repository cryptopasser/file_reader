package com.snakeway.file_reader.models;

public class MenuItem {

    private String name;
    private String type;
    private String enable;
    private String backgroundColor;

    public MenuItem(String name, String type, String enable, String backgroundColor) {
        this.name = name;
        this.type = type;
        this.enable = enable;
        this.backgroundColor = backgroundColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
