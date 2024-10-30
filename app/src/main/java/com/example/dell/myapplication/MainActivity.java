package com.example.dell.myapplication;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    String zc_username="",zc_password="",zc_name="";
    String dl_username="",dl_password="",dl_name="";

    private boolean T2=false;//我定的发送标志位，注册账号成功时T1=true并且执行发送线程
    private boolean T1=false;//登录
    private boolean T3=false;//注册时第一步输入账号，检测账号是否为空
    private boolean empty=true;//注册第一步输入账号，假设为空可用
    private Button DL;//登录按钮
    private TextView USERNAME,PASSWORD;
    private CheckBox CONTRACT_CB,REMEMBER1_CB,REMEMBER2_CB;
    //数据库连接类
    private static Connection con = null;
    private static PreparedStatement stmt = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        USERNAME=findViewById(R.id.USERNAME);
        PASSWORD=findViewById(R.id.PASSWORD);
        CONTRACT_CB=findViewById(R.id.CONTRACT_CB);
        REMEMBER1_CB=findViewById(R.id.REMEMBER1_CB);
        REMEMBER2_CB=findViewById(R.id.REMEMBER2_CB);
        DL=findViewById(R.id.DL);
        //账号密码自动填写
        SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        dl_username= sp.getString("username","");
        dl_password= sp.getString("password","");
        USERNAME.setText(dl_username);
        PASSWORD.setText(dl_password);
        boolean R1=sp.getBoolean("R1",false),R2=sp.getBoolean("R2",false),C=sp.getBoolean("C",false);
        REMEMBER1_CB.setChecked(R1);
        REMEMBER2_CB.setChecked(R2);
        CONTRACT_CB.setChecked(C);
        //按钮设置监听器，后面可以回来学学
        DL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        try {
                            con = MySQLConnections.getConnection();
                            String sql ="Select username,password,name from information order by username";
                            if (con!=null) {
                                if(CONTRACT_CB.isChecked()){
                                    stmt = con.prepareStatement(sql);
                                    con.setAutoCommit(false);
                                    ResultSet rs = stmt.executeQuery();
                                    int acc=0,exit=0;
                                    while (rs.next()&&acc==0&&exit==0) {
                                        if(rs.getString(1).equals(dl_username)){
                                            if(rs.getString(2).equals(dl_password)){//登录成功
                                                acc=1;
                                                dl_name=rs.getString(3);
                                                SharedPreferences sp = getSharedPreferences("data",MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sp.edit();
                                                //保存账号密码
                                                if(REMEMBER1_CB.isChecked()){
                                                    editor.putString("username",dl_username);
                                                }
                                                else{
                                                    editor.clear();
                                                }
                                                if(REMEMBER2_CB.isChecked()){
                                                    editor.putString("username",dl_username);
                                                    editor.putString("password",dl_password);
                                                }
                                                else{
                                                    editor.remove("password");
                                                }
                                                editor.putBoolean("R1",REMEMBER1_CB.isChecked());
                                                editor.putBoolean("R2",REMEMBER2_CB.isChecked());
                                                editor.putBoolean("C",CONTRACT_CB.isChecked());
                                                editor.commit();
                                                //登录成功进行跳转
                                                Intent intent = new Intent(MainActivity.this,P1Activity.class);
                                                intent.putExtra("Username",dl_username);
                                                startActivity(intent);
                                            }
                                            else exit=1;//密码错误
                                        }
                                    }
                                    if(acc==1)
                                    { runOnUiThread(new Runnable() {
                                        @Override public void run() {Toast.makeText(MainActivity.this, "登陆成功，欢迎"+dl_name, Toast.LENGTH_SHORT).show();}
                                    }); }
                                    else
                                    { runOnUiThread(new Runnable() {
                                        @Override public void run() {Toast.makeText(MainActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();}
                                    }); }
                                    con.commit();
                                    rs.close();
                                    stmt.close();
                                }
                                else{
                                    { runOnUiThread(new Runnable() {
                                        @Override public void run() {Toast.makeText(MainActivity.this, "请勾选用户协议", Toast.LENGTH_SHORT).show();}
                                    }); }
                                }
                            }
                        }catch (Exception e){
                            System.out.println(e);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() { }
                            });
                        }
                    }
                }.start();
                dl_username=USERNAME.getText().toString();
                dl_password=PASSWORD.getText().toString();
            }//进行线程中登录
        });
        REMEMBER1_CB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!REMEMBER1_CB.isChecked())REMEMBER2_CB.setChecked(false);
            }//进行线程中登录
        });
        REMEMBER2_CB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(REMEMBER2_CB.isChecked())REMEMBER1_CB.setChecked(true);
            }//进行线程中登录
        });
        //启动发送线程，用按钮控制发送标志位T，来进行发送信息【注意：连接数据库必须在线程内，不然会报错】
        //TODO 启动获取数据线程，读取数据库里的信息【注意：连接数据库必须在线程内，不然会报错】

        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    MainActivity.this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }
    //测试注册
    public void ZC(View v) {

        final AlertDialog.Builder builder_username = new AlertDialog.Builder(this);
        builder_username.setTitle("注册：账号(10位数字)");
        final EditText et1 = new EditText(this);
        et1.setHint("请输入账号");
        et1.setSingleLine(true);
        builder_username.setView(et1);

        final AlertDialog.Builder builder_password = new AlertDialog.Builder(this);
        builder_password.setTitle("注册：密码(10位数字与字母组成的串)");
        final EditText et2= new EditText(this);
        et2.setHint("请输入密码");
        et2.setSingleLine(true);
        builder_password.setView(et2);

        final AlertDialog.Builder builder_name = new AlertDialog.Builder(this);
        builder_name.setTitle("注册：名称（数字、字母、汉字组成的不超过10个字符的串）");
        final EditText et3 = new EditText(this);
        et3.setHint("请输入名称");
        et3.setSingleLine(true);
        builder_name.setView(et3);

        builder_username.setNegativeButton("取消",null);
        builder_username.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        zc_username = et1.getText().toString();
                        if(zc_username.length()==10) {
                            //T3=true;//查询该账号是否可用
                            new Thread(){//为数据库连接创建新线程
                                public void run(){
                                    try {
                                        con = MySQLConnections.getConnection();
                                        String sql ="Select username from information order by username";
                                        if (con!=null) {
                                            stmt = con.prepareStatement(sql);
                                            con.setAutoCommit(false);
                                            ResultSet rs = stmt.executeQuery();
                                            empty=true;
                                            while (rs.next()&&empty) { if(rs.getString(1).equals(zc_username))empty=false; }
                                            con.commit();
                                            rs.close();
                                            stmt.close();
                                        }
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                            //等待一段时间，在这时间中查验账号是否可用
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(empty){
                                Toast.makeText(MainActivity.this, "账号可用！", Toast.LENGTH_SHORT).show();
                                AlertDialog SECOND=builder_password.create();
                                SECOND.show();
                            }
                            else Toast.makeText(MainActivity.this, "账号已存在！", Toast.LENGTH_SHORT).show();
                        }
                        else Toast.makeText(MainActivity.this, "账号长度出错！", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        builder_password.setNegativeButton("取消",null);
        builder_password.setPositiveButton("确定",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        zc_password = et2.getText().toString();
                        if(zc_password.length()==10) {
                            AlertDialog THIRD=builder_name.create();
                            THIRD.show();
                        }
                        else Toast.makeText(MainActivity.this, "密码长度出错！", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        builder_name.setNegativeButton("取消",null);
        builder_name.setPositiveButton("确定",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        zc_name = et3.getText().toString();
                        if(zc_name.length()<=10&&zc_name.length()!=0){
                            Toast.makeText(MainActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                            //T2=true;//进行线程中注册信息

                            new Thread(){
                                public void run(){
                                    try {
                                        con = MySQLConnections.getConnection();
                                        if (con!=null) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            String sql = "insert into information(username,password,name)  values(?,?,?)";
                                            stmt = con.prepareStatement(sql);
                                            // 关闭事务自动提交 ,这一行必须加上
                                            con.setAutoCommit(false);
                                            stmt.setString(1,zc_username);
                                            stmt.setString(2,zc_password);
                                            stmt.setString(3,zc_name);
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
                        else Toast.makeText(MainActivity.this, "名称长度出错！", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        //先输入账号，再输入密码，最后输入名称
        AlertDialog FIRST = builder_username.create();
        FIRST.show();
    }
    //查看用户协议
    protected void CONTRACT(View v){
        AlertDialog dialog;
        dialog = new AlertDialog.Builder(this)
                .setTitle("用户协议")
                .setIcon(R.mipmap.ic_launcher)//图案
                .setMessage("欢迎您使用腾讯统一身份产品及服务！\n" +
                        "\n" +
                        "为使用腾讯统一身份产品（以下统称“本产品”）及服务（以下统称“本服务”），您应当阅读并遵守《腾讯统一身份 用户服务协议》（以下简称“本协议”），以及 《腾讯服务协议》、 《腾讯隐私政策》、 《腾讯统一身份隐私保护指引》。\n" +
                        "\n" +
                        "请您在注册成为腾讯统一身份用户前务必审慎阅读、充分理解各条款内容，特别是免除或者限制腾讯责任的条款、对用户权利进行限制的条款、约定争议解决方式和司法管辖的条款等，以及开通或使用某项服务的单独协议。限制、免责条款或者其他涉及您重大权益的条款可能以加粗、加下划线等形式提示您重点注意。您应重点阅读，如果您对协议有任何疑问，请联系我们进行咨询。\n" +
                        "\n" +
                        "除非您已阅读并接受本协议所有条款，否则您无权使用腾讯统一身份服务。您的注册、登录、发布信息等行为即视为您已阅读并同意本协议的约束。如果您因年龄、智力等因素而不具有完全民事行为能力，请在法定监护人（以下简称\"监护人\"）的陪同下阅读和判断是否同意本协议。如果您是中国大陆地区以外的用户，您订立或履行本协议以及使用本服务的行为还需要同时遵守您所属和/或所处国家或地区的法律。")
                .setPositiveButton("我已知晓",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {CONTRACT_CB.setChecked(true);}})
                .setNegativeButton("拒绝",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {CONTRACT_CB.setChecked(false);}})
                .create();
        dialog.show();
    }
    //仅记住账号
    protected void REMEMBER1(View v){
        if(REMEMBER1_CB.isChecked())
        {
            REMEMBER1_CB.setChecked(false);
            REMEMBER2_CB.setChecked(false);
        }
        else REMEMBER1_CB.setChecked(true);
    }
    //记住账号密码
    protected void REMEMBER2(View v){
        if(REMEMBER2_CB.isChecked())
        {
            REMEMBER2_CB.setChecked(false);
        }
        else {
            REMEMBER1_CB.setChecked(true);
            REMEMBER2_CB.setChecked(true);
        }
    }

}
