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

import com.rigen.rigen.DetailOrderActivity;
import com.rigen.rigen.R;
import com.rigen.rigen.db.DBQuery;
import com.rigen.rigen.db.EntityOrder;
import com.rigen.rigen.lib.ClientConnectPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class BookingService extends Service {

	private String email=null;
	private boolean off=false;

	private ArrayList<EntityOrder> data;
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
	private void  checkOrder(){
		DBQuery db = new DBQuery(this);
		db.open();
		Cursor c = db.getAll();
		if (c.getCount() ==1) {
			c.moveToFirst();
			String email = c.getString(c.getColumnIndex("email"));
			Map<String, String> dataToSend = new HashMap<String, String>();
			dataToSend.put("api", getString(R.string.API));
			dataToSend.put("email", email);
			String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
			ClientConnectPost client = new ClientConnectPost(this);
			client.execute(getResources().getString(R.string.server) + "android/checkorders", encodedStr);
			try {
				JSONObject j = client.get();
				JSONArray ja = j.getJSONArray("data");
				for (int i = 0; i < ja.length(); i++) {
					JSONObject jo = ja.getJSONObject(i);
					EntityOrder e = new EntityOrder();
					e.setId_order(jo.getInt("id_order"));
					e.setAlamat_tujuan(jo.getString("alamat_tujuan"));
					e.setAlamat_jemput(jo.getString("alamat_jemput"));
					e.setBayar(jo.getInt("bayar"));
					e.setJarak(jo.getDouble("jarak"));
					e.setStatus_order(jo.getString("status_order"));
					if (data.size()>0) {
						boolean isexist = false;
						for (EntityOrder eo : data) {
							if(e.getId_order() == eo.getId_order()){
								isexist = true;
							}
						}
						if (!isexist){
							data.add(e);
							addNotification(e);
						}
					}else{
						data.add(e);
						addNotification(e);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		db.close();
		c.close();
	}
	private void addNotification(EntityOrder e) {
		Intent resultIntent = new Intent(this, DetailOrderActivity.class);
		resultIntent.putExtra("id", e.getId_order());
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mNotifyBuilder;
		NotificationManager mNotificationManager;
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Sets an ID for the notification, so it can be updated
		mNotifyBuilder = new NotificationCompat.Builder(this);
		mNotifyBuilder.setContentTitle("Driver ditemukan!");
		mNotifyBuilder.setContentText(e.getAlamat_tujuan());
		mNotifyBuilder.setSmallIcon(R.drawable.ic_drop);
		mNotifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(e.getAlamat_tujuan()));
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
		DecimalFormat df = new DecimalFormat("#,#");
		mNotifyBuilder.setContentInfo(df.format(e.getJarak())+" Km");
		mNotifyBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_drop));
		// Set autocancel
		mNotifyBuilder.setAutoCancel(true);
		mNotificationManager.notify(e.getId_order(), mNotifyBuilder.build());
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
								checkOrder();
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
