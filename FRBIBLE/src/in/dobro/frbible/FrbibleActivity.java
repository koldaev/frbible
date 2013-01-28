package in.dobro.frbible;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class FrbibleActivity extends Activity implements OnItemSelectedListener, OnClickListener  {
	
	public static File INDEX_DIR;
	
	private int mSpinnerCount=2;

	private int mSpinnerInitializedCount=0;
	
	EditText searchtext;
	
	SharedPreferences spref;
	SharedPreferences loadspref;
	
	ArrayAdapter<String> adapter;
	ArrayAdapter<String> adapterglav;
	SQLiteDatabase db;
	DBHelper myDbHelper;
	String fortextview = "";
	static TextView tv;
	Cursor cursor;
	
	 Integer jglav;

	 Handler h;
	 
	 Integer glavforhundler;
	 Integer currentbookforhundler;
	 ProgressDialog pd;
	 
	Spinner spinner;
	Spinner spinner2;
	
	Integer intbookfromsearch;
	Integer intglavfromsearch;
	Integer intpoemfromsearch;
	
	ImageButton backbutton;
	ImageButton forwardbutton;
	Button searchbutton;
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frbible);
		
		copyindexes();
		
		mSpinnerInitializedCount = 2;
		
		pd = new ProgressDialog(this);
		h = new Handler() {
		      public void handleMessage(android.os.Message msg) {
		    	 if (msg.what == glavforhundler) {
		    		 Toast.makeText(getApplicationContext(), getString(R.string.savingbook), Toast.LENGTH_SHORT).show();
		    		 pd.dismiss();
		    	 }
		    };
		};
		
		
		setTitle(getString(R.string.biblename));
		
		tv = (TextView)findViewById(R.id.textView1);
		tv.setText("");
		tv.setPadding(10, 10, 10, 10);
		
		loadspref = getPreferences(MODE_PRIVATE);
	    Float textsize = loadspref.getFloat("fontsize", 0);
	    
	    if(textsize > 0) {
	    	tv.setTextSize(textsize);
	    	Toast.makeText(this, getString(R.string.loadfontsize) + ": " + textsize, Toast.LENGTH_SHORT).show();
	    } else {
	    	tv.setTextSize(18);
	    	Toast.makeText(this, getString(R.string.defaultfontsize) + ": 18.0", Toast.LENGTH_SHORT).show();
	    }
		
		try {
			init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//touchinit();
		
	}

	private void copybiblexml() throws IOException {
		File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/frbible_xml");
		if(!dir.exists()) {
			
			dir.mkdirs();
			AssetManager assetManager = getAssets();
			InputStream in =  assetManager.open("frbible.xml");
			OutputStream out = new FileOutputStream(dir + "/frbible.xml");
	          copyFile(in, out);
	          in.close();
	          in = null;
	          out.flush();
	          out.close();
	          out = null;
	          
		}
		
		Toast.makeText(this, getString(R.string.savingxml) + " " + dir + "/frbible.xml", Toast.LENGTH_SHORT).show();
		
	}
	
	private void copyindexes() {
		File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/frbible_search");
	    
		//создаем директорию и копируем поисковые индексы при первом запуске приложения
		if(!dir.exists()) {
		
		dir.mkdirs();
	    
		 AssetManager assetManager = getAssets();
		    String[] files = null;
		    try {
		        files = assetManager.list("");
		    } catch (IOException e) {
		        //Log.e("tag", "Failed to get asset file list.", e);
		    }
		    
		    for(String filename : files) {
		    	if((!filename.contentEquals("frbible4.jpeg")) && (!filename.contentEquals("frbible.xml"))) {
		        InputStream in = null;
		        OutputStream out = null;
		        try {
		          in = assetManager.open(filename);
		          out = new FileOutputStream(dir + "/" + filename);
		          copyFile(in, out);
		          in.close();
		          in = null;
		          out.flush();
		          out.close();
		          out = null;
		        } catch(IOException e) {
		            //Log.e("tag", "Failed to copy asset file: " + filename, e);
		        }
		    }
		    }
		
		}
	}



	private void touchinit() {
		
		final ScrollView sv = (ScrollView)findViewById(R.id.vscroll);
		
		sv.setLongClickable(true);
		
		registerForContextMenu(sv);
		
		sv.setOnTouchListener(new OnFlingGestureListener() {
				
	        @Override
	        public void onTopToBottom() {
	        	int intpos = sv.getScrollY() - 150;
	        	sv.scrollTo(0, intpos);
	        }

	        @Override
	        public void onRightToLeft() {
	        	sv.fullScroll(View.FOCUS_UP);
	        	Integer book = spinner.getSelectedItemPosition()+1;
	        	Integer glavaminus = spinner2.getSelectedItemPosition();
	        	if(glavaminus > 0) {
	        	bibletextglav(book,glavaminus);
	        	spinner2.setSelection(glavaminus-1);
	        	}
	        }

	        @Override
	        public void onLeftToRight() {
	        	sv.fullScroll(View.FOCUS_UP);
	        	Integer book = spinner.getSelectedItemPosition()+1;
	        	Integer chapters = (Integer)frbibleproperties.frbiblechapters.get("frbible"+book);
	        	Integer glavaplus = spinner2.getSelectedItemPosition()+2;
	        	if((glavaplus-1) < chapters) {
	        	bibletextglav(book,glavaplus);
	        	spinner2.setSelection(glavaplus-1);
	        	}
	        }

	        @Override
	        public void onBottomToTop() {
	        	int intpos = sv.getScrollY() + 150;
	        	sv.scrollTo(0, intpos);
	        }
	     });
		
		
	}


	private void init() throws IOException {		
		

		Intent mainintent = getIntent(); 
		intbookfromsearch = mainintent.getIntExtra("bookint",0);
		intglavfromsearch = mainintent.getIntExtra("glavint",0);
		intpoemfromsearch = mainintent.getIntExtra("poemint",0);

		spinner = (Spinner) findViewById(R.id.spinner1);
		backbutton = (ImageButton) findViewById(R.id.buttonback);
		backbutton.setOnClickListener(this);
		spinner2 = (Spinner) findViewById(R.id.spinner2);
		forwardbutton = (ImageButton) findViewById(R.id.buttonforward);
		forwardbutton.setOnClickListener(this);
		searchtext = (EditText) findViewById(R.id.editText1); 
		searchbutton = (Button) findViewById(R.id.button1);
		searchbutton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
		searchbutton.setOnClickListener(this);
		
		myDbHelper = new DBHelper(getApplicationContext(), "frbible4.jpeg", 23);
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, frbibleproperties.frbiblenames);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 
	     spinner.setAdapter(adapter);
	     spinner.setPrompt(getString(R.string.newtestament));
	     
	     spinner.setOnItemSelectedListener(this);
	     
	     //tv.setText(intbookfromsearch+"");
	     
	     	if(intbookfromsearch > 0) {
	     		
	     	mSpinnerInitializedCount = 1;
	     		
	     	bibletext(intbookfromsearch, intglavfromsearch);

		    mSpinnerInitializedCount = 1;
		    spinner.setSelection(intbookfromsearch-1);
		    
		    mSpinnerInitializedCount = 1;
		    spinner2.setSelection(intglavfromsearch-1);
	     	
	     	}
	     
	}

	public void bibletext(int i, int ch) {
		
		Integer chapters = (Integer)frbibleproperties.frbiblechapters.get("frbible"+i);
 		combochapters(i,chapters);
		
		fortextview = "";

		tv.setText("");
		
		try {
		    db = myDbHelper.getWritableDatabase();
		}
		catch (SQLiteException ex){
		    db = myDbHelper.getReadableDatabase();
		}
		finally{

		cursor = db.rawQuery("select * from frtext where bible = " + i + " and chapter = " + ch , null);

 		while(cursor.moveToNext()){
 			fortextview += cursor.getString(cursor.getColumnIndex("poem")) + ".&nbsp;" + cursor.getString(cursor.getColumnIndex("poemtext"))  +  "<br>" ;
 		}
 		
 		if (cursor != null)
	        cursor.moveToFirst();
 		
 		tv.setText(Html.fromHtml(fortextview));
 		
		}
		
		if(i < 40) {
			setTitle(getString(R.string.oldtestament));
		} else {
			setTitle(getString(R.string.newtestament));
		}
		
		final ScrollView sv1 = (ScrollView)findViewById(R.id.vscroll);
		
		//sv.scrollTo(0, y);
		sv1.post(new Runnable() {
		    @Override
		    public void run() {
		    	int y1 = tv.getLayout().getLineTop(intpoemfromsearch*2);
		        sv1.scrollTo(0, (y1-30));
		    }
		});
		
		myDbHelper.close();
		cursor.close();

	}


	private void combochapters(final Integer book, Integer chapters) {
		
		String frbibleglaves[] = new String[chapters];
		String glavname;
		
		if(chapters != 150) {
			glavname = getString(R.string.chapter) + " ";
		} else {
			glavname = getString(R.string.psalm) + " ";
		}
		
		frbibleglaves[0] = glavname + "1";
		for(int a=1;a<chapters;a++) {
			frbibleglaves[a] = glavname + (a+1);
		}
		
		adapterglav = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, frbibleglaves);
		adapterglav.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 
	     spinner2.setAdapter(adapterglav);
	     spinner2.setPrompt(getString(R.string.chapterchang));
	     spinner2.setOnItemSelectedListener(this);
	     
	     
	}
	
	public void bibletextglav(int i, int ch) {
				
		fortextview = "";

		tv.setText("");
		
		try {
		    db = myDbHelper.getWritableDatabase();
		}
		catch (SQLiteException ex){
		    db = myDbHelper.getReadableDatabase();
		}
		finally{

			String textus = "";
			
		cursor = db.rawQuery("select * from frtext where bible = " + i + " and chapter = " + ch , null);

 		while(cursor.moveToNext()){
 			if(intpoemfromsearch == cursor.getInt(cursor.getColumnIndex("poem"))) {
 				textus = "<b>" + cursor.getString(cursor.getColumnIndex("poemtext")) + "</b>";
 				intpoemfromsearch = 0;
 			} else {
 				textus = cursor.getString(cursor.getColumnIndex("poemtext"));
 			}
 			fortextview +=  cursor.getString(cursor.getColumnIndex("poem")) + ".&nbsp;" + textus + "<br>" ;
 		}
 		
 		tv.setText(Html.fromHtml(fortextview));
 		
 		if (cursor != null)
	        cursor.moveToFirst();
 		
		}
		
		if(i < 40) {
			setTitle(getString(R.string.oldtestament));
		} else {
			setTitle(getString(R.string.newtestament));
		}
		
		cursor.close();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_frbible, menu);
		
		menu.clear();
		
		menu.add(0, 1, 1, getString(R.string.savebookmark));
		menu.add(0, 2, 2, getString(R.string.loadbookmark));
		menu.add(1, 3, 3, getString(R.string.savechapter));
		menu.add(1, 4, 4, getString(R.string.savebook));
		menu.add(1, 5, 5, getString(R.string.savebase));
		menu.add(1, 6, 6, getString(R.string.smallfont));
		menu.add(1, 7, 7, getString(R.string.bigfont));
		menu.add(1, 8, 8, getString(R.string.tosavexml));
		
		return super.onCreateOptionsMenu(menu);
	}
	
	 public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {
		 
		 	case 1:
		 		
		 		addbookmark();
		 	
		 	break;
		 	
		 	case 2:

				loadbookmark();
			 	
			break;
			
		 	case 3:
		 		Savetext();
			break;
				
		 	case 4:
		 		currentbookforhundler = spinner.getSelectedItemPosition()+1;
				glavforhundler = (Integer)frbibleproperties.frbiblechapters.get("frbible"+currentbookforhundler);

				String bookname = frbibleproperties.frbiblenames[currentbookforhundler-1];
		    	  
		 	      pd.setTitle(bookname);
		 	      pd.setIndeterminate(true);
		 	      pd.setInverseBackgroundForced(true);
		 	      pd.setCancelable(false);
		 	      pd.setCanceledOnTouchOutside(false);
		 	      pd.setMessage(getString(R.string.exportprocess));
		 	      pd.show();

		 		Thread t = new Thread(new Runnable() {
		 	        public void run() {
		 		booktotext();
		 	       }
		 	      });
		 	      t.start();
			break;
		 	
		 	case 5:
		 		savebase();
			break;
			
		 	case 6:
		 		fontminus();
			break;
				
		 	case 7:
			 	fontplus();
			break;
		 
		 	case 8:
			try {
				copybiblexml();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 	break;
			
		 }
		 
	      return super.onOptionsItemSelected(item);
	 }
	
	private void booktotext() {
		
		
		
		try {
		    db = myDbHelper.getWritableDatabase();
		}
		catch (SQLiteException ex){
		    db = myDbHelper.getReadableDatabase();
		}
		finally{
		
			Integer currentbook = spinner.getSelectedItemPosition()+1;
			Integer chapters = (Integer)frbibleproperties.frbiblechapters.get("frbible"+currentbook);
			
			String bookname = frbibleproperties.frbiblenames[currentbook-1];
			
		fortextview = "";
		
		fortextview += "\r\n";
		fortextview += bookname;
		fortextview += "\r\n";
		
		for(int i=1;i<=chapters;i++) {
			cursor = db.rawQuery("select * from frtext where bible = " + currentbook + " and chapter = " + i, null);
			
			if(chapters != 150)
				fortextview += "\r\n" + getString(R.string.chapter) + " " + i + "\r\n\r\n";
			else
				fortextview += "\r\n" + getString(R.string.psalm) + " " + i + "\r\n\r\n";

	 		while(cursor.moveToNext()){
	 			fortextview += cursor.getString(cursor.getColumnIndex("poem")) + ". " + cursor.getString(cursor.getColumnIndex("poemtext")) + "\r\n";
	 		}

	 		
	 		if (cursor != null)
		        cursor.moveToFirst();
	 		
	 		h.sendEmptyMessage(i);
	 		
		}
		
		File root = android.os.Environment.getExternalStorageDirectory();
		
		File dir = new File(root.getAbsolutePath() + "/frbible");
	    dir.mkdirs();
	    String filenametosave = "book" + (spinner.getSelectedItemPosition()+1) + ".txt";
	    File file = new File(dir, filenametosave);
		
	    try {
	        FileOutputStream f = new FileOutputStream(file);
	        PrintWriter pw = new PrintWriter(f);
	        pw.println(fortextview);
	        pw.flush();
	        pw.close();
	        f.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
		cursor.close();
		myDbHelper.close();

		}
		
		final ScrollView sv = (ScrollView)findViewById(R.id.vscroll);
		//sv.scrollTo(0, 0);
		
		myDbHelper.close();
		cursor.close();

	}



	private void fontminus() {
			Float currentsize = tv.getTextSize();
			Float sizeminus = currentsize - 2;
			tv.setTextSize(sizeminus);
			spref = getPreferences(MODE_PRIVATE);
		    Editor ed = spref.edit();
		    ed.putFloat("fontsize", sizeminus);
		    ed.commit();
		    Toast.makeText(this, getString(R.string.newfontsize) + ": " + sizeminus, Toast.LENGTH_SHORT).show();
	}



	private void fontplus() {
			Float currentsize = tv.getTextSize();
			Float sizeplus = currentsize + 2;
			tv.setTextSize(sizeplus);
			spref = getPreferences(MODE_PRIVATE);
			Editor ed = spref.edit();
			ed.putFloat("fontsize", sizeplus);
			ed.commit();
			Toast.makeText(this, getString(R.string.newfontsize) + ": " + sizeplus, Toast.LENGTH_SHORT).show();
	}



	@SuppressLint("NewApi")
	private void loadbookmark() {
		loadspref = getPreferences(MODE_PRIVATE);
	    Integer book = loadspref.getInt("book", 1);
	    Integer glav = loadspref.getInt("glav", 1);
	    
	    bibletext(book, glav);

	    mSpinnerInitializedCount = 1;
	    spinner.setSelection(book-1);
	    
	    mSpinnerInitializedCount = 1;
	    spinner2.setSelection(glav-1);
	    
	}


	private void addbookmark() {
		Integer book = spinner.getSelectedItemPosition()+1;
    	Integer glavaplus = spinner2.getSelectedItemPosition()+1;
		
		spref = getPreferences(MODE_PRIVATE);
	    Editor ed = spref.edit();
	    ed.putInt("book", book);
	    ed.commit();
	    ed.putInt("glav", glavaplus);
	    ed.commit();
		Toast.makeText(this, getString(R.string.savingbookmark), Toast.LENGTH_SHORT).show();
	}

	private void savebase() {
		
		File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/frbible/");
	    dir.mkdirs();
	    
	    String forbasepath = dir + "/frbible.db";
	    
	    copyAssets(forbasepath);

	}


	
	private void copyAssets(String forbasepath) {
	    AssetManager assetManager = getAssets();

	        InputStream in = null;
	        OutputStream out = null;
	        try {
	          in = assetManager.open("frbible4.jpeg");
	          out = new FileOutputStream(forbasepath);
	          copyFile(in, out);
	          in.close();
	          in = null;
	          out.flush();
	          out.close();
	          out = null;
	          
	        String textfortoast = getString(R.string.savingbase) + " " + forbasepath;
	  		Toast.makeText(this, textfortoast, Toast.LENGTH_SHORT).show();
	          
	        } catch(IOException e) {
	            Log.e("tag", "Failed to copy asset file: frbible4.jpeg to " + forbasepath, e);
	        }       

	}
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}

	@Override
	public void onBackPressed() {

	    //Toast.makeText(this, "Да любите друг друга!", Toast.LENGTH_SHORT).show();
	}


	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		

		if(arg0 == spinner) {
			Integer i = arg0.getSelectedItemPosition()+1;
			if (mSpinnerInitializedCount < mSpinnerCount) {
					
			} else {
				bibletext(i,1);
			}
   	 		
		} else if(arg0 == spinner2) {
			Integer book = spinner.getSelectedItemPosition()+1;
			jglav = arg0.getSelectedItemPosition()+1;
			if (mSpinnerInitializedCount < mSpinnerCount) {
				
			} else {
				bibletextglav(book,jglav);
			}
		}
		
		mSpinnerInitializedCount = 3;

	}



	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}
	
	void Savetext() {
		
		String input = tv.getText().toString(); 

		File root = android.os.Environment.getExternalStorageDirectory();
		
		File dir = new File(root.getAbsolutePath() + "/frbible");
	    dir.mkdirs();
	    String filenametosave = "book" + (spinner.getSelectedItemPosition()+1) 
	    		+ "_chapter" + (spinner2.getSelectedItemPosition()+1) + ".txt";
	    File file = new File(dir, filenametosave);
		
	    try {
	        FileOutputStream f = new FileOutputStream(file);
	        PrintWriter pw = new PrintWriter(f);
	        pw.println(input);
	        pw.flush();
	        pw.close();
	        f.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
		String textfortoast = getString(R.string.savingtext) + " " + dir + "/"+filenametosave;
		Toast.makeText(this, textfortoast, Toast.LENGTH_SHORT).show();
				    
	}



	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		
		case R.id.buttonback:
			Integer book = spinner.getSelectedItemPosition()+1;
	    	Integer glavaminus = spinner2.getSelectedItemPosition();
	    	if(glavaminus > 0) {
	    	bibletextglav(book,glavaminus);
	    	spinner2.setSelection(glavaminus-1);
	    	}
	    break;
	    
		case R.id.buttonforward:

			Integer book1 = spinner.getSelectedItemPosition()+1;
        	Integer chapters = (Integer)frbibleproperties.frbiblechapters.get("frbible"+book1);
        	Integer glavaplus = spinner2.getSelectedItemPosition()+2;
        	if((glavaplus-1) < chapters) {
        	bibletextglav(book1,glavaplus);
        	spinner2.setSelection(glavaplus-1);
        	}
			
		break;
		
		case R.id.button1:

			if(searchtext.length() == 0) {
				Toast.makeText(this, getString(R.string.enterword), Toast.LENGTH_SHORT).show();
			} else {
				
				  pd.setTitle(searchtext.getText().toString());
		 	      pd.setIndeterminate(true);
		 	      pd.setInverseBackgroundForced(true);
		 	      pd.setCancelable(false);
		 	      pd.setCanceledOnTouchOutside(false);
		 	      pd.setMessage(getString(R.string.findprocess) + "...");
		 	      pd.show();
		 	      
				
				final Intent intentsearch = new Intent(this, Searchaction.class);

                Thread t = new Thread() {
                        @Override
                        public void run() {
            				intentsearch.putExtra("extrasearchvalue", searchtext.getText().toString());
            			    startActivity(intentsearch);
            			    pd.dismiss();
                        }
                };
                t.start();
		
			}
	    break;
		
		}
	}
	
	//вывод конкретной главы через xml - около 10 секунд... долго, но пусть будет этот метод
	private static void bibleglav() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true); // never forget this!
	    DocumentBuilder builder = domFactory.newDocumentBuilder();
	    
	    File root = android.os.Environment.getExternalStorageDirectory();
		File xmlfile = new File(root.getAbsolutePath() + "/frbible_xml/frbible.xml");
	    //String pathxml = "/mnt/sdcard0/frbible_xml/frbible.xml";
	    
	    Document doc = builder.parse(xmlfile);
	    
	    XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    XPathExpression expr = xpath.compile("//booktext[43]/chapter[1]/verse/text()");

	    Object result = expr.evaluate(doc, XPathConstants.NODESET);
	    NodeList nodes = (NodeList) result;

		tv.setText("");
		tv.setPadding(10, 10, 10, 10);
		String fortextview = "";
	    
	    for (int i = 0; i < nodes.getLength(); i++) {
	    	fortextview +=  (i+1)  + ".&nbsp;" + nodes.item(i).getNodeValue() + "<br>" ;
	    }

	    tv.setText(Html.fromHtml(fortextview));
	    
	}

}
