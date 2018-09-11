package com.example.liunian.opendoorview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;


import java.util.ArrayList;
import java.util.List;

public class OpenDoorView extends View {
    Context mContext;
    Bitmap bitmap;//中间锁图标的bitmap

    float scale = 1;  //锁图标缩放比例

    float startAngle = 90;  //循环滚动的渐变圆环开始角度。

    int ringColor = R.color.markerColor;  //循环滚动的渐变圆环的开始颜色

    Paint markerPaint;//刻度画笔

    Paint circlePaint;//圆形画笔

    Paint circleAnimorPaint; //扇形滚动图画笔

    Paint gourPaint; //打钩画笔

    int progress = 1;//进度

    boolean isFinish = false;//整个动画是否已经结束，用来停止onDraw的继续调用

    int px;//圆心
    int py;//圆心
    int radius;//半径
    SweepGradient lg;//循环滚动的渐变圆环的渐变支持类，用的是扇形渐变
    ValueAnimator animRing;
    AnimatorSet animatorSet;
    Matrix matrixBitmap = new Matrix();
    Path path;//已经指定的打钩绘制路径
    Path mDst;//根据PathMeasure计算出来的实时路径，用来绘制打钩
    PathMeasure mPathMeasure;

    public OpenDoorView(Context context) {
        super(context);
        mContext = context;
        initCompassView();
    }

    public OpenDoorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initCompassView();
    }

    public OpenDoorView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        mContext = context;
        initCompassView();
    }

    protected void initCompassView() {
        setFocusable(true);

        // 设置实心圆画笔
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.WHITE);
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        // 设置线条画笔 刻度
        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(mContext.getResources().getColor(R.color.markerColor));
        markerPaint.setStrokeWidth(dip2px(1));

        circleAnimorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleAnimorPaint.setStrokeWidth(dip2px(4));
        circleAnimorPaint.setStyle(Paint.Style.STROKE);

        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.pic_kaisuo);

        gourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gourPaint.setStrokeWidth(dip2px(4));
        gourPaint.setColor(mContext.getResources().getColor(R.color.markerColorFinish));
        gourPaint.setStyle(Paint.Style.STROKE);
    }

    public void startAnimator() {
        progress = 1;
        scale = 1;
        startAngle = 90;
        isFinish = false;
        startRingAnimator();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);
        int d = Math.min(measuredWidth, measuredHeight);
        setMeasuredDimension(d, d);
    }

    private int measure(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            result = 200;
        } else {
            result = specSize;
        }
        // 圆心坐标
        px = getMeasuredWidth() / 2;
        py = getMeasuredHeight() / 2;
        // 半径 取最小值
        radius = Math.min(px, py);

        path = new Path();
        path.moveTo(px - 30, py - 30);
        path.lineTo(px, py);
        path.lineTo(px + 60, py - 50);

        mDst = new Path();
        mDst.reset();
        mDst.moveTo(px - 20, py - 20);
        //因为上面调用了mPathMeasure.nextContour()，所以需要重置
        mPathMeasure = new PathMeasure(path, false);
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // 画圆
        canvas.drawCircle(px, py, radius, circlePaint);
        canvas.save();
        //画60个刻度
        for (int i = 0; i < progress; i++) {
            //画刻度
            canvas.drawLine(px, py - radius, px, py - radius + dip2px(8), markerPaint);
            //每隔15度旋转一下
            canvas.rotate(-6, px, py);
        }
        canvas.restore();

        Matrix matrix = new Matrix();
        matrix.reset();//每次都要重置，不然颜色渐变起始位置不起作用
        matrix.preRotate(startAngle, px, py);  //颜色起始角度
        lg = new SweepGradient(px, py, mContext.getResources().getColor(ringColor), Color.TRANSPARENT);
        lg.setLocalMatrix(matrix);
        circleAnimorPaint.setShader(lg);
        RectF rectARC = new RectF(dip2px(8) + dip2px(4) / 2 + 30, dip2px(8) + dip2px(4) / 2 + 30, radius * 2 - dip2px(8) - dip2px(4) / 2 - 30, radius * 2 - dip2px(8) - dip2px(4) / 2 - 30);
        canvas.drawArc(rectARC, startAngle, 360, false, circleAnimorPaint);

        matrixBitmap.reset();
        matrixBitmap.preTranslate(px - bitmap.getWidth() / 2 * scale, py - bitmap.getHeight() / 2 * scale);
        matrixBitmap.preScale(scale, scale);
        canvas.drawBitmap(bitmap, matrixBitmap, null);

        if (scale == 0) {
            canvas.drawPath(mDst, gourPaint);
        }

        if (!isFinish) {
            postInvalidateDelayed(10);
        }
    }


    //开始循环滚动渐变圆环的动画
    private void startRingAnimator() {
        animRing = new ValueAnimator().ofFloat(360f, 0f);
        animRing.setDuration(2000);// 设置动画运行的时长
        animRing.setStartDelay(0); // 设置动画延迟播放时间
        animRing.setRepeatCount(ValueAnimator.INFINITE);// 动画播放次数 = infinite时,动画无限重复
        animRing.setRepeatMode(ValueAnimator.RESTART); // 设置重复播放动画模式。ValueAnimator.RESTART(默认):正序重放;ValueAnimator.REVERSE:倒序回放
        animRing.setInterpolator(new LinearInterpolator());//设置匀速差插值器
        animRing.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentValue = (float) animation.getAnimatedValue();
                // 获得改变后的值
                setStartAngle(currentValue);
            }
        });
        animRing.start();
    }


    private ValueAnimator getImageChange() {
        final ValueAnimator animImage = new ValueAnimator().ofFloat(1f, 1.5f, 0f);//先放大然后再慢慢缩小消失
        animImage.setDuration(1000);// 设置动画运行的时长
        animImage.setStartDelay(0); // 设置动画延迟播放时间
        animImage.setRepeatCount(0);// 动画播放次数 = infinite时,动画无限重复
        animImage.setInterpolator(new LinearInterpolator());
        animImage.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentValue = (float) animation.getAnimatedValue();
                scale = currentValue;
            }
        });
        return animImage;
    }


    private ValueAnimator getGouChange() {
        ValueAnimator animGou = ValueAnimator.ofFloat(0, 1);
        //平滑过渡
        animGou.setInterpolator(new LinearInterpolator());
        animGou.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float mAnimatorValue = (float) valueAnimator.getAnimatedValue();
                //实时路径的百分百比长度0->mPathMeasure.getLength()
                float currentLength = mPathMeasure.getLength() * mAnimatorValue;
                //将对应长度currentLength的路径获取并传递给mDst
                mPathMeasure.getSegment(0, currentLength, mDst, true);
            }
        });
        animGou.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animRing != null) {
                    animRing.cancel();
                }
                isFinish = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return animGou;
    }


    private void startFinishAnimator() {
        animatorSet = new AnimatorSet();
        List<Animator> valueAnimators = new ArrayList<>();
        valueAnimators.add(getImageChange());
        valueAnimators.add(getGouChange());
        animatorSet.playSequentially(valueAnimators);
        animatorSet.start();
    }


    private final int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (progress >= 40) {
            this.ringColor = R.color.markerColorFinish;
        }
        if (progress == 60) {
            startFinishAnimator();
        }
    }

}