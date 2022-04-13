package com.snakeway.file_reader.utils;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.snakeway.file_reader.BaseActivity;
import com.snakeway.file_reader.R;
import com.snakeway.file_reader.adapters.PeopleItemAdapter;
import com.snakeway.file_reader.adapters.ReadModeAdapter;
import com.snakeway.file_reader.models.BookMarkBean;
import com.snakeway.file_reader.models.PeopleItem;
import com.snakeway.file_reader.views.DrawBoardView;
import com.snakeway.file_reader.views.StatusView;
import com.snakeway.file_reader.views.TreeControlView;
import com.snakeway.pdfviewer.annotation.base.MarkAreaType;

import java.lang.reflect.Method;
import java.util.List;
import android.graphics.Paint;
import android.graphics.Rect;


/**
 * @author snakeway
 * @description:
 * @date :2021/3/10 9:14
 */
public class PopupWindowUtil {
    public static final String POPUPWINDOWKEY = "popupWindowKey";

    public interface OnShowPopupWindowOperatingListener {

        boolean onSelect(PopupWindow popupWindow, View view, MarkAreaType markAreaType);

        boolean onCancelSelect(PopupWindow popupWindow, View view, MarkAreaType markAreaType);

        void clearPage(PopupWindow popupWindow, View view);

        void onDismiss(PopupWindow popupWindow, View view);

    }

    public static PopupWindow showPopupWindowOperating(final BaseActivity baseActivity, View view, final View needEnableView, final String key, final int x, final int y, final boolean isUp, final List<MarkAreaType> selectMarkAreaTypes, final OnShowPopupWindowOperatingListener onShowPopupWindowOperatingListener) {
        if (view.getWindowToken() == null) {
            return null;
        }
        if (needEnableView != null) {
            needEnableView.setEnabled(false);
        }
        LayoutInflater layoutInflater = baseActivity.getLayoutInflater();
        View popupWindowView = layoutInflater.inflate(R.layout.popupwindow_operating, null);
        final String popupWindowKey = key != null ? key : (POPUPWINDOWKEY + TimeUtil.getOnlyTimeWithoutSleep());
        final PopupWindow popupWindow = new PopupWindow(popupWindowView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setPopupWindowTouchModal(popupWindow, false);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
//                WindowManager.LayoutParams windowManagerLayoutParams = baseActivity.getWindow().getAttributes();
//                windowManagerLayoutParams.alpha = 1.0f;
//                baseActivity.getWindow().setAttributes(windowManagerLayoutParams);
                if (needEnableView != null) {
                    needEnableView.setEnabled(true);
                }
                if (onShowPopupWindowOperatingListener != null) {
                    onShowPopupWindowOperatingListener.onDismiss(popupWindow, popupWindowView);
                }
                baseActivity.removePopupWindow(popupWindowKey);
            }
        });
        final ImageView imageViewTriangleUp = (ImageView) popupWindowView.findViewById(R.id.imageViewTriangleUp);
        final ImageView imageViewTriangleDown = (ImageView) popupWindowView.findViewById(R.id.imageViewTriangleDown);
        final StatusView statusViewDeleteLine = (StatusView) popupWindowView.findViewById(R.id.statusViewDeleteLine);
        final StatusView statusViewUnderLine = (StatusView) popupWindowView.findViewById(R.id.statusViewUnderLine);
        final StatusView statusViewUnderWaveLine = (StatusView) popupWindowView.findViewById(R.id.statusViewUnderWaveLine);
        final StatusView statusViewHighLight = (StatusView) popupWindowView.findViewById(R.id.statusViewHighLight);
        final StatusView statusViewClear = (StatusView) popupWindowView.findViewById(R.id.statusViewClear);

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
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StatusView statusView;
                MarkAreaType markAreaType;
                int id = view.getId();
                if (id == R.id.statusViewDeleteLine) {
                    statusView = statusViewDeleteLine;
                    markAreaType = MarkAreaType.DELETELINE;
                } else if (id == R.id.statusViewUnderLine) {
                    statusView = statusViewUnderLine;
                    markAreaType = MarkAreaType.UNDERLINE;
                } else if (id == R.id.statusViewUnderWaveLine) {
                    statusView = statusViewUnderWaveLine;
                    markAreaType = MarkAreaType.UNDERWAVELINE;
                } else if (id == R.id.statusViewHighLight) {
                    statusView = statusViewHighLight;
                    markAreaType = MarkAreaType.HIGHLIGHT;
                } else if (id == R.id.statusViewClear) {
                    onShowPopupWindowOperatingListener.clearPage(popupWindow, view);
                    return;
                } else {
                    return;
                }
                boolean isChecked = statusView.isChecked();
                boolean result = false;
                if (isChecked) {
                    result = onShowPopupWindowOperatingListener.onCancelSelect(popupWindow, view, markAreaType);
                } else {
                    result = onShowPopupWindowOperatingListener.onSelect(popupWindow, view, markAreaType);
                }
                if (result) {
                    statusView.setChecked(!isChecked);
                }
            }
        };
        statusViewDeleteLine.setOnClickListener(onClickListener);
        statusViewUnderLine.setOnClickListener(onClickListener);
        statusViewUnderWaveLine.setOnClickListener(onClickListener);
        statusViewHighLight.setOnClickListener(onClickListener);
        statusViewClear.setOnClickListener(onClickListener);
        if (isUp) {
            imageViewTriangleUp.setVisibility(View.VISIBLE);
            imageViewTriangleDown.setVisibility(View.GONE);
        } else {
            imageViewTriangleUp.setVisibility(View.GONE);
            imageViewTriangleDown.setVisibility(View.VISIBLE);
        }
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        popupWindowView.measure(spec, spec);
        int measuredWidth = popupWindowView.getMeasuredWidth();
        int measuredHeight = popupWindowView.getMeasuredHeight();

        ColorDrawable colorDrawable = new ColorDrawable(Color.argb(0, 255, 255, 255));
        popupWindow.setBackgroundDrawable(colorDrawable);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setAnimationStyle(R.style.popwindowNormalAnimationCenter);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//        WindowManager.LayoutParams windowManagerLayoutParams = baseActivity.getWindow().getAttributes();
