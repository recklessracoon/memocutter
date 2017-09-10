package com.example.roman.audiocuttertest.adapters;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.roman.audiocuttertest.EditActivity;
import com.example.roman.audiocuttertest.R;
import com.example.roman.audiocuttertest.decorators.RecyclerViewAdapterWithRemoveOption;
import com.example.roman.audiocuttertest.decorators.Renameable;
import com.example.roman.audiocuttertest.decorators.RenameableViewDecorator;
import com.example.roman.audiocuttertest.io.Cutter;
import com.example.roman.audiocuttertest.io.Wrap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Roman on 03.09.2017.
 */

public class FileBrowserAdapter extends RecyclerView.Adapter<FileBrowserAdapter.ViewHolder> implements RecyclerViewAdapterWithRemoveOption, Renameable {

    private ArrayList<Wrap> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView, mTextViewTime;
        public SeekBar mSeekBar;
        public ImageButton mPlay;

        public MediaPlayer mediaPlayer;
        public Handler mHandler;

        public File actualFile;
        public View view;

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
                    Intent intent = new Intent(v.getContext(), EditActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable("theFile", actualFile);
                    intent.putExtras(b); //Put your id to your next Intent
                    v.getContext().startActivity(intent);
                }
            });

            mTextView = (TextView) view.findViewById(R.id.textViewRecycler);
            mSeekBar = (SeekBar) view.findViewById(R.id.seekBarRecycler);
            mPlay = (ImageButton) view.findViewById(R.id.imageButtonRecycler);
            mTextViewTime = (TextView) view.findViewById(R.id.nowRecycler);

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
    public FileBrowserAdapter(ArrayList<Wrap> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FileBrowserAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
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

        new RenameableViewDecorator().onView(holder.view).withFile(holder.actualFile).withRenameable(this).onPosition(position).apply();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void updateWithSearchQuery(String query){
        Iterator<Wrap> iterator = mDataset.iterator();

        Log.d("SIZE",""+mDataset.size());
        Wrap wrap;

        while(iterator.hasNext()){
            wrap = iterator.next();
            if(!wrap.name.contains(query)){
                iterator.remove();
            }
        }

        Log.d("SIZE",""+mDataset.size());
        notifyDataSetChanged();
    }

    public void removeCutFile(int position){
        final Wrap wrap = mDataset.get(position);

        new Thread(new Runnable() {
            @Override
            public void run() {
                wrap.actualFile.delete();
            }
        }).start();

        mDataset.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public void renameFile(int position, File actualFile, String in){
        boolean re;
        File newFile;

        re = actualFile.renameTo(newFile = new File(actualFile.getParentFile(), in+".mp3"));

        Wrap wrap = mDataset.get(position);
        wrap.name = newFile.getName();
        wrap.actualFile = newFile;

        notifyItemChanged(position);
    }
}