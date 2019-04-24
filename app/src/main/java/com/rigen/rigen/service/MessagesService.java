package com.rigen.rigen.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.rigen.rigen.PesanActivity;
import com.rigen.rigen.R;
import com.rigen.rigen.db.DBQuery;
import com.rigen.rigen.db.EntityPesan;
import com.rigen.rigen.lib.ClientConnectPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MessagesService extends Service {

	private String email=null;
	private boolean off=false;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("in","ID"));
	SimpleDateFormat sdft = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("in","ID"));

	private ArrayList<EntityPesan> data;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		off = check();
		if (off==false) {
			data = new ArrayList<>();
			mTimer = new Timer();
			mTimer.schedule(timerTask, 2000, 2 * 1000);
		}
	}

	private boolean check(){
		DBQuery db = new DBQuery(this);
		db.open();
		Cursor c = db.getAll();
		if (c.getCount() ==1) {
			c.moveToFirst();
			email = c.getString(c.getColumnIndex("email"));
			c.close();
			db.close();
			return false;
		}else {
			email = null;
			c.close();
			db.close();
			return true;
		}
	}
	private void  checkChat(){
		DBQuery db = new DBQuery(this);
		db.open();
		Cursor c = db.getAll();
		if (c.getCount() ==1) {
			c.moveToFirst();
			email = c.getString(c.getColumnIndex("email"));
			Map<String, String> dataToSend = new HashMap<String, String>();
			dataToSend.put("api", getString(R.string.API));
			dataToSend.put("email", email);
			String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
			ClientConnectPost client = new ClientConnectPost(this);
			client.execute(getResources().getString(R.string.server) + "android/checkchat ", encodedStr);
			try {
				JSONObject j = client.get();
				JSONArray ja = j.getJSONArray("data");
				for (int i = 0; i < ja.length(); i++) {
					JSONObject jo = ja.getJSONObject(i);
					EntityPesan e = new EntityPesan();
					e.setId(jo.getInt("id_messages"));
					e.setId_order(jo.getInt("id_order"));
					e.setDate(jo.getString("date_messages"));
					e.setFrom(jo.getString("from"));
					e.setTo(jo.getString("to"));
					e.setPesan(jo.getString("messages"));
					e.setStatus(jo.getString("status_messages"));
					if (data.size()>0) {
						for (EntityPesan eo : data) {
							if(e.getId() != eo.getId()){
								data.add(e);
								addNotification(e);
							}
						}
					}else{
						data.add(e);
						addNotification(e);
					}
					status("D", e.getId()+"");
				}
			} catch (InterruptedException e) {} catch (ExecutionException e) {} catch (JSONException e) {}
		}
		db.close();
		c.close();
	}

	private void status(String status, String id){
		Map<String, String> dataToSend = new HashMap<String, String>();
		dataToSend.put("api",getResources().getString(R.string.API));
		dataToSend.put("id_messages", id);
		dataToSend.put("status_messages", status);
		String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
		ClientConnectPost client = new ClientConnectPost(this);
		client.execute(getResources().getString(R.string.server) + "android/statuschat", encodedStr);
	}
	private void addNotification(EntityPesan e) {
		Intent resultIntent = new Intent(this, PesanActivity.class);
		resultIntent.putExtra("id", e.getId_order()+"");
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mNotifyBuilder;
		NotificationManager mNotificationManager;
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Sets an ID for the notification, so it can be updated
		mNotifyBuilder = new NotificationCompat.Builder(this);
		mNotifyBuilder.setContentTitle("Pesan : "+e.getFrom());
		mNotifyBuilder.setContentText(e.getPesan());
		mNotifyBuilder.setSmallIcon(R.drawable.ic_action_chat);
		mNotifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(e.getPesan()));
		// Set pending intent
		mNotifyBuilder.setContentIntent(resultPendingIntent);
		mNotifyBuilder.setPriority(Notification.PRIORITY_MAX);
		// Set Vibrate, Sound and Light
		int defaults = 0;
		defaults = defaults | Notification.DEFAULT_LIGHTS;
		defaults = defaults | Notification.DEFAULT_VIBRATE;
		defaults = defaults | Notification.DEFAULT_SOUND;
		mNotifyBuilder.setDefaults(defaults);
		// Set the content for Notification
		try {
			Date dt = sdf.parse(e.getDate());
			mNotifyBuilder.setContentInfo(sdft.format(dt));
		} catch (ParseException e1) {}
		mNotifyBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_chat));
		// Set autocancel
		mNotifyBuilder.setAutoCancel(true);
		mNotificationManager.notify(e.getId_order()+8163, mNotifyBuilder.build());
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private Timer mTimer;

	TimerTask timerTask = new TimerTask() {
		private Handler mHandler = new Handler();
		@Override
		public void run() {
			new Thread(new Runnable(){
				@Override
				public void run(){
					mHandler.post(new Runnable(){
						@Override
						public void run(){
							off = check();
							if (off==false) {
								checkChat();
							}
						}
					});
				}
			}).start();
		}
	};

	public void onDestroy() {
		try {
			mTimer.cancel();
			timerTask.cancel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Intent intent = new Intent("com.test.Receiver");
		intent.putExtra("yourvalue", "torestore");
		sendBroadcast(intent);
	}
}
