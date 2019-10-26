package com.tanyiqu.filesafe.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.tanyiqu.filesafe.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class NineLockView extends View {

    public static final int NORMAL_COLOR = 0xFFFFFFFF;
    public static final int SELECTED_COLOR = 0xFF70DBDB;
    public static final int WRONG_COLOR = 0xFFFF0000;

    private Paint circlePaintNormal;
    private Paint circlePaintWrong;
    private Paint circlePaintSelected;
    private Paint currCirclePaint;
    private Paint linePaintNormal;
    private Paint linePaintWrong;
    private Paint currLinePaint;
    private float radiusNormal;
    private float radiusSelect;
    private float radiusCheck;//检测半径

    private PointView[][] pointViewArray = new PointView[3][3];

    private List<PointView> selectedPointList;

    private int viewWidth;

    private int index = 1;

    //正在移动 并且没有连接到下一个点
    private boolean isMovingWithoutCircle = false;

    private boolean isFinished;

    private float currX,currY;

    private OnPatternChangedListener onPatternChangedListener;

    //是否被激活
    private boolean isSelected;

    private boolean isWrong;

    private boolean refresh = false;

    public NineLockView(Context context) {
        super(context);
    }

    public NineLockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //正常圆的画笔
        circlePaintNormal = new Paint();
        circlePaintNormal.setAntiAlias(true);
        circlePaintNormal.setDither(true);
        circlePaintNormal.setColor(NORMAL_COLOR);
        circlePaintNormal.setStyle(Paint.Style.FILL);
        //错误圆
        circlePaintWrong = new Paint();
        circlePaintWrong.setAntiAlias(true);
        circlePaintWrong.setDither(true);
        circlePaintWrong.setColor(WRONG_COLOR);
        circlePaintWrong.setStyle(Paint.Style.FILL);
        //被选中圆
        circlePaintSelected = new Paint();
        circlePaintSelected.setAntiAlias(true);
        circlePaintSelected.setDither(true);
        circlePaintSelected.setColor(SELECTED_COLOR);
        circlePaintSelected.setStyle(Paint.Style.FILL);
        //线的画笔
        linePaintNormal = new Paint();
        linePaintNormal.setAntiAlias(true);
        linePaintNormal.setDither(true);
        linePaintNormal.setStrokeWidth(20);
        linePaintNormal.setColor(SELECTED_COLOR);
        linePaintNormal.setStyle(Paint.Style.STROKE);
        linePaintWrong = new Paint();
        linePaintWrong.setAntiAlias(true);
        linePaintWrong.setDither(true);
        linePaintWrong.setStrokeWidth(20);
        linePaintWrong.setColor(WRONG_COLOR);
        linePaintWrong.setStyle(Paint.Style.STROKE);
        radiusNormal = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        radiusSelect = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        radiusCheck = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());
        selectedPointList = new ArrayList<>();
        isWrong = false;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = Math.min(getMeasuredHeight(),getMeasuredWidth());
        setMeasuredDimension(viewWidth,viewWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画所有正常点
        drawCircle(canvas);
        if(refresh){
            super.onDraw(canvas);
            refresh = false;
            return;
        }
        //判断是正常的还是错误的
        if(isWrong){//错误
            currLinePaint = linePaintWrong;
            currCirclePaint = circlePaintWrong;
        }else{
            currLinePaint = linePaintNormal;
            currCirclePaint = circlePaintSelected;
        }
        //画连过的点
        for(PointView pointView : selectedPointList){
            canvas.drawCircle(pointView.x,pointView.y,radiusSelect,currCirclePaint);
        }
        //画点与点的连线
        if(selectedPointList.size() > 0){
            Point p1 = selectedPointList.get(0);
            //依次连接起来
            for(int i=0;i<selectedPointList.size();i++){
                Point p2 = selectedPointList.get(i);
                drawLine(canvas,p1,p2,currLinePaint);
                p1 = p2;
            }
            //画当前最后连接的点与手指的连线
            if(isMovingWithoutCircle & !isFinished){
                drawLine(canvas,p1,new PointView((int)currX,(int)currY),currLinePaint);
            }
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        currX = event.getX();
        currY = event.getY();
        isWrong = false;
        PointView selectedPointView = null;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //开始绘制
                //清空选中的点的列表
                selectedPointList.clear();
                isFinished = false;
                selectedPointView = checkSelectPoint();
                if(selectedPointView != null){
                    isSelected = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isSelected){
                    selectedPointView = checkSelectPoint();
                }
                if (selectedPointView == null){
                    isMovingWithoutCircle = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                //绘制完成
                isFinished = true;
                //取消激活状态
                isSelected = false;
                break;
        }
        //未完成 已被激活
        if(!isFinished && isSelected && selectedPointView!=null){
            if(!selectedPointList.contains(selectedPointView)){
                selectedPointList.add(selectedPointView);
            }
        }
        //已完成
        if(isFinished){
            int count = selectedPointList.size();
            //只连了一个点
            if(count == 1){
                selectedPointList.clear();
            }else if(count>0 && count<4){
                //连接点数不够
                if(onPatternChangedListener != null){
                    onPatternChangedListener.onPatternChanged(this,null);
                }
            }else {//绘制完成
                String passwd;
                if(onPatternChangedListener != null){
                    StringBuilder passwdBuilder = new StringBuilder();
                    for (PointView pointView : selectedPointList){
                        passwdBuilder.append(pointView.getIndex());
                    }
                    passwd = passwdBuilder.toString();
                    if(!TextUtils.isEmpty(passwd)){
                        onPatternChangedListener.onPatternChanged(this,passwd);
                    }
                }
            }
        }
        //刷新
        invalidate();
        return true;
    }

    //画出默认的所有点
    private void drawCircle(Canvas canvas) {
        //初始点的位置
        for (int i = 0; i < pointViewArray.length; i++) {
            for (int j = 0; j < pointViewArray.length; j++) {
                //圆心坐标
                int cx = viewWidth / 4 * (j + 1);
                int cy = viewWidth / 4 * (i + 1);
                //将圆心放在一个点数组中
                PointView pointView = new PointView(cx, cy);
                pointView.setIndex(index);
                pointViewArray[i][j] = pointView;
                canvas.drawCircle(cx, cy, radiusNormal, circlePaintNormal);
                index++;
            }
        }
        index = 1;
    }

    //将两个点连接起来
    private void drawLine(Canvas canvas, Point pointA, Point pointB,Paint paint) {
        canvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paint);
    }

    //返回已经连过的点
    private PointView checkSelectPoint() {
        for (int i = 0; i <3; i++) {
            for (int j = 0; j < 3; j++) {
                PointView pointView = pointViewArray[i][j];
                if (isWithInCircle(currX, currY,pointView.x,pointView.y,radiusCheck)) {
                    return pointView;
                }
            }
        }
        return null;
    }

    //当前手指按下的位置，是否在圆内
    private boolean isWithInCircle(float currX, float currY, int x, int y, float mRadius) {
        //如果点和圆心的距离 小于半径，则证明在圆内
        if(Math.sqrt(Math.pow(x - currX, 2) + Math.pow(y - currY, 2)) < mRadius){
//            Util.vibrate(getContext(),10);
            return true;
        }
        return false;
    }

    //刷新视图
    public void refreshView(boolean rightnow){
        if(rightnow){
            refresh = true;
            invalidate();
        }else {
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    refresh = true;
                    invalidate();
                }
            }, 800);
        }
    }

    public void setWrong(){
        isWrong = true;
    }

    public void setOnPatternChangedListener(OnPatternChangedListener onPatternChangedListener){
        this.onPatternChangedListener = onPatternChangedListener;
    }

    /**
     * 点类
     */
    public class PointView extends Point {
        //用于转化密码的下标
        int index;

        PointView(int x, int y) {
            super(x, y);
        }

        int getIndex() {
            return index;
        }

        void setIndex(int index) {
            this.index = index;
        }
    }

    /**
     * 监听器接口
     */
    public interface OnPatternChangedListener {

        void onPatternChanged(NineLockView nineLockView, String passwd);

    }

}
