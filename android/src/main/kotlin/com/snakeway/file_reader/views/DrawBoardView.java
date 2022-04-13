package com.snakeway.file_reader.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.google.gson.reflect.TypeToken;
import com.snakeway.file_reader.models.ReadModeItem;
import com.snakeway.pdfviewer.annotation.pen.ColorPen;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

public class DrawBoardView extends View {
    private static final float TOUCH_TOLERANCE = 4;// 触点之间的公差，此变量用于控制，当路径在绘制过程中，移动距离至少等于4才绘制，减轻频繁的绘制的情况。
    private static final float TOUCH_LONG_TOLERANCE = 12;
    private Paint mPaint;
    private Canvas mCanvas;
    private Bitmap mBitmap;// 保存每一次绘制出来的图形
    private Path mPath;
    private float mX, mY;
    private DrawPath drawPath;// 记录Path路径的对象
    private List<DrawPath> savePaths;// 保存Path路径的集合,用List集合来模拟栈
    private int screenWidth, screenHeight;
    private Paint mBitmapPaint;
    private int mPenSize = 4;
    private int mPenColor = Color.RED;
    private int mPanelColor = Color.TRANSPARENT;
    private boolean canTouch=true;
    private float touchX=0;
    private float touchY=0;


    public DrawBoardView(Context context) {
        super(context);
        init();
    }

    public DrawBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mPenSize);
        mPaint.setColor(mPenColor);
        savePaths = new ArrayList<DrawPath>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = getWidth();
        screenHeight = getHeight();
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);//创建跟view一样大的bitmap，用来保存签名(在控件大小发生改变时调用)
        mCanvas = new Canvas(mBitmap);// 保存每一次绘制出来的图形
        mCanvas.drawColor(mPanelColor);
    }


    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(mPanelColor);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);// 将前面已经画过得显示出来
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
    }


    public boolean isCanTouch() {
        return canTouch;
    }

    public void setCanTouch(boolean canTouch) {
        this.canTouch = canTouch;
    }

    private void touchStart(float x, float y) {
        mPath.moveTo(x, y);
        drawPath.points.add(new Point(x,y,0));
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(mY - y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {// 横坐标或纵坐标移动的距离的绝对值大于或等于TOUCH_TOLERANCE时，才描绘路径
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);// 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
            drawPath.points.add(new Point(x,y,1));
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);// 路径最后一点
        drawPath.points.add(new Point(mX,mY,2));
        mCanvas.drawPath(mPath, mPaint);// 用mPaint在画布上绘制路径,将路径保存在Bitmap上
        savePaths.add(drawPath);// 将一条完整的路径保存下来
        mPath = null;// 重新置空
    }

    /**
     * 撤销的核心思想就是将画布清空
     * 将保存下来的Path路径最后一个移除掉
     * 重新将路径画在画布上面
     */
    public void undo() {
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
                Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
        if (savePaths != null && savePaths.size() > 0) {
            savePaths.remove(savePaths.size() - 1);
            for (DrawPath drawPath : savePaths) {
                Paint paint = getPaint(drawPath.penSize, drawPath.color);
                mCanvas.drawPath(drawPath.path, paint);
            }
            invalidate();
        }
    }

    private Paint getPaint(float penSize, int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(penSize);
        paint.setColor(color);
        return paint;
    }

    public void reset() {
        savePaths.clear();
        mCanvas.drawColor(mPanelColor, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public Bitmap getBitmap() {
        if (savePaths == null || savePaths.size() == 0) {
            return null;
        }
        return mBitmap;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!canTouch){
            return true;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new Path();
                drawPath = new DrawPath();
                drawPath.path = mPath;
                drawPath.penSize = mPenSize;
                drawPath.color = mPenColor;
                drawPath.width=screenWidth;
                drawPath.height=screenHeight;
//                drawPath.paint = mPaint;
                touchStart(x, y);
                touchX=x;
                touchY=y;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(touchX-x)>TOUCH_TOLERANCE||Math.abs(touchY-y)>TOUCH_LONG_TOLERANCE){
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    public void setPenSize(int size) {
        mPenSize = size > 0 ? size : 4;
        this.mPenSize = size;
        if (mPaint != null) {
            mPaint.setStrokeWidth(mPenSize);
        }
    }

    public void setPanelColor(@ColorInt int bgColor) {
        this.mPanelColor = bgColor;
    }

    public void setPenColor(int mPenColor) {
        this.mPenColor = mPenColor;
        if (mPaint != null) {
            mPaint.setColor(mPenColor);
        }
    }

    public String convertDrawPathsData(){
        return GsonUtils.toJson(savePaths);
    }

    public void restoreDrawPathData(String data) {
        try {
            if (ObjectUtils.isNotEmpty(data)) {
                List<DrawPath> drawPaths = GsonUtils.fromJson(data, new TypeToken<List<DrawPath>>() {
                }.getType());
                savePaths.clear();
                savePaths.addAll(drawPaths);

                for (DrawPath drawPath : savePaths) {
                    drawPath.restorePath(screenWidth);
                    Paint paint = getPaint(drawPath.penSize, drawPath.color);
                    mCanvas.drawPath(drawPath.path, paint);
                }
                invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class DrawPath {
        public transient Path path;
        public int penSize;
        public int color;
        public int width;
        public int height;
        public final List<Point> points=new ArrayList<>();

        public void restorePath(int screenWidth){
            float ratio=(float)screenWidth/width;
            path=new Path();
            float mX=0;
            float mY=0;
            for(Point point:points){
                float x=point.x*ratio;
                float y=point.y*ratio;
               switch (point.type){
                   case 0:
                       path.moveTo(x, y);
                       mX = x;
                       mY = y;
                       break;
                   case 1:
                       path.quadTo(mX, mY, (x + mX) / 2, ( y + mY) / 2);
                       mX = x;
                       mY = y;
                       break;
                   case 2:
                       path.lineTo(x, y);
                       break;
                    default:
                       break;
               }
           }
        }
    }

    public static class Point {
        public float x;
        public float y;
        public int type;//0:DOWN,1:MOVE,2:UP

        public Point(float x, float y,int type) {
            this.x = x;
            this.y = y;
            this.type=type;
        }
    }

}
