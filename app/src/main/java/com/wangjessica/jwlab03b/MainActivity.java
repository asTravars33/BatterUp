package com.wangjessica.jwlab03b;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    // Shared preferences variables
    String TAG = "com.wangjessica.jwlab03b.values";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    // GUI component variables
    ConstraintLayout layout;
    Button[] squares;
    TextView message;
    TextView guessCnt;
    TextView record;
    ImageView img;
    ImageView back;
    // Internal variables and storage
    Map<Integer, Boolean> cupcakes = new HashMap<Integer, Boolean>();
    String[] stages = {"wrapper", "cake", "icing", "cherry"};
    boolean gameInProg = false;
    int lastAnswer=-1;
    int cupcakeCnt = 0;
    int streak = 0;
    int answer;
    int waitTime = 2500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        layout = findViewById(R.id.layout);
        guessCnt = findViewById(R.id.guess_cnt);
        record = findViewById(R.id.record);
        message = findViewById(R.id.center_message);
        img = findViewById(R.id.center_image);
        back = findViewById(R.id.img_background);

        Button wrapper = findViewById(R.id.wrapper);
        Button cake = findViewById(R.id.cake);
        Button icing = findViewById(R.id.icing);
        Button cherry = findViewById(R.id.cherry);

        wrapper.setOnClickListener(this);
        cake.setOnClickListener(this);
        icing.setOnClickListener(this);
        cherry.setOnClickListener(this);

        squares = new Button[]{wrapper, cake, icing, cherry};

        SeekBar seekbar = findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int lastProgress;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                waitTime = -i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                lastProgress = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Snackbar snackBar = Snackbar.make(layout,
                        "Pause Time Changed To "+(waitTime/1000.0)+" sec",
                        Snackbar.LENGTH_LONG);
                snackBar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        seekBar.setProgress(lastProgress);
                        waitTime = -lastProgress;
                        Snackbar.make(layout,
                                "Pause Time Reverted To " + (waitTime/1000.0) + " sec",
                                Snackbar.LENGTH_LONG).show();
                    }
                });
                snackBar.setActionTextColor(Color.BLUE);
                View snackBarView = snackBar.getView();
                TextView textView = snackBarView.findViewById(R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snackBar.show();
            }
        });

        setInitialValues();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){
                showDirections();
            }
        }, 2000);
        //showDirections();
    }

    private void setInitialValues() {
        streak = Integer.parseInt(sharedPreferences.getString("streak", "0"));
        guessCnt.setText(getString(R.string.cupcake_cnt)+cupcakeCnt);
        record.setText("Max Streak: "+streak);
    }

    private void showDirections() {
        String[] dirs = getResources().getStringArray(R.array.welcome_messages);
        for(int i=0; i<3; i++){
            final int j=i;
            Handler handler=new Handler();
            handler.postDelayed(new Runnable(){
                public void run(){
                    message.setText(dirs[j+1]);
                }
            }, 2000*i);
        }
        Handler outHandler = new Handler();
        outHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                message.setVisibility(View.INVISIBLE);
                back.setVisibility(View.VISIBLE);
            }
        }, 8000);
        for(int i=1; i<=4; i++){
            final int j=i;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    img.setImageResource(getResources().getIdentifier(stages[j-1], "drawable", "com.wangjessica.jwlab03b"));
                }
            }, 8000+1000*i);
        }
        for(int i=1; i<=2; i++){
            final int j=i;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    back.setVisibility(View.GONE);
                    img.setVisibility(View.GONE);
                    message.setVisibility(View.VISIBLE);
                    message.setText(dirs[j+3]);
                }
            }, 11000+2000*i);
        }
        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable(){
            public void run(){
                message.setText(getResources().getString(R.string.message_full));
                Button startButton = findViewById(R.id.start_button);
                startButton.setVisibility(View.VISIBLE);
            }
        }, 17000);
    }

    public void startGame(View view) {
        message.setVisibility(View.INVISIBLE);
        Button startButton = findViewById(R.id.start_button);
        startButton.setVisibility(View.INVISIBLE);
        cupcakes.clear();
        back.setVisibility(View.VISIBLE);
        cupcakeCnt = 0;
        gameInProg = true;
        newCupcake();
    }

    public void newCupcake(){
        // Choose a new cupcake
        answer = (int)(Math.random()*4);
        while(answer-1==lastAnswer) answer=(int)(Math.random()*4);
        lastAnswer = answer;
        cupcakes.put(cupcakeCnt, false);
        final int cur = cupcakeCnt;
        if(answer==0){
            back.setVisibility(View.VISIBLE);
            img.setVisibility(View.GONE);
        }
        else{
            img.setVisibility(View.VISIBLE);
            img.setImageResource(getResources().getIdentifier(stages[answer-1], "drawable", "com.wangjessica.jwlab03b"));
        }
        // System.out.println(stages[(answer-1+4)%4]);
        // Change the image display
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!cupcakes.get(cur)){
                    gameOver();
                }
            }
        }, waitTime);
    }

    public void gameOver(){
        gameInProg = false;
        // Snackbar
        Snackbar snackbar = Snackbar.make(layout, "You timed out or answered incorrectly", Snackbar.LENGTH_LONG);
        snackbar.show();
        // Reset layout details
        Button startButton = findViewById(R.id.start_button);
        startButton.setText("Replay");
        startButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        if(!gameInProg) return;
        Button clicked = (Button) view;
        cupcakes.replace(cupcakeCnt, true);
        if(clicked==squares[answer]){
            // Change cupcake appearance
            img.setVisibility(View.VISIBLE);
            img.setImageResource(getResources().getIdentifier(stages[answer], "drawable", "com.wangjessica.jwlab03b"));
            // Toast
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            cupcakeCnt++;
            streak = Math.max(streak, cupcakeCnt);
            editor.putString("streak", ""+streak);
            editor.apply();
            // Update statistics
            guessCnt.setText(getString(R.string.cupcake_cnt)+cupcakeCnt);
            record.setText("Max Streak: "+streak);
            // Next cupcake
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    newCupcake();
                }
            }, waitTime/2);
        }
        else{
            gameOver();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setInitialValues();
    }
}