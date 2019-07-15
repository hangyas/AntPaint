package net.hangyas.antpaint.app;

import android.support.multidex.MultiDexApplication;
import com.facebook.FacebookSdk;

/**
 * Created by hangyas on 2015-06-28
 */

public class AntPaint extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}