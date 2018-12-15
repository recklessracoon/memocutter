package com.recklessracoon.roman.audiocuttertest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.recklessracoon.roman.audiocuttertest.ListFileActivity;
import com.recklessracoon.roman.audiocuttertest.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

/**
 * Created by Roman on 05.03.2018.
 */

public class ListFileAdapter extends BaseAdapter {

    private ArrayList<File> mFiles;
    private int size;
    private LayoutInflater mInflater;
    private Context mContext;

    private Stack<File[]> mPreviousLists;

    private Comparator<File> myComparator = new Comparator<File>() {
        @Override
        public int compare(File file, File t1) {
            if(file == t1)
                return 0;
            if (file == null)
                return -1;
            if (t1 == null)
                return 1;

            if(file.isDirectory() && !t1.isDirectory()){
                return -1;
            }
            if(!file.isDirectory() && t1.isDirectory()){
                return 1;
            }

            try {
                String name1 = file.getName();
                name1 = name1.substring(0, name1.lastIndexOf("."));
                String name2 = t1.getName();
                name2 = name2.substring(0, name2.lastIndexOf("."));

                int i1 = Integer.parseInt(name1);
                int i2 = Integer.parseInt(name2);

                return i1-i2;
            } catch (Exception e){

            }

            return file.getName().compareToIgnoreCase(t1.getName());
        }
    };


    public ListFileAdapter(Context context, ArrayList<File> items){
        mFiles = items;

        this.mContext = context;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mPreviousLists = new Stack<>();
        size = mFiles.size();
    }

    public void sortFilesByName(){
        //Directories ganz oben, alles Alphabetisch Comparator
        Collections.sort(mFiles, myComparator);
        notifyDataSetChanged();
    }

    public void doClearData(){
        File[] state = new File[mFiles.size()];
        state = mFiles.toArray(state);

        mPreviousLists.push(state);

        mFiles.clear();
        notifyDataSetChanged();
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

        notifyDataSetChanged();
        return true;
    }

    @Override
    public void notifyDataSetChanged(){
        size = mFiles.size();
        super.notifyDataSetChanged();
    }

    public void addFile(File file){
        mFiles.add(file);
        notifyDataSetChanged();
    }

    public void removeFile(int filePos){
        mFiles.remove(filePos);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return size;
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

        if(!current.isDirectory() && ListFileActivity.isSupportedAudioFile(current)) { //change descriptions
            imageView.setImageResource(R.drawable.ic_library_music_black_24dp);
            //explanation.setText(R.string.list_files_audio);
        } else if(!current.isDirectory()){
            imageView.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
        }

        name.setText(current.getName());

        return rowView;
    }
}
