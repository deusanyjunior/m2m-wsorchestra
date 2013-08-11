package br.usp.ime.compmus.m2m_wsorchestra;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;
import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import br.usp.ime.compmus.m2m_orchestra.R;
import br.usp.ime.compmus.m2m_wsorchestra.scorereader.ScoreReader;
import br.usp.ime.compmus.utils.JSONfunctions;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WSOrchestraActivity extends Activity {
	
	private Context context;
	
	private static final String TAG = "m2m-WSO";
	private static String userName = "guest68919022";
	private int velocity = 128;
	private int octave = 4;
	private static String webserver = "http://wscompmus.deusanyjunior.dj/notes";

	private static Thread readerThread;
	private static ScoreReader scoreReader;
	private static LinkedList<long[]> notesOn = new LinkedList<long[]>();
	private static Handler scoreReaderThreadHandler;
	private boolean WSSynchronized = false;
	private static boolean isRunning = true;
	private static String score = "";
	
	private PdUiDispatcher dispatcher;
	
	private TextView textview_username;
	private Spinner spinner_preset;
	private TextView textview_score;
	private SeekBar seekbar_velocity;
	private Button button_previous_octave;
	private TextView textview_octave;
	private Button button_next_octave;
	private ToggleButton toggle_ws_score_reader;
	private ToggleButton toggle_ws_synchronized;
	
	// Black Keys
	int keyB1;
	int keyB2;
	int keyB3;
	int keyB4;
	int keyB5;
	
	// White Keys
	int keyW1;
	int keyW2;
	int keyW3;
	int keyW4;
	int keyW5;
	int keyW6;
	int keyW7;
	int keyW8;
	
	private static String[] notesList = { "C-1","C#-1","D-1","D#-1","E-1","F-1","F#-1","G-1","G#-1","A-1","A#-1","B-1",
				 		   "C0","C#0","D0","D#0","E0","F0","F#0","G0","G#0","A0","A#0","B0",
				 		   "C1","C#1","D1","D#1","E1","F1","F#1","G1","G#1","A1","A#1","B1",
				 		   "C2","C#2","D2","D#2","E2","F2","F#2","G2","G#2","A2","A#2","B2",
				 		   "C3","C#3","D3","D#3","E3","F3","F#3","G3","G#3","A3","A#3","B3",
				 		   "C4","C#4","D4","D#4","E4","F4","F#4","G4","G#4","A4","A#4","B4",
				 		   "C5","C#5","D5","D#5","E5","F5","F#5","G5","G#5","A5","A#5","B5",
				 		   "C6","C#6","D6","D#6","E6","F6","F#6","G6","G#6","A6","A#6","B6",
				 		   "C7","C#7","D7","D#7","E7","F7","F#7","G7","G#7","A7","A#7","B7",
				 		   "C8","C#8","D8","D#8","E8","F8","F#8","G8","G#8","A8","A#8","B8",
				 		   "C9","C#9","D9","D#9","E9","F9","F#9","G9"};
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	context = this;
    		
    		setRunning(true);
    		notesOn = new LinkedList<long[]>();
    		
    		super.onCreate(savedInstanceState);
    		setContentView(R.layout.activity_wsorchestra);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    		
    		try {
    			initPd();
    			Log.i(TAG, "InitPd ok");
    			loadPatch();
    			Log.i(TAG, "LoadPatch ok");
    		} catch (IOException e) {
    			Log.e(TAG, e.toString());
    			finish();
    		}
    		
    		scoreReaderThreadHandler = new Handler() {
    			public void handleMessage(Message msg) {
    				
    				if (msg.obj != null) {
    					String user = (String) msg.obj;
    					int note = (int) msg.arg1;
    					int vel = (int) msg.arg2;
    					
    					playNote(user, note, vel);        			
    				} else {
    					systemRemoveNoteOn(msg.arg1);
    				}
    				
    			}
    		};
    		
    		setTextsOnScreen();
    		Log.i(TAG, "Texts setted");
    		
    		createSpinnerPreset();
    		Log.i(TAG, "Spinner setted");
    		
    		configKeys(octave);
    		Log.i(TAG, "Keys setted");
    		
    		setupButtons();
    		Log.i(TAG, "Keyboard setted");
    		
    		createOctaveButtons();
    		Log.i(TAG, "Octave buttons setted");
    		
    		setupSeekBarVelocity();
    		Log.i(TAG, "SeekBar setted");
    		
//        startScoreReader(); 
//        Log.i(TAG, "ScoreReader setted");
    	
    		
        
    }
    
    @Override
	public void onBackPressed() {
    	setRunning(false);
    	finish();
    }
    
    public static boolean isRunning() {
		return isRunning;
	}

	public static void setRunning(boolean isRunning) {
		WSOrchestraActivity.isRunning = isRunning;
	}
    
    /**
     * Get the username from first activity and set on this activity.
     * If the username is null, the user will be a guest as in mIRC.
     * 
     * Set the score text to null
     */
    private void setTextsOnScreen() {
    	Bundle b = this.getIntent().getExtras();
        if( !b.getString("username").equals(null)) {
        	userName = b.getString("username");        	
        }
        textview_username = (TextView) findViewById(R.id.textView_username);
        textview_username.setText(userName);
//        textview_username.setText(userName.toCharArray(),0,userName.toCharArray().length);
        
        textview_score = (TextView) findViewById(R.id.textView_score);
        textview_score.setText(score);
    }
    
    /**
     * Add items on Spinner Preset and set the listener.
     * The items are described on /res/values/strings.xml <string-array name="spinner_presset">
     */
    private void createSpinnerPreset() {
    	
    	spinner_preset = (Spinner) findViewById(R.id.spinner_preset);
    	ArrayAdapter<CharSequence> adapter_spinner_preset = ArrayAdapter.createFromResource(context, R.array.spinner_presset, android.R.layout.simple_spinner_item);
    	adapter_spinner_preset.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinner_preset.setAdapter(adapter_spinner_preset);
    	
    	spinner_preset.setOnItemSelectedListener(spinnerPresetOnItemSelectedListener);
    }
    
    /**
     * Set the preset when the user change the preset
     */
    private OnItemSelectedListener spinnerPresetOnItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1,
				int pos, long id) {
			// Send a bang to presets:
			// 		r-preset1, r-preset2, r-preset3, r-preset4
			// Using "pos+1" because the position starts from 0 (zero)
			PdBase.sendBang("r-preset"+(pos+1));
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void configKeys(int new_octave) {
		// for security reasons:
		if (new_octave < -1 || new_octave > 8) {
			return;
		}

		octave = new_octave;
		textview_octave = (TextView) findViewById(R.id.textView_octave);
		textview_octave.setText(Integer.toString(octave));
		
		// Set Keys
		keyW1 = 12+12*octave; // C
		keyB1 = 13+12*octave; // C#
		keyW2 = 14+12*octave; // D
		keyB2 = 15+12*octave; // D#
		keyW3 = 16+12*octave; // E
		keyW4 = 17+12*octave; // F
		keyB3 = 18+12*octave; // F#
		keyW5 = 19+12*octave; // G
		keyB4 = 20+12*octave; // G#
		keyW6 = 21+12*octave; // A
		keyB5 = 22+12*octave; // A#
		keyW7 = 23+12*octave; // B
		keyW8 = 24+12*octave; // C
	}
	
	/**
	 * Configure the buttons and the listeners
	 */
	private void setupButtons() {
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int size = dm.widthPixels/8-(dm.widthPixels/8)/8;
		
		// Set spaces
		// space1
		Button buttonS1 = (Button) findViewById(R.id.button_noteS1);
		buttonS1.setWidth(size/2);
		buttonS1.setVisibility(Button.INVISIBLE);
		// space2
		Button buttonS2 = (Button) findViewById(R.id.button_noteS2);
		buttonS2.setWidth(size);
		buttonS2.setVisibility(Button.INVISIBLE);
		// space3
		Button buttonS3 = (Button) findViewById(R.id.button_noteS3);
		buttonS3.setWidth(size);
		buttonS3.setVisibility(Button.INVISIBLE);
		// space4
		Button buttonS4 = (Button) findViewById(R.id.button_noteS4);
		buttonS4.setWidth(size/2);
		buttonS4.setVisibility(Button.INVISIBLE);
 			
		Button buttonB1 = (Button) findViewById(R.id.button_noteB1);
		Button buttonB2 = (Button) findViewById(R.id.button_noteB2);
		Button buttonB3 = (Button) findViewById(R.id.button_noteB3);
		Button buttonB4 = (Button) findViewById(R.id.button_noteB4);
		Button buttonB5 = (Button) findViewById(R.id.button_noteB5);
		
		Button buttonW1 = (Button) findViewById(R.id.button_noteW1);
		Button buttonW2 = (Button) findViewById(R.id.button_noteW2);
		Button buttonW3 = (Button) findViewById(R.id.button_noteW3);
		Button buttonW4 = (Button) findViewById(R.id.button_noteW4);
		Button buttonW5 = (Button) findViewById(R.id.button_noteW5);
		Button buttonW6 = (Button) findViewById(R.id.button_noteW6);
		Button buttonW7 = (Button) findViewById(R.id.button_noteW7);
		Button buttonW8 = (Button) findViewById(R.id.button_noteW8);
		
		buttonB1.setWidth(size);
		buttonB2.setWidth(size);
		buttonB3.setWidth(size);
		buttonB4.setWidth(size);
		buttonB5.setWidth(size);

		buttonW1.setWidth(size);
		buttonW2.setWidth(size);
		buttonW3.setWidth(size);
		buttonW4.setWidth(size);
		buttonW5.setWidth(size);
		buttonW6.setWidth(size);
		buttonW7.setWidth(size);
		buttonW8.setWidth(size);

		buttonB1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyB1, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyB1, 0);
				}
				return false;
			}
		});

		buttonB2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyB2, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyB2, 0);
				}
				return false;
			}
		});
		
		buttonB3.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyB3, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyB3, 0);
				}
				return false;
			}
		});
		
		buttonB4.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyB4, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyB4, 0);
				}
				return false;
			}
		});
		
		buttonB5.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyB5, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyB5, 0);
				}
				return false;
			}
		});
		
		
		buttonW1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyW1, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyW1, 0);
				}
				return false;
			}
		});
		
		buttonW2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyW2, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyW2, 0);
				}
				return false;
			}
		});
		
		buttonW3.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyW3, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyW3, 0);
				}
				return false;
			}
		});
		
		buttonW4.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyW4, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyW4, 0);
				}
				return false;
			}
		});
		
		buttonW5.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyW5, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyW5, 0);
				}
				return false;
			}
		});
		
		buttonW6.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyW6, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyW6, 0);
				}
				return false;
			}
		});
		
		buttonW7.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyW7, velocity);
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyW7, 0);
				}
				return false;
			}
		});
		
		buttonW8.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
					playNote(keyW8, velocity);					
				} else if ( event.getAction() == MotionEvent.ACTION_UP) {
					playNote(keyW8, 0);
				}
				return false;
			}
		});
		
		
		
		toggle_ws_score_reader = (ToggleButton) findViewById(R.id.toggleButton_WSScoreReader);
		toggle_ws_score_reader.setChecked(false);
		toggle_ws_score_reader.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (toggle_ws_score_reader.isChecked()) {
					// try to start the ScoreReader or notify Internet problems
					if ( startScoreReader() ) {
						Toast.makeText(context, "ScoreReader started succesfully", Toast.LENGTH_SHORT).show();
					} else {
						toggle_ws_score_reader.setChecked(false);
						Toast.makeText(context, "ScoreReader can't start. Check your Internet and webserver status.", Toast.LENGTH_LONG).show();
					}
				} else {
					stopScoreReader();
					Toast.makeText(context, "ScoreReader stopped succesfully", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		toggle_ws_synchronized = (ToggleButton) findViewById(R.id.toggleButton_WSSynchronized);
		toggle_ws_synchronized.setChecked(WSSynchronized);
		toggle_ws_synchronized.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (toggle_ws_synchronized.isChecked()) {
					WSSynchronized = true;
				} else {
					WSSynchronized = false;
				}
				
			}
		});
		
	}
	
	/**
	 * Create the buttons and set the listeners
	 */
	private void createOctaveButtons() {
		button_previous_octave = (Button) findViewById(R.id.button_prev_octave);
		button_previous_octave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int new_octave = octave - 1;
				configKeys(new_octave);
			}
		});
		
		button_next_octave = (Button) findViewById(R.id.button_next_octave);
		button_next_octave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int new_octave = octave + 1;
				configKeys(new_octave);
			}
		});
	}
	
	/**
	 * Configure the Velocity SeekBar listener.
	 * Change the velocity on stop tracking touch.
	 */
	private void setupSeekBarVelocity() {
		seekbar_velocity = (SeekBar) findViewById(R.id.seekBar_velocity);
		seekbar_velocity.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int velocityChanged = 0;
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Check dead lock or other problems with "velocity"
				velocity = velocityChanged;
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				velocityChanged = progress;
			}
		});
	}
	
	
	private class WSNotePlayer extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
					
			try {
				JSONObject noteObject = new JSONObject();
				noteObject.put("username", userName);
				noteObject.put("note", params[1]);
				noteObject.put("velocity", params[2]);
				
				JSONObject messageObject = new JSONObject();
				messageObject.put("note", noteObject);
				
				JSONfunctions.sendJSONToURL(webserver, messageObject.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	private void addTextToScore(String text) {
		score = text.concat("\n"+score);
		textview_score.setText(score);
	}
	
	private void playNote(int note, int vel) {
		if (!WSSynchronized) {
			Object list[] = new Object[2];
			list[0] = note;
			list[1] = vel;
			PdBase.sendList("r-notein", list);
			if (vel == 0) {
				removeNoteOnFromList(note);
			} else {
				long singleNote[] = { note, System.currentTimeMillis() }; 
				notesOn.add( singleNote );				
			}
			addTextToScore("<"+userName+"> n:"+notesList[(Integer) list[0]]+" v: "+list[1]);			
		}
			WSNotePlayer wsNotePlayer = new WSNotePlayer();
			wsNotePlayer.execute(new String[] { userName, Long.toString(note), Integer.toString(vel) });			
	}
	
	private void playNote(String user, int note, int vel) {
		if (user.equals(userName)) {
			Log.e(TAG, user+" == "+userName);
		} else {
			Log.e(TAG, user+" != "+userName);
		}
		if (user.equals(userName)) {
			if (WSSynchronized) {
				
				Object list[] = new Object[2];
				list[0] = note;
				list[1] = vel;
				PdBase.sendList("r-notein", list);
				
				if (vel == 0) {
					removeNoteOnFromList(note);
				} else {
					long singleNote[] = { note, System.currentTimeMillis() }; 
					notesOn.add( singleNote );					
				}
				addTextToScore("<"+user+"> n:"+notesList[(Integer) list[0]]+" v: "+list[1]);
			}
		} else {		
			Object list[] = new Object[2];
			list[0] = note;
			list[1] = vel;
			PdBase.sendList("r-notein", list);
			if (vel == 0) {
				removeNoteOnFromList(note);
			} else {
				long singleNote[] = { note, System.currentTimeMillis() }; 
				notesOn.add( singleNote );				
			}
			addTextToScore("<"+user+"> n:"+notesList[(Integer) list[0]]+" v: "+list[1]);
		}
	}
	
	private void removeNoteOnFromList(int note) {
		for(int i = 0; i < notesOn.size(); i++) {
			if ( ((long) note) == notesOn.get(i)[0] ) {
				notesOn.remove(i);
			}
		}
	}
	
	private void systemRemoveNoteOn(int note) {
		removeNoteOnFromList(note);		
		Object list[] = new Object[2];
		list[0] = note;
		list[1] = 0;
		PdBase.sendList("r-notein", list);
		addTextToScore("[WSO System] n:"+notesList[(Integer) list[0]]+" v: "+list[1]);
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private boolean startScoreReader() {
		boolean started = false;
		try {
				scoreReader = new ScoreReader(new URL(webserver));
				
				if (isNetworkAvailable() && scoreReader.setLastNoteFromWS()) {
					setRunning(true);
					readerThread = new ReaderThread();
					readerThread.start();
					started =  true;
				}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			started = false;
			return started;
		}
		return started;
	}
	
	private void stopScoreReader() {
		setRunning(false);
		if (readerThread != null) {
//			//TODO check if the thread stopped
			readerThread.interrupt();
		}
	}
	

	static public class ReaderThread extends Thread {
		
		public void run() {
			
			while(isRunning()) {
				
				// Check notesOn
				if ( !notesOn.isEmpty() && (System.currentTimeMillis() - notesOn.getFirst()[1]) > 10000 ) {
					Log.e(TAG, "NoteOff missed: "+notesOn.getFirst()[0]);
					Message message = Message.obtain();
					message.obj = null;
					message.arg1 = (int) notesOn.getFirst()[0];
					message.arg2 = 0; // Set the velocity to zero and stop the note;
					message.setTarget(scoreReaderThreadHandler);
					message.sendToTarget();
				}
				
				
				// Get the last note
				String[] nextNote = scoreReader.getNextNote();
				if( nextNote != null ) {
					Message message = Message.obtain();
					message.obj = nextNote[1];
					message.arg1 = Integer.parseInt(nextNote[2]);
					message.arg2 = Integer.parseInt(nextNote[3]);
					message.setTarget(scoreReaderThreadHandler);
					message.sendToTarget();
				} else {
					try {
						Log.i(TAG, "Waiting before trying to get more notes from webserver..");
						sleep(2000);
//						new Thread().sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	// LIBPD code
    
    private void initPd() throws IOException {
    	// Configure the audio glue
    	int sampleRate = AudioParameters.suggestSampleRate();
    	PdAudio.initAudio(sampleRate, 0, 2, 8, true);
    	// Create and install the dispatcher
    	dispatcher = new PdUiDispatcher();
    	PdBase.setReceiver(dispatcher);
    }
    
    private void loadPatch() {
    	try {
    		// Hear the sound
    		File dir = getFilesDir();
			IoUtils.extractZipResource(
					getResources().openRawResource(R.raw.polysynth), dir, true);
			File patchFile = new File(dir, "polysynth.pd");
			PdBase.openPatch(patchFile.getAbsolutePath());
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    protected void onResume() {
    	if (readerThread != null && readerThread.isInterrupted()) {
    		readerThread.start();
    	}
    	super.onResume();
    	PdAudio.startAudio(this);
    }
        
    @Override
    protected void onPause() {
    	super.onPause();
    	PdAudio.stopAudio();
    }
    
    @Override
    protected void onStop() {
    	setRunning(false);
        super.onStop();
    }
    
    @Override
    public void onDestroy() {
    	setRunning(false);
    	super.onDestroy();
    	PdAudio.release();
    	PdBase.release();
    }

	
    
}
