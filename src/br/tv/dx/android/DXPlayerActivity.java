package br.tv.dx.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
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
import android.util.Pair;
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
        
		AulasDBHelper.resetFiles(db);
		
        for(File f : files) {
        	Pair<Integer, Boolean> fileId = AulasDBHelper.getFileID(db, f.getName());
        	if (fileId.second){
        		readDataFile(f, fileId.first, db);
        	}
        }
        
        AulasDBHelper.clearFiles(db);
        
		startActivity(new Intent(this, CategoryViewActivity.class));
		finish();
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
    	
    	private int m_fileId;
    	private int m_category = 0;
    	private ItemData.Attachment m_attachment;
    	private ItemData m_item; 

    	XMLFileHandler(int fileId, SQLiteDatabase db){
    		m_fileId = fileId;
    		m_db = db;
    	}
    	
    	@Override
    	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
    		XMLElements elem = XMLElements.value(localName);
    		m_elements.push(elem);
    		
    		switch(elem){
    		case attachment:
    			m_attachment = new ItemData.Attachment();
    			m_attachment.type = atts.getValue("type");
    			break;
    		case item:
    			m_item = new ItemData();
    			m_item.category = m_category;
    			m_item.file = m_fileId;
    			break;
    		}
    	}
    	
    	@Override
    	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    		if (m_elements.lastElement() == XMLElements.item){
    			AulasDBHelper.setItem(m_db, m_item);
    			m_item = null;
    		}
    		m_elements.pop();
    	}
    	
    	@Override
    	public void characters(char ch[], int start, int length) {
    		String chars = new String(ch, start, length);
    	    chars = chars.trim();
    	    
    		switch(m_elements.lastElement()){
    		
    		case title:
    			XMLElements prev = m_elements.get(m_elements.size() - 2);
    			switch(prev){
	    		case category:
	    			m_category = AulasDBHelper.getCategoryID(m_db, chars);
	    			break;
	    		case item:
	    			m_item.title = chars;
	    			break;
    			}
    			break;
    		
    		case subTitle:
    			m_item.subTitle = chars;
    			break;
    			
    		case id:
    			m_item.id = chars;
    			break;
    			
    		case tag:
    			m_item.tags.add(chars);
    			break;
    			
    		case link:
    			m_item.link = chars;
    			break;
    			
    		case attachment:
    			m_attachment.file = chars;
    			m_item.attachments.add(m_attachment);
    			m_attachment = null;
    			break;
    			
    		case video:
    			m_item.video = chars;
    			break;
    		}
    	}
    }
    
    protected void readDataFile(File file, int fileId, SQLiteDatabase db) {
    	// sax stuff
    	try {
    		SAXParserFactory spf = SAXParserFactory.newInstance();
    		SAXParser sp = spf.newSAXParser();

    		XMLReader reader = sp.getXMLReader();

    		reader.setContentHandler(new XMLFileHandler(fileId, db));

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
	    	if (m_errorMessage == null)
	    		m_errorMessage = getString(R.string.sd_card_read_error);
	    	
	    	alert.setTitle(getResources().getString(R.string.sd_card_error_title))
     				.setMessage(m_errorMessage);
	    	break;
	    default:
	        return null;
	    }
	    
	    return alert.create();
	}
}