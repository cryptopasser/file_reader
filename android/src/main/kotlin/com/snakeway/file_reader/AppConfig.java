package com.snakeway.file_reader;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.Utils;
import com.snakeway.file_reader.listeners.MarkChangeListener;
import com.snakeway.file_reader.utils.MD5Tool;
import com.snakeway.fileviewer.ofd.OFDWebViewActivity;
import com.tencent.smtt.sdk.QbSdk;

import java.io.File;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author snakeway
 * @description:
 * @date :2021/3/4 14:06
 */
public class AppConfig {

    public static final String FILE_KEY = "file_key";
    public static final String FILE_NAME_KEY = "file_name_key";
    public static final String FILE_PATH_KEY = "file_path_key";
    public static final String FILE_TYPE_KEY = "file_type_key";
    public static final String FILE_PASSWORD_KEY = "file_password_key";
    public static final String FILE_ANNOTATION_KEY = "file_annotation_key";
    public static final String IS_READ_ONLY_KEY = "is_read_only_key";
    public static final String OPEN_AUTO_SPACE_KEY = "open_auto_space_key";
    public static final String PAGE_KEY = "page_key";
    public static final String MENU = "menu";
    public static final String EXTRA = "extra";

    public static final String SELECT_PAGE_KEY = "select_page_key";

    public static final String ROOT_PATH = "fileReader";
    public static final String DOWNLOAD_CACHE_DIR_NAME = "downloadCache";

    public static final String TAG = "AppConfig";

    public static final int REQUEST_X5_PERMISSIONS = 1;

    public static final String[] BASE_X5_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    public static boolean needInitX5 = true;

    public static String filesDirRootPath;

    public static boolean isInit = false;

    public static MarkChangeListener markChangeListener;


    public static void init(Context context) {
        if (isInit) {
            return;
        }
        context = context.getApplicationContext();
        Utils.init(context);
        initX5Web(context);
        isInit = true;
    }

    public static MarkChangeListener getMarkChangeListener() {
        return markChangeListener;
    }

    public static void setMarkChangeListener(MarkChangeListener markChangeListener) {
        AppConfig.markChangeListener = markChangeListener;
    }

    public static boolean isNeedInitX5() {
        return needInitX5;
    }

    public static void setNeedInitX5(boolean needInitX5) {
        AppConfig.needInitX5 = needInitX5;
    }

    public static void initX5Web(Context context) {
        if (!EasyPermissions.hasPermissions(context, BASE_X5_PERMISSIONS)) {
            return;
        }
//        HashMap hashMap = new HashMap();
//        hashMap.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
//        hashMap.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
//        QbSdk.initTbsSettings(hashMap);
        setNeedInitX5(false);
        QbSdk.canLoadX5FirstTimeThirdApp(context);
        QbSdk.initX5Environment(context, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.d(TAG, "onCoreInitFinished");
            }

            @Override
            public void onViewInitFinished(boolean initResult) {
                Log.e(TAG, "onViewInitFinished:" + initResult);
            }
        });
    }

    public static synchronized String getFilesDirRootPath(Context context) {
        if (filesDirRootPath == null) {
            File filesDir = new File(context.getFilesDir(), ROOT_PATH);
            filesDirRootPath = filesDir.getAbsolutePath();
            if (!filesDir.exists()) {
                filesDir.mkdirs();
            }
        }
        return filesDirRootPath;
    }

    public static String getDownloadCachePath(Context context, String url, String fileType) {
        String downloadCacheFilePath = null;
        if (url == null) {
            return downloadCacheFilePath;
        }
        String md5 = MD5Tool.stringToMD5(url);
        String rootPath = getFilesDirRootPath(context);
        if (md5 != null && rootPath != null) {
            String downloadCachePath = rootPath + File.separator + DOWNLOAD_CACHE_DIR_NAME;
            File fileDownloadCacheDir = new File(downloadCachePath);
            if (!fileDownloadCacheDir.exists()) {
                fileDownloadCacheDir.mkdir();
            }
            downloadCacheFilePath = downloadCachePath + File.separator + md5 + (fileType == null ? "" : "." + fileType);
        }
        return downloadCacheFilePath;
    }

    public static String getUrlContentType(String urlString) {
        String contentType = "";
        int lastIndex = urlString.lastIndexOf(".");
        if (lastIndex != -1) {
            contentType = urlString.substring(lastIndex);
        }
        return contentType;
    }

    public static boolean isPdf(Context context, String filePath, String fileType) {
        String contentType;
        if (fileType != null && !fileType.equals("")) {
            contentType = "." + fileType;
        } else {
            contentType = AppConfig.getUrlContentType(filePath);
        }
        if (contentType != null && contentType.toLowerCase().equals(".pdf")) {
            return true;
        }
        return false;
    }

    public static void openFile(Context context, String fileKey, String fileName, String filePath, String fileType, String filePassword, String annotation, Boolean isReadOnly,Boolean openAutoSpace,Integer page, String menu, String extra) {
        String contentType;
        if (fileType != null && !fileType.equals("")) {
            contentType = "." + fileType;
        } else {
            contentType = AppConfig.getUrlContentType(filePath);
        }
        if (contentType != null && contentType.toLowerCase().equals(".pdf")) {
            PdfViewerActivity.openPdfViewerActivity(context, fileKey, fileName, filePath, filePassword, annotation, isReadOnly,openAutoSpace,page,menu, extra);
        } else if (contentType != null && contentType.toLowerCase().equals(".ofd")) {
            OFDWebViewActivity.openOFDFile(context, fileName, filePath);
        } else {
            FileViewerActivity.openFileViewerActivity(context, fileName, filePath, fileType, filePassword);
        }
    }

}
