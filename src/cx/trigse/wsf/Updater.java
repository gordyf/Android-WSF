package cx.trigse.wsf;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class Updater implements Runnable {

	private DocksDbAdapter mDocksDbHelper;
	private SchedDbAdapter mSchedDbHelper;
	private Context mCtx;
	
	Handler errorHandler;
	Handler successHandler;

	public Updater(Context c, Handler errHandler, Handler successHandler) {
		this.mCtx = c;
		this.errorHandler = errHandler;
		this.successHandler = successHandler;
	}
	
	public void run() {
		try {
			Document doc = getUpdateData();
			updateDocks(doc);
			updateRuns(doc);
			
			// notify original thread
			Message msg = successHandler.obtainMessage();
			successHandler.sendMessage(msg);
		} catch (UpdateException e) {
			Message msg = errorHandler.obtainMessage(); 
			Bundle b = new Bundle(); 
			b.putString("text", e.getMessage()); 
			b.putInt("time", Toast.LENGTH_SHORT); 
			msg.setData(b); 

			errorHandler.sendMessage(msg); 
		}
		
	}
	
	class UpdateException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 661247907117103391L;

		public UpdateException(String message) {
			super(message);
		}
	}

	private Document getUpdateData() throws UpdateException {
		mDocksDbHelper = new DocksDbAdapter(this.mCtx);
		mDocksDbHelper.open();
		
		mSchedDbHelper = new SchedDbAdapter(this.mCtx);
		mSchedDbHelper.open();

		URLConnection urlConn = null;
		URL updateXmlUrl = null;
		Document updateDoc = null;
		
		//NotificationManager nm = ( NotificationManager ) mCtx.getSystemService( mCtx.NOTIFICATION_SERVICE );
		
		try {
			updateXmlUrl = new URL("http://trigse.cx/scheduleUpdate.xml");
			urlConn = updateXmlUrl.openConnection();
		} catch (Exception e) {
			throw(new UpdateException("Unable to fetch update data."));
		}

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			updateDoc = db.parse(urlConn.getInputStream());
		} catch (IOException ioe) {
			throw(new UpdateException("Invalid XML format. (" + ioe.getMessage() + ")"));
		} catch (ParserConfigurationException pce) {
			throw(new UpdateException("Could not parse XML. (1) (" + pce.getMessage() + ")"));
		} catch (SAXException se) {
			throw(new UpdateException("Could not parse XML. (2) (" + se.getMessage() + ")"));
		}
		
		return updateDoc;
	}
	
	private void updateDocks(Document updateDoc) {
		mDocksDbHelper.deleteAllDocks();
		NodeList dockList = updateDoc.getElementsByTagName("dock");
		int dockListSize = dockList.getLength();
		Element dock;
		int dockId;
		String dockName;
		
		Message msg = errorHandler.obtainMessage(); 
		Bundle b = new Bundle(); 
		b.putString("text", "Updating " + dockListSize + " docks."); 
		b.putInt("time", Toast.LENGTH_SHORT); 
		msg.setData(b); 
		errorHandler.sendMessage(msg);
		
		for (int i = 0; i < dockListSize; i++) {
			dock = (Element)dockList.item(i);
			
			dockId = Integer.parseInt(dock.getAttribute("id"));
			dockName = dock.getAttribute("name");
			
			mDocksDbHelper.createDock(dockId, dockName);
		}
	}
	
	private void updateRuns(Document updateDoc) {
		mSchedDbHelper.deleteAllRuns();
		NodeList runList = updateDoc.getElementsByTagName("run");
		int runListSize = runList.getLength();
		
		Element run;
		//int runId;
		int startDock;
		int endDock;
		String startTime;
		String notes;
		
		Message msg = errorHandler.obtainMessage(); 
		Bundle b = new Bundle(); 
		b.putString("text", "Updating " + runListSize + " ferry runs."); 
		b.putInt("time", Toast.LENGTH_SHORT); 
		msg.setData(b); 
		errorHandler.sendMessage(msg);
		
		for (int i = 0; i < runListSize; i++) {
			run = (Element)runList.item(i);
			
			//runId = Integer.parseInt(run.getAttribute("id"));
			startDock = Integer.parseInt(run.getAttribute("startdock"));
			endDock = Integer.parseInt(run.getAttribute("enddock"));
			startTime = run.getAttribute("time");
			notes = run.getAttribute("notes");
			
			mSchedDbHelper.createRun(startDock, endDock, startTime, notes);
		}
	}
}
