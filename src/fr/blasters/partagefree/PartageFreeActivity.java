package fr.blasters.partagefree;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import org.apache.commons.net.ftp.FTPClient;

public class PartageFreeActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView tv = new TextView(this);
        tv.setText("Hello World");
        
        setContentView(tv);
    }
}