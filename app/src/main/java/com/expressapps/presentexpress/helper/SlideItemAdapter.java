package com.expressapps.presentexpress.helper;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.expressapps.presentexpress.R;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.Collections;
import java.util.List;

public class SlideItemAdapter extends RecyclerView.Adapter<CustomSlideViewHolder> {
    private final List<Bitmap> bitmapList;
    private final View.OnClickListener imgClickListener;

    public SlideItemAdapter(List<Bitmap> bitmapList, View.OnClickListener imgClickListener) {
        this.bitmapList = bitmapList;
        this.imgClickListener = imgClickListener;
    }

    @NonNull
    @Override
    public CustomSlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_item_layout, parent, false);
        return new CustomSlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomSlideViewHolder holder, int position) {
        Bitmap bitmap = bitmapList.get(position);
        holder.imageView.setImageBitmap(bitmap);
        if (bitmap == null)
            holder.imageView.setBackgroundResource(R.drawable.ic_loading_wide);
        else holder.imageView.setBackground(null);

        holder.textViewIndex.setText(String.valueOf(position + 1));

        holder.imageView.setTag(position);
        holder.imageView.setOnClickListener(imgClickListener);
        holder.moreBtn.setTag(position);
        holder.moreBtn.setOnClickListener(imgClickListener);

        holder.imageBorder.setBackgroundColor(Color.WHITE);
        holder.imageBorder.setBackgroundResource(R.drawable.border);

        ViewGroup.LayoutParams lp = holder.itemLayout.getLayoutParams();
        if (lp instanceof FlexboxLayoutManager.LayoutParams) {
            FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
            flexboxLp.setFlexGrow(1f);
            flexboxLp.setMinWidth(Funcs.toDp(200));
            flexboxLp.setMaxWidth(Funcs.toDp(400));
            holder.itemLayout.setPadding(0, Funcs.toDp(20), Funcs.toDp(20), 0);
        }
    }

    @Override
    public int getItemCount() {
        return bitmapList.size();
    }

    public void addItem(Bitmap bitmap) {
        bitmapList.add(bitmap);
        notifyItemInserted(getItemCount() - 1);

        if (getItemCount() >= 2)
            notifyItemChanged(getItemCount() - 2);
    }

    public void updateItem(Bitmap bitmap, int idx) {
        bitmapList.set(idx, bitmap);
        notifyItemChanged(idx);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void removeItem(int idx) {
        bitmapList.remove(idx);
        notifyItemRemoved(idx);

        if (idx == 0)
            notifyDataSetChanged();
        else if (idx < getItemCount())
            notifyItemRangeChanged(idx, getItemCount() - idx);
        else if (idx == getItemCount())
            notifyItemChanged(getItemCount() - 1);
    }

    public void moveItems(int idx1, int idx2) {
        Collections.swap(bitmapList, idx1, idx2);
        notifyItemMoved(idx1, idx2);
        notifyItemRangeChanged(Math.min(idx1, idx2), 2);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearItems() {
        bitmapList.clear();
        notifyDataSetChanged();
    }
}