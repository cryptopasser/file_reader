package com.snakeway.file_reader.items;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.snakeway.file_reader.R;
import com.snakeway.file_reader.models.BookMarkBean;
import com.snakeway.treeview.base.ViewHolder;
import com.snakeway.treeview.factory.ItemHelperFactory;
import com.snakeway.treeview.item.TreeItem;
import com.snakeway.treeview.item.TreeItemGroup;

import java.util.List;

public class BookMarkSecondItem extends TreeItemGroup<BookMarkBean.BookMarkSecondBean> {

    @Override
    public List<TreeItem> initChild(BookMarkBean.BookMarkSecondBean data) {
        List<TreeItem> items = ItemHelperFactory.createItems(data.childs, this);
        return items;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_pdf_viewer_tag_second_item;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder) {
        String remark=data.isRemark?"(√)":"";
        viewHolder.setText(R.id.textViewTag, "   " + data.title+remark);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.onBookMarkListener != null) {
                    data.onBookMarkListener.onItemClick(data);
                }
            }
        });
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, RecyclerView.LayoutParams layoutParams, int position) {
        super.getItemOffsets(outRect, layoutParams, position);
    }
}
