package com.example.roman.audiocuttertest.decorators;

/**
 * Created by Roman on 06.09.2017.
 */

public interface RecyclerViewAdapterWithRemoveOption {

    void removeCutFile(int position);
    int getItemCount();

    void notifyItemNotRemoved(int position);
}
