package com.snakeway.file_reader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.LogUtils;
import com.snakeway.file_reader.models.PeopleItem;
import com.snakeway.file_reader.utils.StringUtil;
import com.snakeway.file_reader.views.DrawBoardView;
import com.snakeway.file_reader.views.WaterMarkBgDrawable;
import com.snakeway.pdfviewer.annotation.TextAnnotation;

import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.google.gson.reflect.TypeToken;
import com.snakeway.file_reader.adapters.ReadModeAdapter;
import com.snakeway.file_reader.databinding.ActivityPdfViewerBinding;
import com.snakeway.file_reader.listeners.MarkChangeListener;
import com.snakeway.file_reader.models.BaseBookMarkBean;
import com.snakeway.file_reader.models.BookMarkBean;
import com.snakeway.file_reader.models.ReadModeItem;
import com.snakeway.file_reader.models.MenuItem;
import com.snakeway.file_reader.utils.ActivityManager;
import com.snakeway.file_reader.utils.AnimationUtil;
import com.snakeway.file_reader.utils.PopupWindowUtil;
import com.snakeway.file_reader.utils.ScreenTool;
import com.snakeway.file_reader.utils.TimeUtil;
import com.snakeway.file_reader.views.CircleView;
import com.snakeway.file_reader.views.FileDownloadView;
import com.snakeway.file_reader.views.StatusView;
import com.snakeway.fileviewer.utils.ToastUtil;
import com.snakeway.pdflibrary.PdfDocument;
import com.snakeway.pdfviewer.PDFView;
import com.snakeway.pdfviewer.annotation.AnnotationBean;
import com.snakeway.pdfviewer.annotation.AnnotationListener;
import com.snakeway.pdfviewer.annotation.base.BaseAnnotation;
import com.snakeway.pdfviewer.annotation.base.MarkAreaType;
import com.snakeway.pdfviewer.annotation.pen.AreaPen;
import com.snakeway.pdfviewer.annotation.pen.DeleteLinePen;
import com.snakeway.pdfviewer.annotation.pen.HighLightPen;
import com.snakeway.pdfviewer.annotation.pen.Pen;
import com.snakeway.pdfviewer.annotation.pen.PenBuilder;
import com.snakeway.pdfviewer.annotation.pen.UnderLinePen;
import com.snakeway.pdfviewer.annotation.pen.UnderWaveLinePen;
import com.snakeway.pdfviewer.listener.OnAreaTouchListener;
import com.snakeway.pdfviewer.listener.OnLoadCompleteListener;
import com.snakeway.pdfviewer.listener.OnPageChangeListener;
import com.snakeway.pdfviewer.listener.OnPageErrorListener;
import com.snakeway.pdfviewer.listener.OnSearchTextListener;
import com.snakeway.pdfviewer.model.SearchTextInfo;
import com.snakeway.pdfviewer.model.TextRemarkInfo;
import com.snakeway.pdfviewer.util.FitPolicy;
import com.snakeway.pdfviewer.listener.OnTextRemarkListener;
import com.snakeway.pdfviewer.annotation.pen.TextPen;
import com.snakeway.pdfviewer.model.RenderedBitmap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static com.snakeway.file_reader.ThumbnailActivity.RESULT_PAGE_KEY;

/**
 * @author snakeway
 * @description:
 * @date :2021/3/4 14:18
 */
public class   PdfViewerActivity extends BaseActivity<ActivityPdfViewerBinding> implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    public static final int THUMBNAIL_CHOOSE_REQUEST = 2222;

    public static final float PEN_SCALE_DEFAULT = 0.4f;

    public static final String CACHE_READ_MODE_PATH = "cache_read_mode";

    public static final String EXTRA_IS_SAME_SCREEN_CUSTOM = "extra_is_same_screen_custom";

    public static final String SAVE_ANNOTATION_KEY = "save_annotation_key";
    public static final String IS_ANNOTATION_CHANGE_KEY = "is_annotation_change_key";
    public static final String SAVE_READ_MODE_ITEM_KEY = "save_read_mode_item_key";
    public static final String SAVE_PAGE_KEY = "save_page_key";

    public static final boolean DEFAULT_SWIPE_HORIZONTAL = true;

    private String fileKey;
    private String fileName;
    private String filePath;
    private String filePassword;
    private String annotation;
    private boolean isReadOnly;
    private boolean openAutoSpace;
    private String menu;
    private String extra;

    private boolean isScreenCustom;

    private View.OnClickListener onClickListener;

    private PDFView.Configurator configurator;
    private int pageNumber = 0;
    private String popupWindowOperatingKey;
    private String popupWindowBookMarketKey;

    private Pen.WritePen pen;
    private int selectPenIndex = 0;

    private TextPen textPen;
    private int selectTextPenIndex = 0;

    private boolean isActiveSearch = false;
    private String searchContent;

    private List<BookMarkBean> bookMarkBeans;

    private boolean isAnnotationChange = false;

    private static boolean isPdfViewerActivityOpen = false;

    final List<ReadModeItem> datas = new ArrayList<>();

    final List<MenuItem> menuItems = new ArrayList<>();

    private ReadModeAdapter adapter;

    private String downloadFilePath;

    private boolean isTextRemark = false;

    private  final List<CircleView> circleViews = new ArrayList<>();

    private  Map<String,Object> extraInfo;

    private boolean showPageOperating=true;

    private int penRemarkSize=1;

    private int textRemarkSize=10;

    private int penRemarkColor;

    private boolean isNight = false;

    private String waterMark;

    private final List<Integer> updatePages = new ArrayList<>();
    private final List<AnnotationBean> annotationBeanImages= new ArrayList<>();


    public static PdfViewerActivity getPdfViewerActivity() {
        ArrayList<Object[]> arrayListActivitys = ActivityManager.getInstance().getTheActivitysByClassName(PdfViewerActivity.class.getName());
        if (arrayListActivitys != null && arrayListActivitys.size() > 0 && arrayListActivitys.get(0) != null) {
            return (PdfViewerActivity) (arrayListActivitys.get(0)[1]);
        }
        return null;
    }

    public static void setPdfViewerActivityOpen(boolean isOpen) {
        isPdfViewerActivityOpen = isOpen;
    }

    public static boolean isPdfViewerActivityOpen() {
        return isPdfViewerActivityOpen;
    }

    public static void openPdfViewerActivity(Context context, String fileKey, String fileName, String filePath, String filePassword, String annotation, boolean isReadOnly, boolean openAutoSpace, int page, String menu, String extra) {
        Intent intent = new Intent(context, PdfViewerActivity.class);
        intent.putExtra(AppConfig.FILE_KEY, fileKey);
        intent.putExtra(AppConfig.FILE_NAME_KEY, fileName);
        intent.putExtra(AppConfig.FILE_PATH_KEY, filePath);
        intent.putExtra(AppConfig.FILE_PASSWORD_KEY, filePassword);
        intent.putExtra(AppConfig.FILE_ANNOTATION_KEY, annotation);
        intent.putExtra(AppConfig.IS_READ_ONLY_KEY, isReadOnly);
        intent.putExtra(AppConfig.OPEN_AUTO_SPACE_KEY, openAutoSpace);
        intent.putExtra(AppConfig.PAGE_KEY, page);
        intent.putExtra(AppConfig.MENU, menu);
        intent.putExtra(AppConfig.EXTRA, extra);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        fileKey = intent.getStringExtra(AppConfig.FILE_KEY);
        fileName = intent.getStringExtra(AppConfig.FILE_NAME_KEY);
        filePath = intent.getStringExtra(AppConfig.FILE_PATH_KEY);
        filePath = filePath == null ? "" : filePath;
        filePassword = intent.getStringExtra(AppConfig.FILE_PASSWORD_KEY);
        annotation = intent.getStringExtra(AppConfig.FILE_ANNOTATION_KEY);
        isReadOnly = intent.getBooleanExtra(AppConfig.IS_READ_ONLY_KEY, false);
        openAutoSpace = intent.getBooleanExtra(AppConfig.OPEN_AUTO_SPACE_KEY, false);
        pageNumber = intent.getIntExtra(AppConfig.PAGE_KEY, 0);
        menu = intent.getStringExtra(AppConfig.MENU);
        extra = intent.getStringExtra(AppConfig.EXTRA);


        if (extra!=null){
            extraInfo=GsonUtils.fromJson(extra,new TypeToken<Map<String,Object>>(){}.getType());
        }
        if (extraInfo != null && extraInfo.get(EXTRA_IS_SAME_SCREEN_CUSTOM)!=null&&extraInfo.get(EXTRA_IS_SAME_SCREEN_CUSTOM).toString().equals("true")) {
            isScreenCustom = true;
        }
        isPdfViewerActivityOpen = true;
        if (savedInstanceState != null) {
            annotation = savedInstanceState.getString(SAVE_ANNOTATION_KEY);
            isAnnotationChange = savedInstanceState.getBoolean(IS_ANNOTATION_CHANGE_KEY);
            pageNumber= savedInstanceState.getInt(SAVE_PAGE_KEY);
        } else if (!isScreenCustom && (annotation != null && annotation.equals("local"))) {
            annotation = CacheDiskUtils.getInstance().getString(filePath, null);
        }
        if(extraInfo!=null&&extraInfo.get("drawPaths")!=null){
            String  drawPaths=(String) extraInfo.get("drawPaths");
            saveDrawPaths(drawPaths);
        }
        Log.e("extra:",extra);
        if(extraInfo!=null&&extraInfo.get("waterMark")!=null){
            waterMark= (String) extraInfo.get("waterMark");
        }
        restoreModeItemData();
        initAll();
    }

    @Override
    protected ActivityPdfViewerBinding getViewBinder() {
        return ActivityPdfViewerBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initHandler() {
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void initUi() {
        AppConfig.init(this);
        initPenColorViews();
        onClickListener();
        onSearchListener();
        addSearchKeyListener();
        addSearchObserver();
        initExtraMenus();
    }

    @Override
    public void initConfigUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            int statusBarHeight = ScreenTool.getStatusBarHeight(PdfViewerActivity.this);
            ViewGroup.LayoutParams layoutParams = viewBinding.layoutSearchAppbar.toolbar.getLayoutParams();

            if (layoutParams != null) {
                layoutParams.height = layoutParams.height + statusBarHeight;
            }
            RelativeLayout.MarginLayoutParams relativeLayoutToolbarLayoutParams = (RelativeLayout.MarginLayoutParams) viewBinding.layoutSearchAppbar.relativeLayoutToolbar.getLayoutParams();
            if (relativeLayoutToolbarLayoutParams != null) {
                relativeLayoutToolbarLayoutParams.topMargin = statusBarHeight;
            }
        }
        viewBinding.layoutSearch.relativeLayoutSearch.setVisibility(View.GONE);

        viewBinding.fileDownloadView.setUrlPath(filePath);
        viewBinding.fileDownloadView.setFileType("pdf");
        viewBinding.fileDownloadView.initDownloadCheck(new FileDownloadView.FileLoadListener() {
            @Override
            public void onLoadSuccess(String path) {
                downloadFilePath = path;
                viewBinding.frameLayoutMenu.setBackgroundColor(ContextCompat.getColor(PdfViewerActivity.this, R.color.mainColorHelp_default));
//                viewBinding.frameLayoutMenu.setBackgroundColor(ContextCompat.getColor(PdfViewerActivity.this,R.color.mainColorHelp_default));
                OpenPdfView(path);
            }

            @Override
            public void onLoadError(String error) {
                ToastUtil.showShortToast(PdfViewerActivity.this, error);
            }
        });
        viewBinding.layoutSearchAppbar.textViewToolbarCenter.setText(fileName);
        hideBottomUIMenu();
        if (isReadOnly) {
            viewBinding.layoutSearchAppbar.imageViewSearch.setVisibility(View.INVISIBLE);
            viewBinding.layoutSearchAppbar.textViewSearchCancel.setVisibility(View.INVISIBLE);
//            viewBinding.frameLayoutBookMark.setVisibility(View.INVISIBLE);
            viewBinding.frameLayoutSymbol.setVisibility(View.INVISIBLE);
            viewBinding.frameLayoutRemark.setVisibility(View.INVISIBLE);
        }
        viewBinding.pdfView.post(new Runnable() {
            @Override
            public void run() {
                viewBinding.pdfView.setTextPen(textPen);
            }
        });
    }

    @Override
    public void initHttp() {
    }

    @Override
    public void initOther() {
        requestPermissions();
    }

    private void onClickListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                if (isReadOnly) {
                    if (id == R.id.imageViewBack) {
                        doBack();
                    }
                    return;
                }
