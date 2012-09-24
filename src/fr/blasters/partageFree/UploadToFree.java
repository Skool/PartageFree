package fr.blasters.partageFree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import android.net.Uri;
import android.os.AsyncTask;

public class UploadToFree extends AsyncTask<PartageFreeActivity, Integer, String> {

	/**
	 * Fonction qui gère l'upload
	 * http://developer.android.com/reference/android/os/AsyncTask.html
	 */
	protected String doInBackground(PartageFreeActivity... tabThreads) {
		
		PartageFreeActivity mainThread = tabThreads[0];
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

            mainThread.printNotif("Envoi","PartageFree","Envoi du fichier");
            // Upload
            InputStream input = mainThread.getContentResolver().openInputStream(fichierUri);
                        
            if (ftp.storeFile(destination, input))
            	mainThread.printNotif("Done","PartageFree","Envoi terminé");
            else
            	mainThread.printNotif("Erreur !","PartageFree","Envoi échoué");
            
            input.close();
          
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
        	// si on a eu une Exception, on ferme na connexion
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
	

}
