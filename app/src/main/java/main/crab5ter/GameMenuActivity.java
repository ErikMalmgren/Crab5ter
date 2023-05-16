package main.crab5ter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class GameMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game_menu);
    }

    public void launchMainActivityEasy(View v) {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra("GameMode" , "easy");
        startActivity(i);
    }

    public void launchMainActivityMedium(View v) {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra("GameMode" , "medium");
        startActivity(i);
    }

    public void launchMainActivityHard(View v) {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra("GameMode" , "hard");
        startActivity(i);
    }

    public void launchMainMenu(View v) {
        Intent i = new Intent(this, MenuActivity.class);
        startActivity(i);
    }
}