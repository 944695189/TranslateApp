package com.example.dell.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class P2Activity extends AppCompatActivity {
    private TextView questionTextView;
    private EditText answerEditText;
    private Button submitBtn;
    private TextView scoreTextView; // 显示得分

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0; // 记录得分
    protected GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2);

        //1.创建一个手势识别器 new 对象，并给这个手势识别器设置监听器
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            //当手指在屏幕上滑动的时候 调用的方法.
            @Override
            //e1代表的是手指刚开始滑动的事件，e2代表手指滑动完了的事件
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getRawX() - e2.getRawX() > 200) {
                    Intent intent = new Intent(P2Activity.this, P3Activity.class);
                    startActivity(intent);
                    return true;
                }
                if (e2.getRawX() - e1.getRawX() > 200) {
                    Intent intent = new Intent(P2Activity.this, P1Activity.class);
                    startActivity(intent);
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        questionTextView = findViewById(R.id.questionTextView);
        answerEditText = findViewById(R.id.answerEditText);
        submitBtn = findViewById(R.id.submitBtn);
        scoreTextView = findViewById(R.id.scoreTextView);

        // 初始化题目列表
        questionList = new ArrayList<>();
        questionList.add(new Question("你", "you"));
        questionList.add(new Question("我", "me"));
        questionList.add(new Question("他", "him"));

        showNextQuestion();

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });
    }

    private void showNextQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            Question question = questionList.get(currentQuestionIndex);
            questionTextView.setText(question.getChinese());
            answerEditText.setText("");
        } else {
            Toast.makeText(this, "All questions answered! Your score: " + score, Toast.LENGTH_SHORT).show();
            scoreTextView.setText("Final Score: " + score); // 显示最终得分
        }
    }
    private void checkAnswer() {
        if (currentQuestionIndex < questionList.size()) {
            Question question = questionList.get(currentQuestionIndex);
            String userAnswer = answerEditText.getText().toString().trim();
            questionTextView.setText(question.getEnglish());
            if (userAnswer.equalsIgnoreCase(question.getEnglish())) {
                if (!question.isAnswered()) {
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
                    score++;
                } else {
                    Toast.makeText(this, "You already answered this question correctly.", Toast.LENGTH_SHORT).show();
                }
                question.setAnswered(true);
            } else {
                Toast.makeText(this, "Incorrect! Try again.", Toast.LENGTH_SHORT).show();
                return; // 不更新题目索引，保持当前题目不变
            }
            currentQuestionIndex++;
            showNextQuestion();
            scoreTextView.setText("Score: " + score);
        }

    }

    //2.让手势识别器生效，重写Activity的触摸事件，并且将Activity的触摸事件传入到手势识别器中
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }








    private static class MyHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "itcast.db";
        private static final int DATABASE_VERSION = 2;

        public MyHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE information( id INTEGER PRIMARY " + "KEY AUTOINCREMENT, Chinese Text, English Text)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public void insert(String chinese, String english) {
        MyHelper helper = new MyHelper(P2Activity.this);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Chinese", chinese);
        values.put("English", english);
        long id = db.insert("information", null, values);
        db.close();
    }


    public int delete(long id){
        MyHelper helper = new MyHelper(P2Activity.this);
        SQLiteDatabase database= helper.getWritableDatabase();
        int number = database.delete("information", "_id=?", new String[]{id+""});
        database.close();
        return number;
    }
    public void find(int id){
        MyHelper helper = new MyHelper(P2Activity.this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("information", null, "id=?", new String[]{id+""},null, null, null);
        if (cursor.getCount() != 0){
            while (cursor.moveToNext()){
                String _id = cursor.getString(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("chinese"));
                String price = cursor.getString(cursor.getColumnIndex("english"));
            }
        }
        cursor.close();
        db.close();
    }








    public void P1(View v){
        Intent intent=new Intent(this,P1Activity.class);
        startActivity(intent);
    }
    public void P2(View v){
        Toast.makeText(P2Activity.this, "已在“闯关学习”页面！", Toast.LENGTH_SHORT).show();
    }
    public void P3(View v){
        Intent intent=new Intent(this,P3Activity.class);
        startActivity(intent);
    }
}
