package com.loganfynne.clerk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

public class sourceModal extends Activity {
	View view;
	
	AlertDialog.Builder builder = new AlertDialog.Builder(this)
	    .setView(view)
        .setTitle("Add source")
        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	//Okay
                dialog.dismiss();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	//Okay
                dialog.dismiss();
            }
        });
	
	AlertDialog dialog = builder.show();
}