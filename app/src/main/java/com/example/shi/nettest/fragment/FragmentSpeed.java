package com.example.shi.nettest.fragment;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shi.nettest.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static android.content.Context.CONNECTIVITY_SERVICE;

class Info
{

    public double speed;
    public int hadsendByte;
    public int hadrecvByte;

}

public class FragmentSpeed extends Fragment {
    private View rootView;//缓存Fragment view
    public static final String MyTag = "MyPing";

    private TextView tv_type,tv_now_speed,tv_ave_speed;
    private ImageView needle;
    private Info info;
    private byte[] imageBytes;
    private boolean flag;
    private int last_degree=0,cur_degree;

    private Button startButton;
    private Thread startSpeed = null;
    public FragmentSpeed() {
        super();
    }
    private Handler handler=new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {
            // TODO Auto-generated method stub
            if(msg.what==0x123)
            {
                tv_now_speed.setText(msg.arg1+"KB/S");
                tv_ave_speed.setText(msg.arg2+"KB/S");
                startAnimation(msg.arg1);
            }
            if(msg.what==0x100)
            {
                tv_now_speed.setText("0KB/S");
                startAnimation(0);
                startButton.setText("开始测试");
                startButton.setEnabled(true);
            }
        }

    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if(rootView==null){
            rootView=inflater.inflate(R.layout.activity_tab_speed, container, false);
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }

        tv_type=(TextView) rootView.findViewById(R.id.connection_type);
        tv_now_speed=(TextView) rootView.findViewById(R.id.now_speed);
        tv_ave_speed=(TextView) rootView.findViewById(R.id.ave_speed);
        needle=(ImageView) rootView.findViewById(R.id.needle);
        startButton = (Button)rootView.findViewById(R.id.start_btn);
        info=new Info();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager=(ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
                tv_type.setText(networkInfo.getTypeName());


                startButton.setText("测试中");
                startButton.setEnabled(false);
                info.hadrecvByte=0;
                info.speed=0;
                info.hadrecvByte=0;
                flag = true;
                new DownloadThread().start();
                new GetInfoThread().start();
            }
        });
        return rootView;
    }

    class DownloadThread extends Thread
    {

        @Override
        public void run()
        {
            String s = "1234567890";
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < 1000; i++) {
                buf.append(s);
            }
            HttpURLConnection conn = null;
            while(true) {
                try {
                    // 创建一个URL对象
                    URL mURL = new URL("http://www.baidu.com");
                    // 调用URL的openConnection()方法,获取HttpURLConnection对象
                    conn = (HttpURLConnection) mURL.openConnection();

                    conn.setRequestMethod("POST");// 设置请求方法为post
                    conn.setReadTimeout(5000);// 设置读取超时为5秒
                    conn.setConnectTimeout(10000);// 设置连接网络超时为10秒
                    conn.setDoOutput(true);// 设置此方法,允许向服务器输出内容
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Content-Length", buf.length() + "");

                    //JSONObject obj = new JSONObject();

                    // 获得一个输出流,向服务器写数据,默认情况下,系统不允许向服务器输出内容
                    OutputStream out = conn.getOutputStream();// 获得一个输出流,向服务器写数据
                    out.write(buf.toString().getBytes());
                    out.flush();
                    out.close();

                    int responseCode = conn.getResponseCode();// 调用此方法就不必再使用conn.connect()方法
                    String msg = conn.getResponseMessage();
                    //InputStream is = conn.getInputStream();
                    //return IOSUtil.inputStream2String(is);
                    //Log.i(MyTag, "send byte:" + buf.length());
                    info.hadsendByte += buf.length();

                    if (responseCode == 200) {
                        String line,response = "";
                        BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line=br.readLine()) != null) {
                            response+=line;
                        }
                        info.hadrecvByte+=response.length();

                    } else {
                        Log.e(MyTag, "errrsp:" + responseCode +" " + msg );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        conn.disconnect();// 关闭连接
                    }
                }
            }
            /*
            while(true) {
                // TODO Auto-generated method stub
                String url_string = "";
                long start_time, cur_time;
                URL url;
                URLConnection connection;
                InputStream iStream;

                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    String uri = "http://www.yourweb.com";
                    HttpPost httppost = new HttpPost(uri);

                    url = new URL(url_string);
                    connection = url.openConnection();

                    info.totalByte = connection.getContentLength();

                    iStream = connection.getInputStream();
                    start_time = System.currentTimeMillis();
                    while (iStream.read() != -1 && flag) {

                        info.hadfinishByte++;
                        cur_time = System.currentTimeMillis();
                        if (cur_time - start_time == 0) {
                            info.speed = 1000;
                        } else {
                            info.speed = info.hadfinishByte / (cur_time - start_time) * 1000;
                        }
                    }
                    iStream.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }*/
        }

    }

    class GetInfoThread extends Thread
    {

        @Override
        public void run()
        {
            // TODO Auto-generated method stub
            double sum,counter;
            int lastsendByte = 0;

            int cur_speed,ave_speed;
            try
            {
                sum=0;
                counter=0;
                while(flag)
                {
                    Thread.sleep(1000);
                    //sum+=info.speed;
                    counter++;

                    cur_speed=(int) info.hadsendByte - lastsendByte;
                    ave_speed=(int) (info.hadsendByte/counter);
                    Log.e("Test", "cur_speed:"+cur_speed/1024+"KB/S ave_speed:"+ave_speed/1024);
                    Message msg=new Message();
                    msg.arg1=((int)cur_speed/1024);
                    msg.arg2=((int)ave_speed/1024);
                    msg.what=0x123;
                    handler.sendMessage(msg);

                    lastsendByte = info.hadsendByte;
                }

                handler.sendEmptyMessage(0x100);
            } catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }


/*
    @Override
    public void onBackPressed()
    {
        // TODO Auto-generated method stub
        flag=false;
        super.onBackPressed();
    }


    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        flag=true;
        super.onResume();
    }*/

    private void startAnimation(int cur_speed)
    {
        cur_degree=getDegree(cur_speed);

        RotateAnimation rotateAnimation=new RotateAnimation(last_degree, cur_degree, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(1000);
        last_degree=cur_degree;
        needle.startAnimation(rotateAnimation);
    }

    private int getDegree(double cur_speed)
    {
        int ret=0;
        if(cur_speed>=0 && cur_speed<=512)
        {
            ret=(int) (15.0*cur_speed/128.0);
        }
        else if(cur_speed>=512 && cur_speed<=1024)
        {
            ret=(int) (60+15.0*cur_speed/256.0);
        }
        else if(cur_speed>=1024 && cur_speed<=10*1024)
        {
            ret=(int) (90+15.0*cur_speed/1024.0);
        }else {
            ret=180;
        }
        return ret;
    }
}
