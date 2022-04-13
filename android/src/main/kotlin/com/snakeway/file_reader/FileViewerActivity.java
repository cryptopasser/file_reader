package com.snakeway.file_reader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;

import com.snakeway.file_reader.databinding.ActivityFileViewerBinding;
import com.snakeway.file_reader.views.FileDownloadView;
import com.snakeway.fileviewer.utils.ToastUtil;
import com.tencent.smtt.sdk.TbsReaderView;

import static com.snakeway.fileviewer.utils.FileUtil.parseFormat;

/**
 * @author snakeway
 * @description:
 * @date :2021/3/8 17:18
 */
public class FileViewerActivity extends BaseActivity<ActivityFileViewerBinding> {

    private String fileName;
    private String filePath;
    private String fileType;
    private String filePassword;

    private TbsReaderView tbsReaderView;
    private View.OnClickListener onClickListener;

    private String localFilePath;

    public static void openFileViewerActivity(Context context, String fileName, String filePath, String fileType, String filePassword) {
        Intent intent = new Intent(context, FileViewerActivity.class);
        intent.putExtra(AppConfig.FILE_NAME_KEY, fileName);
        intent.putExtra(AppConfig.FILE_PATH_KEY, filePath);
        intent.putExtra(AppConfig.FILE_TYPE_KEY, fileType);
        intent.putExtra(AppConfig.FILE_PASSWORD_KEY, filePassword);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        fileName = intent.getStringExtra(AppConfig.FILE_NAME_KEY);
        filePath = intent.getStringExtra(AppConfig.FILE_PATH_KEY);
        fileType = intent.getStringExtra(AppConfig.FILE_TYPE_KEY);
        filePath = filePath == null ? "" : filePath;
        filePassword = intent.getStringExtra(AppConfig.FILE_PASSWORD_KEY);
        initAll();
    }

    @Override
    protected ActivityFileViewerBinding getViewBinder() {
        return ActivityFileViewerBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initHandler() {
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void initUi() {
        AppConfig.init(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(fileName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tbsReaderView = new TbsReaderView(FileViewerActivity.this, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {

            }
        });
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        tbsReaderView.setLayoutParams(layoutParams);
        viewBinding.frameLayoutRoot.addView(tbsReaderView);
        viewBinding.frameLayoutRoot.setVisibility(View.INVISIBLE);
        onClickListener();
    }

    @Override
    public void initConfigUi() {
        viewBinding.fileDownloadView.setUrlPath(filePath);
        viewBinding.fileDownloadView.setFileType(fileType);
        viewBinding.fileDownloadView.initDownloadCheck(new FileDownloadView.FileLoadListener() {
            @Override
            public void onLoadSuccess(String path) {
                localFilePath = path;
                initTbsReaderView();
            }

            @Override
            public void onLoadError(String error) {
                ToastUtil.showShortToast(FileViewerActivity.this, error);
            }
        });
    }

    @Override
    public void initHttp() {
    }

    @Override
    public void initOther() {

    }

    private void onClickListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                if (id == R.id.textViewRetry) {
                    displayFile(localFilePath);
                }
            }
        };
        viewBinding.textViewRetry.setOnClickListener(onClickListener);
    }

    private void initTbsReaderView() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewBinding.frameLayoutRoot.setVisibility(View.VISIBLE);
                viewBinding.linearLayoutContent.setVisibility(View.VISIBLE);
                displayFile(localFilePath);
            }
        }, 600);
    }

    private void displayFile(String filePath) {
        if (filePath == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        bundle.putString("tempPath", getFilesDir().getPath());
        boolean result = tbsReaderView.preOpen(parseFormat(filePath), false);
        if (result) {
            tbsReaderView.openFile(bundle);
            tbsReaderView.setVisibility(View.VISIBLE);
            viewBinding.linearLayoutContent.setVisibility(View.GONE);
        } else {
            tbsReaderView.setVisibility(View.GONE);
            viewBinding.linearLayoutContent.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void doBack() {
        super.doBack();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        tbsReaderView.onStop();
    }
}
