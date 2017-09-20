package com.recklessracoon.roman.audiocuttertest.decorators;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.recklessracoon.roman.audiocuttertest.io.Wrap;

/**
 * Created by Roman on 12.09.2017.
 */

public class MergeableViewDecorator implements Invalidateable {

    private View view;
    private Context context;

    private Mergeable mergeable;
    private Wrap wrap;

    private RecyclerView.ViewHolder holder;

    private boolean isChosen;

    public MergeableViewDecorator(){

    }

    public MergeableViewDecorator onView(View view){
        this.view = view;
        this.context = view.getContext();
        return this;
    }

    public MergeableViewDecorator withViewHolder(RecyclerView.ViewHolder holder){
        this.holder = holder;
        return this;
    }

    public MergeableViewDecorator withWrap(Wrap wrap){
        this.wrap = wrap;
        isChosen = wrap.isSelected;
        return this;
    }

    public MergeableViewDecorator withMergeable(Mergeable mergeable){
        this.mergeable = mergeable;
        return this;
    }

    public MergeableViewDecorator apply(){
        if(context != null && view != null) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    isChosen = !isChosen;
                    if(holder != null)
                        holder.itemView.setSelected(isChosen);

                    if(isChosen){
                        mergeable.enqueueMerge(holder.getAdapterPosition(), wrap.actualFile);
                        wrap.isSelected = true;
                    } else {
                        mergeable.dequeueMerge(holder.getAdapterPosition(), wrap.actualFile);
                        wrap.isSelected = false;
                    }

                    Log.d("LONGCLICK",""+isChosen+" "+wrap.actualFile.getAbsolutePath());

                    return true;
                }
            });
        }

        return this;
    }

    @Override
    public void invalidate() {
        if(holder != null && holder.itemView != null) {
            holder.itemView.setSelected(false);
        }
        isChosen = false;
    }
}
