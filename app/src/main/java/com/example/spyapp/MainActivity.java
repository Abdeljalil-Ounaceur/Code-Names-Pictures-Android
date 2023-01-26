package com.example.spyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout constraintLayout;
    ImageView spyCardImage;
    EditText etWord, etNumberOfCards;
    Button submitButton;
    TextView tvTimer;
    int player, remainingRedCards, remainingBlueCards, globalSequence;
    String nOfCardsStr;
    String word;
    int timer, min, sec;
    Thread countDownT;
    String id;
    int globalCountDownSequence;
    Socket listenerSocket, speakerSocket;
    ServerSocket listenerSS, speakerSS;
    DataInputStream listenerDin, speakerDin;
    DataOutputStream speakerDout, listenerDout;
    boolean listenerConnected, speakerConnected;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spyCardImage = findViewById(R.id.spyCardImageView);
        etWord = findViewById(R.id.etWord);
        etNumberOfCards = findViewById(R.id.editTextNumberOfCards);
        submitButton = findViewById(R.id.submitButton);
        tvTimer = findViewById(R.id.tvTimer);
        constraintLayout  = findViewById(R.id.constrLayout);
        globalSequence = globalCountDownSequence = 0;


        submitButton.setOnClickListener(view -> checkBeforeSending());
        submitButton.setEnabled(false);
        listenerConnected = speakerConnected = false;
        globalSequence = 0;
        context = getApplicationContext();

        launchSpeakerServer();
        launchListenerServer();
    }

    private void launchSpeakerServer() {
        new Thread(() -> {
            try {
                Log.d("screwed", "speaker : creating speaker Server Socket (4200)");
                speakerSS = new ServerSocket(4200);
                Log.d("screwed", "speaker : speaker Server Socket created successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    Thread.sleep(1000);
                    Log.d("screwed", "speaker : accepting speakerSocket");
                    speakerSocket = speakerSS.accept();
                    Log.d("screwed", "speaker : socket created successfully");
                    speakerDin = new DataInputStream(speakerSocket.getInputStream());
                    speakerDout = new DataOutputStream(speakerSocket.getOutputStream());
                    speakerConnected = true;
                    break;

                } catch (Exception e) {
                    Log.d("screwed", "speaker : " + e);
                }
            }

            int localSequence  = globalSequence;
            String response;
            while(true) {
                try {
                    Thread.sleep(1000);
                    Log.d("screwed","-");
                    if(localSequence != globalSequence) {
                        localSequence++;
                        speakerDout.writeUTF(nOfCardsStr+word);
                        speakerDout.flush();
                        Log.d("screwed","speaker: sent " + nOfCardsStr+word);
                        while(true) {
                            try {
                                response = speakerDin.readUTF();
                                Log.d("screwed","speaker : listener responded by " + response);
                                break;
                            }catch(Exception ee) {
                                Log.d("screwed","speaker ee : " + ee);
                            }
                        }
                    }

                }catch (Exception e) {
                    Log.d("screwed","speaker e : " + e);
                }
            }


        }).start();
    }

    private void launchListenerServer() {
        new Thread(() -> {
            try {
                Log.d("screwed", "listener : creating listener Server Socket (4100)");
                listenerSS = new ServerSocket(4100);
                Log.d("screwed", "listener : listener Server Socket created successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    Thread.sleep(1000);
                    Log.d("screwed", "listener : accepting listenerSocket");
                    listenerSocket = listenerSS.accept();
                    Log.d("screwed", "listener : socket created successfully");
                    listenerDin = new DataInputStream(listenerSocket.getInputStream());
                    listenerDout = new DataOutputStream(listenerSocket.getOutputStream());
                    listenerConnected = true;
                    break;

                } catch (Exception e) {
                    Log.d("screwed", "listener : " + e);
                }
            }

            while(true){
                try{
                    while(true) {
                        try {
                            Thread.sleep(1000);
                            Log.d("screwed",".");
                            id = listenerDin.readUTF();
                            Log.d("screwed","listener : received " + id);
                            break;
                        }catch(Exception ee) {
                            Log.d("screwed","listener ee : " + ee);
                        }
                    }
                    listenerDout.writeUTF("ok");
                    listenerDout.flush();
                    Log.d("screwed","sent reception confirmation");


                    //UI things
                    int num = Integer.parseInt(id.substring(3));
                    int resourceId = context.getResources().getIdentifier("image_part_0" + (num < 10 ? "0" : "") + num, "drawable", context.getPackageName());
                    runOnUiThread(() -> spyCardImage.setImageResource(resourceId));
                    stopCounting();


                    player = id.charAt(0) - '0';
                    remainingRedCards = id.charAt(1) - '0';
                    remainingBlueCards = id.charAt(2) - '0';
                    changeBackground();
                    runOnUiThread(() -> submitButton.setEnabled(true));
                    stopCounting();
                    countDown(context);


                }catch(Exception e){
                    Log.d("screwed","listener e : " + e);
                }
            }

        }).start();
    }

 /*   public void launchServer(){
        new Thread(() -> {
            try {
                newGame = false;
                id = "";
                Context context = getApplicationContext();
                ServerSocket ss = new ServerSocket(4900);
                Socket s = ss.accept();
                //s.setSoTimeout(1000);
                Log.d("screwed","connexion established");
                DataInputStream din = new DataInputStream(s.getInputStream());
                DataOutputStream dout = new DataOutputStream(s.getOutputStream());
                int localSequence = globalSequence;
                while (true) {
                    if (s==null || s.isClosed() || !s.isConnected()) {
                        Log.d("screwed","not connected. trying to reconnect...");
                        s = ss.accept();
                        Log.d("screwed","socket accepted");
                        din = new DataInputStream(s.getInputStream());
                        dout = new DataOutputStream(s.getOutputStream());
                    }
                    if(!newGame) {
                        while (true) {
                            Log.d("screwed", "-");
                            try {
                                Thread.sleep(500);
                                id = din.readUTF();
                                //confirmation
                                dout.writeUTF("o");
                                dout.flush();
                                Log.d("screwed","sent confirmation");
                                break;
                            } catch (Exception e) {
                                Log.d("screwed",e.toString());
                            }
                        }
                    }else
                        newGame = false;



                    Log.d("screwed","received an id : " + id);
                    if(spyCardImage.getDrawable() == null) {
                        int num = Integer.parseInt(id.substring(3));
                        int resourceId = context.getResources().getIdentifier("image_part_0" + (num < 10 ? "0" : "") + num,"drawable", context.getPackageName());
                        runOnUiThread(() -> spyCardImage.setImageResource(resourceId));
                    }
                    player = id.charAt(0) - '0';
                    remainingRedCards = id.charAt(1) - '0';
                    remainingBlueCards = id.charAt(2) - '0';
                    changeBackground();
                    runOnUiThread(() -> submitButton.setEnabled(true));

                    countDown(context);
                    listenForNewGame(s,din,dout);
                    while(true){
                        Thread.sleep(50);

                        if(newGame)
                            break;

                        if(localSequence != globalSequence ){
                            Log.d("screwed","clicked on submit");
                            localSequence++;
                            break;
                        }
                        if(timer <=0) {
                            globalCountDownSequence++;
                            nOfCardsStr = "x";
                            word = "";
                            break;
                        }
                    }
                    if(newGame)
                        continue;

                    dout.writeUTF(nOfCardsStr + word);
                    Log.d("screwed","sent the data");
                    dout.flush();
                    if(timer<=0)
                        s.close();
                }
            }catch(Exception e){
                Log.d("screwed",e.toString());
                e.printStackTrace();
            }
        }).start();
    }

  */

    void checkBeforeSending() {
        nOfCardsStr = etNumberOfCards.getText().toString();
        word = etWord.getText().toString();
        if(nOfCardsStr.equals("") || nOfCardsStr.equals("0")){
            Toast.makeText(this, "Enter the number o cards", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(Integer.parseInt(nOfCardsStr) > (player==1?remainingRedCards:remainingBlueCards)){
            Toast.makeText(this, "The entered number is too big", Toast.LENGTH_SHORT).show();
            return;
        }

        submitButton.setEnabled(false);
        globalSequence++;
        stopCounting();
    }

    void changeBackground(){
        if(player == 1)
            runOnUiThread(() -> constraintLayout.setBackground(getDrawable(R.drawable.gradient_drawable_red)));
        else
            runOnUiThread(() -> constraintLayout.setBackground(getDrawable(R.drawable.gradient_drawable_blue)));
    }

    void countDown(Context context){
        timer  = 180;
        countDownT = new Thread(() -> {
            int localCountdownSequence = globalCountDownSequence;
            while(localCountdownSequence == globalCountDownSequence  && timer>0){
                timer-= 1;
                if(timer<=0) {
                    runOnUiThread(() -> Toast.makeText(context, "your time is up", Toast.LENGTH_SHORT).show());
                    break;
                }
                min = timer/60;
                sec = timer%60;
                runOnUiThread(() -> tvTimer.setText("0"+ min +":"+(sec<10?"0":"")+ sec));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
            if(timer <=0) {
                runOnUiThread(() -> submitButton.setEnabled(false));
                nOfCardsStr = "x";
                word = "";
                globalSequence++;
            }

        });
        countDownT.start();
        Log.d("screwed","started countdown");
    }

/*    void listenForNewGame(Socket s, DataInputStream din, DataOutputStream dout){
        listenForNewGameT = new Thread(() -> {
            while(true){
            Log.d("screwed",".");
                try{
                    Thread.sleep(500);
                    id = din.readUTF();
                    if(id.charAt(0) == 'o'){
                        Log.d("screwed","confirmation acknowledgement");
                        continue;
                    }
                    Log.d("screwed","New Game Requested");
                    newGame = true;
                    stopCounting();
                    break;
                }catch(Exception e){
                    if(s.isClosed())
                        break;
                    Log.d("screwed",e.toString());
                }
            }
        });
        listenForNewGameT.start();
        Log.d("screwed","started listening");
    }

 */

    void stopCounting(){
        globalCountDownSequence++;
        runOnUiThread(() ->tvTimer.setText("Timer Paused"));
    }



}