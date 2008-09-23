package ucbang.network;
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException; 
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
public class ServerBrowser {
	public static void main (String args[]){
		new ServerBrowser().downloadList();
	}
	public void downloadList(){
		URL url;
		try {
			url = new URL("http://cardgameservers.appspot.com/xml");
			HttpURLConnection hConnection = (HttpURLConnection) url
					.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			if (HttpURLConnection.HTTP_OK == hConnection.getResponseCode()) {
		        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		        dbf.setIgnoringElementContentWhitespace(true);
	        	DocumentBuilder db = dbf.newDocumentBuilder();
		        Document doc = db.parse(hConnection.getInputStream());
				/*BufferedReader is = new BufferedReader(new InputStreamReader(hConnection.getInputStream()));
				while(is.ready())
				System.out.println(is.readLine());*/
				hConnection.disconnect();
			}else{
				System.out.println("Ugh, something bad happened");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    private static class MyErrorHandler implements ErrorHandler {
        
        private PrintWriter out;

        MyErrorHandler(PrintWriter out) {
            this.out = out;
        }

        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                " Line=" + spe.getLineNumber() +
                ": " + spe.getMessage();
            return info;
        }

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }
        
        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}