package com.example.wordsreader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    TextView tv;
    EditText et;
    EditText etTime;
    int num = 0;
    String[] allWords;

    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.textView);
        et = (EditText) findViewById(R.id.editText2);
        etTime = (EditText) findViewById(R.id.editText3);

        openDictionary("93392");

        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 0, 250);
        //mTimer.
    }

    void openDictionary(String name){
        try {
            StringBuilder text = new StringBuilder();
            File sdcard = Environment.getExternalStorageDirectory();
            FileInputStream file;
            String s = sdcard.getPath();
            file = new FileInputStream(s + "/Books/" + name + ".txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(file, "UTF8"));
            String line;
            boolean firstLine = true;
            String word = "";

            allWords = new String[90000];
            for(int i = 0; i < 90000; i++){
            //while ((line = br.readLine()) != null) {
                line = br.readLine();
                allWords[i] = line;
            }
        }
        catch (IOException e) {
            Log.e("myLogs", "IOException", e);
        }
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.buttonStart:
                if(mTimer == null){
                    mTimer = new Timer();
                    mMyTimerTask = new MyTimerTask();
                    mTimer.schedule(mMyTimerTask, 0, Integer.parseInt(etTime.getText().toString()));
                    //Toast t = new Toast(this);
                    Toast t = Toast.makeText(this, String.valueOf(Integer.parseInt(etTime.getText().toString())), Toast.LENGTH_SHORT);

                    t.show();
                    num = Integer.parseInt(et.getText().toString());

                }
                break;
            case R.id.buttonStop:
                if(mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                break;
        }


    }


    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //Vibrator v = (Vibrator) con.getSystemService(Context.VIBRATOR_SERVICE);
                    //v.vibrate(250);
                    if(num < 90000)
                        tv.setText(allWords[num]);
                    else
                        num = 0;
                    num++;
                }
            });
        }
    }
}