//        windowManagerLayoutParams.alpha = 0.7f;
//        baseActivity.getWindow().setAttributes(windowManagerLayoutParams);
        if (isUp) {
            popupWindow.showAtLocation(view, Gravity.LEFT | Gravity.TOP, x - measuredWidth / 2, y);
        } else {
            popupWindow.showAtLocation(view, Gravity.LEFT | Gravity.TOP, x - measuredWidth / 2, y - measuredHeight);
        }
        baseActivity.addPopupWindow(popupWindowKey, popupWindow);
        return popupWindow;
    }

    public interface OnShowPopupWindowBookMarketListener {

        void onClick(PopupWindow popupWindow, View view);

        void onDismiss(PopupWindow popupWindow, View view);

    }

    public static PopupWindow showPopupWindowBookMarket(final BaseActivity baseActivity, View view, final View needEnableView, final String key, final List<BookMarkBean> bookMarkBeans, final OnShowPopupWindowBookMarketListener onShowPopupWindowBookMarketListener) {
        if (view.getWindowToken() == null || bookMarkBeans == null) {
            return null;
        }
        if (needEnableView != null) {
            needEnableView.setEnabled(false);
        }
        LayoutInflater layoutInflater = baseActivity.getLayoutInflater();
        View popupWindowView = layoutInflater.inflate(R.layout.popupwindow_book_market, null);
        final String popupWindowKey = key != null ? key : (POPUPWINDOWKEY + TimeUtil.getOnlyTimeWithoutSleep());


        //获取字体长度
        Rect rect=new Rect();
        Paint paint = new Paint();
        int width = 0;
        for (int i = 0; i < bookMarkBeans.size(); i++) {
            String bookTitle = bookMarkBeans.get(i).title;
            paint.setTextSize(70);
            paint.getTextBounds(bookTitle, 0, bookTitle.length() , rect);
            if(width < rect.width()) {
                width = rect.width();
            }
            if(bookMarkBeans.get(i).childs != null && !bookMarkBeans.get(i).childs.isEmpty() ) {
                for (int j = 0; j < bookMarkBeans.get(i).childs.size(); j++) {
                    BookMarkBean.BookMarkSecondBean bookMarkSecondBean = bookMarkBeans.get(i).childs.get(j);
                    bookTitle = bookMarkSecondBean.title;
                    paint.setTextSize(40);
                    paint.getTextBounds(bookTitle, 0, bookTitle.length() , rect);
                    if(width < rect.width()) {
                        width = rect.width();
                    }

                    if(bookMarkSecondBean.childs != null && !bookMarkSecondBean.childs.isEmpty() ) {
                        for (int k = 0; k < bookMarkSecondBean.childs.size(); k++) {
                            paint.setTextSize(40);
                            BookMarkBean.BookMarkSecondBean.BookMarkThirdBean bookMarkThirdBean = bookMarkSecondBean.childs.get(k);
                            bookTitle = bookMarkThirdBean.title;
                            paint.getTextBounds(bookTitle, 0, bookTitle.length() , rect);
                            if(width < rect.width()) {
                                width = rect.width();
                            }
                        }
                    }
                }
            }
        }



        int popupWindowWidth = Math.min(BaseActivity.getScreenWidth(baseActivity) ,BaseActivity.getScreenHeight(baseActivity))*3/5;
        if( popupWindowWidth > (width + 70)  && width != 0 ) {
            popupWindowWidth = (width + 70) ;
        }




//        int popupWindowWidth = Math.min(BaseActivity.getScreenWidth(baseActivity) ,BaseActivity.getScreenHeight(baseActivity))*3/5;
        final PopupWindow popupWindow = new PopupWindow(popupWindowView, popupWindowWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams windowManagerLayoutParams = baseActivity.getWindow().getAttributes();
                windowManagerLayoutParams.alpha = 1.0f;
                baseActivity.getWindow().setAttributes(windowManagerLayoutParams);
                if (needEnableView != null) {
                    needEnableView.setEnabled(true);
                }
                if (onShowPopupWindowBookMarketListener != null) {
                    onShowPopupWindowBookMarketListener.onDismiss(popupWindow, popupWindowView);
                }
                baseActivity.removePopupWindow(popupWindowKey);
            }
        });


        final TreeControlView treeControlView = (TreeControlView) popupWindowView.findViewById(R.id.treeControlView);
        treeControlView.refreshAllItem(bookMarkBeans, true);

        ColorDrawable colorDrawable = new ColorDrawable(Color.argb(0, 255, 255, 255));
        popupWindow.setBackgroundDrawable(colorDrawable);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setAnimationStyle(R.style.popwindowNormalAnimationLeft);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        WindowManager.LayoutParams windowManagerLayoutParams = baseActivity.getWindow().getAttributes();
        windowManagerLayoutParams.alpha = 0.7f;
        baseActivity.getWindow().setAttributes(windowManagerLayoutParams);
        popupWindow.showAtLocation(view, Gravity.LEFT, 0, 0);
        baseActivity.addPopupWindow(popupWindowKey, popupWindow);
        return popupWindow;
    }


    public interface OnShowPopupWindowReadModeListener {

        void onSelect(int position, ReadModeAdapter readModeAdapter, PopupWindow popupWindow, View view);

        void onDismiss(PopupWindow popupWindow, View view);

    }

    public static PopupWindow showPopupWindowReadMode(final BaseActivity baseActivity, View view, final View needEnableView, final String key, final ReadModeAdapter readModeAdapter, final OnShowPopupWindowReadModeListener onShowPopupWindowReadModeListener) {
        if (view.getWindowToken() == null) {
            return null;
        }
        if (needEnableView != null) {
            needEnableView.setEnabled(false);
        }

        LayoutInflater layoutInflater = baseActivity.getLayoutInflater();
        View popupWindowView = layoutInflater.inflate(R.layout.popupwindow_read_mode, null);
        final String popupWindowKey = key != null ? key : (POPUPWINDOWKEY + TimeUtil.getOnlyTimeWithoutSleep());
        int popupWindowHeight = (int) (BaseActivity.getScreenHeight(baseActivity) * 0.6f);
        final PopupWindow popupWindow = new PopupWindow(popupWindowView, ViewGroup.LayoutParams.MATCH_PARENT, popupWindowHeight);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams windowManagerLayoutParams = baseActivity.getWindow().getAttributes();
                windowManagerLayoutParams.alpha = 1.0f;
                baseActivity.getWindow().setAttributes(windowManagerLayoutParams);
                if (needEnableView != null) {
                    needEnableView.setEnabled(true);
                }
                if (onShowPopupWindowReadModeListener != null) {
                    onShowPopupWindowReadModeListener.onDismiss(popupWindow, popupWindowView);
                }
                baseActivity.removePopupWindow(popupWindowKey);
            }
        });
        final ImageView imageViewClose = (ImageView) popupWindowView.findViewById(R.id.imageViewClose);
        final ListView listView = (ListView) popupWindowView.findViewById(R.id.listView);

        listView.setAdapter(readModeAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (onShowPopupWindowReadModeListener != null) {
                    onShowPopupWindowReadModeListener.onSelect(position, readModeAdapter, popupWindow, view);
                }
            }
        });

        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        ColorDrawable colorDrawable = new ColorDrawable(Color.argb(0, 255, 255, 255));
        popupWindow.setBackgroundDrawable(colorDrawable);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setAnimationStyle(R.style.popwindowNormalAnimation);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        WindowManager.LayoutParams windowManagerLayoutParams = baseActivity.getWindow().getAttributes();
        windowManagerLayoutParams.alpha = 0.7f;
        baseActivity.getWindow().setAttributes(windowManagerLayoutParams);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        baseActivity.addPopupWindow(popupWindowKey, popupWindow);
        return popupWindow;
    }

    public static void setPopupWindowTouchModal(PopupWindow popupWindow,
                                                boolean touchModal) {
        if (null == popupWindow) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupWindow.setTouchModal(touchModal);
            return;
        }
        Method method;
        try {
            method = PopupWindow.class.getDeclaredMethod("setTouchModal",
                    boolean.class);
            method.setAccessible(true);
            method.invoke(popupWindow, touchModal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnShowPopupWindowInstructListener {

        void onReset(DrawBoardView drawBoardView ,PopupWindow popupWindow, View view);

        void onRecover(DrawBoardView drawBoardView ,PopupWindow popupWindow, View view);

        void onSave(DrawBoardView drawBoardView ,PopupWindow popupWindow, View view);

        void onCommit(DrawBoardView drawBoardView ,PopupWindow popupWindow, View view);

        void onDismiss(PopupWindow popupWindow, View view);

    }
    
    public static PopupWindow showPopupWindowInstruct(final BaseActivity baseActivity, View view, final View needEnableView,final String drawPathData,final String suggestion, final OnShowPopupWindowInstructListener onShowPopupWindowInstructListener) {
        if (view.getWindowToken() == null) {
            return null;
        }
        if (needEnableView != null) {
            needEnableView.setEnabled(false);
        }

        LayoutInflater layoutInflater = baseActivity.getLayoutInflater();
        View popupWindowView = layoutInflater.inflate(R.layout.popupwindow_instruct, null);
        final String popupWindowKey = POPUPWINDOWKEY + TimeUtil.getOnlyTimeWithoutSleep();
        final PopupWindow popupWindow = new PopupWindow(popupWindowView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams windowManagerLayoutParams = baseActivity.getWindow().getAttributes();
                windowManagerLayoutParams.alpha = 1.0f;
                baseActivity.getWindow().setAttributes(windowManagerLayoutParams);
                if (needEnableView != null) {
                    needEnableView.setEnabled(true);
                }
                if (onShowPopupWindowInstructListener != null) {
                    onShowPopupWindowInstructListener.onDismiss(popupWindow, popupWindowView);
                }
                baseActivity.removePopupWindow(popupWindowKey);
            }
        });
        final ImageView imageViewClose = (ImageView) popupWindowView.findViewById(R.id.imageViewClose);
        final TextView textViewAdd = (TextView) popupWindowView.findViewById(R.id.textViewAdd);
        final TextView textViewReduce = (TextView) popupWindowView.findViewById(R.id.textViewReduce);
        final TextView textViewContent = (TextView) popupWindowView.findViewById(R.id.textViewContent);
        final TextView textViewSuggestionContent = (TextView) popupWindowView.findViewById(R.id.textViewSuggestionContent);
        final FrameLayout frameLayoutDrawArea = (FrameLayout) popupWindowView.findViewById(R.id.frameLayoutDrawArea);
        final DrawBoardView drawBoardView = (DrawBoardView) popupWindowView.findViewById(R.id.drawBoardView);

        final FrameLayout frameLayoutReset = (FrameLayout) popupWindowView.findViewById(R.id.frameLayoutReset);
        final FrameLayout frameLayoutRecover = (FrameLayout) popupWindowView.findViewById(R.id.frameLayoutRecover);
        final FrameLayout frameLayoutSave = (FrameLayout) popupWindowView.findViewById(R.id.frameLayoutSave);
        final FrameLayout frameLayoutCommit = (FrameLayout) popupWindowView.findViewById(R.id.frameLayoutCommit);

        textViewSuggestionContent.setText(suggestion);

        int screenHeight = (int) (BaseActivity.getScreenWidth(baseActivity) * 0.75f);
        frameLayoutDrawArea.getLayoutParams().height=screenHeight;

        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        textViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String penSize= textViewContent.getText().toString();
               int size= StringUtil.stringToInt(penSize);
               size=size+1;
                if(size>99){
                    size=99;
                }
               drawBoardView.setPenSize(size);
               textViewContent.setText(String.valueOf(size)+".0");
            }
        });
        textViewReduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String penSize= textViewContent.getText().toString();
                int size= StringUtil.stringToInt(penSize);
                size=size-1;
                if(size<1){
                    size=1;
                }
                drawBoardView.setPenSize(size);
                textViewContent.setText(String.valueOf(size)+".0");
            }
        });
        frameLayoutReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onShowPopupWindowInstructListener != null) {
                  onShowPopupWindowInstructListener.onReset(drawBoardView,popupWindow, popupWindowView);
                  }
            }
        });
        frameLayoutRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onShowPopupWindowInstructListener != null) {
                    onShowPopupWindowInstructListener.onRecover(drawBoardView,popupWindow, popupWindowView);
                }
            }
        });
        frameLayoutSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onShowPopupWindowInstructListener != null) {
                    onShowPopupWindowInstructListener.onSave(drawBoardView,popupWindow, popupWindowView);
                }
            }
        });
        frameLayoutCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onShowPopupWindowInstructListener != null) {
                    onShowPopupWindowInstructListener.onCommit(drawBoardView,popupWindow, popupWindowView);
                }
            }
        });
        ColorDrawable colorDrawable = new ColorDrawable(Color.argb(0, 255, 255, 255));
        popupWindow.setBackgroundDrawable(colorDrawable);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setAnimationStyle(R.style.popwindowNormalAnimation);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if(drawPathData!=null) {
            popupWindowView.post(new Runnable() {
                @Override
                public void run() {
                    drawBoardView.restoreDrawPathData(drawPathData);
                }
            });
        }
        WindowManager.LayoutParams windowManagerLayoutParams = baseActivity.getWindow().getAttributes();
        windowManagerLayoutParams.alpha = 0.7f;
        baseActivity.getWindow().setAttributes(windowManagerLayoutParams);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        baseActivity.addPopupWindow(popupWindowKey, popupWindow);
        return popupWindow;
    }


    public interface OnShowPopupWindowInstructHistoryListener {

        void onDismiss(PopupWindow popupWindow, View view);

    }

    public static PopupWindow showPopupWindowInstructHistory(final BaseActivity baseActivity, View view, final View needEnableView, final String drawPathData,final List<PeopleItem> peopleItems, final OnShowPopupWindowInstructHistoryListener onShowPopupWindowInstructHistoryListener) {
        if (view.getWindowToken() == null) {
            return null;
        }
        if (needEnableView != null) {
            needEnableView.setEnabled(false);
        }

        LayoutInflater layoutInflater = baseActivity.getLayoutInflater();
        View popupWindowView = layoutInflater.inflate(R.layout.popupwindow_instruct_history, null);
        final String popupWindowKey = POPUPWINDOWKEY + TimeUtil.getOnlyTimeWithoutSleep();


        final PopupWindow popupWindow = new PopupWindow(popupWindowView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams windowManagerLayoutParams = baseActivity.getWindow().getAttributes();
                windowManagerLayoutParams.alpha = 1.0f;
                baseActivity.getWindow().setAttributes(windowManagerLayoutParams);
                if (needEnableView != null) {
                    needEnableView.setEnabled(true);
                }
                if (onShowPopupWindowInstructHistoryListener != null) {
                    onShowPopupWindowInstructHistoryListener.onDismiss(popupWindow, popupWindowView);
                }
                baseActivity.removePopupWindow(popupWindowKey);
            }
        });
        final DrawBoardView drawBoardView = (DrawBoardView) popupWindowView.findViewById(R.id.drawBoardView);
        final FrameLayout frameLayoutDrawArea = (FrameLayout) popupWindowView.findViewById(R.id.frameLayoutDrawArea);
        final ListView listView = (ListView) popupWindowView.findViewById(R.id.listView);
        if(peopleItems!=null) {
            listView.setAdapter(new PeopleItemAdapter(baseActivity, peopleItems));
        }
        int screenHeight = (int) (BaseActivity.getScreenWidth(baseActivity) * 0.75f);
        frameLayoutDrawArea.getLayoutParams().height=screenHeight;

        final ImageView imageViewClose = (ImageView) popupWindowView.findViewById(R.id.imageViewClose);
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        drawBoardView.setCanTouch(false);
        ColorDrawable colorDrawable = new ColorDrawable(Color.argb(0, 255, 255, 255));
        popupWindow.setBackgroundDrawable(colorDrawable);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setAnimationStyle(R.style.popwindowNormalAnimation);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if(drawPathData!=null) {
            popupWindowView.post(new Runnable() {
                @Override
                public void run() {
                    drawBoardView.restoreDrawPathData(drawPathData);
                }
            });
        }
        WindowManager.LayoutParams windowManagerLayoutParams = baseActivity.getWindow().getAttributes();
        windowManagerLayoutParams.alpha = 0.7f;
        baseActivity.getWindow().setAttributes(windowManagerLayoutParams);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        baseActivity.addPopupWindow(popupWindowKey, popupWindow);
        return popupWindow;
    }

}
