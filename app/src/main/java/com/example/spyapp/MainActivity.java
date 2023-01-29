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
    boolean listenerConnected, speakerConnected, receiptConfirmation, listeningSuccess;
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
        listenerConnected = speakerConnected = receiptConfirmation = listeningSuccess = true;
        globalSequence = 0;
        context = getApplicationContext();

        launchSpeakerServer();
        launchListenerServer();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(context, "Back button disabled to prevent any surprises ", Toast.LENGTH_SHORT).show();
    }

    private void launchSpeakerServer() {
        new Thread(() -> {
            while(true) {
                ServerSocket ss;
                try {
                    Log.d("screwed", "speaker : creating speaker Server Socket (4200)");
                    ss = new ServerSocket(4200);
                    if(ss != null)
                        speakerSS = ss;
                    Log.d("screwed", "speaker : speaker Server Socket created successfully");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("screwed",e.toString());
                }
                speakerConnected = false;
                while (true) {
                    try {
                        Thread.sleep(400);
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

                while(!listenerConnected) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ignored) {}
                    System.out.println("speaker : waiting for listenerSocket to accept...");
                }

                int localSequence = globalSequence;
                String response;

                while (true) {
                    try {
                        Thread.sleep(400);
                        Log.d("screwed", "-");
                        if (!receiptConfirmation) {
                            globalSequence++;
                        }
                        if (localSequence != globalSequence) {
                            localSequence++;
                            speakerDout.writeUTF(nOfCardsStr + word);
                            speakerDout.flush();
                            Log.d("screwed", "speaker: sent " + nOfCardsStr + word);
                            while (true) {
                                try {
                                    response = speakerDin.readUTF();
                                    Log.d("screwed", "speaker : listener responded by " + response);
                                    runOnUiThread(()-> submitButton.setEnabled(false));
                                    if (!receiptConfirmation) {
                                        runOnUiThread(() -> Toast.makeText(context, "Sent the data", Toast.LENGTH_LONG).show());
                                        receiptConfirmation = true;
                                    }
                                    break;
                                } catch (Exception ee) {
                                    Log.d("screwed", "speaker ee : " + ee);
                                    receiptConfirmation = false;
                                    break;
                                }
                            }
                        }

                    } catch (Exception e) {
                        Log.d("screwed", "speaker e : " + e);
                    }
                    if(!receiptConfirmation  || !listeningSuccess)
                        break;
                }

            }
        }).start();
    }

    private void launchListenerServer() {
        new Thread(() -> {
            while(true) {
                ServerSocket ss;
                try {
                    Log.d("screwed", "listener : creating listener Server Socket (4100)");
                    ss = new ServerSocket(4100);
                    if(ss != null)
                        listenerSS = ss;
                    Log.d("screwed", "listener : listener Server Socket created successfully");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("screwed",e.toString());
                }
                listenerConnected = false;
                while (true) {
                    try {
                        Thread.sleep(400);
                        Log.d("screwed", "listener : accepting listenerSocket");
                        listenerSocket = listenerSS.accept();
                        Log.d("screwed", "listener : socket created successfully");
                        listenerDin = new DataInputStream(listenerSocket.getInputStream());
                        listenerDout = new DataOutputStream(listenerSocket.getOutputStream());
                        listenerConnected = true;
                        listeningSuccess  = true;
                        break;

                    } catch (Exception e) {
                        Log.d("screwed", "listener : " + e);
                    }
                }

                while(!speakerConnected) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ignored) {}
                    System.out.println("listener : waiting for speakerSocket to accept...");
                }

                runOnUiThread(()  -> Toast.makeText(context, "Connexion Established", Toast.LENGTH_SHORT).show());

                while (true) {
                    try {
                        while (true) {
                            try {
                                Thread.sleep(400);
                                Log.d("screwed", ".");
                                id = listenerDin.readUTF();
                                Log.d("screwed", "listener : received " + id);
                                listeningSuccess = true;
                                break;
                            } catch (Exception ee) {
                                Log.d("screwed", "listener ee : " + ee);
                                listeningSuccess = false;
                                break;
                            }
                        }
                        if(!listeningSuccess || !receiptConfirmation)
                            break;
                        listenerDout.writeUTF("ok");
                        listenerDout.flush();
                        Log.d("screwed", "sent reception confirmation");


                        //UI things
                        int num = Integer.parseInt(id.substring(3));
                        int resourceId = context.getResources().getIdentifier("image_part_0" + (num < 10 ? "0" : "") + num, "drawable", context.getPackageName());
                        runOnUiThread(() -> spyCardImage.setImageResource(resourceId));


                        player = id.charAt(0) - '0';
                        remainingRedCards = id.charAt(1) - '0';
                        remainingBlueCards = id.charAt(2) - '0';
                        changeBackground();
                        runOnUiThread(() -> submitButton.setEnabled(true));
                        stopCounting();
                        countDown(context);


                    } catch (Exception e) {
                        Log.d("screwed", "listener e : " + e);
                    }
                }
            }
        }).start();
    }

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

        globalSequence++;
        stopCounting();

        if(!listeningSuccess || !speakerConnected || !listenerConnected){
            Toast.makeText(this, "Make sure the PC and phone are Connected first", Toast.LENGTH_LONG).show();
            return;
        }
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

    void stopCounting(){
        globalCountDownSequence++;
        runOnUiThread(() ->tvTimer.setText("Timer Paused"));
    }



}