package com.example.shi.nettest;

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


}