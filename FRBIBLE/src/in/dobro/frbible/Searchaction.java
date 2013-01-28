package in.dobro.frbible;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Searchaction extends Activity {
	
	public static File INDEX_DIR;
	
	Button addbutton;
	TextView tvsearch;
	EditText searchtext;
	String searchvalue;
	
	ProgressDialog pd;
	
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.searching);
	   
	    pd = new ProgressDialog(this);
	    
	    try {
			Lucenesearch();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	  }

	private void Lucenesearch() throws IOException, ParseException {
		
		final Intent intentmain = new Intent(this, FrbibleActivity.class);
		
		Intent searchintent = getIntent(); 
		searchvalue = searchintent.getStringExtra("extrasearchvalue");
		
		setTitle(getString(R.string.findofword) + ": " + searchvalue);
		
		tvsearch = (TextView)findViewById(R.id.textViewsearch);
		
		tvsearch.setPadding(15, 15, 15, 15);
		
		File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/frbible_search");
		
		//полнотекстовый поиск	
	    INDEX_DIR = new File(dir.toString());
		
		Directory index = FSDirectory.open(INDEX_DIR); 
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
		
		String querytext = searchvalue;

    	Query q = new QueryParser(Version.LUCENE_35, "poemtext", analyzer).parse(querytext);
    	
    	int hitsPerPage = 500;
    	
    	IndexReader reader = IndexReader.open(index);
    	
    	IndexSearcher searcher = new IndexSearcher(reader);
   
    	Sort sort = new Sort(); 
    	sort.setSort(new SortField("bible", SortField.INT));
    	
    	TopFieldCollector collector = TopFieldCollector.create(sort, hitsPerPage, false, true, true, false);
    	
    	searcher.search(q, collector);
    	//searcher.search(q, 50, sort);
    	
    	ScoreDoc[] hits = collector.topDocs().scoreDocs;
    	
	    String result = "<b>" + getString(R.string.findresults) + ": " + hits.length + " " + getString(R.string.matches) + "</b><br><br>";

	    tvsearch.append(Html.fromHtml(result));
	    
	    String glavname;
	    
	    for(int i=0;i<hits.length;++i) {
    	    int docId = hits[i].doc;
    	    Document d = searcher.doc(docId);
    	    
    	    final String getbible = d.get("bible");
    	    
    	    String namebible = d.get("indexbiblename");
    	    
    	    final String getchapter = d.get("chapter");
    	    final String getpoem = d.get("poem");
    	    //ниже в стихах должна быть подсветка искомой фразы/слова
    	    String getpoemtext = d.get("poemtext");
    	    
    	    final String searchinbook = frbibleproperties.frbiblenames[Integer.parseInt(getbible)-1];
    	    
    	    if(Integer.parseInt(getbible) != 19) {
    	    	glavname = ", " + getString(R.string.chapter) + " ";
    	    } else {
    	    	glavname = ", " + getString(R.string.psalm) + " ";
    	    }
    	    
    	    String stringglav = searchinbook + glavname + getchapter + ", " + getString(R.string.poem) + " " + getpoem;

    	    SpannableString link = makeLinkSpan(stringglav, new View.OnClickListener() {          
    	        @Override
    	        public void onClick(View v) {
    	        	
    	          pd.setTitle(searchinbook);
  		 	      pd.setIndeterminate(true);
  		 	      pd.setInverseBackgroundForced(true);
  		 	      pd.setCancelable(false);
  		 	      pd.setCanceledOnTouchOutside(false);
  		 	      pd.setMessage(getString(R.string.loadprocess) + "...");
  		 	      pd.show();

    	        Thread t = new Thread() {
                        @Override
                        public void run() {
                        	intentmain.putExtra("bookint", Integer.parseInt(getbible));
            				intentmain.putExtra("glavint", Integer.parseInt(getchapter));
            				intentmain.putExtra("poemint", Integer.parseInt(getpoem));
            			    startActivity(intentmain);
            			    pd.dismiss();
                        }
                };
                t.start();
    	        	
    	        }
    	    });
    	    
    	    tvsearch.append(link);
    	    
    	    result = "<br>";
    	    tvsearch.append(Html.fromHtml(result));
 
    	    tvsearch.append(Html.fromHtml(result));
    	    
    	    
    	    tvsearch.append(Html.fromHtml(getpoemtext));
    	    
    	    result = "<br><br>";
    	    tvsearch.append(Html.fromHtml(result));  
    	    
    	}

	    makeLinksFocusable(tvsearch);	
		
	}

	private void makeLinksFocusable(TextView tv) {
	    MovementMethod m = tv.getMovementMethod();  
	    if ((m == null) || !(m instanceof LinkMovementMethod)) {  
	        if (tv.getLinksClickable()) {  
	            tv.setMovementMethod(LinkMovementMethod.getInstance());  
	        }  
	    }  
	}

	private SpannableString makeLinkSpan(CharSequence text, View.OnClickListener listener) {
	    SpannableString link = new SpannableString(text);
	    link.setSpan(new ClickableString(listener), 0, text.length(), 
	        SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
	    return link;
	}

	
	private static class ClickableString extends ClickableSpan {  
	    private View.OnClickListener mListener;          
	    public ClickableString(View.OnClickListener listener) {              
	        mListener = listener;  
	    }          
	    @Override  
	    public void onClick(View v) {  
	        mListener.onClick(v);  
	    }        
	}

}
