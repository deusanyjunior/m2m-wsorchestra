package br.usp.ime.compmus.m2m_wsorchestra.scorereader;

import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import br.usp.ime.compmus.utils.JSONfunctions;

import android.util.Log;

public class ScoreReader {

	private URL webserver;
	
	private int lastNote;
	
	public ScoreReader(URL ws) {
		webserver = ws;
		lastNote = 0;
	}
	
	public int getLastNoteFromWS() {
		int lastNoteFromWS = -1;
		JSONObject results = JSONfunctions.getJSONfromURL(webserver+".json");
		JSONObject resultsObject = null;
		
		try {
			if (results != null && !results.isNull("note")) {
				resultsObject = results.getJSONObject("note");
				if (resultsObject != null && !resultsObject.isNull("id")) {
					lastNoteFromWS = resultsObject.getInt("id");					
				}
			} else {
				lastNoteFromWS = -1;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			lastNoteFromWS = -1;
		}
		Log.i("ScoreReader", "getLastNoteFromWS "+lastNoteFromWS);
		return lastNoteFromWS;
	}
	
	public boolean setLastNoteFromWS() {
		boolean lastNoteSetted = false;
		int lastNoteFromWS = -1;
		
		if ( (lastNoteFromWS = getLastNoteFromWS()) != -1 ) {
			Log.i("ScoreReader", "setLastNoteFromWS "+lastNoteFromWS);
			this.lastNote = lastNoteFromWS;
			lastNoteSetted = true;
		}

		return lastNoteSetted;
	}
	
	public int getLastNote() {
		Log.i("ScoreReader", "getLastNote "+lastNote);
		return lastNote;
	}
	
	public void setLastNote(int note) {
		Log.i("ScoreReader", "setLastNote "+lastNote);
		lastNote = note;
	}
	
	public String[] getNextNote() {
		String[] nextNote = null;
		int note = -1;
		
		if (lastNote != 1) {
			note = lastNote+1;
			
			JSONObject resultsObject = null;
			JSONObject nextNoteObject = null;
			resultsObject = JSONfunctions.getJSONfromURL(webserver+"/"+note+".json");
			try {
				if (resultsObject != null && !resultsObject.isNull("note")) {
					nextNoteObject = resultsObject.getJSONObject("note");
					if (nextNoteObject != null && !nextNoteObject.isNull("note")) {
						// set nextNote
						nextNote = new String[4];
						nextNote[0] = nextNoteObject.get("created_at").toString();
						nextNote[1] = nextNoteObject.getString("username");
						nextNote[2] = Integer.toString(nextNoteObject.getInt("note"));
						nextNote[3] = Integer.toString(nextNoteObject.getInt("velocity"));
						// update the last note
						setLastNote(note);
					} else {
						return null;
					}
				} else {
					return null;
				}
			} catch (JSONException e) {
				Log.i("ScoreReader", "getNextNote JSONException");
				e.printStackTrace();
				return null;
			}	
		}
		
		return nextNote;
	}

}
