package com.loganfynne.clerk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
//import android.text.TextPaint;
import android.widget.TextView;
//import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;

public class ArticleActivity extends Activity {
	private Article a = null;
	DatabaseHelper db = Clerk.getDatabase();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent i = getIntent();
        
        String[] title = {i.getStringExtra("title")};
        
        a = db.readArticle(title);
        
		Log.d("Database", "Title " + a.title); // Title
		Log.d("Database", "Author " + a.author);
		Log.d("Database", "Category" + a.categories);
		Log.d("Database", "Content " + a.content);
		
		Log.d("Database", "Date" + a.published);
		Log.d("Database", "Unread" + a.unread);
        
        setContentView(R.layout.article);
        
        int number = 0;
        
        /*float ascent = 0;
        float bottom = 0;
        float descent = 0;
        float leading = 0;
        float top = 0;
        
        TextPaint ligature = new TextPaint();
        FontMetrics font = ligature.getFontMetrics();*/
 
        TextView textTitle = (TextView) findViewById(R.id.textTitle);
        WebView textContent = (WebView) findViewById(R.id.textContent);
        //textContent.setMovementMethod(new ScrollingMovementMethod());
        
        Log.d("Article", Integer.toString(number));
        //String email = i.getStringExtra("content");
 
        // Displaying Received data
        textTitle.setText(a.title);
        textContent.loadDataWithBaseURL(null, a.content, "text/html", "utf-8", null);
        
 
    }
    
    public static class TextArticle extends TextView {

		private static Typeface tf = null;

		public TextArticle(Context context) {
			super(context);
			
			String fontPath = "fonts" + '/' + "fontname" + ".ttf";
		    tf = Typeface.createFromAsset(context.getAssets(), fontPath);
			this.setTypeface(tf);
		}
    }
}