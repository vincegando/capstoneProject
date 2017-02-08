package capstone.cs189.com.smartnetwork.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.shinelw.library.ColorArcProgressBar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import capstone.cs189.com.smartnetwork.R;

public class SpeedTestActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ColorArcProgressBar colorArcProgressBar;
    private Button button;
    private TextView text_max, text_min, text_retry, text_drops, text_errors;
    private WifiManager wifiManager;
    private String ipAddress;
    private IperfTask iperfTask;

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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initIperf();
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

    public void initIperf() {
        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        // get the device's ip address for use in iperf command
        if (wifiInfo != null) {
            ipAddress = android.text.format.Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
            Log.d("INIT_IPERF", "This is your IP: " + ipAddress);
            ipAddress = "192.168.0.2";
        }
        else {
            Toast.makeText(getApplicationContext(), "Test failed! Verify your device is connected to wifi and try again", Toast.LENGTH_SHORT).show();
            return;
        }
        // copy the iperf executable into device's internal storage
        InputStream inputStream;
        try {
            inputStream = getResources().getAssets().open("iperf9");
        }
        catch (IOException e) {
            Log.d("Init Iperf error!", "Error occurred while accessing system resources, no iperf3 found in assets");
            e.printStackTrace();
            return;
        }
        try {
            //Checks if the file already exists, if not copies it.
            new FileInputStream("/data/data/capstone.cs189.com.smartnetwork/iperf9");
        }
        catch (FileNotFoundException f) {
            try {
                OutputStream out = new FileOutputStream("/data/data/capstone.cs189.com.smartnetwork/iperf9", false);
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                inputStream.close();
                out.close();
                Process process =  Runtime.getRuntime().exec("/system/bin/chmod 744 /data/data/capstone.cs189.com.smartnetwork/iperf9");
                process.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            iperfTask = new IperfTask();
            iperfTask.execute();
            return;
        }

        iperfTask = new IperfTask();
        iperfTask.execute();
    }


    public class IperfTask extends AsyncTask<Void, String, String> {
        Process p = null;
        String command = "iperf3 -c " + ipAddress;
        int max;

        @Override
        protected void onPreExecute() {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            max = wifiInfo.getLinkSpeed();
            Log.d("ON_PRE_EXECUTE", "link speed: " + max);
            button.setText("STOP");
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (!command.matches("(iperf3 )?((-[s,-server])|(-[c,-client] ([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5]))|(-[c,-client] \\w{1,63})|(-[h,-help]))(( -[f,-format] [bBkKmMgG])|(\\s)|( -[l,-len] \\d{1,5}[KM])|( -[B,-bind] \\w{1,63})|( -[r,-tradeoff])|( -[v,-version])|( -[N,-nodelay])|( -[T,-ttl] \\d{1,8})|( -[U,-single_udp])|( -[d,-dualtest])|( -[w,-window] \\d{1,5}[KM])|( -[n,-num] \\d{1,10}[KM])|( -[p,-port] \\d{1,5})|( -[L,-listenport] \\d{1,5})|( -[t,-time] \\d{1,8})|( -[i,-interval] \\d{1,4})|( -[u,-udp])|( -[b,-bandwidth] \\d{1,20}[bBkKmMgG])|( -[m,-print_mss])|( -[P,-parallel] d{1,2})|( -[M,-mss] d{1,20}))*"))
            {
                Log.d("DO_IN_BACKGROUND", "Error! Invalid syntax for iperf3 command!");
                publishProgress("Error: invalid syntax \n\n");
                return null;
            }
            try {
                String[] commands = command.split(" ");
                List<String> commandList = new ArrayList<>(Arrays.asList(commands));
                commandList.add(0, "/data/data/capstone.cs189.com.smartnetwork/iperf9");
                p = new ProcessBuilder().command(commandList).redirectErrorStream(true).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                int read;
                char[] buffer = new char[4096];
                StringBuffer output = new StringBuffer();
                while((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                    publishProgress(output.toString());
                    output.delete(0, output.length());
                }
                reader.close();
                p.destroy();
            }
            catch (IOException e) {
                e.printStackTrace();
                Log.d("DO_IN_BACKGROUND", "Error! Failed retrieving iperf3 results");
            }
            return null;
        }

        @Override
        public void onProgressUpdate(String... strings) {
            String output = strings[0];
            Log.d("ON_PROGRESS_UPDATE", "Iperf output: " + output);
            String[] s = output.split("\\s+");
            ArrayList<String> outList = new ArrayList<>(Arrays.asList(s));
            for (int i = 0; i < outList.size(); i++) {
               // Log.d("ON_PROGRESS_UPDATE", "list: " + outList.get(i));
                if (outList.get(i).equals("-")) {
                    if (outList.get(i + 1).equals("-")) {
                        Log.d("ON_PROGRESS_UPDATE", "Should be end of iperf, should exit");
                        return;
                    }
                }
            }

            if (outList.contains("sec")) {
                String st = outList.get(outList.size() - 2);
                Log.d("ON_PROGRESS_UPDATE", "string speed value: " + st);
                if (st.equals("iperf")) {
                    return;
                }
                int speed = (int)Double.parseDouble(st);
                Log.d("ON_PROGRESS_UPDATE", "speed: " + speed + " Mbits/sec" + ", max: " + max);
                if (speed > max) {
                    max = speed;
                }
                colorArcProgressBar.setCurrentValues(speed);
                colorArcProgressBar.setMaxValues(max);
            }
        }

        @Override
        public void onPostExecute(String result) {
            //The running process is destroyed and system resources are freed.
            if (p != null) {
                p.destroy();

                try {
                    p.waitFor();
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "test has finished", Toast.LENGTH_SHORT).show();
                button.setText("TEST");
            }
        }
    }
}
