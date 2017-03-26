package com.example.dictionary;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    final static String TAG = "myLogs";
    DBHelper dbHelper;
    private String m_Text = "";
    TextView tvWord;
    TextView tvDescription;
    String word = "";
    String desc = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        dbHelper = new DBHelper(this);
        tvWord = (TextView)findViewById(R.id.tvWord);
        tvDescription = (TextView)findViewById(R.id.tvDescription);
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

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            while ((line = br.readLine()) != null) {
                if(firstLine)
                {
                    if(line.isEmpty())
                        continue;
                    word = line.toString();
                    firstLine = false;
                }
                else
                {
                    text.append(line);
                    text.append('\n');
                    firstLine = false;
                }

                if(line.isEmpty()) {
                    firstLine = true;
                    addWord(db, word, text.toString());
                    text.setLength(0);
                }
            }
            addWord(db, word, text.toString());
            db.close();
            br.close();
        }
        catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
    }

    void addWord(SQLiteDatabase db, String word, String desc) {
        ContentValues cv = new ContentValues();
        cv.put("word", word);
        cv.put("desc", desc.toString());
        db.insert("mytable", null, cv);
    }

    public void onClick(View v){
        SQLiteDatabase db;
        switch(v.getId()){
            case R.id.btnDelete:
                db = dbHelper.getWritableDatabase();
                String s[] = new String[1];
                s[0] = word;
                db.delete("mytable", "word = ?", s);
                db.close();
                break;

            case R.id.btnShow:
                tvDescription.setText(desc);
                break;

            case R.id.btnNext:
                db = dbHelper.getWritableDatabase();
                String[] str = new String[1];

                Cursor c = db.rawQuery("SELECT * FROM mytable WHERE id IN (SELECT id FROM mytable ORDER BY RANDOM() LIMIT 1)", new String[]{});
                c.moveToFirst();
                word = c.getString(1);
                tvWord.setText(word);

                str[0] = c.getString(0);
                c = db.rawQuery("select desc from mytable where id = ?", str);
                c.moveToFirst();

                desc = c.getString(0);
                tvDescription.setText("");
                db.close();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //int groupId, int itemId, int order,
        menu.add(0, 0, 0, "Add words");
        menu.add(0, 1, 0, "Clear dictionary");
        menu.add(0, 2, 0, "Words count");
        menu.add(0, 3, 0, "Edit description");

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        final SQLiteDatabase db;
        final EditText input;
        AlertDialog.Builder builder;
        switch(item.getItemId()){
            case 0:
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Enter the name of dictionary:");
                input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        openDictionary(m_Text);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                break;
            case 1:
                db = dbHelper.getWritableDatabase();
                db.delete("mytable", "1", null);
                db.close();
                break;
            case 2:
                db = dbHelper.getWritableDatabase();
                Cursor c = db.rawQuery("SELECT * FROM mytable", new String[]{});
                Toast toast;
                toast = Toast.makeText(this, String.valueOf(c.getCount()), Toast.LENGTH_SHORT);
                toast.show();
                db.close();
                break;
            case 3:
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Enter new description:");
                input = new EditText(this);
                input.setText(desc);
                //input.setMaxLines(10);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        SQLiteDatabase db;
                        db = dbHelper.getWritableDatabase();
                        String s[] = new String[1];
                        s[0] = word;
                        db.delete("mytable", "word = ?", s);
                        addWord(db, word, m_Text);
                        db.close();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("DROP TABLE IF EXISTS mytable");
            db.execSQL("create table mytable ("
                    + "id integer primary key autoincrement,"
                    + "word text,"
                    + "desc text,"
                    + "CONSTRAINT word_unique UNIQUE (word));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
