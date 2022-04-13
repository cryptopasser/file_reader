package com.snakeway.file_reader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.snakeway.file_reader.R;
import com.snakeway.file_reader.models.ReadModeItem;

import java.util.ArrayList;
import java.util.List;

public class ReadModeAdapter extends BaseAdapter {
    private Context context = null;
    private List<ReadModeItem> datas = null;

    public ReadModeAdapter(Context context, List<ReadModeItem> datas) {
        if (datas == null) {
            datas = new ArrayList<>();
        }
        this.context = context;
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.popupwindow_read_mode_item, null);
            viewHolder.imageViewIcon = (ImageView) view.findViewById(R.id.imageViewIcon);
            viewHolder.textViewTitle = (TextView) view.findViewById(R.id.textViewTitle);
            viewHolder.imageViewCheck = (ImageView) view.findViewById(R.id.imageViewCheck);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        ReadModeItem readModeItem = datas.get(i);
        if (readModeItem.isShow()) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
//        viewHolder.imageViewIcon.setImageResource(readModeItem.getIcon());//gradle 7.4版本会闪退
        if ( ReadModeItem.ReadModeType.SINGLE==readModeItem.getType()) {
            viewHolder.imageViewIcon.setImageResource(R.mipmap.read_mode_single);
        }else if(ReadModeItem.ReadModeType.MULTI==readModeItem.getType()) {
            viewHolder.imageViewIcon.setImageResource(R.mipmap.read_mode_multi);
        }else if(ReadModeItem.ReadModeType.THUMBNAIL==readModeItem.getType()) {
            viewHolder.imageViewIcon.setImageResource(R.mipmap.read_mode_thumbnail);
        }else if(ReadModeItem.ReadModeType.NIGHT==readModeItem.getType()) {
            viewHolder.imageViewIcon.setImageResource(R.mipmap.read_mode_night);
        }else if(ReadModeItem.ReadModeType.AUTO_FILL_WHITE_SPACE==readModeItem.getType()) {
            viewHolder.imageViewIcon.setImageResource(R.mipmap.read_mode_crop);
        }
        viewHolder.textViewTitle.setText(readModeItem.getTitle());
        viewHolder.imageViewCheck.setVisibility(readModeItem.isCheck() ? View.VISIBLE : View.INVISIBLE);
        return view;
    }

    public final class ViewHolder {
        public ImageView imageViewIcon;
        public TextView textViewTitle;
        public ImageView imageViewCheck;
    }
}