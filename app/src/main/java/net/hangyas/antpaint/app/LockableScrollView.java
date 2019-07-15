package net.hangyas.antpaint.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by hangyas on 2015-07-06
 */
public class LockableScrollView extends ScrollView {

    private boolean enableScrolling = true;

    public boolean isEnableScrolling() {
        return enableScrolling;
    }

    public void setEnableScrolling(boolean enableScrolling) {
        this.enableScrolling = enableScrolling;
    }

    public void toggleEnableScrolling(){
        enableScrolling = !enableScrolling;
    }

    public LockableScrollView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LockableScrollView (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockableScrollView (Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isEnableScrolling()) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isEnableScrolling()) {
            return super.onTouchEvent(ev);
        } else {
            return false;
        }
    }

    public void scrollDown(){
        final LockableScrollView self = this;
        post(new Runnable() {
            @Override
            public void run() {
                self.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
