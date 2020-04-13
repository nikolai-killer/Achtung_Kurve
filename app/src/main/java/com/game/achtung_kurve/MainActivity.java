package com.game.achtung_kurve;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.hello_world.R;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button newGameOffline;
    private Button newGameOnline;
    public static String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newGameOffline = findViewById(R.id.button_newGame_Offline);
        newGameOnline = findViewById(R.id.button_newGame_Online);
        newGameOffline.setOnClickListener(this);
        newGameOnline.setOnClickListener(this);

        if(MainActivity.name == null){
            MainActivity.name = "me";
            File file = new File(getFilesDir(),"playername");
            String name = "unknown";
            if(file.exists()){
                try {
                    FileInputStream fis = new FileInputStream(file);
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                    name = br.readLine();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MainActivity.name = name;
            }
            else{
                openAskForNameDialog(file);
            }
        }
    }
    @Override
    public void onClick(View view){
        switch(view.getId()){
            case(R.id.button_newGame_Offline):
                this.startnewGameOffline();
                break;
            case(R.id.button_newGame_Online):
                this.startNewGameOnline();
                break;
        }
    }

    private void startnewGameOffline(){
        Intent newGameOffline = new Intent("com.example.hello_world.NEWGAMEOFFLINE");
        startActivity(newGameOffline);
    }
    private void startNewGameOnline(){
        Intent newGameLobby = new Intent("com.example.hello_world.GAMELOBBY");
        startActivity(newGameLobby);
    }


    private void openAskForNameDialog(final File file) {
        AlertDialog ad;
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        final LayoutInflater lf = getLayoutInflater();
        final View view = lf.inflate(R.layout.namedialog,null);
        adb.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            FileOutputStream fos = openFileOutput(file.getName(),MODE_PRIVATE);
                            EditText et = view.findViewById(R.id.nametext);
                            String content = et.getText().toString();
                            fos.write(content.getBytes(StandardCharsets.UTF_8));
                            MainActivity.name = content;
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        ad = adb.create();
        ad.setCanceledOnTouchOutside(false);
        ad.setCancelable(false);
        ad.show();
    }
}
