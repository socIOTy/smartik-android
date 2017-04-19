package com.socioty.smartik;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by serhiipianykh on 2017-04-19.
 */

public class CustomProgressDialog extends Dialog {
    private ImageView mImageView;

    public CustomProgressDialog(Context context) {
        super(context, R.style.CustomProgressDialog);
        WindowManager.LayoutParams manager = getWindow().getAttributes();
        getWindow().setAttributes(manager);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getWindow().getAttributes().width,getWindow().getAttributes().height);
        mImageView = new ImageView(context);
        mImageView.setBackgroundResource(R.drawable.progressdialog_animation);
        layout.addView(mImageView);
        addContentView(layout,params);

    }

    @Override
    public void show() {
        super.show();
        AnimationDrawable frameAnimation = (AnimationDrawable)mImageView.getBackground();
        frameAnimation.start();

    }
}
