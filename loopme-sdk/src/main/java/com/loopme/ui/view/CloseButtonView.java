package com.loopme.ui.view;

import android.app.Activity;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.loopme.R;
import com.loopme.common.Utils;

/**
 * Created by katerina on 2/3/17.
 */

public class CloseButtonView extends View {

    private static final int CLOSE_BUTTON_MARGIN = 15;
    private static final int PARENT_LAYOUT_SIZE = 80;
    private static final int IMAGE_BUTTON_SIZE = 30;
    private Activity mParentActivity;
    private RelativeLayout mRootLayout;

    public CloseButtonView(Activity activity, RelativeLayout rootLayout) {
        super(activity);
        mParentActivity = activity;
        mRootLayout = rootLayout;

    }

    public View init() {
        ImageButton closeButton = new ImageButton(mParentActivity);
        mRootLayout.setOnClickListener(mOnCloseClickListener);
        closeButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.svg_close, null));

        RelativeLayout layout = new RelativeLayout(mParentActivity);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(PARENT_LAYOUT_SIZE, PARENT_LAYOUT_SIZE));
//        layout.setOnClickListener(mOnCloseClickListener);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                Utils.convertDpToPixel(IMAGE_BUTTON_SIZE),
                Utils.convertDpToPixel(IMAGE_BUTTON_SIZE));

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.setMargins(0, Utils.convertDpToPixel(CLOSE_BUTTON_MARGIN),
                Utils.convertDpToPixel(CLOSE_BUTTON_MARGIN), 0);
        layout.setLayoutParams(layoutParams);
        layout.addView(closeButton, layoutParams);
        mRootLayout.addView(layout);
        return mRootLayout;
    }

    private OnClickListener mOnCloseClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mParentActivity.finish();
        }
    };
}
