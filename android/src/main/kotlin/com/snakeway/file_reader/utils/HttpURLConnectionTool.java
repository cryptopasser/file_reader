package com.snakeway.file_reader.utils;


import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpURLConnectionTool {

    public static String sendGet(String requestUrl) {
        if (requestUrl == null) {
            return null;
        }
        if (!requestUrl.startsWith("http")) {
            requestUrl = "http://" + requestUrl;
        }
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setDoInput(true);
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuffer buffer = new StringBuffer();
                String readLine;
                BufferedReader responseReader;
                responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((readLine = responseReader.readLine()) != null) {
                    buffer.append(readLine).append("\n");
                }
                responseReader.close();
                return buffer.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sendPost(String requestUrl, String requestBody) {
        if (requestUrl == null || requestBody == null) {
            return null;
        }
        if (!requestUrl.startsWith("http")) {
            requestUrl = "http://" + requestUrl;
        }
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            byte[] requestStringBytes = requestBody.getBytes();
            connection.setRequestProperty("Content-length", "" + requestStringBytes.length);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestStringBytes);
            outputStream.close();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuffer buffer = new StringBuffer();
                String readLine;
                BufferedReader responseReader;
                responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((readLine = responseReader.readLine()) != null) {
                    buffer.append(readLine).append("\n");
                }
                responseReader.close();
                return buffer.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String downloadFile(String requestUrl, String cachePath) {
        if (requestUrl == null) {
            return null;
        }
        if (!requestUrl.startsWith("http")) {
            requestUrl = "http://" + requestUrl;
        }
        Log.e("", requestUrl + ";" + cachePath);
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoOutput(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setConnectTimeout(8000);
            httpURLConnection.setReadTimeout(8000);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpURLConnection.getInputStream();
                FileOutputStream fileOutputStream = null;
                try {
                    File file = new File(cachePath);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    fileOutputStream = new FileOutputStream(file);
                    int downloadSize = 0;
                    byte[] bytes = new byte[1024];
                    int length = -1;
                    while ((length = inputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, length);
                        downloadSize = downloadSize + length;
                    }
                    fileOutputStream.flush();
                    return cachePath;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
