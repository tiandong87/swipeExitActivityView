package org.linglong.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;

/**
 * Created by Tiandong on 2016/1/29.
 */
public class SwipeExitActivityLayout extends FrameLayout {
    // 主要界面
    private View mContentView;
    // 感应区域
    private int inductionArea;
    // 感应区域
    private int inductionAreaRate;
    // 按下位置，如果在感应区域内才能满足
    private int downX;
    private int downY;
    private int tempX;
    // 按下时间
    private long timeDown;

    // 手指收起结束动画
    private Scroller mScrollerMainView;

    // 整体宽度
    private int viewWidth;
    private boolean isSilding;
    private boolean isFinish;

    // 当前Acitvity
    private Activity mActivity;

    // 阴影绘制
    private Paint shadowPaint;

    // 一些配置 阴影宽度 快速滑动退出时间 结束动画时间
    private static final int shadowWidth = 25;
    private static final long exitTime = 200;
    private static final int timeForEnd = 300;

    // 底部背景
    ImageView PerImageView;

    private boolean isSrcollAuto = false;

    public SwipeExitActivityLayout(Context context, Bitmap perActivitybackground) {
        super(context);
        inductionArea = ViewConfiguration.get(context).getScaledTouchSlop();
        inductionAreaRate = 3;
        if (perActivitybackground != null) {
            PerImageView = new ImageView(context);
            PerImageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            PerImageView.setImageBitmap(perActivitybackground);
        } else {
            // 如果没有背景图，感应区域增加3倍
            inductionAreaRate = 6;
        }
        int shadowColor = Color.argb(120, 0, 0, 0);
        shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        /**
         * 解决旋转时的锯齿问题
         */
        shadowPaint.setFilterBitmap(true);
        shadowPaint.setDither(true);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(shadowColor);
        LinearGradient lg = new LinearGradient(0, 0, shadowWidth, 0,
                shadowColor, Color.TRANSPARENT, TileMode.MIRROR);
        shadowPaint.setShader(lg);

        mScrollerMainView = new Scroller(context);
        setMainActivity((Activity) context);
    }

    public SwipeExitActivityLayout(Context context) {
        this(context, null);
    }

    private void setMainActivity(Activity activity) {
        mActivity = activity;
        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
        decorChild.setBackgroundColor(Color.TRANSPARENT);
        decor.removeView(decorChild);
        addView(decorChild);
        setContentView(decorChild);
       if (PerImageView != null) decor.addView(PerImageView);
        decor.addView(this);
    }

    private void setContentView(View decorChild) {
        mContentView = (View) decorChild.getParent();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int left = mContentView.getLeft() - shadowWidth;
        int right = left + shadowWidth;
        int top = mContentView.getTop();
        int bottom = mContentView.getBottom();
        Rect rec = new Rect(left, top, right, bottom);
        canvas.drawRect(rec, shadowPaint);
    }

    /**
     * 事件拦截操作
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isSrcollAuto) {
            return super.onInterceptTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = tempX = (int) ev.getRawX();
                downY = (int) ev.getRawY();
                timeDown = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getRawX();
                // 满足此条件屏蔽SildingFinishLayout里面子类的touch事件
                if (moveX - downX > 0
                        && Math.abs((int) ev.getRawY() - downY) < inductionArea
                        && downX < inductionArea * inductionAreaRate) {
                    return true;
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    private int getPerImageScrollX() {
        return (viewWidth - Math.abs(mContentView.getScrollX())) / 4;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isSrcollAuto) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) event.getRawX();
                int deltaX = tempX - moveX;
                tempX = moveX;
                if (moveX - downX > 0
                        && Math.abs((int) event.getRawY() - downY) < inductionArea
                        && downX < inductionArea * inductionAreaRate) {
                    isSilding = true;
                }

                // 如果滑动了并且有底层图片 绘制滑动效果
                if (isSilding && PerImageView != null) {
                    if (mContentView.getScrollX() + deltaX >= 0) {
                        deltaX = -mContentView.getScrollX();
                    }
                    mContentView.scrollBy(deltaX, 0);
                    PerImageView.scrollTo(getPerImageScrollX(), 0);
                }
                break;
            case MotionEvent.ACTION_UP:
                downX = 0;
                downY = 0;
                tempX = 0;
                // 如果滑动时间很短直接退出
                if (isSilding && (exitTime > System.currentTimeMillis() - timeDown)) {
                    isFinish = true;
                    isSilding = false;
                    if (PerImageView != null) {
                        scrollRight();
                    } else {
                        mActivity.finish();
                        mActivity.overridePendingTransition(
                                android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right);
                    }
                    return true;
                }
                isSilding = false;
                if (PerImageView != null) {
                    if (mContentView.getScrollX() <= -viewWidth / 3) {
                        isFinish = true;
                        scrollRight();
                    } else {
                        scrollOrigin();
                        isFinish = false;
                    }
                }
                break;
        }

        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            viewWidth = this.getWidth();
        }
    }

    /**
     * 滚动出界面
     */
    private void scrollRight() {
        isSrcollAuto = true;
        final int delta = (viewWidth + mContentView.getScrollX());
        // 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
        mScrollerMainView.startScroll(mContentView.getScrollX(), 0, -delta + 1,
                0, timeForEnd);
        postInvalidate();
    }

    /**
     * 滚动到起始位置
     */
    private void scrollOrigin() {
        isSrcollAuto = true;
        int delta = mContentView.getScrollX();
        mScrollerMainView.startScroll(mContentView.getScrollX(), 0, -delta, 0,
                timeForEnd);
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        // 调用startScroll的时候scroller.computeScrollOffset()返回true，
        if (mScrollerMainView.computeScrollOffset()) {
            mContentView.scrollTo(mScrollerMainView.getCurrX(),
                    mScrollerMainView.getCurrY());
            if (PerImageView != null && isFinish)
                PerImageView.scrollTo(getPerImageScrollX(), 0);
            postInvalidate();

            if (mScrollerMainView.isFinished() && isFinish){
                mActivity.finish();
                mActivity.overridePendingTransition(0, 0);
            } else if (mScrollerMainView.isFinished()) {
                isSrcollAuto = false;
                if (PerImageView != null)
                    PerImageView.scrollTo(0, 0);
            }
        }
    }
}
