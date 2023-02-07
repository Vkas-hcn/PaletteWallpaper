package com.vkas.wallpaperapp.wautils;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class BottomOffsetDecoration extends RecyclerView.ItemDecoration{
    private int mBottomOffset;

    public BottomOffsetDecoration(int bottomOffset) {
        mBottomOffset = bottomOffset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int dataSize = state.getItemCount();
        int position = parent.getChildAdapterPosition(view);
        GridLayoutManager grid = (GridLayoutManager) parent.getLayoutManager();
        if ((dataSize - position) <= grid.getSpanCount()) {
            outRect.set(0, 0, 0, mBottomOffset);
        } else {
            outRect.set(0, 0, 0, 0);
        }

    }
}
