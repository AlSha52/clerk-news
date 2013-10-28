package com.loganfynne.clerk;

public class Article {
    public final String title;
    public final String description;
    public final String link;
    public final String date;
    public final String author;
    public final String image;
    public final String categories;
    public final String favicon;
    
    public int set;
    public int rank;
    public int save;
    
    Article(String title, String description, String link, String date, String author, String image, String categories, String favicon) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.date = date;
        this.author = author;
        this.image = image;
        this.categories = categories;
        this.favicon = favicon;
    }
    
    Article(String title, String description, String link, String date, String author, String image, String categories, String favicon, int set, int rank, int save) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.date = date;
        this.author = author;
        this.image = image;
        this.categories = categories;
        this.favicon = favicon;
        this.set = set;
        this.rank = rank;
        this.save = save;
    }
	
	@Override
	public String toString() {
	    return title;
	}
}
