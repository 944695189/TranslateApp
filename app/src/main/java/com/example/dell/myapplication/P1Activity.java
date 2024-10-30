package com.example.dell.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class P1Activity extends AppCompatActivity{

    protected GestureDetector mGestureDetector;
    Button P1,P2,P3;
    TextView NEWWORD,TRANSLATE;
    String newword,translate,username;
    ProgressBar pb;
    private int mTimeCount; // 时间计数
    File file;
    String up_Chinese,up_English;//上传的数据
    String picBase64;

    public myHelper myHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p1);

        Intent intent = getIntent();
        username = intent.getStringExtra("Username");
        myHelper=new myHelper(this);

        P1=findViewById(R.id.P1);
        P2=findViewById(R.id.P2);
        P3=findViewById(R.id.P3);
        NEWWORD=findViewById(R.id.NEWWORD);
        TRANSLATE=findViewById(R.id.TRANSLATE);

        pb=findViewById(R.id.pb_record);

        //1.创建一个手势识别器 new 对象，并给这个手势识别器设置监听器
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
            //当手指在屏幕上滑动的时候 调用的方法.
            @Override
            //e1代表的是手指刚开始滑动的事件，e2代表手指滑动完了的事件
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(e1.getRawX() - e2.getRawX() > 200){
                    Intent intent=new Intent(P1Activity.this,P2Activity.class);
                    startActivity(intent);
                    return true;
                }
                if(e2.getRawX() - e1.getRawX() > 200){
                    Intent intent=new Intent(P1Activity.this,P3Activity.class);
                    startActivity(intent);
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        get_up();
        //Toast.makeText(this, up_Chinese, Toast.LENGTH_SHORT).show();
    }

    //2.让手势识别器生效，重写Activity的触摸事件，并且将Activity的触摸事件传入到手势识别器中
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void SEARCH(View v) {
        newword = NEWWORD.getText().toString();
        if (newword.isEmpty()) Toast.makeText(this, "请输入单词", Toast.LENGTH_SHORT).show();
        else{
            TRANSLATE.setText("正在翻译中");
            String salt = num(1);
            String spliceStr = "20231206001903095" + newword + salt + "aIp01fX9KfoPxgURIFTz";
            String sign = stringToMD5(spliceStr);
            asyncGet(newword, "en", "zh", salt, sign);
        }
    }
    public static String num(int a) {
        Random r = new Random(a);
        int ran1 = 0;
        for (int i = 0; i < 5; i++) {
            ran1 = r.nextInt(100);
            System.out.println(ran1);
        }
        return String.valueOf(ran1);
    }
    public static String stringToMD5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
    private void asyncGet(String content, String fromType, String toType, String salt, String sign) {
        String httpsStr = "https://fanyi-api.baidu.com/api/trans/vip/translate";
        //拼接请求的地址
        String url = httpsStr +
                "?appid=" + "20231206001903095" + "&q=" + content + "&from=" + fromType + "&to=" +
                toType + "&salt=" + salt + "&sign=" + sign;
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                //异常返回
                goToUIThread(e.toString(), 0);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //正常返回
                goToUIThread(response.body().string(), 1);
            }
        });
    }
    private void goToUIThread(final Object object, final int key) {
        //切换到主线程处理数据
        P1Activity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TRANSLATE.setText("翻译");
                if (key == 0) {//异常返回
                    //showMsg("异常信息：" + object.toString());
                    Log.e("MainActivity",object.toString());
                } else {//正常返回
                    //通过Gson 将 JSON字符串转为实体Bean
                    final TranslateResult result = new Gson().fromJson(object.toString(), TranslateResult.class);
                    //显示翻译的结果
                    TRANSLATE.setText(result.getTrans_result().get(0).getDst());
                }
            }
        });
    }

