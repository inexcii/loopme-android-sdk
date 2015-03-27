package com.loopme;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class LoopMeBannerView extends FrameLayout {
	
	private static final String LOG_TAG = LoopMeBannerView.class.getSimpleName();

    private int mWidth;
    private int mHeight;

    public LoopMeBannerView(Context context) {
        super(context);
    }

    public LoopMeBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoopMeBannerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    /**
     * Sets banner size
     * @param width - width
     * @param height - height
     */
    public void setViewSize(int width, int height) {
    	mWidth = width;
    	mHeight = height;
    	
    	android.view.ViewGroup.LayoutParams params = getLayoutParams();
    	params.width = mWidth;
    	params.height = mHeight;
    	setLayoutParams(params);
    }
}
