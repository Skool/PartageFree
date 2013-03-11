package fr.blasters.partageFree;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import android.net.Uri;
import android.os.AsyncTask;

public class UploadToFree extends AsyncTask<PartageFreeActivity, Long, String> {

	/**
	 * Fonction qui gère l'upload
	 * http://developer.android.com/reference/android/os/AsyncTask.html
	 */
	PartageFreeActivity mainThread;
	
	protected String doInBackground(PartageFreeActivity... tabThreads) {
		
		mainThread = tabThreads[0];
		mainThread.printNotif("Upload...", "PartageFree", "Début de l'upload");
    	
		String email = mainThread.getEmail();
		String password = mainThread.getPassword();
		Uri fichierUri = mainThread.getFchierUri();
		String destination = mainThread.getDestination();
		
    	FTPClient ftp = new FTPClient();
    	
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
                mainThread.printNotif("Erreur !","PartageFree","Erreur au connect()");
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
            mainThread.printNotif("Erreur !","PartageFree","Exception au connect()");
            e.printStackTrace();
            // System.exit(1);
            //TODO: Afficher un message d'erreur
        }

    	// La, on est connecté
        mainThread.printNotif("Connecté","PartageFree","Là, on est connecté");
    	
        
        try
        {
            if (!ftp.login(email, password))
            {
                ftp.logout();
                mainThread.printNotif("Erreur !","PartageFree","Erreur de login "+email);
                return null;
            }

            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            
            ftp.enterLocalPassiveMode();

            // Upload
            InputStream streamIn = mainThread.getContentResolver().openInputStream(fichierUri);
            OutputStream streamOut = new BufferedOutputStream(ftp.storeFileStream(destination), ftp.getBufferSize());
            
            // pour le pourcentage
            // genere des force close sur les liens content://
            /*
            File fichier = new File(new URI(fichierUri.toString()));
            size = fichier.length();
            */
            mainThread.printNotif("Envoi","PartageFree","Envoi du fichier ");
            
            
            int numRead;
            long totalSize=0;
            byte[] buf = new byte[100000];
            while ( (numRead = streamIn.read(buf) ) >= 0) {
            	streamOut.write(buf, 0, numRead);
            	totalSize += numRead;
            	// mise à jour de la barre de progression
            	publishProgress(Long.valueOf(totalSize));
            }

            streamIn.close();
            streamOut.close();
            
            mainThread.printNotif("Envoi","PartageFree","Envoi terminé");
            
            ftp.noop(); // check that control connection is working OK

            ftp.logout();
        }
        catch (FileNotFoundException e)
        {
        	mainThread.printNotif("Erreur !","PartageFree","Fichier non trouvé");
            e.printStackTrace();
        }
        catch (FTPConnectionClosedException e)
        {
            // error = true;
            //System.err.println("Server closed connection.");
        	mainThread.printNotif("Erreur !","PartageFree","Server closed connection");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // error = true;
        	mainThread.printNotif("Erreur !","PartageFree","Exception à l'upload");
            e.printStackTrace();
        }
        finally
        {
        	// si on a eu une Exception, on ferme la connexion
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
		
		return null;
	}
	
	/**
	 * Fonction qui est appelée par le publishProgress pour la progressBar
	 * @param result
	 */
    protected void onProgressUpdate(Long ... currentSize) {
    	super.onProgressUpdate(currentSize);
    	long size = currentSize[0].longValue() / 1024;
        mainThread.printNotif(size+"ko ","PartageFree", "Avancement : "+size+"ko");
    }

}
