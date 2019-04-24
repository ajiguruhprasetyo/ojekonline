package com.rigen.rigen.lib;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class ClientConnectPost extends AsyncTask<String, String, JSONObject> {
	private String responseString;
	private Context c;

	public ClientConnectPost(Context context){
		c= context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected JSONObject doInBackground(String... params) {

		JSONObject response = new JSONObject();
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(params[0]);
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();
	        con.setRequestMethod("POST");
	        con.setDoOutput(true);
	        OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
	        writer.write(params[1]);
	        writer.flush();
	        StringBuilder sb = new StringBuilder();
	        reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

	        String responseString = readStream(con.getInputStream());
			response = new JSONObject(responseString);
			
	        String line;
	        while((line = reader.readLine()) != null) { 
	            sb.append(line + "\n");
	        }
	        line = sb.toString();
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if(reader != null) {
	            try {
	                reader.close();     //Closing the 
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }

		return response;
	    
	}

	public static String getEncodedData(Map<String,String> data) {
        StringBuilder sb = new StringBuilder();
        for(String key : data.keySet()) {
            String value = null;
            try {
                value = URLEncoder.encode(data.get(key),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if(sb.length()>0)
                sb.append("&");

            sb.append(key + "=" + value);
        }
        return sb.toString();
    }
	public String hasil(){
		return responseString;
	}
	private String readStream(InputStream in) {
		  BufferedReader reader = null;
		  StringBuffer response = new StringBuffer();
		  try {
		    reader = new BufferedReader(new InputStreamReader(in));
		    String line = "";
		    while ((line = reader.readLine()) != null) {
		      response.append(line);
		    }
		  } catch (IOException e) {
		    e.printStackTrace();
		  } finally {
		    if (reader != null) {
		      try {
		        reader.close();
		      } catch (IOException e) {
		        e.printStackTrace();
		      }
		    }
		  }
		  return response.toString();
		}
}