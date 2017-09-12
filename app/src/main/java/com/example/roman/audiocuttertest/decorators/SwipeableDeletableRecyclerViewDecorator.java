package com.example.roman.audiocuttertest.decorators;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.example.roman.audiocuttertest.R;

/**
 * Created by Roman on 06.09.2017.
 */

public class SwipeableDeletableRecyclerViewDecorator implements SwipeableDeletable {

    private RecyclerView mRecyclerView;
    private Context context;

    public SwipeableDeletableRecyclerViewDecorator(){
    }

    public SwipeableDeletableRecyclerViewDecorator withContext(Context context){
        this.context = context;
        return this;
    }

    public SwipeableDeletableRecyclerViewDecorator withRecyclerView(RecyclerView recyclerView){
        this.mRecyclerView = recyclerView;
        return this;
    }

    public void apply(){
        if(context != null && mRecyclerView != null)
            initRecycleViewRemover();
    }

    @Override
    public void initRecycleViewRemover(){
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

                if (direction == ItemTouchHelper.LEFT) {    //if swipe left

                    AlertDialog.Builder builder = new AlertDialog.Builder(context); //alert for confirm to delete
                    builder.setMessage(context.getString(R.string.search_delete));    //set message

                    builder.setPositiveButton(context.getString(R.string.search_delete_yes), new DialogInterface.OnClickListener() { //when click on DELETE
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((RecyclerViewAdapterWithRemoveOption)mRecyclerView.getAdapter()).removeCutFile(viewHolder.getAdapterPosition());
                            ((Invalidateable)mRecyclerView.getAdapter()).invalidate();
                        }
                    }).setNegativeButton(context.getString(R.string.search_delete_no), new DialogInterface.OnClickListener() {  //not removing items if cancel is done
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //mAdapter.notifyItemRemoved(position + 1);    //notifies the RecyclerView Adapter that data in adapter has been removed at a particular position.
                            //mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());   //notifies the RecyclerView Adapter that positions of element in adapter has been changed from position(removed element index to end of list), please update it.
                            ((RecyclerViewAdapterWithRemoveOption)mRecyclerView.getAdapter()).notifyItemNotRemoved(viewHolder.getAdapterPosition());
                            ((Invalidateable)mRecyclerView.getAdapter()).invalidate();
                        }
                    }).show();  //show alert dialog
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView); //set swipe to recylcerview
    }

}
