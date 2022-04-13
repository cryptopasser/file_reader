package com.snakeway.file_reader;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.snakeway.file_reader.adapters.ImageThumbnailAdapter;
import com.snakeway.file_reader.adapters.ImageThumbnailRecyclerAdapter;
import com.snakeway.file_reader.databinding.ActivityThumbnailBinding;
import com.snakeway.file_reader.utils.BitmapMemoryCacheHelper;
import com.snakeway.pdflibrary.util.SizeF;
import com.snakeway.pdfviewer.PDFView;
import com.snakeway.pdfviewer.RenderingCustomHandler;
import com.snakeway.pdfviewer.annotation.AnnotationListener;
import com.snakeway.pdfviewer.annotation.base.BaseAnnotation;
import com.snakeway.pdfviewer.annotation.base.MarkAreaType;
import com.snakeway.pdfviewer.annotation.pen.AreaPen;
import com.snakeway.pdfviewer.annotation.pen.DeleteLinePen;
import com.snakeway.pdfviewer.annotation.pen.HighLightPen;
import com.snakeway.pdfviewer.annotation.pen.PenBuilder;
import com.snakeway.pdfviewer.annotation.pen.UnderLinePen;
import com.snakeway.pdfviewer.annotation.pen.UnderWaveLinePen;
import com.snakeway.pdfviewer.listener.OnAreaTouchListener;
import com.snakeway.pdfviewer.listener.OnLoadCompleteListener;
import com.snakeway.pdfviewer.util.FitPolicy;
import com.snakeway.file_reader.utils.ScreenTool;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.GridLayoutManager;

import com.snakeway.file_reader.databinding.ActivityThumbnailBinding;

import android.app.Activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class ThumbnailActivity extends BaseActivity<ActivityThumbnailBinding> implements OnLoadCompleteListener {
    public static String RESULT_PAGE_KEY = "page";

    private View.OnClickListener onClickListener;
    private PDFView.Configurator configurator;
    private BitmapMemoryCacheHelper bitmapMemoryCacheHelper = new BitmapMemoryCacheHelper();

    private String filePath;
    private String filePassword;
    private int selectPage;

    public static void openThumbnailActivity(Activity activity, String filePath, String filePassword, int selectPage, int requestCode) {
        Intent intent = new Intent(activity, ThumbnailActivity.class);
        intent.putExtra(AppConfig.FILE_PATH_KEY, filePath);
        intent.putExtra(AppConfig.FILE_PASSWORD_KEY, filePassword);
        intent.putExtra(AppConfig.SELECT_PAGE_KEY, selectPage);
        if (requestCode == -1) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        filePath = intent.getStringExtra(AppConfig.FILE_PATH_KEY);
        filePassword = intent.getStringExtra(AppConfig.FILE_PASSWORD_KEY);
        selectPage = intent.getIntExtra(AppConfig.SELECT_PAGE_KEY, 0);
        initAll();
    }

    @Override
    protected ActivityThumbnailBinding getViewBinder() {
        return ActivityThumbnailBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initHandler() {
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void initUi() {
        onClickListener();
    }

    @Override
    public void initConfigUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            int statusBarHeight = ScreenTool.getStatusBarHeight(ThumbnailActivity.this);
            RelativeLayout.MarginLayoutParams relativeLayoutToolbarLayoutParams = (RelativeLayout.MarginLayoutParams) viewBinding.relativeLayoutToolbar.getLayoutParams();
            if (relativeLayoutToolbarLayoutParams != null) {
                relativeLayoutToolbarLayoutParams.topMargin = statusBarHeight;
            }
        }
        OpenPdfView(filePath);
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
                if (id == R.id.imageViewBack) {
                    doBack();
                }
            }
        };
        viewBinding.imageViewBack.setOnClickListener(onClickListener);
    }

    private void OpenPdfView(String path) {
        openPdf(path, filePassword);
        viewBinding.pdfView.setMinZoom(1F);
        viewBinding.pdfView.setMidZoom(1F);
        viewBinding.pdfView.setMaxZoom(1F);
    }


    private void openPdf(@NonNull String filePath, @Nullable String password) {
        configurator = viewBinding.pdfView.fromFile(new File(filePath));
        configurator.password(password)
                .swipeHorizontal(true)
                .pageSnap(false)
                .onLoad(this)
                .pageFitPolicy(FitPolicy.HEIGHT)
                .enableAnnotationRendering(false)
                .linkHandler(null)
                .setSupportCustomRendering(true)
                .load();
    }

    public void choosePage(int page) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_PAGE_KEY, page);
        setResult(RESULT_OK, intent);
        finish();
    }


    private List<RenderingCustomHandler.RenderingCustomPageInfo> getAllRenderingCustomPageInfos() {
        int pageCount = viewBinding.pdfView.getPageCount();
        List<RenderingCustomHandler.RenderingCustomPageInfo> pages = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            SizeF size = viewBinding.pdfView.getPageSize(i);
            pages.add(new RenderingCustomHandler.RenderingCustomPageInfo(i, size.getWidth(), size.getHeight(), new RectF(0, 0, 1, 1)));
        }
        return pages;
    }

    public void getRenderingImages(List<RenderingCustomHandler.RenderingCustomPageInfo> pages, RenderingCustomHandler.OnRenderingCustomListener onRenderingCustomListener) {
        if (pages == null) {
            return;
        }
        RenderingCustomHandler.RenderingCustomTask renderingCustomTask = new RenderingCustomHandler.RenderingCustomTask(pages, true, false, false, onRenderingCustomListener);
        renderingCustomTask.thumbnailRatio = 0.2f;
        viewBinding.pdfView.addRenderingCustomTask(renderingCustomTask);
    }

    @Override
    public void doBack() {
        super.doBack();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onDestroy() {
        if (viewBinding.pdfView != null) {
            viewBinding.pdfView.recycle();
        }
        super.onDestroy();
    }

    @Override
    public void loadComplete(int nbPages) {
        viewBinding.gridView.setLayoutManager(new GridLayoutManager(this, 3));
        viewBinding.gridView.setAdapter(new ImageThumbnailRecyclerAdapter(ThumbnailActivity.this, bitmapMemoryCacheHelper, getAllRenderingCustomPageInfos(), selectPage));
//        viewBinding.gridView.setAdapter(new ImageThumbnailAdapter(ThumbnailActivity.this, bitmapMemoryCacheHelper, getAllRenderingCustomPageInfos(), selectPage));
    }
}
