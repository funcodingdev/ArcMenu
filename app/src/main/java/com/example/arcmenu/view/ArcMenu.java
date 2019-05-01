package com.example.arcmenu.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.example.arcmenu.R;

public class ArcMenu extends ViewGroup implements View.OnClickListener {
    private static final String TAG = "ArcMenu";
    private final int LEFT_TOP = 0;
    private final int LEFT_BOTTOM = 1;
    private final int RIGHT_TOP = 2;
    private final int RIGHT_BOTTOM = 3;

    private Position mPosition = Position.RIGHT_BOTTOM;
    private int mRadius;
    private Status mCurrentStatus = Status.CLOSE;
    private View mCButton;
    private OnMenuItemClickListener menuItemClickListener;


    public boolean isOpen() {
        return (mCurrentStatus == Status.OPEN ? true : false);
    }

    public void setMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
    }

    /**
     * 菜单的状态
     */
    public enum Status {
        OPEN, CLOSE
    }

    /**
     * 菜单的位置
     */
    public enum Position {
        LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM
    }

    /**
     * 点击子菜单项的回调接口
     */
    public interface OnMenuItemClickListener {
        void onClick(View view, int pos);
    }

    public ArcMenu(Context context) {
        this(context, null);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //设置半径的默认值
        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        //获取自定义属性的值
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ArcMenu, defStyleAttr, 0);
        int pos = a.getInt(R.styleable.ArcMenu_position, 3);
        switch (pos) {
            case LEFT_TOP: {
                mPosition = Position.LEFT_TOP;
                break;
            }
            case LEFT_BOTTOM: {
                mPosition = Position.LEFT_BOTTOM;
                break;
            }
            case RIGHT_TOP: {
                mPosition = Position.RIGHT_TOP;
                break;
            }
            case RIGHT_BOTTOM: {
                mPosition = Position.RIGHT_BOTTOM;
                break;
            }
        }
        mRadius = (int) a.getDimension(R.styleable.ArcMenu_radius,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
        Log.d(TAG, "ArcMenu: " + mPosition + ":" + mRadius);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            layoutCButton();
            int count = getChildCount();
            for (int i = 0; i < count - 1; i++) {
                View child = getChildAt(i + 1);

                child.setVisibility(View.GONE);

                int cl = (int) (mRadius * Math.sin(Math.PI / 2 / (count - 2) * i));
                int ct = (int) (mRadius * Math.cos(Math.PI / 2 / (count - 2) * i));

                int cWidth = child.getMeasuredWidth();
                int cHeight = child.getMeasuredHeight();

                //如果菜单在左下、右下
                if (mPosition == Position.LEFT_BOTTOM || mPosition == Position.RIGHT_BOTTOM) {
                    ct = getMeasuredHeight() - cHeight - ct;
                }
                //右上、右下
                if (mPosition == Position.RIGHT_TOP || mPosition == Position.RIGHT_BOTTOM) {
                    cl = getMeasuredWidth() - cWidth - cl;
                }
                child.layout(cl, ct, cl + cWidth, ct + cHeight);
            }
        }
    }

    /**
     * 定义主菜单
     */
    private void layoutCButton() {
        mCButton = getChildAt(0);
        mCButton.setOnClickListener(this);
        int l = 0;
        int t = 0;

        int width = mCButton.getMeasuredWidth();
        int height = mCButton.getMeasuredHeight();

        switch (mPosition) {
            case LEFT_TOP: {
                l = 0;
                t = 0;
                break;
            }
            case LEFT_BOTTOM: {
                l = 0;
                t = getMeasuredHeight() - height;
                break;
            }
            case RIGHT_TOP: {
                l = getMeasuredWidth() - width;
                t = 0;
                break;
            }
            case RIGHT_BOTTOM: {
                l = getMeasuredWidth() - width;
                t = getMeasuredHeight() - height;
                break;
            }
        }

        mCButton.layout(l, t, l + width, t + height);
    }

    @Override
    public void onClick(View v) {
        rotateCButton(v, 0f, 360f, 300);
        toggleMenu(300);
    }

    /**
     * 切换菜单
     */
    public void toggleMenu(int duration) {
        //为menuItem添加平移动画和旋转动画
        int count = getChildCount();
        for (int i = 0; i < count - 1; i++) {
            final View childView = getChildAt(i + 1);
            childView.setVisibility(View.VISIBLE);
            //end(0,0)
            //start
            int cl = (int) (mRadius * Math.sin(Math.PI / 2 / (count - 2) * i));
            int ct = (int) (mRadius * Math.cos(Math.PI / 2 / (count - 2) * i));

            int xFlag = 1;
            int yFlag = 1;

            if (mPosition == Position.LEFT_TOP || mPosition == Position.LEFT_BOTTOM) {
                xFlag = -1;
            }

            if (mPosition == Position.LEFT_TOP || mPosition == Position.RIGHT_TOP) {
                yFlag = -1;
            }

            AnimationSet animset = new AnimationSet(true);
            Animation tranAnim = null;
            //判断当前状态
            if (mCurrentStatus == Status.CLOSE) {//去打开
                tranAnim = new TranslateAnimation(xFlag * cl, 0, yFlag * ct, 0);
                childView.setClickable(true);
                childView.setFocusable(true);
            } else {//去关闭
                tranAnim = new TranslateAnimation(0, xFlag * cl, 0, yFlag * ct);
                childView.setClickable(false);
                childView.setFocusable(false);
            }
            tranAnim.setDuration(duration);
            tranAnim.setFillAfter(true);
            tranAnim.setStartOffset((i * 100) / count);
            //平移动画
            tranAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mCurrentStatus == Status.CLOSE) {
                        childView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            //旋转动画
            RotateAnimation rotateAnim = new RotateAnimation(0, 720,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnim.setDuration(duration);
            rotateAnim.setFillAfter(true);
            animset.addAnimation(rotateAnim);//先旋转
            animset.addAnimation(tranAnim);//后平移
            childView.startAnimation(animset);
            final int pos = i+1;
            childView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(menuItemClickListener != null){
                        menuItemClickListener.onClick(v,pos);
                    }
                    menuItemAnim(pos - 1);
                    changMenuStatus();
                }
            });
        }
        //切换菜单状态
        changMenuStatus();
    }

    /**
     * 子项事件监听
     * @param pos
     */
    private void menuItemAnim(int pos) {
        final int count = getChildCount();
        for (int i = 0; i < count-1; i++) {
            View childView = getChildAt(i+1);
            if(i == pos){
                childView.startAnimation(scaleBigAnim(300));
            }else{
                childView.startAnimation(scaleSmallAnim(300));
            }
            childView.setClickable(false);
            childView.setFocusable(false);
        }
    }

    /**
     * 为当前点击的Item设置变小和透明度降低的动画
     * @param duration
     * @return
     */
    private Animation scaleSmallAnim(int duration) {
        AnimationSet animationSet = new AnimationSet(true);
        //缩放动画
        ScaleAnimation scaleAnim = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
                Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        //透明度动画
        AlphaAnimation alphaAnim = new AlphaAnimation(1.0f,0.0f);
        animationSet.addAnimation(scaleAnim);
        animationSet.addAnimation(alphaAnim);
        animationSet.setDuration(duration);
        animationSet.setFillAfter(true);
        return animationSet;
    }

    /**
     * 为当前点击的Item设置变大和透明度降低的动画
     * @param duration
     * @return
     */
    private Animation scaleBigAnim(int duration) {
        AnimationSet animationSet = new AnimationSet(true);
        //缩放动画
        ScaleAnimation scaleAnim = new ScaleAnimation(1.0f,4.0f,1.0f,4.0f,
                Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        //透明度动画
        AlphaAnimation alphaAnim = new AlphaAnimation(1.0f,0.0f);
        animationSet.addAnimation(scaleAnim);
        animationSet.addAnimation(alphaAnim);
        animationSet.setDuration(duration);
        animationSet.setFillAfter(true);
        return animationSet;
    }

    /**
     * 切换菜单状态
     */
    private void changMenuStatus() {
        mCurrentStatus = (mCurrentStatus == Status.OPEN ? Status.CLOSE : Status.OPEN);
    }

    /**
     * 旋转动画
     *
     * @param v
     * @param start
     * @param end
     * @param duration
     */
    private void rotateCButton(View v, float start, float end, int duration) {
        RotateAnimation anim = new RotateAnimation(start, end,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(duration);
        anim.setFillAfter(true);
        v.startAnimation(anim);
    }
}
