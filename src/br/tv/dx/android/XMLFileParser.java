package br.tv.dx.android;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class XMLFileParser extends DefaultHandler {

	private SQLiteDatabase m_db;
	private String m_filePath;

	private Stack<XMLElements> m_elements = new Stack<XMLElements>();

	private int m_fileId;
	private CategoryData m_category;
	private ItemData.Attachment m_attachment;
	private ItemData m_item;

	XMLFileParser(int fileId, String filePath, SQLiteDatabase db) {
		m_fileId = fileId;
		m_filePath = filePath;
		m_db = db;
	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		XMLElements elem = XMLElements.value(localName);
		m_elements.push(elem);

		switch (elem) {
		case attachment:
			m_attachment = new ItemData.Attachment();
			m_attachment.type = atts.getValue("type");
			break;
		case item:
			m_item = new ItemData();
			m_item.category = m_category.id;
			m_item.file = m_fileId;
			break;
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		switch (m_elements.lastElement()) {
		case category: {
			DXPlayerDBHelper.setCategoryID(m_db, m_category);
			break;
		}
		case item: {
			DXPlayerDBHelper.setItem(m_db, m_item);
			m_item = null;
			break;
		}
		}
		m_elements.pop();
	}

	@Override
	public void characters(char ch[], int start, int length) {
		String chars = new String(ch, start, length);
		chars = chars.trim();

		switch (m_elements.lastElement()) {

		case background: {
			try {
				m_category.imgBackground = new File(m_filePath + "/" + chars)
						.getCanonicalPath();
			} catch (IOException e) {
				Log.e(DXPlayerActivity.TAG, "Attachment file not found: '"
						+ m_filePath + "/" + chars + "'");
			}
			break;
		}

		case image: {
			try {
				String fileName = new File(m_filePath + "/" + chars)
						.getCanonicalPath();

				XMLElements prev = m_elements.get(m_elements.size() - 2);
				switch (prev) {
				case category:
					m_category.imgButton = fileName;
					break;
				case item:
					m_item.image = fileName;
					break;
				}
			} catch (IOException e) {
				Log.e(DXPlayerActivity.TAG, "Attachment file not found: '"
						+ m_filePath + "/" + chars + "'");
			}
			break;
		}

		case title: {
			XMLElements prev = m_elements.get(m_elements.size() - 2);
			switch (prev) {
			case category:
				m_category = DXPlayerDBHelper.getCategory(m_db, chars);
				break;
			case item:
				m_item.title = chars;
				break;
			}
			break;
		}

		case teacher: {
			m_item.teacher = chars;
			break;
		}

		case subTitle: {
			m_item.subTitle = chars;
			break;
		}

		case tag: {
			m_item.tags.add(chars);
			break;
		}

		case link: {
			m_item.link = chars;
			break;
		}

		case attachment: {
			try {
				m_attachment.file = new File(m_filePath + "/" + chars)
						.getCanonicalPath();
				m_item.attachments.add(m_attachment);
			} catch (IOException e) {
				Log.e(DXPlayerActivity.TAG, "Attachment file not found: '"
						+ m_filePath + "/" + chars + "'");
			}

			m_attachment = null;
			break;
		}

		case video: {
			try {
				m_item.video = new File(m_filePath + "/" + chars)
						.getCanonicalPath();
			} catch (IOException e) {
				Log.e(DXPlayerActivity.TAG, "Video file not found: '"
						+ m_filePath + "/" + chars + "'");
			}
			break;
		}
		}
	}

	private enum XMLElements {
		NULL, dxtv, background, category, title, image, items, item, subTitle, tags, tag, teacher, link, attachment, video;

		XMLElements() {
		}

		public static XMLElements value(String str) {
			str = str.toLowerCase();

			switch (str.charAt(0)) {
			case 'a':
				if (str.equals("attachment")) {
					return XMLElements.attachment;
				}
				break;
			case 'b':
				if (str.equals("background")) {
					return XMLElements.background;
				}
				break;
			case 'c':
				if (str.equals("category")) {
					return XMLElements.category;
				}
				break;
			case 'd':
				if (str.equals("dxtv")) {
					return XMLElements.dxtv;
				}
				break;
			case 'i':
				if (str.equals("image")) {
					return XMLElements.image;
				} else if (str.equals("item")) {
					return XMLElements.item;
				} else if (str.equals("items")) {
					return XMLElements.items;
				}
				break;
			case 'l':
				if (str.equals("link")) {
					return XMLElements.link;
				}
				break;
			case 's':
				if (str.equals("subtitle")) {
					return XMLElements.subTitle;
				}
				break;
			case 't':
				if (str.equals("tag")) {
					return XMLElements.tag;
				} else if (str.equals("tags")) {
					return XMLElements.tags;
				} else if (str.equals("title")) {
					return XMLElements.title;
				} else if (str.equals("teacher")) {
					return XMLElements.teacher;
				}
				break;
			case 'v':
				if (str.equals("video")) {
					return XMLElements.video;
				}
				break;
			}
			return NULL;
		}
	}
}
