package br.tv.dx.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;

public class DXPlayerActivity extends Activity {
    
	static final public String TAG = "DXPlayer";
	
	static final private int DIALOG_SD_CARD_UNKNOWN_ERROR = 0;
	static final private int DIALOG_SD_CARD_MOUNT_ERROR = 1;
	static final private int DIALOG_SD_CARD_ACCESS_ERROR = 2;
	
	
	private String m_errorMessage;
	
	private class XmlFileNameFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String filename) {
			return filename.endsWith(".xml");
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.main);        
        
        // Check if the SD card is mounted and readable
        String state = Environment.getExternalStorageState();
        
        if (!(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
        	showDialog(DIALOG_SD_CARD_MOUNT_ERROR);
        	return;
        }
        
        File dir;
        
        try {
        	String path;
        	
        	try {
        		File p = getExternalFilesDir(null);
        		path = p.getAbsolutePath();
        	} catch (Exception e) {
        		path = Environment.getExternalStorageDirectory().getAbsolutePath();
        		path += "/Android/data/br.tv.dx.android/files";
        	}
        	
        	path += "/xml/";
        	
        	dir = new File(path);
        	if ( !dir.exists() && !dir.mkdirs() ) {
        		showDialog(DIALOG_SD_CARD_ACCESS_ERROR);
            	return;
        	}
        	
        	Log.d(TAG, path);
        }
        catch (Exception e) {
        	m_errorMessage = e.getLocalizedMessage();
        	showDialog(DIALOG_SD_CARD_UNKNOWN_ERROR);
        	return;
        }
        
        File files[] = dir.listFiles(new XmlFileNameFilter());
        
        AulasDBHelper helper = new AulasDBHelper( this );
		SQLiteDatabase db = helper.getWritableDatabase();
        
        for(File f : files) {
        	readDataFile(f, db);
        }
        
		startActivity(new Intent(this, CategoryViewActivity.class));		
		finish();
    }
    
    private class Attachment {
    	public String type;
    	public String file;
    }
    
    private class XMLData {
    	public String category;
    	public String id;
    	public String title;
    	public String subTitle;
    	public List<String> tags;
    	public String link;
    	public List<Attachment> attachments;
    	public String video;
    }
    
    private enum XMLElements {
    	NULL,
		dxtv,
		category,
		title,
		items,
		item,
		id,
		subTitle,
		tags,
		tag,
		link,
		attachment,
		video;
		
		XMLElements() {
		}		
		
		public static XMLElements value(String str) {
			str = str.toLowerCase();
    		
    		switch(str.charAt(0)) {
    		case 'a':
    			if(str.equals("attachment")) {
    				return XMLElements.attachment;
    			}
    			break;
    		case 'c':
    			if(str.equals("category")) {
    				return XMLElements.category;
    			}
    			break;
    		case 'd':
    			if(str.equals("dxtv")) {
    				return XMLElements.dxtv;
    			}
    			break;
    		case 'i':
    			if(str.equals("id")) {
    				return XMLElements.title;
    			} else if(str.equals("item")) {
    				return XMLElements.item;
    			} else if(str.equals("items")) {
    				return XMLElements.items;
    			}
    			break;
    		case 'l':
    			if(str.equals("link")) {
    				return XMLElements.link;
    			}
    			break;
    		case 's':
    			if(str.equals("subTitle")) {
    				return XMLElements.subTitle;
    			}
    			break;
    		case 't':
    			if(str.equals("tag")) {
    				return XMLElements.tag;
    			} else if(str.equals("tags")) {
    				return XMLElements.tags;
    			} else if(str.equals("title")) {
    				return XMLElements.title;
    			}
    			break;
    		case 'v':
    			if(str.equals("video")) {
    				return XMLElements.video;
    			}
    			break;
    		}
    		return NULL;
		}
	}
    
    private class XMLFileHandler extends DefaultHandler {
    	
    	private SQLiteDatabase m_db;
    	private Stack<XMLElements> m_elements = new Stack<XMLElements>();
    	private XMLData m_data; 

    	XMLFileHandler(SQLiteDatabase db){
    		m_db = db;
    	}
    	
    	@Override
    	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
    		m_elements.push(XMLElements.value(localName));
    	}
    	
    	@Override
    	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    		m_elements.pop();
    	}
    	
    	@Override
    	public void characters(char ch[], int start, int length) {
    		switch(m_elements.lastElement()){
    		
    		}
    	}
    }
    
    protected void readDataFile(File file, SQLiteDatabase db) {
    	// sax stuff
    	try {
    		SAXParserFactory spf = SAXParserFactory.newInstance();
    		SAXParser sp = spf.newSAXParser();

    		XMLReader reader = sp.getXMLReader();

    		reader.setContentHandler(new XMLFileHandler(db));

    		reader.parse(new InputSource(new FileInputStream(file)));

    	} catch(ParserConfigurationException pce) {
    		Log.e(TAG, "sax parse error", pce);
    	} catch(SAXException se) {
    		Log.e(TAG, "sax error", se);
    	} catch(IOException ioe) {
    		Log.e(TAG, "sax parse io error", ioe);
    	}
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	alert.setCancelable(true)
		.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	                DXPlayerActivity.this.finish();
	           }
	       });
    	
	    switch(id) {
	    case DIALOG_SD_CARD_MOUNT_ERROR:
	    case DIALOG_SD_CARD_ACCESS_ERROR:
	        alert.setTitle(getResources().getString(R.string.sd_card_error_title))
	        		.setMessage(R.string.sd_card_mount_error);
	        break;
	    case DIALOG_SD_CARD_UNKNOWN_ERROR:
	    	 alert.setTitle(getResources().getString(R.string.sd_card_error_title))
     				.setMessage( m_errorMessage );
	    default:
	        return null;
	    }
	    
	    return alert.create();
	}
}