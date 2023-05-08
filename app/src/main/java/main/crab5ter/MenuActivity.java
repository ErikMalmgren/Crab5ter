package main.crab5ter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);
    }

    public void launchMainActivity(View v) {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra("testing" , "a");
        startActivity(i);
    }

    public void launchMainActivityB(View v) {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra("testing" , "b");
        startActivity(i);
    }
}