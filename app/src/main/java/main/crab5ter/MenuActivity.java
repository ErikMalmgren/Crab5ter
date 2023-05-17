package main.crab5ter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class MenuActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button closeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);
        this.imageView = findViewById(R.id.howtoplay);
        this.closeButton = findViewById(R.id.close_button);
    }

    public void launchMainActivity(View v) {
        Intent i = new Intent(this, GameMenuActivity.class);
        i.putExtra("testing" , "b");
        startActivity(i);
    }

    public void launchTutorial(View v) {
        this.imageView.setVisibility(View.VISIBLE);
        this.closeButton.setVisibility(View.VISIBLE);
    }

    public void hideTutorial(View view) {
        this.imageView.setVisibility(View.INVISIBLE);
        this.closeButton.setVisibility(View.INVISIBLE);
    }


}