package com.example.t5_skylake.hcl;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.t5_skylake.hcl.Map.ImageSurfaceView;
import com.example.t5_skylake.hcl.Map.RandomAccessFileInputStream;
import com.example.t5_skylake.hcl.VoiceManage.JsonParser;
import com.example.t5_skylake.hcl.VoiceManage.VoiceControl;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity{

    private long exitTime=0;//退出时间

    private static final String TAG = "ImageViewerActivity";
    private static final String KEY_X = "X";
    private static final String KEY_Y = "Y";
    private static final String KEY_FN = "FN";
    private ImageSurfaceView imageSurfaceView;
    private String filename = null;
    private SensorManager mManager = null;
    private Sensor mSensor = null;
    //public static Intent intents=new Intent();


    private Button startSpeech;
    private SpeechRecognizer iatRecognizer;
    //private VoiceControl vc=new VoiceControl(this);
    private String engine="iat";
    //set frequency,8000 or 16000
    private String rate="16000";
    private Toast mToast;
    private static final int RECORD_AUDIO=32;
    private AudioManager am;//媒体控制
    private Timer mTimer;

    /*
    private MyMap sceneMap;
    private int reqWidth=200;
    private int reqHeight=5000;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        SpeechUtility.createUtility(this, SpeechConstant.APPID+"=593ae998");//讯飞注册


        //传感器
        mManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//加速度传感器
        am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mTimer=new Timer();

        mToast=new Toast(this);

        //intents=Login.f;
        //按钮
        startSpeech=(Button)findViewById(R.id.button);

        //Login.ma.finish();//关闭前一个活动
        //Intent=this;

        //图片压缩法处理超大图片
        /*sceneMap= (MyMap) findViewById(R.id.hclMap);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;//仅获取图片信息
        BitmapFactory.decodeResource(getResources(),R.drawable.hcl,options);//导入超大图片
        options.inSampleSize=2;
        options.inJustDecodeBounds=false;
        Bitmap b=BitmapFactory.decodeResource(getResources(),R.drawable.hcl,options);
        sceneMap.setBitmap(b);*/

        //输入流法动态加载超大图片
        imageSurfaceView = (ImageSurfaceView) findViewById(R.id.worldview);
        // Setup/restore state
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_X) && savedInstanceState.containsKey(KEY_Y)) {
            Log.d(TAG, "restoring state");
            int x = (Integer) savedInstanceState.get(KEY_X);
            int y = (Integer) savedInstanceState.get(KEY_Y);

            String fn = null;
            if (savedInstanceState.containsKey(KEY_FN))
                fn = (String) savedInstanceState.get(KEY_FN);

            try {
                if (fn == null || fn.length() == 0) {
                    imageSurfaceView.setInputStream(getAssets().open("hcl.jpg"));
                } else {
                    imageSurfaceView.setInputStream(new RandomAccessFileInputStream(fn));
                }
                imageSurfaceView.setViewport(new Point(x, y));
            } catch (java.io.IOException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            // Centering the map to start
            Intent intent = getIntent();
            try {
                Uri uri = null;
                if (intent != null)
                    uri = getIntent().getData();

                InputStream is;
                if (uri != null) {
                    filename = uri.getPath();
                    is = new RandomAccessFileInputStream(uri.getPath());
                } else {
                    is = getAssets().open("hcl.jpg");
                }

                imageSurfaceView.setInputStream(is);
            } catch (java.io.IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        //下属功能，地图标签添加
        imageSurfaceView.setMarklist();

        SensorEventListener lsn=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                Log.d("A",""+x+" "+y+" "+z);
                //x控制左右，z控制前后
                if(x>8&&x<10)
                {
                    //时间管理器，0.5秒调用一次，单位毫秒
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            imageSurfaceView.gravity(1);
                        }
                    },500);
                }
                else if(x<-8&&x>-10)
                {
                    //时间管理器，0.5秒调用一次，单位毫秒
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            imageSurfaceView.gravity(2);
                        }
                    },500);
                }
                else if(x>5&&x<6)//左倾，但是程度不大，切换声道
                {
                    //时间管理器，0.5秒调用一次，单位毫秒
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            imageSurfaceView.oriention(1);
                        }
                    },500);
                }
                else if(x>-6&&x<-5)//右倾，但是程度不大，切换声道
                {
                    //时间管理器，0.5秒调用一次，单位毫秒
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            imageSurfaceView.oriention(2);
                        }
                    },500);
                }
                else if(x<=5&&x>=-5){
                    //水声重置
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            imageSurfaceView.oriention(3);
                        }
                    },500);
                }
                else if(x>10||x<-10){
                    //1.5倍速水声
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            imageSurfaceView.shake();
                        }
                    },500);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        mManager.registerListener(lsn, mSensor, SensorManager.SENSOR_DELAY_GAME);

        //百度语音识别
        /*startSpeech.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        vc.startASR(MainActivity.this);
                        break;
                    case MotionEvent.ACTION_UP:
                        vc.stopASR();
                        break;
                    default:
                        return false;
                }

                return true;
            }
        });*/
        startSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED){
                    Log.d("A","申请权限");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{"Manifest.permission.RECORD_AUDIO"}, RECORD_AUDIO);
                }else {
                    showIatinvisble();
                }

            }
        });
    }
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.e("合成监听监听", "InitListener init() code = " + code);
            if (code == ErrorCode.SUCCESS) {
                //btnWZ.setEnabled(true);
                //text.setText("可以进行识别文字(文字转声音)\n");
                //btnSY.setEnabled(true);
                //text.append("可以进行识别声音(声音转文字)\n");
            }
        }
    };


    protected void showIatinvisble() {
        iatRecognizer= SpeechRecognizer.createRecognizer(this,mTtsInitListener);//初始化合成对象
        //获取引擎参数
        iatRecognizer.setParameter(SpeechConstant.DOMAIN, engine);
        iatRecognizer.setParameter(SpeechConstant.SAMPLE_RATE, rate);
        //clear myResulttext
        //mResultText.setText(null);
        iatRecognizer.startListening(myrecognizerListener);
    }

    private RecognizerListener myrecognizerListener=new RecognizerListener(){
        @Override
        public void onBeginOfSpeech() {
            //showTip("开始说话");
            Log.d("A","开始说话");
        }
        @Override
        public void onError(SpeechError err) {
            //showTip(err.getPlainDescription(true));
        }
        @Override
        public void onEndOfSpeech() {
            //showTip("结束说话");
            Log.d("B","结束说话");
        }
        @Override
        public void onVolumeChanged(int i,byte[] buffer){
        }
        //扩展
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle msg) {

        }
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());
            //mResultText.append(text);
            //mResultText.setSelection(mResultText.length());
            if(text.equals("到")){
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
            }
            else if(text.equals("小")){
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
            }
            else
                imageSurfaceView.Speech(text);
        }
    };

    /**
     * 用户登录回调监听器.
     */
    private SpeechListener listener = new SpeechListener(){

        @Override
        public void onBufferReceived(byte[] buffer){
        }
        @Override
        public void onCompleted(SpeechError error) {
            if(error != null) {
                //
            }

        }
        @Override
        public void onEvent(int arg0, Bundle arg1) {

        }

    };

    private void showTip(String str)
    {
        if(!TextUtils.isEmpty(str))
        {
            mToast.setText(str);
            mToast.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == RECORD_AUDIO){
            if (grantResults !=null&&grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                showIatinvisble();
            } else {
                // Permission Denied
                Toast.makeText(this,"权限被拒绝了",Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        imageSurfaceView.setViewport(new Point(imageSurfaceView.getWidth()/2, imageSurfaceView.getHeight()/2));//画面居中
    }

    @Override//退出事件
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序！", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                //finish();
                super.onDestroy();
                System.exit(0);
            }
            return true;
        }


        return super.dispatchKeyEvent(event);
    }

    /*public int calculateInSampleSize(BitmapFactory.Options options,//一般也用不到它了
                                     int reqWidth, int reqHeight) {
        // 保存图片原宽高值
        final int height = options. outHeight;
        final int width = options. outWidth;
        // 初始化压缩比例为1
        int inSampleSize = 1;

        // 当图片宽高值任何一个大于所需压缩图片宽高值时,进入循环计算系统
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // 压缩比例值每次循环两倍增加,
            // 直到原图宽高值的一半除以压缩值后都~大于所需宽高值为止
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    protected void onStart() {
        super.onStart();
        vc.createTool();
    }

    @Override
    protected void onStop() {
        super.onStop();
        vc.destroyTool();
    }

    @Override
    public void onResults(String result) {
        final String finalResult = result;
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mTextView.setText(finalResult);
            }
        });
    }*/
}
