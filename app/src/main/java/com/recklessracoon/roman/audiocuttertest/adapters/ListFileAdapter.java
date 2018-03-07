package com.recklessracoon.roman.audiocuttertest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.recklessracoon.roman.audiocuttertest.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Roman on 05.03.2018.
 */

public class ListFileAdapter extends BaseAdapter {

    private ArrayList<File> mFiles;
    private LayoutInflater mInflater;
    private Context mContext;

    private Stack<File[]> mPreviousLists;

    public ListFileAdapter(Context context, ArrayList<File> items){
        mFiles = items;
        this.mContext = context;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mPreviousLists = new Stack<>();
    }

    public void doClearData(){
        File[] state = new File[mFiles.size()];
        state = mFiles.toArray(state);

        mPreviousLists.push(state);

        mFiles.clear();
    }

    public boolean undoClearData(){
        if(mPreviousLists.isEmpty()){
            return false;
        }

        File[] state = mPreviousLists.pop();
        mFiles.clear();

        for(File f : state){
            mFiles.add(f);
        }

        return true;
    }

    public void addFile(File file){
        mFiles.add(file);
    }

    public void removeFile(int filePos){
        mFiles.remove(filePos);
    }

    @Override
    public int getCount() {
        return mFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return mFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View rowView = mInflater.inflate(R.layout.list_files_item, viewGroup, false);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_files_item_pic);
        TextView name = (TextView) rowView.findViewById(R.id.list_files_item_name);
        //TextView explanation = (TextView) rowView.findViewById(R.id.list_files_item_explanation);

        final File current = mFiles.get(position);

        if(!current.isDirectory()) { //change descriptions
            imageView.setImageResource(R.drawable.ic_library_music_black_24dp);
            //explanation.setText(R.string.list_files_audio);
        }

        name.setText(current.getName());

        return rowView;
    }
}
