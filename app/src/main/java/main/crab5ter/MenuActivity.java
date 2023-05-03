package main.crab5ter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void launchMainActivity(View v) {
        Intent i = new Intent(this, GameActivity.class);
        startActivity(i);
    }

    public void quit() {
        // stop
    }
}