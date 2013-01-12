package in.dobro.frbible;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Tofavorits extends Activity implements OnClickListener {
	
	Button addbutton;
	
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.favorits);
	    
	    addbutton = (Button) findViewById(R.id.button1);
	    addbutton.setOnClickListener(this);
	    
	  }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		
		case R.id.button1:

			Intent intent = new Intent(this, FrbibleActivity.class);
		    startActivity(intent);
			
	    break;
		
		}
		
	}


}
