package com.game.achtung_kurve;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public abstract class Game extends AppCompatActivity {
    public void goBackToMainActivity(){
        finish();
    }
    public void startAgain(){
        finish();
        Intent newGame = new Intent(this.getIntent());
        startActivity(newGame);
    }

    public abstract void startGameHandler(GamefieldView gamefieldView);
}
