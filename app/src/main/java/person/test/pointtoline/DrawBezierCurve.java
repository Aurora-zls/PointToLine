package person.test.pointtoline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawBezierCurve extends View {

    private static final int POINT_NUMBER = 4;
    //线画笔
    private Paint linePaint;
    //点画笔
    private Paint pointPaint;
    private Paint circlePaint;
    //是否首次绘制
    private boolean isFirstDraw = true;
    boolean isThrough = false;

    private Point[] pointList = new Point[POINT_NUMBER];
    private double[] distanceList = new double[POINT_NUMBER];

    private Path mPath;

    private float eventX, eventY;
    private static final float TOUCH_TOLERANCE = 4;

    public DrawBezierCurve(Context context) {
        this(context, null);
    }

    public DrawBezierCurve(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public DrawBezierCurve(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //线的Paint
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5);
        linePaint.setColor(Color.GREEN);

        //点的paint
        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setStrokeWidth(25);
        pointPaint.setColor(Color.BLACK);

        //圆的paint
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(10);
        circlePaint.setColor(Color.BLUE);
        //路径
        mPath = new Path();
    }

    public DrawBezierCurve(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(x, y, event);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y, event);
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        }
        invalidate();
        return true;
    }

    //手指按下时
    private void touchDown(float x, float y, MotionEvent event) {
        //每次点击事件重置连线过程
        mPath.reset();
        isThrough = false ;
        for( Point point :pointList){
            point.setChoose(false);
        }

        eventX = event.getX();
        eventY = event.getY();

        for (int i = 0; i < POINT_NUMBER; i++) {
            double xDis = pointList[i].getPointX() - eventX;
            double yDis = pointList[i].getPointY() - eventY;
            distanceList[i] = Math.sqrt(Math.pow(xDis, 2) + Math.pow(yDis, 2));
            if (distanceList[i] <= 35){
                mPath.moveTo(pointList[i].getPointX(), pointList[i].getPointY());
                pointList[i].setChoose(true);
                isThrough = true;
            }
        }

    }

    //手指移动时
    private void touchMove(float x, float y, MotionEvent event) {

        if (isThrough == true){
            float endX = (event.getX() - eventX) / 2 + eventX;
            float endY = (event.getY() - eventY) / 2 + eventY;
            for (int i = 0; i < POINT_NUMBER; i++) {
                double xDis = pointList[i].getPointX() - eventX;
                double yDis = pointList[i].getPointY() - eventY;
                distanceList[i] = Math.sqrt(Math.pow(xDis, 2) + Math.pow(yDis, 2));
                if (distanceList[i] <= 35 ){
                    mPath.quadTo(pointList[i].getPointX(), pointList[i].getPointY(), pointList[i].getPointX(), pointList[i].getPointY());
                    pointList[i].setChoose(true);
                }else {
                    mPath.quadTo(eventX, eventY, endX, endY);
                }
            }
            eventX = event.getX();
            eventY = event.getY();
        }
    }

    //手指抬起
    private void touchUp() {
        //mPath.lineTo(eventX, eventY);
        if (isThrough == true){
            for (int i = 0; i < POINT_NUMBER; i++) {
                double xDis = pointList[i].getPointX() - eventX;
                double yDis = pointList[i].getPointY() - eventY;
                distanceList[i] = Math.sqrt(Math.pow(xDis, 2) + Math.pow(yDis, 2));
            }
            //距离最近点索引
            int index = (int) getMinIndex(distanceList)[1];
            Point minDistancePoint = pointList[index];
//            mPath.lineTo(minDistancePoint.getPointX(), minDistancePoint.getPointY());
//            mPath.quadTo(minDistancePoint.getPointX(), minDistancePoint.getPointY(), minDistancePoint.getPointX(), minDistancePoint.getPointY());
            mPath.quadTo(eventX-50, minDistancePoint.getPointY()-50, minDistancePoint.getPointX(), minDistancePoint.getPointY());
            minDistancePoint.setChoose(true);
        }

    }
    @Override
    protected void onDraw(Canvas canvas) {
        randomPaintPoint(canvas);
        canvas.drawPath(mPath, linePaint);
    }

    //随机绘制指定数量的点
    private void randomPaintPoint(Canvas canvas) {
        float pointX;
        float pointY;
        //不是首次绘制跳过此逻辑
        if (isFirstDraw == true) {
            isFirstDraw = false;
            for (int i = 0; i < POINT_NUMBER; i++) {
                //-300  +150避免距离屏幕边缘太近
                pointX = (float) Math.random() * (this.getWidth() - 300) + 150;
                pointY = (float) Math.random() * (this.getHeight() - 300) + 150;
                pointList[i] = new Point(pointX, pointY);
                canvas.drawPoint(pointX, pointY, pointPaint);
                canvas.drawCircle(pointX, pointY,35, circlePaint);
            }
        } else {
            for (Point point : pointList) {
                canvas.drawPoint(point.getPointX(), point.getPointY(), pointPaint);
                if (point.isChoose() == true){
                    circlePaint.setStyle(Paint.Style.FILL);
                    circlePaint.setColor(Color.GREEN);
                    canvas.drawCircle(point.getPointX(), point.getPointY(),35, circlePaint);
                }else{
                    circlePaint.setStyle(Paint.Style.STROKE);
                    circlePaint.setColor(Color.BLUE);
                    canvas.drawCircle(point.getPointX(), point.getPointY(),35, circlePaint);
                }

            }
        }

    }

    //获取数组最小值和最小值索引
    public double[] getMinIndex(double[] arr) {
        if (arr == null || arr.length == 0) {
            return null;//如果数组为空 或者是长度为0 就返回null
        }
        int minIndex = 0;//假设第一个元素为最小值 那么下标设为0
        double[] arrnew = new double[2];//设置一个 长度为2的数组 用作记录 规定第一个元素存储最小值 第二个元素存储下标
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[minIndex] > arr[i + 1]) {
                minIndex = i + 1;
            }
        }
        arrnew[0] = arr[minIndex];
        arrnew[1] = minIndex;
        return arrnew;
    }

}