//    public void RECORD(View v) {
//            file = new File(getExternalCacheDir(), "aa.amr");
//            try {
//                if (file.exists()) {
//                    file.delete();
//                }
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            MediaRecorder mMediaRecorder = new MediaRecorder(); // 创建一个媒体录制器
//            mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener(){
//                public void onError(MediaRecorder mr, int what, int extra) {
//                    if (mr != null) {
//                        mr.reset(); // 重置媒体录制器
//                    }
//                }
//            }); // 设置媒体录制器的错误监听器
//            mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
//                @Override
//                public void onInfo(MediaRecorder mr, int what, int extra) {
//                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
//                            || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
//                        mr.release();
//                    }
//                }
//            }); // 设置媒体录制器的信息监听器
//            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 设置音频源为麦克风
//            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB); // 设置媒体的输出格式
//            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // 设置媒体的音频编码器
//            mMediaRecorder.setMaxDuration(5 * 1000); // 设置媒体的最大录制时长
//            mMediaRecorder.setOutputFile(file.getPath()); // 设置媒体文件的保存路径
//            try {
//
//                mMediaRecorder.prepare(); // 媒体录制器准备就绪
//                mMediaRecorder.start(); // 媒体录制器开始录制
//
//                mTimeCount = 0; // 时间计数清零
//                pb.setProgress(mTimeCount);
//                Timer mTimer = new Timer(); // 创建一个计时器
//                pb.setMax(5);
//                // 计时器每隔一秒就更新进度条上的录制进度
//                mTimer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        pb.setProgress(mTimeCount++);
//                    }
//                }, 0, 1000);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//    }
    public void PICTURE(View v){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // 关键：新建相机的 Intent
        startActivityForResult(intent, 1); // 加载相机 Activity
    }
    public void DKXC(View v){
        //打开本地相册
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //设定结果返回
        startActivityForResult(intent, 2);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm = null;
        try{
            if (requestCode == 1 && resultCode == RESULT_OK) {
                bm = (Bitmap) data.getExtras().get("data");
                //imageView.setImageBitmap(bm);
            }else if (requestCode == 2 && resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
                //imageView.setImageBitmap(bm);
            }
            picBase64= com.example.dell.myapplication.translatehttp.bitmapToBase64(bm);    } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UPLOAD(View v) {
        // 使用 AsyncTask 进行异步操作，避免在主线程中进行网络请求
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                return uploadImageToServer(picBase64);
            }
            @Override
            protected void onPostExecute(String result) {
                // 处理上传结果
                handleUploadResult(result);
            }
        }.execute();
    }

    private String uploadImageToServer(String base64Image) {
        final Map<String, String> params = new HashMap<String, String>();
        // 更新其他参数
        params.put("image", base64Image);
        params.put("id_card_side", "front");
        params.put("type", "1");
        params.put("q", base64Image);
        params.put("from", "en");
        params.put("to", "zh-CHS");
        params.put("appKey", "308a5c6e452e0567");
        params.put("salt", String.valueOf(System.currentTimeMillis()));
        params.put("sign", calculateSign(params.get("appKey"), base64Image, params.get("salt")));
        params.put("signType", "v3");
        params.put("curtime", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("ext", "v3");
        params.put("docType", "json");
        String uploadUrl = "https://openapi.youdao.com/ocrtransapi";
        try {
            // 构建 HTTP 请求
            URL url = new URL(uploadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);
            // 构建请求体
            StringBuilder requestBody = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                requestBody.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                requestBody.append("=");
                requestBody.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                requestBody.append("&");
            }
            // 将请求体写入连接
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.toString().getBytes("UTF-8"));
            outputStream.close();
            // 获取服务器响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取服务器响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } else {
                return "上传失败。响应代码：" + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "上传失败。异常：" + e.getMessage();
        }
    }
    private String calculateSign(String appKey, String q, String salt) {
        String input;
        if (q.length() > 20) {
            input = q.substring(0, 10) + q.length() + q.substring(q.length() - 10);
        } else {
            input = q;
        }
        input = appKey + input + salt + System.currentTimeMillis() / 1000 + "CF5pozyO1BzLZ61c2TYAnjpyoN6qLuW4";
        Log.i("sss",input);
        Log.i("sm",SHAUtil.getSHA256(input));// 替换为您的应用密钥
        return SHAUtil.getSHA256(input);
    }
    private void handleUploadResult(String result) {
        try {
            // 清理 JSON 字符串
            result = result.replaceAll("[^\\x20-\\x7e]", "").trim();
            // 解析 JSON 响应
            JSONObject jsonResponse = new JSONObject(result);
            // 提取相关信息
            if (jsonResponse.has("resRegions")) {
                JSONArray resRegionsArray = jsonResponse.getJSONArray("resRegions");
                // 检查是否存在有效的翻译文本
                if (resRegionsArray.length() > 0) {
                    JSONObject firstRegion = resRegionsArray.getJSONObject(0);
                    //String translatedText = firstRegion.optString("tranContent");
                    String translatedText = firstRegion.optString("context");
                    // 根据响应更新 UI 或执行其他操作
                    if (translatedText != null && !translatedText.isEmpty()) {
                        // 使用已翻译的文本更新 UI
                        NEWWORD.setText(translatedText);
                    } else {
                        // 处理无法获得翻译文本的情况
                        Toast.makeText(this, "无法获取翻译文本", Toast.LENGTH_SHORT).show();
                        Log.d("UploadResult", result);
                    }
                } else {
                    // 处理无 resRegions 的情况
                    Toast.makeText(this, "无 resRegions 字段", Toast.LENGTH_SHORT).show();
                    Log.d("UploadResult", result);
                }
            } else {
                // 处理无 resRegions 字段的情况
                Toast.makeText(this, "无 resRegions 字段", Toast.LENGTH_SHORT).show();
                Log.d("UploadResult", result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // 处理 JSON 解析异常
            Toast.makeText(this, "解析JSON响应时出错", Toast.LENGTH_SHORT).show();
            Log.d("UploadResult", result);
        }
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
    //插入
    public void insert(String english_word,String chinese_word){
        SQLiteDatabase db=myHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("english_word",english_word);
        values.put("chinese_word",chinese_word);
        db.insert("information",null,values);
        db.close();
    }
    //删除
    public void delete(String name){
        SQLiteDatabase db=myHelper.getWritableDatabase();
        db.delete("information","english_word=?",new String[]{name});
        db.close();
    }
    public boolean find(String english_word){
        SQLiteDatabase db=myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from information",null);
        while (cursor.moveToNext()) {
            if(english_word.equals(cursor.getString(cursor.getColumnIndex("english_word")))){
                cursor.close();
                return true;
            }
        }
        cursor.close();
        return false;
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
    public void COLLECT(View v)throws IOException{
        //检查新单词是否已存在
        if(find(NEWWORD.getText().toString())) Toast.makeText(P1Activity.this, "已经存在，无法添加！", Toast.LENGTH_SHORT).show();
        else {insert(NEWWORD.getText().toString(),TRANSLATE.getText().toString());//本地插入新单词
            //Tup=true;
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
                            //stmt.setString(1,"54");
                            //stmt.setString(2,"885");
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
            Toast.makeText(P1Activity.this, "成功添加", Toast.LENGTH_SHORT).show();
        }
    }
    //数据库连接类
    private static Connection con = null;
    private static PreparedStatement stmt = null;
    boolean Tup=false;
    public void P1(View v){
        Toast.makeText(P1Activity.this, "已在“闯关学习”页面！", Toast.LENGTH_SHORT).show();
    }
    public void P2(View v){
        Intent intent=new Intent(this,P2Activity.class);
        intent.putExtra("Username",username);
        startActivity(intent);
    }
    public void P3(View v){
        Intent intent=new Intent(this,P3Activity.class);
        intent.putExtra("Username",username);
        startActivity(intent);
    }
}
