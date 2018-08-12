package com.example.shi.nettest;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shi.nettest.fragment.FragmentPing;
import com.example.shi.nettest.fragment.FragmentSpeed;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    List<Fragment> fragList = new ArrayList<>();
    /**
     * Tab的那个引导线
     */
    private ImageView mTabLineIv;
    /**
     * ViewPager的当前选中页
     */
    private int currentIndex;
    /**
     * 屏幕的宽度
     */
    private int screenWidth;

    private TextView mTabChatTv, mTabContactsTv, mTabFriendTv;

    protected void OnCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabLineIv = (ImageView) this.findViewById(R.id.id_tab_line_iv);
        mTabChatTv = (TextView) this.findViewById(R.id.id_chat_tv);
        mTabFriendTv = (TextView) this.findViewById(R.id.id_friend_tv);


        fragList.add(new FragmentPing());
        fragList.add(new FragmentSpeed());

        ViewPager vp = (ViewPager) findViewById(R.id.viewPager);
        vp.setCurrentItem(0);
        vp.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), fragList));
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            /**
             * state滑动中的状态 有三种状态（0，1，2） 1：正在滑动 2：滑动完毕 0：什么都没做。
             */
            @Override
            public void onPageScrollStateChanged(int state) {

            }

            /**
             * position :当前页面，及你点击滑动的页面 offset:当前页面偏移的百分比
             * offsetPixels:当前页面偏移的像素位置
             */
            @Override
            public void onPageScrolled(int position, float offset,
                                       int offsetPixels) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabLineIv
                        .getLayoutParams();

                Log.e("offset:", offset + "");
                /**
                 * 利用currentIndex(当前所在页面)和position(下一个页面)以及offset来
                 * 设置mTabLineIv的左边距 滑动场景：
                 * 记3个页面,
                 * 从左到右分别为0,1,2
                 * 0->1; 1->2; 2->1; 1->0
                 */

                if (currentIndex == 0 && position == 0)// 0->1
                {
                    lp.leftMargin = (int) (offset * (screenWidth * 1.0 / 3) + currentIndex
                            * (screenWidth / 3));

                } else if (currentIndex == 1 && position == 0) // 1->0
                {
                    lp.leftMargin = (int) (-(1 - offset)
                            * (screenWidth * 1.0 / 3) + currentIndex
                            * (screenWidth / 3));

                } else if (currentIndex == 1 && position == 1) // 1->2
                {
                    lp.leftMargin = (int) (offset * (screenWidth * 1.0 / 3) + currentIndex
                            * (screenWidth / 3));
                } else if (currentIndex == 2 && position == 1) // 2->1
                {
                    lp.leftMargin = (int) (-(1 - offset)
                            * (screenWidth * 1.0 / 3) + currentIndex
                            * (screenWidth / 3));
                }
                mTabLineIv.setLayoutParams(lp);
            }

            @Override
            public void onPageSelected(int position) {
                resetTextView();
                switch (position) {
                    case 0:
                        mTabChatTv.setTextColor(Color.BLUE);
                        break;
                    case 1:
                        mTabFriendTv.setTextColor(Color.BLUE);
                        break;
                }
                currentIndex = position;
            }
        });
        initTabLineWidth();
    }

    class MyPagerAdapter extends FragmentPagerAdapter {
        List<Fragment> fragmentList;

        public MyPagerAdapter(FragmentManager fm, List<Fragment> fragList) {
            super(fm);
            this.fragmentList = fragList;
        }

        @Override
        public Fragment getItem(int position) {
            return (fragmentList == null || fragmentList.size() == 0) ? null : fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList == null ? 0 : fragmentList.size();
        }
    }

    /**
     * 设置滑动条的宽度为屏幕的1/3(根据Tab的个数而定)
     */
    private void initTabLineWidth() {
        DisplayMetrics dpMetrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay()
                .getMetrics(dpMetrics);
        screenWidth = dpMetrics.widthPixels;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabLineIv
                .getLayoutParams();
        lp.width = screenWidth / 2;
        mTabLineIv.setLayoutParams(lp);

    }
    /**
     * 重置颜色
     */
    private void resetTextView() {
        mTabChatTv.setTextColor(Color.BLACK);
        mTabFriendTv.setTextColor(Color.BLACK);
        mTabContactsTv.setTextColor(Color.BLACK);
    }

}

