package com.example.t5_skylake.hcl.VoiceManage;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.example.t5_skylake.hcl.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by T5-SKYLAKE on 2017/6/9.
 */
public class MusicPlayerControl {
    //soundpool
    private SoundPool pool;
    private Map<Integer,Integer> poolmap;
    private float leftvolume=1f;
    private float rightvolume=1f;
    private float rate=1;
    private Context context;


    public MusicPlayerControl(Context context){
        this.context=context;
        init();
    }

    private void init(){
        poolmap=new HashMap<Integer, Integer>();
        pool=new SoundPool(8, AudioManager.STREAM_MUSIC,0);//实例化，而且已经不推荐使用这种方式了
        poolmap.put(0, pool.load(context,R.raw.wind,1));
        poolmap.put(1,pool.load(context,R.raw.water,1));
        poolmap.put(2,pool.load(context,R.raw.bird,1));
        poolmap.put(3,pool.load(context,R.raw.human,1));
        poolmap.put(4,pool.load(context,R.raw.shopping,1));
        poolmap.put(5,pool.load(context,R.raw.drama,1));
        poolmap.put(6,pool.load(context,R.raw.wedding,1));
        poolmap.put(7,pool.load(context,R.raw.palacememories,1));

        pool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                // 每次装载完成均会回调
                Log.i("main", "音频池资源id为：" + sampleId + "的资源装载完成");
                // 当前装载完成ID为map的最大值，即为最后一次装载完成
                if (sampleId == poolmap.size()) {
                    //Toast.makeText(MainActivity.this, "加载声音池完成!",
                            //Toast.LENGTH_SHORT).show();
                    //btn_newqqmsg.setOnClickListener(click);
                    //btn_newweibontf.setOnClickListener(click);
                    //btn_newweibotoast.setOnClickListener(click);
                    // 进入应用播放四次声音
                }
            }
        });
    }

    //声音播放
    public int voiceplay(int i){
        //int w= pool.load(context,R.raw.wind,1);
        int tt=pool.play(poolmap.get(i),leftvolume,rightvolume,1,-1,rate);
        return tt;
    }
    //声音停止
    public void stopplay(int i){
        pool.stop(i);
    }

    //声音切换声道播放
    public int voicevolumeplay(int i){
        int tta;
        if(i==1)//左倾,水从右流，右声道发声
        {
            tta=pool.play(poolmap.get(1),0,rightvolume,1,-1,rate);
        }
        else//右倾
        {
            tta=pool.play(poolmap.get(1),leftvolume,0,1,-1,rate);
        }
        return tta;
    }
    //声音切换倍速播放
    public int voicerateplay(){
        int tta=pool.play(poolmap.get(1),leftvolume,rightvolume,1,0,2);
        return tta;
    }
}
