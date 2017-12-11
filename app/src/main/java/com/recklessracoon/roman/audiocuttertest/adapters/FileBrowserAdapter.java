package com.recklessracoon.roman.audiocuttertest.adapters;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.recklessracoon.roman.audiocuttertest.EditActivity;
import com.recklessracoon.roman.audiocuttertest.FileBrowserActivity;
import com.recklessracoon.roman.audiocuttertest.R;
import com.recklessracoon.roman.audiocuttertest.decorators.Invalidateable;
import com.recklessracoon.roman.audiocuttertest.decorators.Mergeable;
import com.recklessracoon.roman.audiocuttertest.decorators.MergeableViewDecorator;
import com.recklessracoon.roman.audiocuttertest.decorators.RecyclerViewAdapterWithRemoveOption;
import com.recklessracoon.roman.audiocuttertest.decorators.Renameable;
import com.recklessracoon.roman.audiocuttertest.decorators.RenameableViewDecorator;
import com.recklessracoon.roman.audiocuttertest.io.Cutter;
import com.recklessracoon.roman.audiocuttertest.io.Wrap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Roman on 03.09.2017.
 */

public class FileBrowserAdapter extends RecyclerView.Adapter<FileBrowserAdapter.ViewHolder> implements RecyclerViewAdapterWithRemoveOption, Renameable, Mergeable, Invalidateable {

    private ArrayList<Wrap> mDataset;

    private static boolean mergeModeOn;
    private static ArrayList<File> enqueued;

    private ArrayList<Invalidateable> invalidateables;

    private FloatingActionButton mCombineButton;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView, mTextViewTime;
        public SeekBar mSeekBar;
        public ImageButton mPlay, mChevron;

        public MediaPlayer mediaPlayer;
        public Handler mHandler;

        public File actualFile;
        public View view;

        public MergeableViewDecorator mergeableViewDecorator;
        public RenameableViewDecorator renameableViewDecorator;

        public final Runnable updateBar = new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null){
                    mSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                    mTextViewTime.setText(Cutter.formatDurationPrecise(mediaPlayer.getCurrentPosition()));
                    if(mediaPlayer.isPlaying())
                        mHandler.postDelayed(this, 50);
                }
            }
        };

        public ViewHolder(View view) {
            super(view);
            this.view = view;

            // Handle click on a card in RecycleView
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!mergeModeOn) {
                        Intent intent = new Intent(v.getContext(), EditActivity.class);
                        Bundle b = new Bundle();

                        b.putSerializable("theFile", actualFile);
                        if (getEnqueued().size() > 1) { // user wants to concat audios, also put the list in
                            b.putSerializable("filesList", getEnqueued());
                        }

                        intent.putExtras(b); //Put your id to your next Intent
                        v.getContext().startActivity(intent);
                    }
                }
            });

            mTextView = (TextView) view.findViewById(R.id.textViewRecycler);
            mSeekBar = (SeekBar) view.findViewById(R.id.seekBarRecycler);
            mPlay = (ImageButton) view.findViewById(R.id.imageButtonRecycler);
            mTextViewTime = (TextView) view.findViewById(R.id.nowRecycler);

            mChevron = (ImageButton) view.findViewById(R.id.imageButtonRecyclerChevron);

            mHandler = new Handler();

            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(mediaPlayer != null && fromUser){
                        mediaPlayer.seekTo(progress);
                        mTextViewTime.setText(Cutter.formatDurationPrecise(progress));
                    }
                }
            });

            mPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mediaPlayer != null){
                        if(!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                            mHandler.post(updateBar);
                            mPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                        } else {
                            mediaPlayer.pause();
                            mPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                        }
                    }
                }
            });
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FileBrowserAdapter(ArrayList<Wrap> myDataset, boolean mergeModeOn, final FloatingActionButton mCombineButton) {
        mDataset = myDataset;
        this.mergeModeOn = mergeModeOn;
        enqueued = new ArrayList<>();
        invalidateables = new ArrayList<>();
        this.mCombineButton = mCombineButton;

        mCombineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getEnqueued().size() < 2){ //no
                    FileBrowserActivity.makeSnackbarOnRecyclerView(mCombineButton.getContext().getString(R.string.combine_not_possible));
                    return;
                }

                Intent intent = new Intent(v.getContext(), EditActivity.class);
                Bundle b = new Bundle();

                b.putSerializable("theFile", getEnqueued().get(0));
                b.putSerializable("filesList", getEnqueued());

                intent.putExtras(b); //Put your id to your next Intent
                v.getContext().startActivity(intent);
            }
        });
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FileBrowserAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item, parent, false);

        ViewHolder holder = new ViewHolder(v);

        if(mergeModeOn) {
            holder.mergeableViewDecorator = new MergeableViewDecorator();
            invalidateables.add(holder.mergeableViewDecorator);
        }else {
            holder.renameableViewDecorator = new RenameableViewDecorator();
        }

        return holder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