/*
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    public static final String MyTag = "MyPing";

    private int offset;
    TextView result_vi;
    Handler handler1;
    Thread ping = null;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result_vi = (TextView) findViewById(R.id.result_textView);
        result_vi.setMovementMethod(ScrollingMovementMethod.getInstance());
        result_vi.setHorizontallyScrolling(true); // disable auto line break
        result_vi.setFocusable(true);
        result_vi.setVerticalScrollBarEnabled(true);

        ping = new PingThread();

        Button bu = (Button)findViewById(R.id.start_button);
        bu.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                EditText te = (EditText)findViewById(R.id.ip_input);
                String host = te.getText().toString();
                if (host.isEmpty() || !isIP(host)) {
                    Log.w(MyTag, "ip is empty, please input valid IP!");
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("notice")
                            .setMessage("Please input valid IP!")
                            .setPositiveButton("yes", null)
                            .show();
                    return;
                }
                Button bu = (Button) view;
                String text = bu.getText().toString();
                if ("START".equals(text)) {
                    bu.setText("STOP");
                    bu.setBackgroundColor(Color.parseColor("#930000"));
                    ping.start();
                } else if ("STOP".equals(text)) {
                    bu.setText("START");
                    bu.setBackgroundColor(Color.parseColor("#007500"));

                    if (ping.isAlive()) {
                        Log.i(MyTag, "thread is runing! stop it!");
                        ping.interrupt();
                    }
                } else {
                    Log.e(MyTag, "unknown text: " + text);
                }
            }
        });


        handler1 = new Handler() {// 创建一个handler对象 ，用于监听子线程发送的消息
            public void handleMessage(Message msg)// 接收消息的方法
            {
                switch (msg.what) {
                    case 10:
                        String resultmsg = (String) msg.obj;
                        result_vi.append(resultmsg);
                        offset = result_vi.getLineCount() * result_vi.getLineHeight();
                        if (offset > result_vi.getHeight()) {
                            result_vi.scrollTo(0, offset - result_vi.getHeight());
                        }
//                        Log.i(MyTag, "====handlerThread====:"
//                                + Thread.currentThread().getId());
//                        Log.i(MyTag, "====resultmsg====:" + msg.what);
//                        Log.i(MyTag, "====resultmsg====:" + resultmsg);
                        break;
                    default:
                        break;
                }
            }
        };
    }
    class PingThread extends Thread {
      public void run() {
          EditText te = (EditText)findViewById(R.id.ip_input);
          String host = te.getText().toString();

          Log.d(MyTag,  "host: " + host);
          String line;
          Process process = null;
          BufferedReader successReader = null;
          BufferedReader errReader;
          String command = "ping " + " -W 1 " + host;
          Log.d(MyTag, "cmd: " + command);
          try {
              process = Runtime.getRuntime().exec(command);
              if (process == null) {
                  Log.e(MyTag, "ping fail:process is null.");
              }

              successReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(process).getInputStream()));
              while (!this.isInterrupted() && (line = successReader.readLine()) != null) {
                  Log.i(MyTag, "line: " + line);
                  sendMsgToHandle(line);
              }

              int status = process.waitFor();
              if (status == 0) {
                  Log.i(MyTag, "exec cmd success:" + command);
              } else {
                  errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                  while ((line = errReader.readLine()) != null) {
                      Log.e(MyTag, "err: " + line);
                  }
                  Log.e(MyTag, command + " exec cmd fail. return " + status);
              }
              Log.i(MyTag, "exec finished.");
          } catch (IOException e) {
              Log.e(MyTag, e.toString());
          } catch (InterruptedException e) {
              Log.e(MyTag, "interrupt: " + e.toString());
          } finally {
              Log.i(MyTag, "ping exit.");
              if (process != null) {
                  process.destroy();
              }
              if (successReader != null) {
                  try {
                      successReader.close();
                  } catch (IOException e) {
                      Log.e(MyTag, e.toString());
                  }
              }
          }
      }
    };


    public void sendMsgToHandle(String line) {
        Message msg = handler1.obtainMessage();
        msg.what = 10;
        msg.obj = line + "\r\n";
        msg.sendToTarget();
    }
    public String clearBlank(String IP){
        while(IP.startsWith(" ")){
            IP= IP.substring(1,IP.length()).trim();
        }
        while(IP.endsWith(" ")){
            IP= IP.substring(0,IP.length()-1).trim();
        }
        return IP;
    }
    public boolean isIP(String addr)
    {
        addr = clearBlank(addr);
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr))
        {
            return false;
        }

        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(addr);

        return mat.find();
    }
}*/