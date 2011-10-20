package br.tv.dx.android;

import java.util.List;

public class ItemData {
    static public class Attachment {
    	public String type;
    	public String file;
    }
    
    public int file;
    
	public int category;
	public String id;
	public String title;
	public String subTitle;
	public List<String> tags;
	public String link;
	public List<Attachment> attachments;
	public String video;
}
