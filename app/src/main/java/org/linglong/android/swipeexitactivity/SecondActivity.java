package org.linglong.android.swipeexitactivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import org.linglong.ui.SwipeExitActivityLayout;

/**
 * Created by Tiandong on 2016/2/2.
 */
public class SecondActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
        Bitmap perActivitybackground = null;
            View view = MainActivity.mainActivity.getWindow().getDecorView();
            view.invalidate();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            perActivitybackground = view.getDrawingCache();
        new SwipeExitActivityLayout(this,perActivitybackground);
    }
}
