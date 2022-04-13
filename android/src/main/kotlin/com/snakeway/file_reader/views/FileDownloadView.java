package com.snakeway.file_reader.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.snakeway.file_reader.AppConfig;
import com.snakeway.file_reader.R;
import com.snakeway.file_reader.utils.CoroutineHelper;
import com.snakeway.file_reader.utils.HttpURLConnectionTool;
import com.snakeway.fileviewer.utils.ToastUtil;

import java.io.File;

/**
 * @author snakeway
 * @description:
 * @date :2021/4/21 16:20
 */
public class FileDownloadView extends FrameLayout {
    final private CoroutineHelper coroutineHelper = new CoroutineHelper();
    private Context context;
    private boolean activeDownloadCheck;
    private String urlPath;
    private String fileType;
    private FileLoadListener fileLoadListener;

    public FileDownloadView(Context context) {
        this(context, null, 0);
    }

    public FileDownloadView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FileDownloadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FileDownloadView, defStyleAttr, 0);
        activeDownloadCheck = typedArray.getBoolean(R.styleable.FileDownloadView_activeDownloadCheck, true);
        urlPath = typedArray.getString(R.styleable.FileDownloadView_urlPath);
        init();
    }

    private void init() {
        initView();
    }

    private void initView() {
        if (!isInEditMode()) {
            LayoutInflater.from(getContext()).inflate(R.layout.view_loading, this, true);
        }
        setVisibility(GONE);
    }

    public void initDownloadCheck(FileLoadListener fileLoadListener) {
        this.fileLoadListener = fileLoadListener;
        if (activeDownloadCheck) {
            if (urlPath == null) {
                ToastUtil.showShortToast(context, context.getString(R.string.file_not_exists));
                return;
            }
            boolean isHttp = false;
            boolean isFile = false;
            if (urlPath.startsWith("http")) {
                isHttp = true;
            } else if (urlPath.startsWith("file")) {
                isFile = true;
            }
            if (!isHttp && !isFile) {
                ToastUtil.showShortToast(context, context.getString(R.string.file_format_error));
                return;
            }
            if (isHttp) {
                downloadFile(urlPath);
            } else if (isFile) {
                if (fileLoadListener != null) {
                    String path = urlPath.replace("file://", "");
                    fileLoadListener.onLoadSuccess(path);
                }
            }
        }
    }


    private void downloadFile(final String fileUrl) {
        if (fileUrl == null) {
            return;
        }
        String path = AppConfig.getDownloadCachePath(context, fileUrl, fileType);
        File file = new File(path);
        if (file.exists()) {
            if (fileLoadListener != null) {
                fileLoadListener.onLoadSuccess(path);
            }
            return;
        }
        setVisibility(VISIBLE);
        coroutineHelper.launch(new CoroutineHelper.OnCoroutineListener<String>() {
            @Override
            public String runOnIo() {
                String result = HttpURLConnectionTool.downloadFile(fileUrl, path);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            public void overRunOnMain(String path) {
                setVisibility(GONE);
                if (path == null) {
                    if (fileLoadListener != null) {
                        fileLoadListener.onLoadError(context.getString(R.string.file_download_error));
                    }
                    return;
                }
                if (fileLoadListener != null) {
                    fileLoadListener.onLoadSuccess(path);
                }
            }
        });
    }


    public boolean isActiveDownloadCheck() {
        return activeDownloadCheck;
    }

    public void setActiveDownloadCheck(boolean activeDownloadCheck) {
        this.activeDownloadCheck = activeDownloadCheck;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public FileLoadListener getFileLoadListener() {
        return fileLoadListener;
    }

    public void setFileLoadListener(FileLoadListener fileLoadListener) {
        this.fileLoadListener = fileLoadListener;
    }

    public interface FileLoadListener {
        void onLoadSuccess(String path);

        void onLoadError(String error);
    }
}