//                if (id == R.id.frameLayoutBookMark) {
//                    dismissPopupWindowOperating();
//                    showBottomMenuView(true);
//                }
                if (id == R.id.frameLayoutRemark) {
                    dismissPopupWindowOperating();
                    cancelPenDrawing();
                    showRemarkView(true, false);
                } else if (id == R.id.textViewCancel) {
                    cancelPenDrawing();
                } else if (id == R.id.textViewSave) {
                    savePenDrawing();
                } else if (id == R.id.imageViewBack) {
                    doBack();
                } else if (id == R.id.imageViewSearch) {
                    showSearchView(true);
                } else if (id == R.id.textViewSearchCancel) {
                    viewBinding.pdfView.clearSearchArea();
                    showSearchView(false);
                } else if (id == R.id.frameLayoutCover) {
                    showSearchView(false);
                } else if (id == R.id.imageViewSearchClear) {
                    viewBinding.layoutSearch.editTextSearch.setText("");
                    viewBinding.pdfView.clearSearchArea();
                } else if (id == R.id.textViewSearch) {
                    searchText(searchContent);
                    showSearchView(false);
                } else if (id == R.id.linearLayoutMenuBookMark) {
                    showBookmarks();
                    showBottomMenuView(false);
                } else if (id == R.id.frameLayoutSymbol ) {
                    dismissPopupWindowOperating();
                    showBottomMenuView(false);
                    showRemarkView(true, true);
                } else if (id == R.id.linearLayoutMenuMore) {
                    showReadMode();
                    showBottomMenuView(false);
                } else if (id == R.id.frameLayoutPdfCover) {
                    showBottomMenuView(false);
                } else if (id == R.id.pdfView) {

                }else if (id == R.id.textViewPrePage) {
                    savePenDrawing();
                    int page=pageNumber-1;
                    if(page<0){
                        page=0;
                    }
                    updatePage(page);
                }else if (id == R.id.textViewNextPage) {
                    savePenDrawing();
                    int page=pageNumber+1;
                    updatePage(page);
                }else if(id == R.id.linearLayoutMenuText) {
                    configurator.defaultPage(pageNumber);
                    if (isNight) {
                        configurator.nightMode(false).load();
                        viewBinding.includeLayoutBottomMenu.imageNightViewCrop.setImageResource(R.mipmap.moon_stars);
                        isNight = false;
                    } else {
                        configurator.nightMode(true).load();
                        isNight = true;
                        viewBinding.includeLayoutBottomMenu.imageNightViewCrop.setImageResource(R.mipmap.moon_stars_blue);
                    }
                }else if(id == R.id.linearLayoutMenuPen) {
                    ThumbnailActivity.openThumbnailActivity(PdfViewerActivity.this, downloadFilePath, filePassword, pageNumber, THUMBNAIL_CHOOSE_REQUEST);
                }
            }
        };
        viewBinding.frameLayoutSymbol.setOnClickListener(onClickListener);
        viewBinding.frameLayoutRemark.setOnClickListener(onClickListener);
        viewBinding.includeLayoutPenOperating.textViewCancel.setOnClickListener(onClickListener);
        viewBinding.includeLayoutPenOperating.textViewSave.setOnClickListener(onClickListener);
        viewBinding.layoutSearchAppbar.imageViewBack.setOnClickListener(onClickListener);
        viewBinding.layoutSearchAppbar.imageViewSearch.setOnClickListener(onClickListener);
        viewBinding.layoutSearchAppbar.textViewSearchCancel.setOnClickListener(onClickListener);
        viewBinding.layoutSearch.frameLayoutCover.setOnClickListener(onClickListener);
        viewBinding.layoutSearch.imageViewSearchClear.setOnClickListener(onClickListener);
        viewBinding.layoutSearch.textViewSearch.setOnClickListener(onClickListener);
        viewBinding.includeLayoutBottomMenu.linearLayoutMenuBookMark.setOnClickListener(onClickListener);
        viewBinding.includeLayoutBottomMenu.linearLayoutMenuPen.setOnClickListener(onClickListener);
        viewBinding.includeLayoutBottomMenu.linearLayoutMenuText.setOnClickListener(onClickListener);
        viewBinding.includeLayoutBottomMenu.linearLayoutMenuMore.setOnClickListener(onClickListener);
        viewBinding.frameLayoutPdfCover.setOnClickListener(onClickListener);
        viewBinding.textViewPrePage.setOnClickListener(onClickListener);
        viewBinding.textViewNextPage.setOnClickListener(onClickListener);
        viewBinding.pdfView.setOnPdfViewClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopupWindowOperating();
                showBottomMenuView(true);
            }
        });
    }

    private void initExtraMenus() {
        if (menu == null) {
            return;
        }
        menuItems.clear();
        List<MenuItem> theMenuItems=  getMenuItems(menu);
        for(MenuItem menuItem:theMenuItems){
            if("批示".equals(menuItem.getName())||"查看批示".equals(menuItem.getName())){
                menuItems.add(menuItem);
            }
        }
        if (menuItems.size() > 0) {
            viewBinding.linearLayoutExtraMenu.setVisibility(View.VISIBLE);
            for (int i = 0; i < menuItems.size(); i++) {
                View view;
                if (i % 2 == 0) {
                    view = LayoutInflater.from(this).inflate(R.layout.view_menu_item1, null);
                } else {
                    view = LayoutInflater.from(this).inflate(R.layout.view_menu_item2, null);
                }
                String name=menuItems.get(i).getName();
                TextView textView = view.findViewById(R.id.textView);
                textView.setText(name);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (name){
                            case "批示":
                                String suggestion="";
                                if(extraInfo!=null&&extraInfo.get("suggestion")!=null){
                                    suggestion=extraInfo.get("suggestion").toString();
                                }
                                String drawPaths=getDrawPaths();

                                PopupWindowUtil.showPopupWindowInstruct(PdfViewerActivity.this, viewBinding.linearLayoutExtraMenu.getRootView(),view,drawPaths,suggestion,new PopupWindowUtil.OnShowPopupWindowInstructListener(){
                                    @Override
                                    public void onReset(DrawBoardView drawBoardView, PopupWindow popupWindow, View view) {
                                        drawBoardView.reset();
                                    }

                                    @Override
                                    public void onRecover(DrawBoardView drawBoardView, PopupWindow popupWindow, View view) {
                                        drawBoardView.undo();
                                    }

                                    @Override
                                    public void onSave(DrawBoardView drawBoardView, PopupWindow popupWindow, View view) {
                                        saveDrawPaths(drawBoardView.convertDrawPathsData());
                                    }

                                    @Override
                                    public void onCommit(DrawBoardView drawBoardView, PopupWindow popupWindow, View view) {
                                        String data= drawBoardView.convertDrawPathsData();
                                        saveDrawPaths(data);
                                        notifyUpdateInfo(data);
                                        popupWindow.dismiss();
                                    }

                                    @Override
                                    public void onDismiss(PopupWindow popupWindow, View view) {

                                    }
                                });
                                break;
                            case "查看批示":
                                String peoples=null;
                                if(extraInfo!=null&&extraInfo.get("peoples")!=null){
                                    peoples=extraInfo.get("peoples").toString();
                                }
                                List<PeopleItem> peopleItems=new ArrayList<>();
                                if(peoples!=null){
                                    peopleItems= getPeopleItems(peoples);
                                }
                                PopupWindowUtil.showPopupWindowInstructHistory(PdfViewerActivity.this, viewBinding.linearLayoutExtraMenu.getRootView(),view,getDrawPaths(),peopleItems,new PopupWindowUtil.OnShowPopupWindowInstructHistoryListener(){
                                    @Override
                                    public void onDismiss(PopupWindow popupWindow, View view) {

                                    }
                                });
                                break;
                            default:
                                break;
                        }

                    }
                });
                viewBinding.linearLayoutExtraMenu.addView(view);
            }
        }
    }

    private void showBookmarks() {
        if (popupWindowBookMarketKey != null) {
            return;
        }
        if (bookMarkBeans == null) {
            List<PdfDocument.Bookmark> bookmarks = viewBinding.pdfView.getTableOfContents();
            BaseBookMarkBean.OnBookMarkListener onBookMarkListener = new BaseBookMarkBean.OnBookMarkListener() {
                @Override
                public void onItemClick(BaseBookMarkBean baseBookMarkBean) {
//                if (baseBookMarkBean instanceof BookMarkBean.BookMarkSecondBean.BookMarkThirdBean) {
                    jumpToPageWithAutoFillCheck((int) baseBookMarkBean.pageIndex);
//                }
                }
            };
            bookMarkBeans = BookMarkBean.convertBookMark(bookmarks, onBookMarkListener);
            if (bookMarkBeans.size() == 0) {
                for (int i = 0; i < viewBinding.pdfView.getPageCount(); i++) {
                    BookMarkBean bookMarkBean = new BookMarkBean();
                    bookMarkBean.title = getString(R.string.page_title) + (i + 1);
                    bookMarkBean.pageIndex = i;
                    bookMarkBean.setRemark(viewBinding.pdfView.isHaveAnnotation(i));
                    bookMarkBean.onBookMarkListener = onBookMarkListener;
                    bookMarkBeans.add(bookMarkBean);
                }
            }else{
                for (int i = 0; i < bookMarkBeans.size(); i++) {
                    BookMarkBean bookMarkBean = bookMarkBeans.get(i);
                    bookMarkBean.setRemark(viewBinding.pdfView.isHaveAnnotation((int)bookMarkBean.getPageIndex()));
                }
            }
        }
        popupWindowBookMarketKey = PopupWindowUtil.POPUPWINDOWKEY + TimeUtil.getOnlyTimeWithoutSleep();
        PopupWindowUtil.showPopupWindowBookMarket(this, viewBinding.rootView, null, popupWindowBookMarketKey, bookMarkBeans, new PopupWindowUtil.OnShowPopupWindowBookMarketListener() {
            @Override
            public void onClick(PopupWindow popupWindow, View view) {

            }

            @Override
            public void onDismiss(PopupWindow popupWindow, View view) {
                dismissPopupWindowBookMarket();
            }
        });
    }

    private void hideBottomUIMenu() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window _window = getWindow();
            WindowManager.LayoutParams params = _window.getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
            _window.setAttributes(params);
        }
    }

    private void addSearchKeyListener() {
        viewBinding.layoutSearch.editTextSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    searchText(searchContent);
                    return true;
                }
                return false;
            }
        });
    }

    private void addSearchObserver() {
        viewBinding.rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                viewBinding.rootView.getWindowVisibleDisplayFrame(rect);
                int screenHeight = viewBinding.rootView.getRootView().getHeight();
                int softHeight = screenHeight - rect.bottom;
                if (softHeight > screenHeight / 5) {
                    viewBinding.layoutSearch.relativeLayoutSearch.scrollTo(0, softHeight);
                } else {
                    viewBinding.layoutSearch.relativeLayoutSearch.scrollTo(0, 0);
                }
            }
        });
    }

    private void onSearchListener() {
        viewBinding.layoutSearch.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchContent = viewBinding.layoutSearch.editTextSearch.getText().toString();
                //  searchText(searchContent);
            }
        });
    }

    private void showSearchView(boolean isShow) {
        if (isShow) {
            isActiveSearch = true;
            viewBinding.layoutSearchAppbar.imageViewSearch.setVisibility(View.GONE);
            viewBinding.layoutSearchAppbar.textViewSearchCancel.setVisibility(View.VISIBLE);
            viewBinding.layoutSearch.relativeLayoutSearch.setVisibility(View.VISIBLE);
            ScreenTool.showSoftInput(PdfViewerActivity.this, true, viewBinding.layoutSearch.editTextSearch);
        } else {
            isActiveSearch = false;
            viewBinding.layoutSearchAppbar.imageViewSearch.setVisibility(View.VISIBLE);
            viewBinding.layoutSearchAppbar.textViewSearchCancel.setVisibility(View.GONE);
            viewBinding.layoutSearch.relativeLayoutSearch.setVisibility(View.GONE);
            ScreenTool.showSoftInput(PdfViewerActivity.this, false, viewBinding.layoutSearch.editTextSearch);
        }
    }

    private void searchText(String text) {
        if (text == null || text.equals("")) {
            viewBinding.pdfView.clearSearchArea();
            return;
        }
        viewBinding.pdfView.searchText(text, 3, new OnSearchTextListener() {

            @Override
            public void onResult(SearchTextInfo searchTextInfo) {
                if (searchTextInfo == null) {
                    ToastUtil.showShortToast(PdfViewerActivity.this, getString(R.string.activity_file_viewer_search_result_empty));
                    return;
                }
                if (pageNumber != searchTextInfo.getPage()) {
                    jumpToPageWithAutoFillCheck(searchTextInfo.getPage());
                }
                viewBinding.pdfView.drawSearchArea(searchTextInfo);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void OpenPdfView(String path) {
        openPdf(path, filePassword);
        viewBinding.pdfView.setMinZoom(1F);
        viewBinding.pdfView.setMidZoom(2F);
        viewBinding.pdfView.setMaxZoom(3F);

        AreaPen areaPen = PenBuilder.areaPenBuilder().setColor(getResources().getColor(R.color.areaPen_default)).build();
        areaPen.setCursorColor(getResources().getColor(R.color.areaPen_cursor_default));

        DeleteLinePen deleteLinePen = PenBuilder.deleteLinePenBuilder().setColor(getResources().getColor(R.color.deleteLinePen_default)).build();
        UnderLinePen underLinePen = PenBuilder.underLinePenBuilder().setColor(getResources().getColor(R.color.underLinePen_default)).build();
        UnderWaveLinePen underWaveLinePen = PenBuilder.underWaveLinePenBuilder().setColor(getResources().getColor(R.color.wavesLinePen_default)).build();
        HighLightPen highLightPen = PenBuilder.selectedPenBuilder().setColor(getResources().getColor(R.color.highLightPen_default)).build();
        viewBinding.pdfView.setAreaPen(areaPen);
        viewBinding.pdfView.setDrawAreaPen(MarkAreaType.DELETELINE, deleteLinePen);
        viewBinding.pdfView.setDrawAreaPen(MarkAreaType.UNDERLINE, underLinePen);
        viewBinding.pdfView.setDrawAreaPen(MarkAreaType.UNDERWAVELINE, underWaveLinePen);
        viewBinding.pdfView.setDrawAreaPen(MarkAreaType.HIGHLIGHT, highLightPen);
        viewBinding.pdfView.setSearchAreaPen(PenBuilder.searchAreaPenBuilder().setColor(getResources().getColor(R.color.searchAreaPen_default)).build());


        List<AnnotationBean> annotationBeans= getAnnotationsData(annotation);
        for (AnnotationBean annotationBean : annotationBeans) {
            if (annotationBean.type == AnnotationBean.TYPE_IMAGE) {
                annotationBeanImages.add(annotationBean);
            }
        }

        viewBinding.pdfView.addAnnotations(annotationBeans, false);
        viewBinding.pdfView.setAnnotationListener(new AnnotationListener() {
            @Override
            public void onAnnotationAdd(BaseAnnotation annotation) {
                isAnnotationChange = true;
                addChangePages(annotation.page,false);
                saveAnnotationData();
                Log.e("Annotation", "onAnnotationAdd");
                notifyAddRemoveAnnotationData(annotation, true);
            }

            @Override
            public void onAnnotationRemove(BaseAnnotation annotation) {
                isAnnotationChange = true;
                addChangePages(annotation.page,false);
                saveAnnotationData();
                notifyAddRemoveAnnotationData(annotation, false);
            }

            @Override
            public void onAnnotationPageRemove(int page) {
                isAnnotationChange = true;
                addChangePages(page,true);
                saveAnnotationData();
                notifyAnnotationPageRemove(page);
            }

            @Override
            public void onAnnotationAllRemove() {
                isAnnotationChange = true;
                annotationBeanImages.clear();
                saveAnnotationData();
                notifyAnnotationAllRemove();
            }
        });
        viewBinding.pdfView.setOnAreaTouchListener(new OnAreaTouchListener() {
            @Override
            public void onActiveArea() {
                doVibrate();
            }

            @Override
            public void onAreaSelect(@NonNull RectF startRect, @NonNull RectF endRect, float translateX, float translateY, float targetViewSize, List<MarkAreaType> selectMarkAreaTypes) {
                int[] position = getPopupWindowShowPosition(startRect, endRect, translateX, translateY, targetViewSize);
                showPopupWindowOperating(viewBinding.rootView, position[0], position[1], position[2] == 1, selectMarkAreaTypes);
            }

            @Override
            public void onReTouchStart() {
                visiblePopupWindowOperating(false);
            }

            @Override
            public void onReTouchAreaSelectUpdate(@NonNull RectF startRect, @NonNull RectF endRect, float translateX, float translateY, float targetViewSize, @NonNull List<MarkAreaType> selectMarkAreaTypes) {
                updatePopupWindowPosition(startRect, endRect, translateX, translateY, targetViewSize, selectMarkAreaTypes);
            }

            @Override
            public void onReTouchComplete() {
                visiblePopupWindowOperating(true);
            }

            @Override
            public void onDismiss() {
                dismissPopupWindowOperating();
            }
        });
        viewBinding.pdfView.setOnTextRemarkListener(new OnTextRemarkListener() {
            @Override
            public void onShow(EditText editText) {
                ScreenTool.showSoftInput(PdfViewerActivity.this, true, editText);
            }

            @Override
            public void onSave(EditText editText, TextRemarkInfo textRemarkInfo) {
                ScreenTool.showSoftInput(PdfViewerActivity.this, false, editText);
                showRemarkView(false, true);
            }

            @Override
            public void onDelete(EditText editText, TextAnnotation textAnnotation) {
                ScreenTool.showSoftInput(PdfViewerActivity.this, false, editText);
            }

            @Override
            public void onCancel(EditText editText, boolean isEdit) {
                ScreenTool.showSoftInput(PdfViewerActivity.this, false, editText);
                showRemarkView(false, true);
            }
        });
    }

    void visiblePopupWindowOperating(boolean isVisible) {
        if (popupWindowOperatingKey == null) {
            return;
        }
        PopupWindow popupWindow = getPopupWindow(popupWindowOperatingKey);
        if (popupWindow == null) {
            return;
        }
        popupWindow.getContentView().setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    void updatePopupWindowPosition(@NonNull RectF startRect, @NonNull RectF endRect, float translateX, float translateY, float targetViewSize, List<MarkAreaType> selectMarkAreaTypes) {
        if (popupWindowOperatingKey == null) {
            return;
        }
        PopupWindow popupWindow = getPopupWindow(popupWindowOperatingKey);
        if (popupWindow == null) {
            return;
        }
        View contentView = popupWindow.getContentView();
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        contentView.measure(spec, spec);
        int measuredWidth = contentView.getMeasuredWidth();
        int measuredHeight = contentView.getMeasuredHeight();
        final ImageView imageViewTriangleUp = (ImageView) contentView.findViewById(R.id.imageViewTriangleUp);
        final ImageView imageViewTriangleDown = (ImageView) contentView.findViewById(R.id.imageViewTriangleDown);
        final StatusView statusViewDeleteLine = (StatusView) contentView.findViewById(R.id.statusViewDeleteLine);
        final StatusView statusViewUnderLine = (StatusView) contentView.findViewById(R.id.statusViewUnderLine);
        final StatusView statusViewUnderWaveLine = (StatusView) contentView.findViewById(R.id.statusViewUnderWaveLine);
        final StatusView statusViewHighLight = (StatusView) contentView.findViewById(R.id.statusViewHighLight);
        statusViewDeleteLine.setChecked(false);
        statusViewUnderLine.setChecked(false);
        statusViewUnderWaveLine.setChecked(false);
        statusViewHighLight.setChecked(false);

        if (selectMarkAreaTypes != null) {
            for (MarkAreaType markAreaType : selectMarkAreaTypes) {
                switch (markAreaType) {
                    case DELETELINE:
                        statusViewDeleteLine.setChecked(true);
                        break;
                    case UNDERLINE:
                        statusViewUnderLine.setChecked(true);
                        break;
                    case UNDERWAVELINE:
                        statusViewUnderWaveLine.setChecked(true);
                        break;
                    case HIGHLIGHT:
                        statusViewHighLight.setChecked(true);
                        break;
                    default:
                        break;
                }
            }
        }

        int[] position = getPopupWindowShowPosition(startRect, endRect, translateX, translateY, targetViewSize);
        if (position[2] == 1) {
            imageViewTriangleUp.setVisibility(View.VISIBLE);
            imageViewTriangleDown.setVisibility(View.GONE);
            popupWindow.update(position[0] - measuredWidth / 2, position[1], -1, -1);
        } else {
            imageViewTriangleUp.setVisibility(View.GONE);
            imageViewTriangleDown.setVisibility(View.VISIBLE);
            popupWindow.update(position[0] - measuredWidth / 2, position[1] - measuredHeight, -1, -1);
        }
    }

    private void initPenColorViews() {
        int itemWidth = (int) ((getScreenWidth(this) - getResources().getDimension(R.dimen.layout_pen_operating_right_view_width) - 2 * getResources().getDimension(R.dimen.view_normal_margin_narrow)) / 7);
        int operatingHeight = (int) (getResources().getDimension(R.dimen.layout_operating_height) * 0.8);
        if (operatingHeight < itemWidth) {
            itemWidth = operatingHeight;
        }
        List<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.pen_color_1));
        colors.add(getResources().getColor(R.color.pen_color_2));
        colors.add(getResources().getColor(R.color.pen_color_3));
        colors.add(getResources().getColor(R.color.pen_color_4));
        colors.add(getResources().getColor(R.color.pen_color_5));
        colors.add(getResources().getColor(R.color.pen_color_6));
        colors.add(getResources().getColor(R.color.pen_color_7));
        if(penRemarkColor==0) {
            penRemarkColor = getResources().getColor(R.color.pen_color_1);
        }
        circleViews.clear();
        int borderColor = getResources().getColor(R.color.circleViewBorder_default);
        for (int i = 0; i < colors.size(); i++) {
            CircleView circleView = new CircleView(this);
            circleView.setBackgroundColor(colors.get(i));
            circleView.setInnerCirclePercent(0.6f);
            circleView.setBorderColor(borderColor);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(itemWidth, itemWidth);
            final int index = i;
            circleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatePenColor(circleViews, index, isTextRemark);
                }
            });
            viewBinding.includeLayoutPenOperating.linearLayoutPenColor.addView(circleView, layoutParams);
            if (i == 0) {
                circleView.setChecked(true);
            }
            circleViews.add(circleView);
        }

        viewBinding.includeLayoutPenOperating.textViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String penSize= viewBinding.includeLayoutPenOperating.textViewSizeContent.getText().toString();
                int size= StringUtil.stringToInt(penSize);
                size=size+1;
                if(size>99){
                    size=99;
                }
                if(isTextRemark) {
                    textRemarkSize=size;
                    textPen = PenBuilder.textPenBuilder().setColor(penRemarkColor).setFontSize(textRemarkSize).build();
                    viewBinding.pdfView.setTextMode(textPen);
                }else{
                    penRemarkSize=size;
                    pen = PenBuilder.colorPenBuilder().setColor(penRemarkColor).setPenWidthScale(PEN_SCALE_DEFAULT*penRemarkSize).build();
                    viewBinding.pdfView.setPen(pen);
                }
                viewBinding.includeLayoutPenOperating.textViewSizeContent.setText(String.valueOf(size)+".0");
            }
        });
        viewBinding.includeLayoutPenOperating.textViewReduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String penSize= viewBinding.includeLayoutPenOperating.textViewSizeContent.getText().toString();
                int size= StringUtil.stringToInt(penSize);
                size=size-1;
                if(size<1){
                    size=1;
                }
                if(isTextRemark) {
                    textRemarkSize=size;
                    textPen = PenBuilder.textPenBuilder().setColor(penRemarkColor).setFontSize(textRemarkSize).build();
                    viewBinding.pdfView.setTextMode(textPen);
                }else{
                    penRemarkSize=size;
                    pen = PenBuilder.colorPenBuilder().setColor(penRemarkColor).setPenWidthScale(PEN_SCALE_DEFAULT*penRemarkSize).build();
                    viewBinding.pdfView.setPen(pen);
                }
                viewBinding.includeLayoutPenOperating.textViewSizeContent.setText(String.valueOf(size)+".0");
            }
        });

        pen = PenBuilder.colorPenBuilder().setColor(penRemarkColor).setPenWidthScale(PEN_SCALE_DEFAULT*penRemarkSize).build();
        textPen = PenBuilder.textPenBuilder().setColor(penRemarkColor).setFontSize(textRemarkSize).build();

        if(waterMark!=null) {
            List<String> labels = new ArrayList<>();
            labels.add(waterMark);
            viewBinding.waterMarkBg.setBackgroundDrawable(new WaterMarkBgDrawable(PdfViewerActivity.this, labels, -30, 13));
        }
    }

    private void updatePenColor(List<CircleView> circleViews, int index, boolean isTextRemark) {
        if (circleViews == null || index > circleViews.size() - 1 || index < 0) {
            return;
        }
        for (int i = 0; i < circleViews.size(); i++) {
            CircleView circleView = circleViews.get(i);
            if (i == index) {
                circleView.setChecked(true);
            } else {
                circleView.setChecked(false);
            }
        }
        CircleView circleView = circleViews.get(index);
        int color = circleView.getBackgroundColor();
        if (!isTextRemark) {
            pen = PenBuilder.colorPenBuilder().setColor(color).setPenWidthScale(PEN_SCALE_DEFAULT*penRemarkSize).build();
            viewBinding.pdfView.setPenMode(pen);
            selectPenIndex = index;
        } else {
            textPen = PenBuilder.textPenBuilder().setColor(color).setFontSize(textRemarkSize).build();
            viewBinding.pdfView.setTextMode(textPen);
            selectTextPenIndex = index;
        }
    }

    private void showRemarkView(boolean isShow, boolean isTextRemark) {
        if (isShow) {
//            viewBinding.frameLayoutMenu.setBackgroundColor(ContextCompat.getColor(PdfViewerActivity.this, R.color.background_transparent));
//            viewBinding.frameLayoutBookMark.setVisibility(View.GONE);
            if(showPageOperating&&!isTextRemark) {
                viewBinding.linearLayoutPage.setVisibility(View.VISIBLE);
            }

            viewBinding.frameLayoutSymbol.setVisibility(View.GONE);
            viewBinding.frameLayoutRemark.setVisibility(View.GONE);
            viewBinding.frameLayoutPenOperating.setVisibility(View.VISIBLE);
            viewBinding.frameLayoutPenOperating.setAnimation(AnimationUtil.moveToViewLocation(null));
            if (isTextRemark) {
                viewBinding.includeLayoutPenOperating.textViewSizeContent.setText(String.valueOf(textRemarkSize)+".0");
                viewBinding.includeLayoutPenOperating.textViewSave.setVisibility(View.GONE);
                updatePenColor(circleViews, selectTextPenIndex == -1 ? 0 : selectTextPenIndex, true);
            } else {
                viewBinding.includeLayoutPenOperating.textViewSizeContent.setText(String.valueOf(penRemarkSize)+".0");
                viewBinding.includeLayoutPenOperating.textViewSave.setVisibility(View.VISIBLE);
                updatePenColor(circleViews, selectPenIndex == -1 ? 0 : selectPenIndex, false);
            }
            this.isTextRemark = isTextRemark;
        } else {
//            if (viewBinding.textViewPageNumber.getVisibility() == View.VISIBLE) {
//                viewBinding.frameLayoutMenu.setBackgroundColor(ContextCompat.getColor(PdfViewerActivity.this, R.color.mainColorHelp_default));
//            }
//            viewBinding.frameLayoutBookMark.setVisibility(View.VISIBLE);
            if(showPageOperating) {
                viewBinding.linearLayoutPage.setVisibility(View.GONE);
            }
            viewBinding.frameLayoutSymbol.setVisibility(View.VISIBLE);
            viewBinding.frameLayoutRemark.setVisibility(View.VISIBLE);
            viewBinding.frameLayoutPenOperating.setVisibility(View.GONE);
            viewBinding.frameLayoutPenOperating.setAnimation(AnimationUtil.moveToViewBottom(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            }));
            viewBinding.pdfView.setViewerMode();
        }
    }

    private void showBottomMenuView(boolean isShow) {
        if (isShow) {
            viewBinding.frameLayoutPdfCover.setVisibility(View.VISIBLE);
//            viewBinding.frameLayoutBookMark.setVisibility(View.GONE);
            viewBinding.frameLayoutSymbol.setVisibility(View.GONE);
            viewBinding.frameLayoutRemark.setVisibility(View.GONE);
            viewBinding.frameLayoutBottomMenu.setVisibility(View.VISIBLE);
            viewBinding.frameLayoutBottomMenu.setAnimation(AnimationUtil.moveToViewLocation(null));
        } else {
            viewBinding.frameLayoutPdfCover.setVisibility(View.GONE);
//            viewBinding.frameLayoutBookMark.setVisibility(View.VISIBLE);
            viewBinding.frameLayoutSymbol.setVisibility(View.VISIBLE);
            viewBinding.frameLayoutRemark.setVisibility(View.VISIBLE);
            viewBinding.frameLayoutBottomMenu.setVisibility(View.GONE);
            viewBinding.frameLayoutBottomMenu.setAnimation(AnimationUtil.moveToViewBottom(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            }));
        }
    }


    private void savePenDrawing() {
        viewBinding.pdfView.savePenDrawing();
        showRemarkView(false, false);
    }

    private void cancelPenDrawing() {
        viewBinding.pdfView.cancelPenDrawing();
        showRemarkView(false, false);
    }

    private void clearPenDrawing() {
        viewBinding.pdfView.clearPenDrawing();
        showRemarkView(false, false);
    }

    private void requestPermissions() {
        if (!EasyPermissions.hasPermissions(this, AppConfig.BASE_X5_PERMISSIONS)) {
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, AppConfig.REQUEST_X5_PERMISSIONS, AppConfig.BASE_X5_PERMISSIONS)
                            .setRationale(getString(R.string.permission_request_rationale))
                            .setPositiveButtonText(getString(R.string.permission_request_ok))
                            .setNegativeButtonText(getString(R.string.permission_request_cancel))
                            .build());
        }
    }

    private int[] getPopupWindowShowPosition(RectF startRect, RectF endRect, float translateX, float translateY, float targetViewSize) {
        int[] location = new int[2];
        viewBinding.pdfView.getLocationInWindow(location);

        float startTop = (int) (location[1] + startRect.top + translateY);
        float endBottom = (int) (location[1] + endRect.bottom + translateY);

        float startLeft = (int) (translateX + startRect.left);
        float endRight = (int) (translateX + endRect.right);

        int screenHeight = getScreenHeight(PdfViewerActivity.this);
        int x, y, isUp;
        if (endBottom <= screenHeight * 0.666) {
            x = (int) endRight;
            y = (int) (endBottom + targetViewSize);
            isUp = 1;
        } else {
            x = (int) startLeft;
            y = (int) startTop;
            isUp = 0;
        }
        return new int[]{x, y, isUp};
    }

    void openPdf(@NonNull String filePath, @Nullable String password) {
        if (filePath == null) {
            return;
        }
        configurator = viewBinding.pdfView.fromFile(new File(filePath));
        configurator.password(password)
                .defaultPage(pageNumber)
                .swipeHorizontal(true)
                .pageFling(false)
                .pageSnap(true)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .autoSpacing(true)//开启后批注计算可能还有一些地方可以改进
//                .scrollHandle(new DefaultScrollHandle(this))
//                .spacing(10) // in dp
                .onPageError(this)
                .pageFitPolicy(FitPolicy.WIDTH)
                .setAutoFillWhiteSpace(openAutoSpace)
                .setLoadAfterCheckWhiteSpace(true)
                .setUseMinWhiteSpaceZoom(false)
                .setInitWhiteSpaceOptimization(true)
                .setWhiteSpaceRenderBestQuality(false)
                .setWhiteSpaceRenderThumbnailRatio(0.3f)
                .setWhiteSpaceRenderPageCountWhenOptimization(12)
                .setShowLoadingWhenWhiteSpaceRender(true)
                .setEditTextRemarkFontSize(textRemarkSize)
                .setEditTextNormalColor(getResources().getColor(R.color.edit_text_remark_theme))
                .setEditTextRemarkThemeColor(getResources().getColor(R.color.edit_text_remark_theme))
                .setReadOnlyMode(isReadOnly)
                .setSingleZoom(true)
                .setAnnotationRenderingArea(2);
        initPdfConfig();
        configurator.load();
    }

    void showPopupWindowOperating(View view, int x, int y, boolean isUp, List<MarkAreaType> selectMarkAreaTypes) {
        popupWindowOperatingKey = PopupWindowUtil.POPUPWINDOWKEY + TimeUtil.getOnlyTimeWithoutSleep();
        PopupWindowUtil.showPopupWindowOperating(PdfViewerActivity.this, view.getRootView(), view, popupWindowOperatingKey, x, y, isUp, selectMarkAreaTypes,
                new PopupWindowUtil.OnShowPopupWindowOperatingListener() {
                    @Override
                    public boolean onSelect(PopupWindow popupWindow, View view, MarkAreaType markAreaType) {
                        return viewBinding.pdfView.drawSelectAreaWithMarkAreaType(markAreaType);
                    }

                    @Override
                    public boolean onCancelSelect(PopupWindow popupWindow, View view, MarkAreaType markAreaType) {
                        return viewBinding.pdfView.cancelSelectAreaAnnotation(markAreaType);
                    }

                    @Override
                    public void clearPage(PopupWindow popupWindow, View view) {
                        viewBinding.pdfView.clearPage();
                        popupWindow.dismiss();
                    }

                    @Override
                    public void onDismiss(PopupWindow popupWindow, View view) {
                        viewBinding.pdfView.dismissAreaSelect();
                    }
                });
    }


    private void doVibrate() {
        Vibrator vibrator = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(100);
    }

    private void showReadMode() {
        if (adapter == null) {
            if (datas.size() == 0) {
                datas.add(new ReadModeItem(ReadModeItem.ReadModeType.SINGLE, R.mipmap.read_mode_single, getResources().getString(R.string.popupwindow_read_mode_item_single), DEFAULT_SWIPE_HORIZONTAL));
                datas.add(new ReadModeItem(ReadModeItem.ReadModeType.MULTI, R.mipmap.read_mode_multi, getResources().getString(R.string.popupwindow_read_mode_item_multi), !DEFAULT_SWIPE_HORIZONTAL));
                datas.add(new ReadModeItem(ReadModeItem.ReadModeType.AUTO_FILL_WHITE_SPACE, R.mipmap.read_mode_crop, getResources().getString(R.string.popupwindow_read_mode_item_auto_fill_white_space), openAutoSpace));
                datas.add(new ReadModeItem(ReadModeItem.ReadModeType.NIGHT, R.mipmap.read_mode_night, getResources().getString(R.string.popupwindow_read_mode_item_night), false));
                datas.add(new ReadModeItem(ReadModeItem.ReadModeType.THUMBNAIL, R.mipmap.read_mode_thumbnail, getResources().getString(R.string.popupwindow_read_mode_item_thumbnail), false));
            }
            for(ReadModeItem readModeItem:datas){
                if(readModeItem.getType() == ReadModeItem.ReadModeType.THUMBNAIL || readModeItem.getType() == ReadModeItem.ReadModeType.NIGHT){
                    readModeItem.setShow(false);
                }else if(readModeItem.getType() == ReadModeItem.ReadModeType.AUTO_FILL_WHITE_SPACE){
                    ReadModeItem theReadModeItem = getReadModeItem(ReadModeItem.ReadModeType.MULTI, datas);
                    if(theReadModeItem.isCheck()){
                        readModeItem.setShow(false);
                    }
                }
            }
            adapter = new ReadModeAdapter(PdfViewerActivity.this, datas);
        }
        String popupWindowReadModeKey = PopupWindowUtil.POPUPWINDOWKEY + TimeUtil.getOnlyTimeWithoutSleep();
        PopupWindowUtil.showPopupWindowReadMode(this, viewBinding.rootView, null, popupWindowReadModeKey, adapter, new PopupWindowUtil.OnShowPopupWindowReadModeListener() {
            @Override
            public void onSelect(int position, ReadModeAdapter readModeAdapter, PopupWindow popupWindow, View view) {
                if (position > datas.size() - 1) {
                    return;
                }
                ReadModeItem readModeItem = datas.get(position);
                if (readModeItem.isCheck() && (readModeItem.getType() == ReadModeItem.ReadModeType.SINGLE || readModeItem.getType() == ReadModeItem.ReadModeType.MULTI)) {
                    return;
                }
                configurator.defaultPage(pageNumber);
                if (readModeItem.getType() == ReadModeItem.ReadModeType.SINGLE) {
                    getReadModeItem(ReadModeItem.ReadModeType.MULTI, datas).setCheck(false);
                    getReadModeItem(ReadModeItem.ReadModeType.AUTO_FILL_WHITE_SPACE, datas).setShow(true);
                    ReadModeItem autoFillWhiteSpaceReadModeItem = getReadModeItem(ReadModeItem.ReadModeType.AUTO_FILL_WHITE_SPACE, datas);
                    configurator.swipeHorizontal(true).setAutoFillWhiteSpace(autoFillWhiteSpaceReadModeItem.isCheck()).load();
                    readModeItem.setCheck(true);
                } else if (readModeItem.getType() == ReadModeItem.ReadModeType.MULTI) {
                    getReadModeItem(ReadModeItem.ReadModeType.SINGLE, datas).setCheck(false);
                    getReadModeItem(ReadModeItem.ReadModeType.AUTO_FILL_WHITE_SPACE, datas).setShow(false);
                    configurator.swipeHorizontal(false).setAutoFillWhiteSpace(false).load();
                    readModeItem.setCheck(true);
                } else if (readModeItem.getType() == ReadModeItem.ReadModeType.NIGHT) {
                    if (readModeItem.isCheck()) {
                        configurator.nightMode(false).load();
                        readModeItem.setCheck(false);
                    } else {
                        configurator.nightMode(true).load();
                        readModeItem.setCheck(true);
                    }
                } else if (readModeItem.getType() == ReadModeItem.ReadModeType.AUTO_FILL_WHITE_SPACE) {
                    if (readModeItem.isCheck()) {//之前已经选中过了
                        configurator.setAutoFillWhiteSpace(false).load();
                        readModeItem.setCheck(false);
                    } else {
                        configurator.setAutoFillWhiteSpace(true).load();
                        readModeItem.setCheck(true);
                    }
                } else if (readModeItem.getType() == ReadModeItem.ReadModeType.THUMBNAIL) {
                    if (downloadFilePath == null) {
                        return;
                    }
                    ThumbnailActivity.openThumbnailActivity(PdfViewerActivity.this, downloadFilePath, filePassword, pageNumber, THUMBNAIL_CHOOSE_REQUEST);
                    popupWindow.dismiss();
                }
                readModeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDismiss(PopupWindow popupWindow, View view) {

            }
        });
    }

    private ReadModeItem getReadModeItem(ReadModeItem.ReadModeType readModeType, List<ReadModeItem> allReadModeItems) {
        for (ReadModeItem readModeItem : allReadModeItems) {
            if (readModeItem.getType() == readModeType) {
                return readModeItem;
            }
        }
        return null;
    }


    void dismissPopupWindowBookMarket() {
        if (popupWindowBookMarketKey == null) {
            return;
        }
        PopupWindow popupWindow = getPopupWindow(popupWindowBookMarketKey);
        if (popupWindow == null) {
            return;
        }
        popupWindow.dismiss();
        popupWindowBookMarketKey = null;
    }

    void dismissPopupWindowOperating() {
        if (popupWindowOperatingKey == null) {
            return;
        }
        PopupWindow popupWindow = getPopupWindow(popupWindowOperatingKey);
        if (popupWindow == null) {
            return;
        }
        popupWindow.dismiss();
        popupWindowOperatingKey = null;
    }

    private List<MenuItem> getMenuItems(String data) {
        List<MenuItem> menuItems = new ArrayList<>();
        try {
            if (ObjectUtils.isNotEmpty(data)) {
                menuItems = GsonUtils.fromJson(data, new TypeToken<List<MenuItem>>() {
                }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return menuItems;
    }

    private List<PeopleItem> getPeopleItems(String data) {
        List<PeopleItem> peopleItems = new ArrayList<>();
        try {
            if (ObjectUtils.isNotEmpty(data)) {
                peopleItems = GsonUtils.fromJson(data, new TypeToken<List<PeopleItem>>() {
                }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return peopleItems;
    }


    private void saveDrawPaths(String data) {
        CacheDiskUtils.getInstance().put("draw_path_key"+filePath, data);
    }

    private String getDrawPaths() {
        return CacheDiskUtils.getInstance().getString("draw_path_key"+filePath, null);
    }

    private AnnotationBean getAnnotationData(String annotation) {
        AnnotationBean annotationBean = null;
        if (annotation == null || annotation.equals("")) {
            return null;
        }
        try {
            annotationBean = GsonUtils.fromJson(annotation, new TypeToken<AnnotationBean>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return annotationBean;
    }

    private List<AnnotationBean> getAnnotationsData(String annotation) {
        List<AnnotationBean> annotationBeans = new ArrayList<>();
        try {
            if (ObjectUtils.isNotEmpty(annotation)) {
                annotationBeans = GsonUtils.fromJson(annotation, new TypeToken<List<AnnotationBean>>() {
                }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return annotationBeans;
    }

    private void notifyAddRemoveAnnotationData(BaseAnnotation annotation, boolean isAdd) {
        String result = GsonUtils.toJson(viewBinding.pdfView.getOptimizationAnnotation(annotation));
        MarkChangeListener markChangeListener = AppConfig.getMarkChangeListener();
        if (markChangeListener != null) {
            if (isAdd) {
                markChangeListener.addAnnotation(fileKey, result, isScreenCustom, extra);
            } else {
                markChangeListener.removeAnnotation(fileKey, result, isScreenCustom, extra);
            }
        }
    }

    private void notifyAnnotationPageRemove(int page) {
        MarkChangeListener markChangeListener = AppConfig.getMarkChangeListener();
        if (markChangeListener != null) {
            markChangeListener.annotationPageRemove(fileKey, String.valueOf(page), isScreenCustom, extra);
        }
    }

    private void notifyAnnotationAllRemove() {
        MarkChangeListener markChangeListener = AppConfig.getMarkChangeListener();
        if (markChangeListener != null) {
            markChangeListener.annotationAllRemove(fileKey, "", isScreenCustom, extra);
        }
    }

    private void notifyUpdateInfo(String data) {
        MarkChangeListener markChangeListener = AppConfig.getMarkChangeListener();
        if (markChangeListener != null) {
            markChangeListener.updateInfo(fileKey, data, isScreenCustom, extra);
        }
    }

    private void addChangePages(int page,boolean justRemove){
        for (int i = 0; i < annotationBeanImages.size(); i++) {
            AnnotationBean annotationBean=annotationBeanImages.get(i);
            if (annotationBean.page==page){
                annotationBeanImages.remove(annotationBean);
                break;
            }
        }
        if(!justRemove) {
            RenderedBitmap renderedBitmap = viewBinding.pdfView.getRenderingBitmapWithBase64(page, 540);
            AnnotationBean annotationBean = new AnnotationBean(null, GsonUtils.toJson(renderedBitmap), page, UUID.randomUUID().toString(), AnnotationBean.TYPE_IMAGE);
            annotationBeanImages.add(annotationBean);
        }
    }

    private void saveAnnotationData() {
        if (isScreenCustom) {
            return;
        }
        String result = GsonUtils.toJson(viewBinding.pdfView.getAllAnnotation());
        CacheDiskUtils.getInstance().put(filePath, result);
    }

    private void notifyAnnotationDataChange() {
        List<AnnotationBean> annotationBeans= viewBinding.pdfView.getAllOptimizationAnnotation();

        annotationBeans.addAll(annotationBeanImages);

        String result = GsonUtils.toJson(annotationBeans);
        MarkChangeListener markChangeListener = AppConfig.getMarkChangeListener();
        if (markChangeListener != null) {
            markChangeListener.updateAnnotation(fileKey, result, isScreenCustom, extra);
        }
    }

    private void setPageNumber(int page, int pageCount) {
        if (viewBinding.textViewPageNumber.getVisibility() != View.VISIBLE) {
            viewBinding.textViewPageNumber.setVisibility(View.VISIBLE);
        }
        viewBinding.textViewPageNumber.setText(String.format("%s / %s", page + 1, pageCount));
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setPageNumber(page, pageCount);
//        if (isActiveSearch && searchContent != null && !searchContent.equals("")) {
//            searchText(searchContent);
//        }
        MarkChangeListener markChangeListener = AppConfig.getMarkChangeListener();
        if (markChangeListener != null) {
            markChangeListener.updatePage(fileKey, String.valueOf(page), isScreenCustom, extra);
        }
    }

    @Override
    public void loadComplete(int nbPages) {
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(getTag(), "Cannot load page " + page);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, new EasyPermissions.PermissionCallbacks() {
            @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            }

            @Override
            public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
                switch (requestCode) {
                    case AppConfig.REQUEST_X5_PERMISSIONS:
                        if (perms.size() == AppConfig.BASE_X5_PERMISSIONS.length) {
                            if (AppConfig.isNeedInitX5()) {
                                AppConfig.initX5Web(PdfViewerActivity.this);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
                if (EasyPermissions.somePermissionPermanentlyDenied(PdfViewerActivity.this, perms)) {
                    showAppSettingDialog();
                }
            }
        });
    }

    private void showAppSettingDialog() {
        new AppSettingsDialog.Builder(PdfViewerActivity.this).setRationale(getString(R.string.permission_request_open_setting_tips)).build().show();
    }


    private void showSaveDialog() {
        if (!isAnnotationChange) {
            finish();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifyAnnotationDataChange();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setMessage(getString(R.string.remark_change_tips));
        builder.setTitle(getString(R.string.tips));
        builder.show();
    }

    private void showCustomExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setMessage(getString(R.string.custom_exit_tips));
        builder.setTitle(getString(R.string.tips));
        builder.show();
    }

    public void updatePage(int page) {
        jumpToPageWithAutoFillCheck(page);
    }

    public void addRemoveAnnotation(String annotation, boolean isAdd) {
        if (annotation == null || annotation.equals("")) {
            return;
        }
        List<AnnotationBean> annotationBeans = new ArrayList<>();
        AnnotationBean annotationBean = getAnnotationData(annotation);
        if (annotationBean == null) {
            return;
        }
        Log.e("addRemoveAnnotation", annotation);
        annotationBeans.add(annotationBean);
        if (isAdd) {
            viewBinding.pdfView.addAnnotations(annotationBeans, true);
        } else {
            viewBinding.pdfView.removeAnnotations(annotationBeans, true);
        }
    }

    public void addRemoveAnnotations(String annotations, boolean isAdd) {
        if (annotations == null || annotations.equals("")) {
            return;
        }
        Log.e("addRemoveAnnotations", annotations);
        List<AnnotationBean> annotationBeans = getAnnotationsData(annotations);
        if (annotationBeans.size() > 0) {
            if (isAdd) {
                viewBinding.pdfView.addAnnotations(annotationBeans, true);
            } else {
                viewBinding.pdfView.removeAnnotations(annotationBeans, true);
            }
        }
    }

    public void closePdfActivity() {
        finish();
    }

    @Override
    public void doBack() {
        super.doBack();
        if (extra != null && extra.equals(EXTRA_IS_SAME_SCREEN_CUSTOM)) {
            showCustomExitDialog();
        } else {
            showSaveDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == THUMBNAIL_CHOOSE_REQUEST) {
            int page = intent.getIntExtra(RESULT_PAGE_KEY, -1);
            if (page != -1) {
                jumpToPageWithAutoFillCheck(page);
            }
        }
    }

    private void jumpToPageWithAutoFillCheck(int page) {
        viewBinding.pdfView.addAnimationEndRunnable("jumpToPage", new Runnable() {
            @Override
            public void run() {
                viewBinding.pdfView.jumpToPageWithAutoFillCheck(page);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(SAVE_ANNOTATION_KEY, GsonUtils.toJson(viewBinding.pdfView.getAllOptimizationAnnotation()));
        savedInstanceState.putBoolean(IS_ANNOTATION_CHANGE_KEY, isAnnotationChange);
        savedInstanceState.putInt(SAVE_PAGE_KEY, pageNumber);

        saveReadModeItemData();

    }

    private void saveReadModeItemData() {
        String result = GsonUtils.toJson(datas);
        CacheDiskUtils.getInstance().put(CACHE_READ_MODE_PATH, result);
    }

    private void restoreModeItemData() {
        String text = CacheDiskUtils.getInstance().getString(CACHE_READ_MODE_PATH, null);
        try {
            if (ObjectUtils.isNotEmpty(text)) {
                List<ReadModeItem> readModeItems = GsonUtils.fromJson(text, new TypeToken<List<ReadModeItem>>() {
                }.getType());
                datas.clear();
                datas.addAll(readModeItems);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void initPdfConfig() {
        if (datas.size() == 0) {
            return;
        }
        configurator.defaultPage(pageNumber);
        for (int i = 0; i < datas.size(); i++) {
            ReadModeItem readModeItem = datas.get(i);
            if (readModeItem.getType() == ReadModeItem.ReadModeType.SINGLE) {
                if (readModeItem.isCheck()) {
                    ReadModeItem autoFillWhiteSpaceReadModeItem = getReadModeItem(ReadModeItem.ReadModeType.AUTO_FILL_WHITE_SPACE, datas);
                    configurator.swipeHorizontal(true).setAutoFillWhiteSpace(autoFillWhiteSpaceReadModeItem.isCheck());
                }
            } else if (readModeItem.getType() == ReadModeItem.ReadModeType.MULTI) {
                if (readModeItem.isCheck()) {
                    configurator.swipeHorizontal(false).setAutoFillWhiteSpace(false);
                }
            } else if (readModeItem.getType() == ReadModeItem.ReadModeType.NIGHT) {
                if (readModeItem.isCheck()) {
                    configurator.nightMode(true);
                } else {
                    configurator.nightMode(false);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (viewBinding.pdfView != null) {
            saveAnnotationData();
            viewBinding.pdfView.recycle();
        }
        saveReadModeItemData();

        MarkChangeListener markChangeListener = AppConfig.getMarkChangeListener();
        if (markChangeListener != null) {
            markChangeListener.close(fileKey, "", isScreenCustom, extra);
        }
        isPdfViewerActivityOpen = false;
        super.onDestroy();
    }

}
