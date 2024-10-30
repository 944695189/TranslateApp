package com.example.dell.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class P3Activity extends AppCompatActivity {

    protected GestureDetector mGestureDetector;
    String username;
    ListView LV;
    public myHelper myHelper;
    TextView EMPTY;
    String up_Chinese,up_English;
    String dw_Chinese,dw_English;

    List<String> list_english=new ArrayList<>();
    List<String> list_chinese=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p3);

        Intent intent = getIntent();
        username = intent.getStringExtra("Username");
        EMPTY=findViewById(R.id.EMPTY);
        myHelper=new myHelper(this);
        LV=findViewById(R.id.LV);
        show();
        LV.setDivider(new ColorDrawable(Color.RED));//设置分割线
        LV.setDividerHeight(5);//设置分割线的宽度
        LV.setAdapter(new MyAdapter());//填充数据

        //1.创建一个手势识别器 new 对象，并给这个手势识别器设置监听器
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
            //当手指在屏幕上滑动的时候 调用的方法.
            @Override
            //e1代表的是手指刚开始滑动的事件，e2代表手指滑动完了的事件
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(e1.getRawX() - e2.getRawX() > 200){
                    Intent intent=new Intent(P3Activity.this,P1Activity.class);
                    startActivity(intent);
                    return true;
                }
                if(e2.getRawX() - e1.getRawX() > 200){
                    Intent intent=new Intent(P3Activity.this,P2Activity.class);
                    startActivity(intent);
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }
    //2.让手势识别器生效，重写Activity的触摸事件，并且将Activity的触摸事件传入到手势识别器中
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    //创建数据库,初始化
    public class myHelper extends SQLiteOpenHelper {
        public myHelper(Context context) { super(context, username+".db", null, 4); }
        //下面这个onCreate只执行一次，之后要想再添加变量需要用更新，改变version,改更新
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE information( english_word Text,chinese_word Text)"); }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
    public void show(){
        SQLiteDatabase db=myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from information",null);
        boolean empty=true;
        while (cursor.moveToNext()) {
            empty=false;
            list_english.add(cursor.getString(cursor.getColumnIndex("english_word")));
            list_chinese.add(cursor.getString(cursor.getColumnIndex("chinese_word")));
        }
        cursor.close();
        if(empty)EMPTY.setText("目前还没有单词收录");
    }
    //插入
    public void insert(String english_word,String chinese_word){
        SQLiteDatabase db=myHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("english_word",english_word);
        values.put("chinese_word",chinese_word);
        db.insert("information",null,values);
        db.close();
    }
    public void delete(String name){
        SQLiteDatabase db=myHelper.getWritableDatabase();
        db.delete("information","english_word=?",new String[]{name});
        db.close();
    }
    public void delete_all(){
        SQLiteDatabase db=myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from information",null);
        while (cursor.moveToNext()) {
            delete(cursor.getString(cursor.getColumnIndex("english_word")));
        }
        cursor.close();
    }
    //从本地数据库中读取中文单词，并且组成一句话
    public void get_up(){
        SQLiteDatabase db=myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from information",null);
        up_Chinese="";
        up_English="";
        while (cursor.moveToNext()) {
            up_Chinese=up_Chinese+cursor.getString(cursor.getColumnIndex("chinese_word"))+",";
            up_English=up_English+cursor.getString(cursor.getColumnIndex("english_word"))+",";
        }
        cursor.close();
    }
    //数据库连接类
    private static Connection con = null;
    private static PreparedStatement stmt = null;
    boolean Tup2=false;//修改单词，本地覆盖云端
    boolean Tup3=false;//云端覆盖本地
    public class MyAdapter extends BaseAdapter{//BaseAdapter是一个抽象类，所以需要重写这个抽象类方法
        @Override
        public int getCount() {//产生的条目的数量
            return list_english.size();//获取lists的大小
        }
        @Override
        public Object getItem(int position) {
            return null;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            String s1 = list_english.get(position);
            String s2 = list_chinese.get(position);
            View view=View.inflate(P3Activity.this,R.layout.list_item,null);
            //拿到文本的引用(直接用findViewById只能拿到activity对应布局文件下的控件，所以要加一个view
            final TextView ENGLISH=view.findViewById(R.id.ENGLISH);
            ENGLISH.setText(s1);
            final TextView CHINESE=view.findViewById(R.id.CHINESE);
            CHINESE.setText(s2);
            Button DELETE=view.findViewById(R.id.DELETE);
            DELETE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder DELETE = new AlertDialog.Builder(P3Activity.this);
                    DELETE.setTitle("是否删除该单词？");
                    DELETE.setNegativeButton("取消",null);
                    DELETE.setPositiveButton("确定",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    list_chinese.remove(position);
                                    list_english.remove(position);
                                    delete(ENGLISH.getText().toString());
                                    notifyDataSetChanged();
                                    //Tup2=true;
                                    new Thread(){
                                        public void run(){
                                            try {
                                                con = MySQLConnections.getConnection();
                                                if (con != null) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            //Toast.makeText(P1Activity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                    String sql = "update information set english_word = ? ,chinese_word = ? where username = ?;";
                                                    stmt = con.prepareStatement(sql);
                                                    // 关闭事务自动提交 ,这一行必须加上
                                                    con.setAutoCommit(false);
                                                    get_up();
                                                    stmt.setString(1,up_English);
                                                    stmt.setString(2,up_Chinese);
                                                    stmt.setString(3,username);
                                                    stmt.addBatch();
                                                    stmt.executeBatch();
                                                    con.commit();
                                                }
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }
                            }
                    );
                    AlertDialog delete = DELETE.create();
                    delete.show();
                }
            });
            return view;
        }
    }
    public void UPDATE(View v){
                final AlertDialog.Builder DELETE = new AlertDialog.Builder(P3Activity.this);
                DELETE.setTitle("选择同步数据");
                DELETE.setNegativeButton("云端数据",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        //Tup3=true;
                        new Thread(){
                            public void run(){
                                try {
                                    con = MySQLConnections.getConnection();
                                    String sql ="Select username,chinese_word,english_word from information order by username";
                                    if (con!=null) {
                                        stmt = con.prepareStatement(sql);
                                        con.setAutoCommit(false);
                                        ResultSet rs = stmt.executeQuery();
                                        while (rs.next()) { if(rs.getString(1).equals(username))
                                        {dw_Chinese=rs.getString(2);
                                            dw_English=rs.getString(3);
                                            String[] ch_parts = dw_Chinese.split(",");
                                            String[] en_parts = dw_English.split(",");
                                            delete_all();
                                            for(int i=0;ch_parts[i]!=null;i++){
                                                if(ch_parts[i]!="")
                                                    insert(en_parts[i],ch_parts[i]);
                                            }
                                        }
                                        }
                                        con.commit();
                                        rs.close();
                                        stmt.close();
                                    }
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }});
                DELETE.setPositiveButton("本地数据",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Tup2=true;//借修改数据的直接本地覆盖云端
                        new Thread(){
                            public void run(){
                                try {
                                    con = MySQLConnections.getConnection();
                                    if (con != null) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //Toast.makeText(P1Activity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        String sql = "update information set english_word = ? ,chinese_word = ? where username = ?;";
                                        stmt = con.prepareStatement(sql);
                                        // 关闭事务自动提交 ,这一行必须加上
                                        con.setAutoCommit(false);
                                        get_up();
                                        stmt.setString(1,up_English);
                                        stmt.setString(2,up_Chinese);
                                        stmt.setString(3,username);
                                        stmt.addBatch();
                                        stmt.executeBatch();
                                        con.commit();
                                    }
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }});
                AlertDialog delete = DELETE.create();
                delete.show();
    }


    public void P1(View v){
        Intent intent=new Intent(this,P1Activity.class);
        intent.putExtra("Username",username);
        startActivity(intent);
    }
    public void P2(View v){
        Intent intent=new Intent(this,P2Activity.class);
        intent.putExtra("Username",username);
        startActivity(intent);
    }
    public void P3(View v){
        Toast.makeText(P3Activity.this, "已在“单词本”页面！", Toast.LENGTH_SHORT).show();
    }
}
