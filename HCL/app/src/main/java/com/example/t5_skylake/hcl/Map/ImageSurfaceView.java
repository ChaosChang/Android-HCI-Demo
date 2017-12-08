package com.example.t5_skylake.hcl.Map;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.OnDoubleTapListener;
import android.widget.Scroller;

import com.example.t5_skylake.hcl.MainActivity;
import com.example.t5_skylake.hcl.R;
import com.example.t5_skylake.hcl.SensorManage.OrientionSensor;
import com.example.t5_skylake.hcl.VoiceManage.MusicPlayerControl;
import com.example.t5_skylake.hcl.VoiceManage.VoiceControl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by T5-SKYLAKE on 2017/6/8.
 */
public class ImageSurfaceView extends SurfaceView implements SurfaceHolder.Callback, OnGestureListener,OnDoubleTapListener{
    private final static String TAG = ImageSurfaceView.class.getSimpleName();

    private InputStreamScene scene;
    private final Touch touch;
    private GestureDetector gestureDectector;
    private ScaleGestureDetector scaleGestureDetector;
    private long lastScaleTime = 0;
    private long SCALE_MOVE_GUARD = 500; // milliseconds after scale to ignore move events
    private List<Point> marklist=new ArrayList<>();//这个是地图标签管理
    private boolean scalemode=false;//这个是用来控制zoom in时候播放细节声音
    private MusicPlayerControl mpc;//声音控制

    private OrientionSensor os;

    //音乐播放状态,s是state，懒得加下划线了
    private boolean palaces=false;
    private boolean winds=false;
    private boolean waters=false;
    private boolean birds=false;
    private boolean humans=false;
    private boolean weddings=false;
    private boolean dramas=false;
    private boolean shops=false;
    private boolean waas=false;
    private boolean waterrate=false;
    //音乐流标号,用于控制声音播放与停止
    private int pa;
    private int wi;
    private int wa;
    private int bi;
    private int hu;
    private int we;
    private int dr;
    private int sh;
    private int waa;//这个是声道音乐播放控制

    private DrawThread drawThread;

    //region getters and setters

    public void getViewport(Point p){
        scene.getViewport().getOrigin(p);
    }
    //getorigin应该是获取屏幕左上角的位置大概

    public void setViewport(Point viewport){
        scene.getViewport().setOrigin(viewport.x, viewport.y);
    }

    public void setViewportCenter() {
        Point viewportSize = new Point();
        //Point test=new Point();
        Point sceneSize = scene.getSceneSize();

        PointF center= new PointF();
        scene.getViewport().getSize(viewportSize);
        //scene.getViewport().getOrigin(test);

        int x = (sceneSize.x - viewportSize.x) / 2;
        int y = (sceneSize.y - viewportSize.y) / 2;
        center.set(x,y);//图片位置设置，这个有可能进行定位,用point做地图标签

        scene.getViewport().setOrigin(x, y);
        scene.getViewport().zoom(8,center);//设置缩放
        voiceplay();
    }

    public void setInputStream(InputStream inputStream) throws IOException {
        scene = new InputStreamScene(inputStream);
    }

    //endregion

    //region extends SurfaceView
    @Override
    public boolean onTouchEvent(MotionEvent me) {
        boolean consumed = gestureDectector.onTouchEvent(me);
        if (consumed)
            return true;
        scaleGestureDetector.onTouchEvent(me);
        switch (me.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: return touch.down(me);
            case MotionEvent.ACTION_MOVE:
                if (scaleGestureDetector.isInProgress() || System.currentTimeMillis()-lastScaleTime<SCALE_MOVE_GUARD)
                    break;
                return touch.move(me);
            case MotionEvent.ACTION_UP: return touch.up(me);
            case MotionEvent.ACTION_CANCEL: return touch.cancel(me);
        }
        return super.onTouchEvent(me);
    }
    //endregion

    //region SurfaceHolder.Callback constructors
    public ImageSurfaceView(Context context) {
        super(context);
        touch = new Touch(context);
        init(context);
    }

