package com.loopme;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class LoopMeBannerView extends FrameLayout {
	
    public LoopMeBannerView(Context context) {
        super(context);
    }

    public LoopMeBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoopMeBannerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public LoopMeBannerView(Context context, int width, int height) {
    	super(context);
    	LayoutParams params = new LayoutParams(width, height);
    	setLayoutParams(params);
	}

	/**
     * Sets banner size
     * @param width - width
     * @param height - height
     */
    public void setViewSize(int width, int height) {
    	android.view.ViewGroup.LayoutParams params = getLayoutParams();
    	params.width = width;
    	params.height = height;
    	setLayoutParams(params);
    }
}
