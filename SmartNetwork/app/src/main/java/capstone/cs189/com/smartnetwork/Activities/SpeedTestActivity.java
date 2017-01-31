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
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Socket;

public class SpeedTestActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ColorArcProgressBar colorArcProgressBar;
    private Button button;
    private TextView text_max, text_min, text_retry, text_drops, text_errors;
    Handler handler = new Handler();
    Runnable runnable;
    WifiManager wifiManager;
    io.socket.engineio.client.Socket socket;
    int i;
    String ipAddress;
    IperfTask iperfTask;

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
       /* wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);

        try {
            socket = new Socket("ws://192.168.0.2/websocket/");
            socket.open();
            Log.d("FFFFFFFFFFFFFFFFF", "Socket opened!");

        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.d("FFFFFFFFFFFFFFFFF", "Socket failed!");

        }
        socket.on(Socket.EVENT_OPEN, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String request = "{\"jsonrpc\":\"2.0\",\"id\":16,\"method\":\"login\",\"params\":[\"admin\",\"admin\"]}";
                //socket.emit("myevent",request);
                Log.d("FFFFFFFFFFFFFFFFF", "Data sent to SR400: " + request);
                //socket.close();
            }
        });


        socket.on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String data = (String)args[0];
                Log.d("FFFFFFFFFFFFFFFFFFFFF", "Response from SR400: " + data);
            }
        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Exception err = (Exception) args[0];
                Log.d("FFFFFFFFFFFFFFFFFFFFF", "Error -- no response");

            }
        });*/

       /* button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button.getText().equals("Test")) {
                    button.setText("Stop");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runnable = this;
                            //Random r = new Random();
                            //int randomActual = r.nextInt(55-45) + 45;
                            //int randomMax = r.nextInt(100-80) + 80;

                            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                            Integer speed = 0;
                            Integer rssi = 0;
                            if (wifiInfo != null) {
                                speed = wifiInfo.getLinkSpeed();
                                rssi = wifiInfo.getRssi();
                            }

//                          colorArcProgressBar.setCurrentValues(randomActual);
                            colorArcProgressBar.setCurrentValues(speed);

                            colorArcProgressBar.setMaxValues(150);
                            //text_max.setText("PHY rate max: " + randomMax + " mbps");
                            text_min.setText("Speed: " + speed + "mbps");
                            text_retry.setText("Rssi: " + rssi + "mbps");
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
        });*/
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
        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        // get the device's ip address for use in iperf command
        if (wifiInfo != null) {
            ipAddress = android.text.format.Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        }
        else {
            Toast.makeText(getApplicationContext(), "Test failed! Verify your device is connected to wifi and try again", Toast.LENGTH_SHORT).show();
            return;
        }
        // copy the iperf executable into device's internal storage
        InputStream inputStream;
        try {
            inputStream = getResources().getAssets().open("iperf3");
        }
        catch (IOException e) {
            Log.d("Init Iperf error!", "Error occurred while accessing system resources, please reboot and try again");
            e.printStackTrace();
            return;
        }
        try {
            //Checks if the file already exists, if not copies it.
            new FileInputStream("/data/data/capstone.cs189.com.smartnetwork/iperf3");
        }
        catch (FileNotFoundException f) {
            try {
                OutputStream out = new FileOutputStream("/data/data/capstone.cs189.com.smartnetwork/iperf3", false);
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                inputStream.close();
                out.close();
                Process process =  Runtime.getRuntime().exec("/system/bin/chmod 744 /data/data/capstone.cs189.com.smartnetwork/iperf3");
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

        @Override
        protected String doInBackground(Void... voids) {
            if (!command.matches("(iperf3 )?((-[s,-server])|(-[c,-client] ([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5]))|(-[c,-client] \\w{1,63})|(-[h,-help]))(( -[f,-format] [bBkKmMgG])|(\\s)|( -[l,-len] \\d{1,5}[KM])|( -[B,-bind] \\w{1,63})|( -[r,-tradeoff])|( -[v,-version])|( -[N,-nodelay])|( -[T,-ttl] \\d{1,8})|( -[U,-single_udp])|( -[d,-dualtest])|( -[w,-window] \\d{1,5}[KM])|( -[n,-num] \\d{1,10}[KM])|( -[p,-port] \\d{1,5})|( -[L,-listenport] \\d{1,5})|( -[t,-time] \\d{1,8})|( -[i,-interval] \\d{1,4})|( -[u,-udp])|( -[b,-bandwidth] \\d{1,20}[bBkKmMgG])|( -[m,-print_mss])|( -[P,-parallel] d{1,2})|( -[M,-mss] d{1,20}))*"))
            {
                Log.d("FFFFFFFFFFFFFFFFF", "Error! Invalid syntax for iperf3 command!");
                publishProgress("Error: invalid syntax \n\n");
                return null;
            }
            try {
                String[] commands = command.split(" ");
                List<String> commandList = new ArrayList<>(Arrays.asList(commands));
               // if (commandList.get(0).equals((String) "src/main/temp/iperf")) {
                //    commandList.remove(0);
               // }

                commandList.add(0, "/data/data/capstone.cs189.com.smartnetwork/iperf3");
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
            }
            return null;
        }

        @Override
        public void onProgressUpdate(String... strings) {
            //tv.append(strings[0]);
            Log.d("FFFFFFFFFFFFFFFFFFFFF", "Iperf output: " + strings[0]);
            //The next command is used to roll the text to the bottom
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
                Toast.makeText(getApplicationContext(), "iperf test has finished", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
