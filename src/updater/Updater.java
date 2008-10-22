package updater;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class Updater {
	String updateurl;
	public static void main(String[] args){
		Updater up = new Updater("http://inst.eecs.berkeley.edu/~ibrahima/bang/bang.jar");
		up.downloadLatestVersion();
	}
	public Updater(String url){
		updateurl = url;
	}
	void downloadLatestVersion(){
		URL url;
		try {
			url = new URL(updateurl);
			HttpURLConnection hConnection = (HttpURLConnection) url
					.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			if (HttpURLConnection.HTTP_OK == hConnection.getResponseCode()) {
				InputStream in = hConnection.getInputStream();
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream("bang.jar"));
				byte[] buffer = new byte[1024];
				int numRead;
				long numWritten = 0;
				while ((numRead = in.read(buffer)) != -1) {
					out.write(buffer, 0, numRead);
					numWritten += numRead;
				}
				System.out.println(numWritten);
				out.close();
				in.close();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
