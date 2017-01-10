package capstone.cs189.com.smartnetwork.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.shinelw.library.ColorArcProgressBar;

import java.util.Random;

import capstone.cs189.com.smartnetwork.R;

public class SpeedTestActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ColorArcProgressBar colorArcProgressBar;
    private Button button;
    private TextView text_max, text_min, text_retry, text_drops, text_errors;
    Handler handler = new Handler();
    Runnable runnable;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        colorArcProgressBar = (ColorArcProgressBar) findViewById(R.id.speed_meter);
        text_max = (TextView) findViewById(R.id.text_max);
        text_min = (TextView) findViewById(R.id.text_min);
        text_drops = (TextView) findViewById(R.id.text_drops);
        text_retry = (TextView) findViewById(R.id.text_retry);
        text_errors = (TextView) findViewById(R.id.text_errors);


        button = (Button) findViewById(R.id.button);
        i = 0;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button.getText().equals("Test")) {
                    button.setText("Stop");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runnable = this;
                            Random r = new Random();
                            int randomActual = r.nextInt(55-45) + 45;
                            int randomMax = r.nextInt(100-80) + 80;

                            colorArcProgressBar.setCurrentValues(randomActual);
                            colorArcProgressBar.setMaxValues(85);
                            text_max.setText("PHY rate max: " + randomMax + " mbps");
                            text_min.setText("PHY rate min: " + 0 + " mbps");
                            text_retry.setText("Number of retrys: " + 1);
                            text_drops.setText("Number of drops: " + 0);
                            text_errors.setText("Errors: " + i);
                            i++;
                            handler.postDelayed(this, 500);
                        }
                    }, 500);
                }
                else {
                    handler.removeCallbacks(runnable);
                    button.setText("Test");
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.speed_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(SpeedTestActivity.this, HomeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_speed_test) {

        } else if (id == R.id.nav_map) {
            Intent intent = new Intent(SpeedTestActivity.this, HeatMapActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
