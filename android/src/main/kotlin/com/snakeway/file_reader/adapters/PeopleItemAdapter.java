package com.snakeway.file_reader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.snakeway.file_reader.R;
import com.snakeway.file_reader.models.PeopleItem;
import com.snakeway.file_reader.models.ReadModeItem;

import java.util.ArrayList;
import java.util.List;

public class PeopleItemAdapter extends BaseAdapter {
    private Context context = null;
    private List<PeopleItem> datas = null;

    public PeopleItemAdapter(Context context, List<PeopleItem> datas) {
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
            view = LayoutInflater.from(context).inflate(R.layout.popupwindow_instruct_history_item, null);
            viewHolder.textViewTitle = (TextView) view.findViewById(R.id.textViewTitle);
            viewHolder.textViewDate = (TextView) view.findViewById(R.id.textViewDate);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        PeopleItem peopleItem = datas.get(i);
        viewHolder.textViewTitle.setText(peopleItem.getName());
        viewHolder.textViewDate.setText(peopleItem.getDate());
        return view;
    }

    public final class ViewHolder {
        public TextView textViewTitle;
        public TextView textViewDate;
    }
}