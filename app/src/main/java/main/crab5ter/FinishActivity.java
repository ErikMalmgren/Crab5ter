package main.crab5ter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FinishActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_finish);
        TextView message = (TextView) findViewById(R.id.msgText);
        message.setText(getIntent().getStringExtra("showMsg"));
        message.setShadowLayer(20f, 10f, 10f, Color.BLACK);
    }

    public void playAgain(View v){
        Intent i = new Intent(this,GameActivity.class);
        i.putExtra("GameMode" , getIntent().getStringExtra("GameMode"));
        startActivity(i);
    }
    public void backToMenu(View v){
        startActivity(new Intent(this,MenuActivity.class));
    }
}