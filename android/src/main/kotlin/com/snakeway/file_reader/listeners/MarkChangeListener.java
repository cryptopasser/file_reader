package com.snakeway.file_reader.listeners;

public interface MarkChangeListener {
    void updatePage(String key, String page, boolean isScreenCustom, String extra);

    void addAnnotation(String key, String data, boolean isScreenCustom, String extra);

    void updateAnnotation(String key, String data, boolean isScreenCustom, String extra);

    void removeAnnotation(String key, String data, boolean isScreenCustom, String extra);

    void close(String key, String data, boolean isScreenCustom, String extra);

    void annotationPageRemove(String key, String data, boolean isScreenCustom, String extra);

    void annotationAllRemove(String key, String data, boolean isScreenCustom, String extra);

    void updateInfo(String key, String data, boolean isScreenCustom, String extra);

}
