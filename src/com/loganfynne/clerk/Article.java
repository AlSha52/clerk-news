package com.loganfynne.clerk;

public class Article {
    public final String title;
    public final String author;
    public String content;
    public String entryid;
    
    public int published;
	public int unread;
    
    Article(String title, String author, String content, String entryid, int published, int unread) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.entryid = entryid;
        
        this.published = published;
        this.unread = unread;
    }
	
	@Override
	public String toString() {
	    return title;
	}
}
