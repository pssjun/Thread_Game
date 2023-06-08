package com.example.thread_game;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    Handler feverEndHandler;
    Thread thread = null;
    TextView coin;
    TextView time;
    TextView score;
    TextView life; // 라이프 텍스트뷰 추가
    ImageView[] imgViewArr = new ImageView[16];
    ImageView[] lifeViewArr = new ImageView[5];

    int[] imageId = {R.id.card01, R.id.card02, R.id.card03, R.id.card04, R.id.card05, R.id.card06, R.id.card07, R.id.card08, R.id.card09, R.id.card10, R.id.card11, R.id.card12, R.id.card13, R.id.card14, R.id.card15, R.id.card16};
    int[] imagelifeID = {R.id.life1, R.id.life2, R.id.life3, R.id.life4, R.id.life5};

    public static final int ran[] = {R.drawable.up_mole, R.drawable.up_mole1, R.drawable.up_rabbit, R.drawable.coin};
    int sc = 0;
    int cn = 0;
    int lifeCount = 5; // 라이프 개수 변수 추가

    final String TAG_Mole1 = "mole";
    final String TAG_Mole2 = "mole1";
    final String TAG_Rabbit = "rabbit";
    final String TAG_Coin = "coin";

    boolean isInFever = false;
    int feverLevel = 0;
    boolean fever = false; // fever 변수 추가
    final int[] feverThresholds = {10, 20, 30};
    final int[] feverColors = {R.color.white, R.color.yellow, R.color.blue, R.color.red};
    final String TAG_Empty = "empty";
    int feverDuration = 0;
    int feverScore = 0;
    int moleCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);


        coin = findViewById(R.id.coin_tv);
        coin.setText("Coin\n0");
        feverEndHandler = new Handler();
        time = findViewById(R.id.time_tv);
        score = findViewById(R.id.score_tv);
        life = findViewById(R.id.life_tv); // 라이프 텍스트뷰 연결

        for(int i = 0; i < lifeCount; i++) {
            lifeViewArr[i] = (ImageView) findViewById(imagelifeID[i]);
            lifeViewArr[i].setImageResource(R.drawable.life);
        }


        for (int i = 0; i < imgViewArr.length; i++) {
            final int position = i;
            imgViewArr[i] = (ImageView) findViewById(imageId[i]);
            imgViewArr[i].setImageResource(R.drawable.off);
            imgViewArr[i].setTag(TAG_Empty);

            imgViewArr[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isInFever) {
                        handleFeverClick(v, position);
                    } else {
                        handleNormalClick(v, position); // Handle normal click
                    }

                    if (((ImageView) v).getTag().toString().equals(TAG_Mole1) ||
                            ((ImageView) v).getTag().toString().equals(TAG_Mole2) ||
                            ((ImageView) v).getTag().toString().equals(TAG_Rabbit)) {
                        ((ImageView) v).setImageResource(R.drawable.off);
                    }
                }
            });

        }
        time.setText("Time : 20");
        score.setText("Point : 0");
        life.setText("Life : " + lifeCount); // Initialize life count

        new Thread(new Timer()).start();
        for (int i = 0; i < imgViewArr.length; i++) {
            new Thread(new ObjectThread(i)).start();
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            time.setText("Time : " + msg.arg1);
        }
    };

    Handler onHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            for (int i = 0; i < 5; i++) {
                int index = (int) (Math.random() * 4);
                imgViewArr[msg.arg1].setImageResource(ran[index]);
                if (index == 0) {
                    imgViewArr[msg.arg1].setTag(TAG_Mole1);
                } else if (index == 1) {
                    imgViewArr[msg.arg1].setTag(TAG_Mole2);
                } else if (index == 2) {
                    imgViewArr[msg.arg1].setTag(TAG_Rabbit);
                } else {
                    imgViewArr[msg.arg1].setTag(TAG_Coin);
                }
            }
        }
    };

    Handler offHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            imgViewArr[msg.arg1].setImageResource(R.drawable.off);
        }
    };

    public class Timer implements Runnable {
        final int TIME = 60;

        @Override
        public void run() {
            for (int i = TIME; i >= 0; i--) {
                Message msg = new Message();
                msg.arg1 = i;
                handler.sendMessage(msg);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent(GameActivity.this, ResultActivity.class);
            intent.putExtra("score", sc);
            startActivity(intent);
            finish();
        }
    }

    public class ObjectThread implements Runnable {
        int index;

        ObjectThread(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Message msg1 = new Message();
                    int offTime = new Random().nextInt(6000) + 1000;
                    Thread.sleep(offTime);

                    msg1.arg1 = index;
                    onHandler.sendMessage(msg1);

                    int onTime = new Random().nextInt(3000) + 500;
                    Thread.sleep(onTime);
                    Message msg2 = new Message();
                    msg2.arg1 = index;
                    offHandler.sendMessage(msg2);

                    if (isInFever) {
                        feverDuration--;
                        if (feverDuration <= 0) {
                            isInFever = false;
                            feverLevel = 0;
                            feverScore = 0;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    score.setText("Point : " + String.valueOf(sc));
                                    score.setTextColor(getResources().getColor(R.color.white));
                                }
                            });
                        }
                    } else {
                        if (!fever && sc >= feverThresholds[feverLevel]) {
                            fever = true;
                            feverLevel++;
                            feverDuration = 5;
                            feverScore = sc;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    score.setText("Point : " + String.valueOf(feverScore));
                                    score.setTextColor(getResources().getColor(feverColors[feverLevel]));
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void handleNormalClick(View v,int position) {
        if (((ImageView) v).getTag().toString().equals(TAG_Mole1)) {
            score.setText("Point : " + String.valueOf(sc += 100));
            v.setTag(TAG_Empty);
        } else if (((ImageView) v).getTag().toString().equals(TAG_Mole2)) {
            score.setText("Point : " + String.valueOf(sc -= 100));
            if (sc < 0) {
                sc = 0;
            }
            v.setTag(TAG_Empty);
        } else if (((ImageView) v).getTag().toString().equals(TAG_Rabbit)) {
            //연타 하면 튕기는 오류 있음.
            //연타 하면 라이프 두개 깎이는 오류 있음.
            lifeCount--; // 라이프 감소
            life.setText("Life : " + lifeCount);
            v.setTag(TAG_Empty);
            for (int j = lifeCount; j < lifeViewArr.length; j++) {
                //lifeViewArr[j] = (ImageView) findViewById(imagelifeID[j]);
                lifeViewArr[j].setImageResource(R.drawable.off);
            }
            if (lifeCount <= 0) {
                Toast.makeText(GameActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                intent.putExtra("score", sc);
                startActivity(intent);
                finish();
            }
        } else if (((ImageView) v).getTag().toString().equals(TAG_Coin)) { // 코인 처리 부분 추가
            coin.setText("Coin\n" + String.valueOf(cn += 5));
            ((ImageView) v).setImageResource(R.drawable.off);
            v.setTag(TAG_Empty);
        } else if (((ImageView) v).getTag().toString().equals(TAG_Empty)) { // 빈자리 처리 부분 추가
            //필요시 코드 추가
            v.setTag(TAG_Empty);
            lifeCount--;
        } else {
        }

        imgViewArr[position].setImageResource(R.drawable.off);
        imgViewArr[position].setTag(TAG_Empty);
    }

    private void handleFeverClick(View v, int position){
        if (((ImageView) v).getTag().toString().equals(TAG_Mole1)) {
            score.setText("Point : " + String.valueOf(sc += (2 * (feverLevel + 1) * 100)));
            v.setTag(TAG_Empty);
        } else if (((ImageView) v).getTag().toString().equals(TAG_Mole2)) {
            score.setText("Point : " + String.valueOf(sc -= (2 * (feverLevel + 1) * 100)));
            v.setTag(TAG_Empty);
        } else if (((ImageView) v).getTag().toString().equals(TAG_Rabbit)) {
            isInFever = false;
            fever = false;
            feverLevel = 0;
            feverDuration = 0;
            feverScore = 0;
            lifeCount--; // Decrease life count
            v.setTag(TAG_Empty);
            life.setText("Life : " + lifeCount);
            for (int j = lifeCount; j < lifeViewArr.length; j++) {
                //lifeViewArr[j] = (ImageView) findViewById(imagelifeID[j]);
                lifeViewArr[j].setImageResource(R.drawable.off);
            }
            if (lifeCount <= 0) {
                Toast.makeText(GameActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                intent.putExtra("score", sc);
                startActivity(intent);
                finish();
                score.setTextColor(getResources().getColor(R.color.white));
            }
        } else if (((ImageView) v).getTag().toString().equals(TAG_Coin)) { // Coin handling
            coin.setText("Coin\n" + String.valueOf(cn += 5));
            ((ImageView) v).setImageResource(R.drawable.off);
            v.setTag(TAG_Empty);
        } else if (((ImageView) v).getTag().toString().equals(TAG_Empty)) { // 빈자리 처리 부분 추가
            lifeCount--;
            v.setTag(TAG_Empty);
        } else {
        }
    }

}
