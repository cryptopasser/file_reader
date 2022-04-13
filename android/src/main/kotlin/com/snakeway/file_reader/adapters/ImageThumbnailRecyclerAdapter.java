package com.snakeway.file_reader.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.snakeway.file_reader.BaseActivity;
import com.snakeway.file_reader.R;
import com.snakeway.file_reader.ThumbnailActivity;
import com.snakeway.file_reader.utils.BitmapMemoryCacheHelper;
import com.snakeway.pdfviewer.RenderingCustomHandler;
import com.snakeway.pdfviewer.model.RenderedCustomInfo;

import java.util.ArrayList;
import java.util.List;

public class ImageThumbnailRecyclerAdapter extends RecyclerView.Adapter<ImageThumbnailRecyclerAdapter.ImageThumbnailViewHolder> {

    private ThumbnailActivity thumbnailActivity;
    private List<RenderingCustomHandler.RenderingCustomPageInfo> datas;
    private BitmapMemoryCacheHelper bitmapMemoryCacheHelper;
    private int itemWidth = 0;
    private int selectPage = -1;

    public ImageThumbnailRecyclerAdapter(ThumbnailActivity thumbnailActivity, BitmapMemoryCacheHelper bitmapMemoryCacheHelper, List<RenderingCustomHandler.RenderingCustomPageInfo> datas, int selectPage) {
        if (datas == null) {
            datas = new ArrayList<>();
        }
        this.thumbnailActivity = thumbnailActivity;
        this.bitmapMemoryCacheHelper = bitmapMemoryCacheHelper;
        this.datas = datas;
        this.selectPage = selectPage;
//        itemWidth = (int) (
//                BaseActivity.getScreenWidth(thumbnailActivity)  / 3);
        itemWidth = (int) ((BaseActivity.getScreenWidth(thumbnailActivity) - thumbnailActivity.getResources().getDimension(R.dimen.view_normal_margin_default) * 4) / 3);
    }


    @NonNull
    @Override
    public ImageThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(thumbnailActivity).inflate(R.layout.activity_thumbnail_item, null);
        View view = LayoutInflater.from(thumbnailActivity).inflate(R.layout.activity_thumbnail_item, parent, false);
        return new ImageThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageThumbnailViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
//        int i = position;
        RenderingCustomHandler.RenderingCustomPageInfo renderingCustomPageInfo = datas.get(position);
        viewHolder.textViewPageNumber.setText(String.valueOf(position + 1));
        viewHolder.imageView.setTag(position);
        Bitmap cacheBitmap = bitmapMemoryCacheHelper.getBitmap(String.valueOf(position));
        viewHolder.frameLayoutRoot.setVisibility(View.INVISIBLE);
        viewHolder.frameLayoutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thumbnailActivity.choosePage(position);
            }
        });
        if (selectPage == position) {
            Log.e("selectPage", "onBindViewHolder:  "+position+"      "+selectPage );
            viewHolder.frameLayoutRoot.setBackgroundResource(R.drawable.thumbnail_item_select_background);
        } else {
            viewHolder.frameLayoutRoot.setBackground(null);
        }
        if (cacheBitmap != null) {
            viewHolder.imageView.setImageBitmap(cacheBitmap);
            viewHolder.frameLayoutRoot.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imageView.setImageBitmap(null);
            List<RenderingCustomHandler.RenderingCustomPageInfo> theDatas = new ArrayList<>();
            theDatas.add(renderingCustomPageInfo);
            getRenderingImages(theDatas, position, viewHolder.imageView, viewHolder.frameLayoutRoot);
        }
    }

    private void getRenderingImages(List<RenderingCustomHandler.RenderingCustomPageInfo> pages, int position, ImageView imageView, FrameLayout frameLayoutRoot) {
        thumbnailActivity.getRenderingImages(pages, new RenderingCustomHandler.OnRenderingCustomListener() {

            @Override
            public void onSuccessOne(RenderedCustomInfo renderedCustomInfo) {
                bitmapMemoryCacheHelper.putBitmap(String.valueOf(position), renderedCustomInfo.getRenderedBitmap());
                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                ViewGroup.LayoutParams frameLayoutRootParams = frameLayoutRoot.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.width = itemWidth;
                    layoutParams.height = (int) ((float) itemWidth * renderedCustomInfo.getHeight() / renderedCustomInfo.getWidth() );
                    frameLayoutRootParams.width = itemWidth;
                }
                if (imageView.getTag() != null && (int) (imageView.getTag()) == renderedCustomInfo.getPage()) {
                    imageView.setImageBitmap(renderedCustomInfo.getRenderedBitmap());
                    frameLayoutRoot.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSuccess(List<RenderedCustomInfo> renderedCustomInfos) {
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public final class ImageThumbnailViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout frameLayoutRoot;
        public ImageView imageView;
        public TextView textViewPageNumber;

        public ImageThumbnailViewHolder(@NonNull View view) {
            super(view);
            frameLayoutRoot = (FrameLayout) view.findViewById(R.id.frameLayoutRoot);
            imageView = (ImageView) view.findViewById(R.id.imageView);
            textViewPageNumber = (TextView) view.findViewById(R.id.textViewPageNumber);
        }
    }
}