    public ImageSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        touch = new Touch(context);
        init(context);
    }

    public ImageSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touch = new Touch(context);
        init(context);
    }

    private void init(Context context){
        gestureDectector = new GestureDetector(context,this);
        getHolder().addCallback(this);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        mpc=new MusicPlayerControl(context);
    }
    //endregion

    //region class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private PointF screenFocus = new PointF();
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            if (scaleFactor!=0f && scaleFactor!=1.0f){
                scaleFactor = 1/scaleFactor;
                screenFocus.set(detector.getFocusX(),detector.getFocusY());
                scene.getViewport().zoom(
                        scaleFactor,
                        screenFocus);
                invalidate();
            }
            lastScaleTime = System.currentTimeMillis();
            return true;
        }
    }

    //endregion


    //region implements SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        scene.getViewport().setSize(width, height);
        Log.d(TAG,String.format("onSizeChanged(w=%d,h=%d)",width,height));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(holder);
        drawThread.setName("drawThread");
        drawThread.setRunning(true);
        drawThread.start();
        scene.start();
        touch.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        touch.stop();
        scene.stop();
        drawThread.setRunning(false);
        boolean retry = true;
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
    }
    //endregion

    //region implements OnGestureListener
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return touch.fling( e1, e2, velocityX, velocityY);
    }
    //region the rest are defaults
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    //长按重置
    @Override
    public void onLongPress(MotionEvent e) {
        //Log.d("a","a");
        setViewportCenter();
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    //单击轻击缩小
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        PointF focus=new PointF();
        focus.set(e.getX(),e.getY());
        scene.getViewport().zoom((float)2,focus);
        return true;
    }

    //onDoubleTapListener测试
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e){return false;}
    @Override
    public boolean onDoubleTapEvent(MotionEvent e){return false;}

    //双击放大
    @Override
    public boolean onDoubleTap(MotionEvent e){
        Log.d("b","doubletap");
        PointF focus=new PointF();
        focus.set(e.getX(),e.getY());
        scene.getViewport().zoom((float)0.5, focus);
        return true;
    }
    //endregion

    //endregion

    //region class DrawThread

    class DrawThread extends Thread {
        private SurfaceHolder surfaceHolder;

        private boolean running = false;
        public void setRunning(boolean value){ running = value; }

        public DrawThread(SurfaceHolder surfaceHolder){
            this.surfaceHolder = surfaceHolder;
        }

        @Override
        public void run() {
            Canvas c;
            while (running) {
                try {
                    // Don't hog the entire CPU
                    Thread.sleep(5);
                } catch (InterruptedException e) {}
                c = null;
                try {
                    c = surfaceHolder.lockCanvas();
                    if (c!=null){
                        synchronized (surfaceHolder) {
                            scene.draw(c);// draw it
                        }
                    }
                } finally {
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
    //endregion

    //region class Touch

    enum TouchState {UNTOUCHED,IN_TOUCH,START_FLING,IN_FLING};
    class Touch {
        TouchState state = TouchState.UNTOUCHED;
        /** Where on the view did we initially touch */
        final Point viewDown = new Point(0,0);
        /** What was the coordinates of the viewport origin? */
        final Point viewportOriginAtDown = new Point(0,0);

        final Scroller scroller;

        TouchThread touchThread;

        Touch(Context context){
            scroller = new Scroller(context);
        }

        void start(){
            touchThread = new TouchThread(this);
            touchThread.setName("touchThread");
            touchThread.start();
        }

        void stop(){
            touchThread.running = false;
            touchThread.interrupt();

            boolean retry = true;
            while (retry) {
                try {
                    touchThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // we will try it again and again...
                }
            }
            touchThread = null;
        }

        Point fling_viewOrigin = new Point();
        Point fling_viewSize = new Point();
        Point fling_sceneSize = new Point();
        boolean fling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
            scene.getViewport().getOrigin(fling_viewOrigin);
            scene.getViewport().getSize(fling_viewSize);
            scene.getSceneSize(fling_sceneSize);

            synchronized(this){
                state = TouchState.START_FLING;
                scene.setSuspend(true);
                scroller.fling(
                        fling_viewOrigin.x,
                        fling_viewOrigin.y,
                        (int)-velocityX,
                        (int)-velocityY,
                        0,
                        fling_sceneSize.x-fling_viewSize.x,
                        0,
                        fling_sceneSize.y-fling_viewSize.y);
                touchThread.interrupt();
            }
//            Log.d(TAG,String.format("scroller.fling(%d,%d,%d,%d,%d,%d,%d,%d)",
//                    fling_viewOrigin.x,
//                    fling_viewOrigin.y,
//                    (int)-velocityX,
//                    (int)-velocityY,
//                    0,
//                    fling_sceneSize.x-fling_viewSize.x,
//                    0,
//                    fling_sceneSize.y-fling_viewSize.y));
            voiceplay();
            return true;
        }
        boolean down(MotionEvent event){
            scene.setSuspend(false);    // If we were suspended because of a fling
            synchronized(this){
                state = TouchState.IN_TOUCH;
                viewDown.x = (int) event.getX();
                viewDown.y = (int) event.getY();
                Point p = new Point();
                scene.getViewport().getOrigin(p);
                viewportOriginAtDown.set(p.x,p.y);
            }
            voiceplay();
            return true;
        }

        boolean move(MotionEvent event){
            if (state==TouchState.IN_TOUCH){
                float zoom = scene.getViewport().getZoom();
                float deltaX = zoom * ((float)(event.getX()-viewDown.x));
                float deltaY = zoom * ((float)(event.getY()-viewDown.y));
                float newX = ((float)(viewportOriginAtDown.x - deltaX));
                float newY = ((float)(viewportOriginAtDown.y - deltaY));

                scene.getViewport().setOrigin((int)newX, (int)newY);
                invalidate();
            }
            voiceplay();
            return true;
        }

        boolean up(MotionEvent event){
            if (state==TouchState.IN_TOUCH){
                state = TouchState.UNTOUCHED;
            }
            return true;
        }

        boolean cancel(MotionEvent event){
            if (state==TouchState.IN_TOUCH){
                state = TouchState.UNTOUCHED;
            }
            return true;
        }

        class TouchThread extends Thread {
            final Touch touch;
            boolean running = false;
            void setRunning(boolean value){ running = value; }

            TouchThread(Touch touch){ this.touch = touch; }
            @Override
            public void run() {
                running=true;
                while(running){
                    while(touch.state!=TouchState.START_FLING && touch.state!=TouchState.IN_FLING){
                        try {
                            Thread.sleep(Integer.MAX_VALUE);
                        } catch (InterruptedException e) {}
                        if (!running)
                            return;
                    }
                    synchronized (touch) {
                        if (touch.state==TouchState.START_FLING){
                            touch.state = TouchState.IN_FLING;
                        }
                    }
                    if (touch.state==TouchState.IN_FLING){
                        scroller.computeScrollOffset();
                        scene.getViewport().setOrigin(scroller.getCurrX(), scroller.getCurrY());
                        if (scroller.isFinished()){
                            scene.setSuspend(false);
                            synchronized (touch) {
                                touch.state = TouchState.UNTOUCHED;
                                try{
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {}
                            }
                        }
                    }
                }
            }
        }
    }
    //endregion

    //建立地图标签
    public void setMarklist(){
        Point a1=new Point();
        a1.set(6250,463);//皇宫地标设置
        marklist.add(a1);
        Point a2=new Point();
        a2.set(20500,463);//市场范围
        marklist.add(a2);
        Point a3=new Point();
        a3.set(19550,463);//码头
        marklist.add(a3);
        Point a4=new Point();
        a4.set(29240,463);//郊外
        marklist.add(a4);
        Point a5=new Point();
        a5.set(26600,463);//婚礼点
        marklist.add(a5);
        Point a6=new Point();
        a6.set(12400,463);//茶楼
        marklist.add(a6);
    }
    //导航
    public void Speech(String str){
        if(str.equals("上"))
        {
            Point a=new Point();
            scene.getViewport().getOrigin(a);
            scene.getViewport().setOrigin(a.x,a.y-100);
        }
        else if(str.equals("下"))
        {
            Point a=new Point();
            scene.getViewport().getOrigin(a);
            scene.getViewport().setOrigin(a.x,a.y+100);
        }
        else if(str.equals("左"))
        {
            Point a=new Point();
            scene.getViewport().getOrigin(a);
            scene.getViewport().setOrigin(a.x-500,a.y);
        }
        else if(str.equals("又"))
        {
            Point a=new Point();
            scene.getViewport().getOrigin(a);
            scene.getViewport().setOrigin(a.x+500,a.y);
        }
        else if(str.equals("皇宫"))
        {
            scene.getViewport().setOrigin(marklist.get(0).x,marklist.get(0).y);
        }
        else if(str.equals("码头"))
        {
            scene.getViewport().setOrigin(marklist.get(2).x,marklist.get(2).y);
        }
        else if(str.equals("郊外"))
        {
            scene.getViewport().setOrigin(marklist.get(3).x,marklist.get(3).y);
        }
        voiceplay();
    }
    //重力感应
    public void gravity(int i){
        //左滑
        if(i==1) {
            Point a = new Point();
            scene.getViewport().getOrigin(a);
            scene.getViewport().setOrigin(a.x - 500, a.y);
        }
        //右滑
        if(i==2){
            Point a = new Point();
            scene.getViewport().getOrigin(a);
            scene.getViewport().setOrigin(a.x + 500, a.y);
        }
    }
    //方向感应
    public void oriention(int i){
        //对水流声音进行修改,声道切换
        if(birds==true&&i!=3)
        {
            if(waters==true) {
                mpc.stopplay(wa);
                waters = false;
            }
            if(i==1&&waas==false)
            {
                waa=mpc.voicevolumeplay(1);
                waas=true;
            }
            else if(i==2&&waas==false)
            {
                waa=mpc.voicevolumeplay(2);
                waas=true;
            }
        }
        else if(birds==true&&i==3)
        {
            if(waas==true) {
                mpc.stopplay(waa);
                waas=false;
            }
            if(waters==false) {
                wa = mpc.voiceplay(1);
                waters=true;
            }
        }
    }
    //摇一摇感应
    public void shake(){
        if(waters==true&&waterrate==false) {
            mpc.voicerateplay();
            waterrate=true;
        }
    }

    //声音加载
    public void voiceplay(){
        Point a= new Point();
        scene.getViewport().getOrigin(a);
        int place=a.x;
        if(waterrate==true)
            waterrate=false;
        if(marklist.get(0).x-place>=0)//声音播放
        {
            if(palaces!=true)//宫廷
            {
                pa=mpc.voiceplay(7);
                palaces=true;
            }
        }
        else
        {
            mpc.stopplay(pa);
            palaces=false;
        }

        if(winds==false) {
            wi=mpc.voiceplay(0);
            winds = true;
        }

        if(marklist.get(1).x-place<=14200&&marklist.get(1).x-place>=0)//市场
        {
            if(shops!=true) {
                sh=mpc.voiceplay(3);
                shops=true;
            }
            //放大更具体的说话声音
            float z=scene.getViewport().getZoom();
            if(z<=0.47&&humans!=true)
            {
                hu=mpc.voiceplay(4);
                humans=true;
            }
            else if(z>0.47&&humans==true)
            {
                mpc.stopplay(hu);
                humans=false;
            }
        }
        else{
            mpc.stopplay(sh);
            mpc.stopplay(hu);
            shops=false;
            humans=false;
        }

        if(marklist.get(3).x-place<= 8700)//郊区
        {
            if(waters!=true&&birds!=true)
            {
                wa=mpc.voiceplay(1);
                bi=mpc.voiceplay(2);
                waters=true;
                birds=true;
            }
        }
        else
        {
            if(waters==true&&birds==true)
            {
                mpc.stopplay(wa);
                mpc.stopplay(bi);
                waters=false;
                birds=false;
            }
        }
    }
}
