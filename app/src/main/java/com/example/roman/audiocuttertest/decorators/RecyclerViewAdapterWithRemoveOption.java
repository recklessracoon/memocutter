package com.example.roman.audiocuttertest.decorators;

import android.support.v7.widget.RecyclerView;

import com.example.roman.audiocuttertest.io.Wrap;

import java.util.List;

/**
 * Created by Roman on 06.09.2017.
 */

public interface RecyclerViewAdapterWithRemoveOption {

    void removeCutFile(int position);
    void notifyItemRemoved(int position);
    int getItemCount();
    void notifyItemRangeChanged(int position, int total);

}
