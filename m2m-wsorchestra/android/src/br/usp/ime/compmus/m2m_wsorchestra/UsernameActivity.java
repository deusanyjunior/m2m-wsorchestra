package br.usp.ime.compmus.m2m_wsorchestra;

import br.usp.ime.compmus.m2m_orchestra.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class UsernameActivity extends Activity {

	Context context;
	EditText edittext_userName;
	Button button_startPlaying;
	
	private static String userName = "guest68919022";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_username);
		
		context = this;
		edittext_userName = (EditText) findViewById(R.id.input_username);
		button_startPlaying = (Button) findViewById(R.id.button_start_playing);
		button_startPlaying.setOnClickListener(startPlayingOnClickListener);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.username, menu);
		return true;
	}
	
	private OnClickListener startPlayingOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (! edittext_userName.getEditableText().toString().equals("") ) {
				userName = edittext_userName.getEditableText().toString();
			}
			Log.i("UsernameActivity", "username: "+userName);
			Intent startPlaying = new Intent(context, WSOrchestraActivity.class);
			startPlaying.putExtra("username", userName);
			context.startActivity(startPlaying);
		}
	};

}
