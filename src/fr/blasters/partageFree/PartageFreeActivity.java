package fr.blasters.partageFree;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PartageFreeActivity extends Activity {
	
	
	EditText email;
	EditText password;
	TextView fichierTxt;
	Uri fichierUri;
	String destination;
	public static final String PREFS_NAME = "PartageFreePrefs";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // apparament ça se met au début
        setContentView(R.layout.activity_main);
        
        fichierTxt = (TextView) findViewById(R.id.textSrcUri);
        TextView destTxt = (TextView) findViewById(R.id.textDestPath);
        Button save = (Button) findViewById(R.id.buttonSave);
        
        loadPreferences();
        
       // Après le load, on regarde si on a lancé l'appli via le menu share
        
       Intent intent = getIntent();
       Bundle extras = intent.getExtras();
       String action = intent.getAction();

       // if this is from the share menu
       if (Intent.ACTION_SEND.equals(action)) {   
    	  if (extras.containsKey(Intent.EXTRA_STREAM)) {
    		  // Get resource path
    		  fichierUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
		      fichierTxt.setText(fichierUri.toString());
		      
		      // pompé chez K9
		      String name = null;
		      ContentResolver contentResolver = getContentResolver();
		      Cursor metadataCursor = contentResolver.query(fichierUri, new String[] { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE }, null, null, null);
		      if (metadataCursor != null) {
			      try {
			         if (metadataCursor.moveToFirst()) {
			            name = metadataCursor.getString(0);
			         }
			      } finally {
			         metadataCursor.close();
			      }
			  }
		      if (name == null) {
		            name = fichierUri.getLastPathSegment();
		      }
		      
		      destination = "/"+ PartageFreeActivity.sanitizeFilename(name);
		      destTxt.setText(destination);
		      
		      save.setText(R.string.upload);
		      save.setOnClickListener(new View.OnClickListener() {
		        	public void onClick(View v) {
		        		savePreferences();
		        		uploadFile();
		        	}
		        });
    	  }
       } else {
    	  save.setText(R.string.save);
    	  save.setOnClickListener(new View.OnClickListener() {
          	public void onClick(View v) {
          		savePreferences();
          		// quand on n'a pas un upload a faire, on quitte apres la sauvegarde
          		finish();
          		//System.exit(0);
          	}
          });
       }
        
    }
    
    
    private void loadPreferences() {
    	
    	email = (EditText) findViewById(R.id.editMail);
        password = (EditText) findViewById(R.id.editPwd);
    	
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	email.setText(settings.getString("email", "mail@example.com"));
		password.setText(settings.getString("password", "12345"));
		
	}


	private void savePreferences() {
    	
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString("email", email.getText().toString());
	    editor.putString("password", password.getText().toString());

	    // Commit the edits!
	    editor.commit();
		
    	Toast.makeText(this, "Modifications enregistrées", Toast.LENGTH_SHORT).show();
    }
    
	/**
	 * Fonction qui copie un InputStream dans un OutputStream
	 * Piqué là :
	 * http://www.java2s.com/Code/Android/File/CopyStream.htm
	 * disabled
	private void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
		    for (;;) {
		    	int count = is.read(bytes, 0, buffer_size);
		    	if (count == -1)
		    		break;
		    	os.write(bytes, 0, count);
		    }
		} catch (Exception ex) {
			printNotif("Erreur","PartageFree","Exception durant la copie");
		}
	}
	 */
	
	/**
	 * Fonction principale qui gère l'upload du fichier
	 */
    private void uploadFile() {

    	loadPreferences();
    	
    	new UploadToFree().execute(this);
    	
    }
    
    @SuppressWarnings("deprecation")
	protected void printNotif(String entete, String titre, String message) {
    	//On crée un "gestionnaire de notification"
    	NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);        
 
    	//On crée la notification
    	//Avec son icône et son texte défilant (optionnel si l'on ne veut pas de texte défilant on met cet argument à null)
    	Notification notification = new Notification(R.drawable.ic_launcher, entete, System.currentTimeMillis());  
 
    	//Intent intent = new Intent(this, NotificationViewer.class);
    	//Intent intent = new Intent(this.getApplicationContext(), null);
    	//PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, null, 0);
    	PendingIntent contentIntent = null;
    	
        //On configure notre notification avec tous les paramètres que l'on vient de créer
        notification.setLatestEventInfo(this, titre, message, contentIntent);
        
        //Enfin on ajoute notre notification et son ID à notre gestionnaire de notification
        notificationManager.notify(5487364, notification);
    }
    
	/**
	 * Retourne un objet String qui contient une version compatible avec dl.free.fr du nom de fichier entré
	 * @param name Le mom de fichier à rendre compatible
	 * @return Le nom de fichier rendu compatible
	 * @author Gilles Maurer
	 * Fonction récupérée dans le programme DLUploadClient de Gilles Maurer
	 */
	public static String sanitizeFilename(String name)
	{
		return name.replaceAll("[^a-zA-Z0-9\\.\\(\\)\\-\\_\\[\\]]", "_");
	}
    
	protected String getEmail()
	{
		return email.getText().toString();
	}
	protected String getPassword()
	{
		return password.getText().toString();
	}
	protected Uri getFchierUri()
	{
		return fichierUri;
	}
	protected String getDestination()
	{
		return destination;
	}
}