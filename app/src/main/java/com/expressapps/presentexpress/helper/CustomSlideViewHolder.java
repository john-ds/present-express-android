package com.expressapps.presentexpress.helper;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.expressapps.presentexpress.R;
import com.google.android.material.button.MaterialButton;

public class CustomSlideViewHolder extends RecyclerView.ViewHolder {
    public final ImageView imageView;
    public final TextView textViewIndex;
    public final LinearLayout itemLayout;
    public final MaterialButton moreBtn;
    public final ConstraintLayout imageBorder;

    public CustomSlideViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.itemLayoutImg);
        textViewIndex = itemView.findViewById(R.id.itemLayoutId);
        itemLayout = itemView.findViewById(R.id.itemLayout);
        moreBtn = itemView.findViewById(R.id.imgLayoutMoreBtn);
        imageBorder = itemView.findViewById(R.id.imgLayoutBorder);
    }
}

