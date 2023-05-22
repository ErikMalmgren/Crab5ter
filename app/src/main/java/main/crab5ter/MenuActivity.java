package main.crab5ter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MenuActivity extends AppCompatActivity {


    private RelativeLayout tutorial;
    private Button playButton;
    private Button howToPlayButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_menu);
        this.tutorial = findViewById(R.id.howToPlayLayout);
        this.playButton = findViewById(R.id.playButton);
        this.howToPlayButton = findViewById(R.id.howToPlayButton);

    }

    public void launchMainActivity(View v) {
        Intent i = new Intent(this, GameMenuActivity.class);
        i.putExtra("testing" , "b");
        startActivity(i);
    }

    public void launchTutorial(View v) {
        this.tutorial.setVisibility(View.VISIBLE);
        this.playButton.setVisibility(View.INVISIBLE);
        this.playButton.setEnabled(false);
        this.howToPlayButton.setVisibility(View.INVISIBLE);
        this.howToPlayButton.setEnabled(false);

    }

    public void hideTutorial(View view) {
        this.tutorial.setVisibility(View.INVISIBLE);
        this.playButton.setVisibility(View.VISIBLE);
        this.playButton.setEnabled(true);
        this.howToPlayButton.setVisibility(View.VISIBLE);
        this.howToPlayButton.setEnabled(true);

    }


}