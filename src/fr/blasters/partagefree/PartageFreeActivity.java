package fr.blasters.partagefree;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.apache.commons.net.ftp.*;

public class PartageFreeActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // apparament ça se met au début
        setContentView(R.layout.main);
        
        //TextView tv = (TextView) findViewById(R.id.textView1);
        Button bt = (Button) findViewById(R.id.button1);
        //tv.setText("Hello World");
        //bt.setText("Upload");
        
        bt.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		uploadFile();
        	}
        });
        
    }
    
    public void uploadFile() {
    	
    	printNotif("Upload...", "PartageFree", "Début de l'upload");
    	
    	FTPClient ftp = new FTPClient();
    	String email = "xxxxx";
    	String password = "abc123";
    	String fichier = "/sdcard/test.zip";
    	
        try
        {
            int reply;
            ftp.connect("dl.free.fr");

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp.disconnect();
                printNotif("Erreur !","PartageFree","Erreur au connect()");
                //TODO: Afficher un message d'erreur
                
            }
        }
        catch (IOException e)
        {
            if (ftp.isConnected())
            {
                try
                {
                    ftp.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
            printNotif("Erreur !","PartageFree","Exception au connect()");
            e.printStackTrace();
            // System.exit(1);
            //TODO: Afficher un message d'erreur
        }

    	// La, on est connecté
        printNotif("Connecté","PartageFree","Là, on est connecté");
    	
        
        try
        {
            if (!ftp.login(email, password))
            {
                ftp.logout();
                printNotif("Erreur !","PartageFree","Erreur de login");
                return;
            }

            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            
            ftp.enterLocalPassiveMode();

            printNotif("Envoi","PartageFree","Envoi du fichier");
            // Upload
            InputStream input = new FileInputStream(fichier);
            ftp.storeFile("/", input);
            input.close();
          
            ftp.noop(); // check that control connection is working OK

            ftp.logout();
        }
        catch (FTPConnectionClosedException e)
        {
            // error = true;
            //System.err.println("Server closed connection.");
        	printNotif("Erreur !","PartageFree","Server closed connection");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // error = true;
        	printNotif("Erreur !","PartageFree","Exception à l'upload");
            e.printStackTrace();
        }
        finally
        {
            if (ftp.isConnected())
            {
                try
                {
                    ftp.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
        }

    }
    
    private void printNotif(String entete, String titre, String message) {
    	//On crée un "gestionnaire de notification"
    	NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);        
 
    	//On crée la notification
    	//Avec son icône et son texte défilant (optionnel si l'on ne veut pas de texte défilant on met cet argument à null)
    	Notification notification = new Notification(R.drawable.ic_launcher, entete, System.currentTimeMillis());  
 
    	//Intent intent = new Intent(this, NotificationViewer.class);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, null, 0);
    	
        //On configure notre notification avec tous les paramètres que l'on vient de créer
        notification.setLatestEventInfo(this, titre, message, contentIntent);
        
        //Enfin on ajoute notre notification et son ID à notre gestionnaire de notification
        notificationManager.notify(5487354, notification);
    }
}