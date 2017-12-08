package com.example.t5_skylake.hcl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by T5-SKYLAKE on 2017/5/28.
 */
public class Login extends Activity {

    //ImageView homeImage;
    public static Login ma=null;
    public static Intent f=new Intent();

    @Override//载入动画
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);
        ma=this;
        final Intent first=new Intent(this,MainActivity.class);//活动跳转
        f=first;
        Timer timer=new Timer();//Timer
        TimerTask ts=new TimerTask() {
            @Override
            public void run() {
                startActivity(first);
            }
        };//计时器自动跳转
        timer.schedule(ts,1000*4);//定时
        ImageView homeImage=(ImageView)findViewById(R.id.app_load);//载入logo
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);//淡入淡出动画
        alphaAnimation.setDuration(2000);//设定动画时间

                /*这一部分都是重载内容，记住套路即可,homeImage.setVisibility(View.GONE)为不可见
                大致三个部分，动画开始，动画重复，动画结束*/
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });

        homeImage.setAnimation(alphaAnimation);
        homeImage.setVisibility(View.VISIBLE);//可见
    }
}
