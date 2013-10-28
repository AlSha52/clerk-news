package com.loganfynne.clerk;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

public class parseContent {
	public static String getHtml(String url) {
		String text = "";
		try {
			Document doc = Jsoup.connect(url).get();
			Elements content = doc.select("div[class*=body]");
			for (Element e : content) {
				Log.d(e.tagName() + e.className(), e.text());
				if (e.tagName().equals("div")) {
					text += e.text();
					Log.d("Clerk",e.text());
				} else if (e.tagName().equals("p")) {
					return text;
				}
				
			}
			return text;
		} catch (IOException e) {
			Log.d("Clerk", "Failed to load HTML code", e);
		}
		return null;
	}
}