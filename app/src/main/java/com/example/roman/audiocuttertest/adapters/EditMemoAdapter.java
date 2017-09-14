package com.example.roman.audiocuttertest.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.roman.audiocuttertest.R;
import com.example.roman.audiocuttertest.decorators.Invalidateable;
import com.example.roman.audiocuttertest.decorators.RecyclerViewAdapterWithRemoveOption;
import com.example.roman.audiocuttertest.decorators.Renameable;
import com.example.roman.audiocuttertest.decorators.RenameableViewDecorator;
import com.example.roman.audiocuttertest.io.Cutter;
import com.example.roman.audiocuttertest.io.Wrap;
import com.example.roman.audiocuttertest.theming.BackgroundStyle;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Roman on 05.09.2017.
 */

public class EditMemoAdapter extends RecyclerView.Adapter<EditMemoAdapter.ViewHolder> implements RecyclerViewAdapterWithRemoveOption, Renameable, Invalidateable {

    private ArrayList<Wrap> mDataset;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder{
            // each data item is just a string in this case
        public TextView mTextView, mTextViewTime;
        public SeekBar mSeekBar;
        public ImageButton mPlay, mShare;

        public MediaPlayer mediaPlayer;
        public Handler mHandler;

        public File actualFile;
        public View view;

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
                    // TODO decide what to do on view onclick
                }
            });

            mTextView = (TextView) view.findViewById(R.id.textViewRecycler);
            mSeekBar = (SeekBar) view.findViewById(R.id.seekBarRecycler);
            mPlay = (ImageButton) view.findViewById(R.id.imageButtonRecycler);
            mShare = (ImageButton) view.findViewById(R.id.imageButtonRecyclerChevron);
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

            mShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String sharePath = actualFile.getAbsolutePath();
                    Uri uri = Uri.parse(sharePath);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("audio/*");
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    Context context = v.getContext();
                    context.startActivity(Intent.createChooser(share, context.getString(R.string.other_share)));
                }
            });
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public EditMemoAdapter(ArrayList<Wrap> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public EditMemoAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_recycler_view_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);

        viewHolder.renameableViewDecorator = new RenameableViewDecorator();

        return viewHolder;
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

        holder.itemView.setSelected(mDataset.get(position).isSelected);

        if(holder.mediaPlayer.isPlaying()) {
            holder.mPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
            holder.mSeekBar.setProgress(holder.mediaPlayer.getCurrentPosition());
            holder.mHandler.post(holder.updateBar);
        } else {
            holder.mPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        }

        holder.renameableViewDecorator.onView(holder.view).withViewHolder(holder).withFile(holder.actualFile).withRenameable(this).apply();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addCutFile(Wrap wrap){
        mDataset.add(wrap);
        notifyItemInserted(mDataset.size()-1);
        notifyItemRangeInserted(mDataset.size()-1, mDataset.size());
        //notifyDataSetChanged();
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
        notifyItemRemoved(position);
        notifyItemRangeRemoved(position, mDataset.size());
    }

    public void notifyItemNotRemoved(int position){
        //notifyItemRemoved(position + 1);
        notifyItemRangeChanged(position, getItemCount());
        notifyDataSetChanged();
    }

    @Override
    public void renameFile(int position, File actualFile, String newName) {
        File newFile;

        actualFile.renameTo(newFile = new File(actualFile.getParentFile(), newName+".mp3"));

        Wrap wrap = mDataset.get(position);
        wrap.name = newFile.getName();
        wrap.actualFile = newFile;

        notifyItemChanged(position);
    }

    public void pauseAll(){
        for(Wrap wrap : mDataset)
            if(wrap.mediaPlayer.isPlaying())
                wrap.mediaPlayer.pause();
    }

    @Override
    public void invalidate() {

    }
}