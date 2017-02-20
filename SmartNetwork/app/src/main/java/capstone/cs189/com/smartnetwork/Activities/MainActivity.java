package capstone.cs189.com.smartnetwork.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import capstone.cs189.com.smartnetwork.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        RelativeLayout card_speed_test = (RelativeLayout)findViewById(R.id.card_speed_test);
        card_speed_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SpeedTestActivity.class);
                startActivity(intent);

            }
        });

        RelativeLayout card_heat_map = (RelativeLayout)findViewById(R.id.card_heat_map);
        card_heat_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HeatMapActivity.class);
                startActivity(intent);

            }
        });


        RelativeLayout card_devices = (RelativeLayout)findViewById(R.id.card_devices);
        card_devices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);

            }
        });


        RelativeLayout card_settings = (RelativeLayout)findViewById(R.id.card_settings);
        card_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent intent = new Intent(MainActivity.this, SpeedTestActivity.class);
               // startActivity(intent);

            }
        });



    }

}
