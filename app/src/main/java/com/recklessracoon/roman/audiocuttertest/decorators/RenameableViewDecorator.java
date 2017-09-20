package com.recklessracoon.roman.audiocuttertest.decorators;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.recklessracoon.roman.audiocuttertest.R;

import java.io.File;

/**
 * Created by Roman on 07.09.2017.
 */

public class RenameableViewDecorator {

    private Context context;
    private View view;
    private File actualFile;
    private Renameable renameable;
    private RecyclerView.ViewHolder holder;

    public RenameableViewDecorator(){

    }

    public RenameableViewDecorator onView(View view){
        this.view = view;
        this.context = view.getContext();
        return this;
    }

    public RenameableViewDecorator withFile(File actualFile){
        this.actualFile = actualFile;
        return this;
    }

    public RenameableViewDecorator withRenameable(Renameable renameable){
        this.renameable = renameable;
        return this;
    }
    public RenameableViewDecorator withViewHolder(RecyclerView.ViewHolder holder){
        this.holder = holder;
        return this;
    }

    public RenameableViewDecorator apply(){
        if(context != null && view != null) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    alertDialog();
                    return true;
                }
            });
        }

        return this;
    }

    private void alertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.search_rename_title)+" "+actualFile.getName());

        // Set up the input
        final EditText input = new EditText(context);
        input.setText(actualFile.getName().replace(".mp3",""));
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(context.getString(R.string.search_delete_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String in = input.getText().toString();

                renameable.renameFile(holder.getAdapterPosition(), actualFile, in);
            }
        }).setNegativeButton(context.getString(R.string.search_delete_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show(); // Show

    }
}