/*
        for(Wrap wrap : mDataset){
            Log.d("WRAPS",""+wrap.toString());
        }
*/
        holder.mTextView.setText(mDataset.get(position).name);
        holder.mediaPlayer = mDataset.get(position).mediaPlayer;

        holder.mSeekBar.setMax(holder.mediaPlayer.getDuration());

        holder.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                        holder.mPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            }
        });

        holder.mTextViewTime.setText(Cutter.formatDurationPrecise(0));
        holder.actualFile = mDataset.get(position).actualFile;

        holder.itemView.setSelected(mDataset.get(position).isSelected);

        //Log.d("HOLDER","pos: "+position+" is marked as: "+mDataset.get(position).isSelected+" enqueued:"+enqueued.toString());

        if(holder.mediaPlayer.isPlaying()) {
            holder.mPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
            holder.mSeekBar.setProgress(holder.mediaPlayer.getCurrentPosition());
            holder.mHandler.post(holder.updateBar);
        } else {
            holder.mPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        }

        if(mergeModeOn){
            holder.mChevron.setVisibility(View.INVISIBLE);
            holder.mergeableViewDecorator.onView(holder.view).withViewHolder(holder).withWrap(mDataset.get(position)).withMergeable(this).apply();
        } else {
            holder.renameableViewDecorator.onView(holder.view).withViewHolder(holder).withFile(holder.actualFile).withRenameable(this).apply();
        }

        /* // dont create new objects here
        if(mergeModeOn)
            invalidateables.add(new MergeableViewDecorator().onView(holder.view).withViewHolder(holder).withWrap(mDataset.get(position)).withMergeable(this).apply());
        else
            new RenameableViewDecorator().onView(holder.view).withViewHolder(holder).withFile(holder.actualFile).withRenameable(this).apply();
            */
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void updateWithSearchQuery(String query){
        Iterator<Wrap> iterator = mDataset.iterator();

        //Log.d("SIZE",""+mDataset.size());
        Wrap wrap;

        while(iterator.hasNext()){
            wrap = iterator.next();
            if(!wrap.name.contains(query)){
                iterator.remove();
            }
        }

        //Log.d("SIZE",""+mDataset.size());
        notifyDataSetChanged();
    }

    public void removeCutFile(int position){
        final Wrap wrap = mDataset.get(position);

        if(wrap.mediaPlayer != null && wrap.mediaPlayer.isPlaying())
            wrap.mediaPlayer.pause();

        new Thread(new Runnable() {
            @Override
            public void run() {
                wrap.actualFile.delete();
            }
        }).start();

        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeRemoved(position, mDataset.size());
    }

    public void notifyItemNotRemoved(int position){
        //notifyItemRemoved(position + 1);
        notifyItemRangeChanged(position, getItemCount());
        notifyDataSetChanged();
    }

    @Override
    public void renameFile(int position, File actualFile, String in){
        File newFile;

        actualFile.renameTo(newFile = new File(actualFile.getParentFile(), in+".mp3"));

        Wrap wrap = mDataset.get(position);
        wrap.name = newFile.getName();
        wrap.actualFile = newFile;

        notifyItemChanged(position);
    }

    @Override
    public void enqueueMerge(int position, File actualFile) {
        //Log.d("MERGE","enqueued "+position+" "+actualFile.getAbsolutePath());
        getEnqueued().add(actualFile);
    }

    @Override
    public void dequeueMerge(int position, File actualFile) {
        //Log.d("MERGE","dequeued "+position+" "+actualFile.getAbsolutePath());
        getEnqueued().remove(actualFile);
    }

    private static ArrayList<File> getEnqueued(){
        if(enqueued == null)
            enqueued = new ArrayList<>();
        return enqueued;
    }

    private static void resetEnqueued(){
        getEnqueued().clear();
    }

    public void pauseAll(){
        for(Wrap wrap : mDataset)
            if(wrap.mediaPlayer.isPlaying())
                wrap.mediaPlayer.pause();

    }

    public void invalidate(){
        resetEnqueued();
        for(Wrap wrap : mDataset)
            wrap.isSelected = false;
        for(Invalidateable invalidateable : invalidateables)
            invalidateable.invalidate();
    }

}