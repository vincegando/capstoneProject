package capstone.cs189.com.iperfexectest;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button button;
    IperfTask iperfTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.ip);
        button = (Button) findViewById(R.id.button);

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            if (wifiManager.getConnectionInfo() != null) {
                textView.append("Your Ip address is: " + android.text.format.Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress()));
            }
            else {
                textView.append("Error");
            }
        }
        else {
            textView.append("error");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText)findViewById(R.id.inputCommands);
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                initIperf();
            }
        });
    }


    public void initIperf() {
        InputStream inputStream;
        try {
            inputStream = getResources().getAssets().open("iperf");
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            new FileInputStream("/data/data/capstone.cs189.com.iperfexectest/iperf");
        }
        catch (FileNotFoundException f) {
            try {
                OutputStream out = new FileOutputStream("/data/data/capstone.cs189.com.iperfexectest/iperf", false);
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                inputStream.close();
                out.close();
                Process process =  Runtime.getRuntime().exec("/system/bin/chmod 744 /data/data/capstone.cs189.com.iperfexectest/iperf");
                process.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            iperfTask = new IperfTask();
            iperfTask.execute();
            return;
        }
        iperfTask = new IperfTask();
        iperfTask.execute();
        return;

    }

    public class IperfTask extends AsyncTask<Void, String, String> {
        Process p = null;
        final ScrollView scroller = (ScrollView)findViewById(R.id.scroller);
        final TextView tv = (TextView)findViewById(R.id.outputText);
        final EditText inputCommands = (EditText) findViewById(R.id.inputCommands);

        @Override
        protected String doInBackground(Void... voids) {
            String str = inputCommands.getText().toString();
            if (!str.matches("(iperf )?((-[s,-server])|(-[c,-client] ([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5]))|(-[c,-client] \\w{1,63})|(-[h,-help]))(( -[f,-format] [bBkKmMgG])|(\\s)|( -[l,-len] \\d{1,5}[KM])|( -[B,-bind] \\w{1,63})|( -[r,-tradeoff])|( -[v,-version])|( -[N,-nodelay])|( -[T,-ttl] \\d{1,8})|( -[U,-single_udp])|( -[d,-dualtest])|( -[w,-window] \\d{1,5}[KM])|( -[n,-num] \\d{1,10}[KM])|( -[p,-port] \\d{1,5})|( -[L,-listenport] \\d{1,5})|( -[t,-time] \\d{1,8})|( -[i,-interval] \\d{1,4})|( -[u,-udp])|( -[b,-bandwidth] \\d{1,20}[bBkKmMgG])|( -[m,-print_mss])|( -[P,-parallel] d{1,2})|( -[M,-mss] d{1,20}))*"))
            {
                publishProgress("Error: invalid syntax \n\n");
                return  null;
            }
            try {
                String[] commands = inputCommands.getText().toString().split(" ");
                List<String> commandList = new ArrayList<>(Arrays.asList(commands));
                if (commandList.get(0).equals((String) "src/main/temp/iperf")) {
                    commandList.remove(0);
                }

                commandList.add(0, "/data/data/capstone.cs189.com.iperfexectest/iperf");
                p = new ProcessBuilder().command(commandList).redirectErrorStream(true).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                int read;
                char[] buffer = new char[4096];
                StringBuffer output = new StringBuffer();
                while((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                    publishProgress(output.toString());
                    output.delete(0, output.length());
                    tv.setText("ERROR WITH IPERF TEST"+"while loop issue");
                }
                reader.close();
                p.destroy();
            }
            catch (IOException e) {
                tv.setText("ERROR WITH IPERF TEST"+ "IO execption thrown");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onProgressUpdate(String... strings) {
            tv.append(strings[0]);
            //The next command is used to roll the text to the bottom
            scroller.post(new Runnable() {
                public void run() {
                    scroller.smoothScrollTo(0, tv.getBottom());
                }
            });
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
                tv.append("\nTest is done.\n\n");
            }
            //The next command is used to roll the text to the bottom
            scroller.post(new Runnable() {
                public void run() {
                    scroller.smoothScrollTo(0, tv.getBottom());
                }
            });
        }


    }

}
