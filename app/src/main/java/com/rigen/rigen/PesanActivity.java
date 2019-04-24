package com.rigen.rigen;

import android.app.NotificationManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.rigen.rigen.db.AdapterPesan;
import com.rigen.rigen.db.DBQuery;
import com.rigen.rigen.db.EntityPesan;
import com.rigen.rigen.lib.ClientConnectPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by ILHAM HP on 19/12/2016.
 */

public class PesanActivity extends AppCompatActivity {
    private RecyclerView rv;
    private ArrayList<EntityPesan> data;
    private String id_order;
    private EditText et_pesan;
    private ImageButton img_send;
    private String username, nama, usernamedriver;
    private Timer mTimer;
    AdapterPesan adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.chat_layout);
        id_order =  getIntent().getStringExtra("id");
        rv = (RecyclerView) findViewById(R.id.rv);
        et_pesan = (EditText) findViewById(R.id.et_pesan);
        img_send = (ImageButton) findViewById(R.id.img_send);
        reload();
        mTimer = new Timer();
        mTimer.schedule(timerTask, 2000, 2 * 1000);
        img_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_pesan.getText().toString().isEmpty()){
                    sendChat(et_pesan.getText().toString());
                }
            }
        });
    }


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
                            checknew();
                        }
                    });
                }
            }).start();
        }
    };

    @Override
    protected void onDestroy() {
        mTimer.purge();
        super.onDestroy();
    }

    private void sendChat(String msg){
        Map<String, String> dataToSend = new HashMap<String, String>();
        dataToSend.put("api",getResources().getString(R.string.API));
        dataToSend.put("id_order", id_order);
        dataToSend.put("to", usernamedriver);
        dataToSend.put("from", username);
        dataToSend.put("msg", msg);

        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(this);
        client.execute(getString(R.string.server) + "android/sendchat", encodedStr);
        try {
            JSONObject j = client.get();
            int res = j.getInt("r");;
            if (res == 1) {
                reload();
                et_pesan.setText(null);
            } else {
            }
        }catch (Exception e){}
    }
    private void reload(){
        try {
            NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(this.NOTIFICATION_SERVICE);
            nMgr.cancel(Integer.parseInt(id_order)+8163);
        }catch (Exception e){}
        data = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, OrientationHelper.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        rv.setLayoutManager(linearLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());

        Map<String, String> dataToSend = new HashMap<String, String>();

        DBQuery db = new DBQuery(this);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        username = c.getString(c.getColumnIndex("email"));
        dataToSend.put("api",getResources().getString(R.string.API));
        dataToSend.put("id_order", id_order);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(this);
        client.execute(getResources().getString(R.string.server) + "android/chat", encodedStr);

        try {
            JSONObject j = client.get();
            JSONObject jorder = j.getJSONObject("data_order");
            getSupportActionBar().setTitle(jorder.getString("nama"));
            usernamedriver = jorder.getString("username_driver");
            String status_order = jorder.getString("status_order");
            if(status_order.equalsIgnoreCase("finish") || status_order.equalsIgnoreCase("Not Confirm")){
                et_pesan.setVisibility(EditText.GONE);
                img_send.setVisibility(ImageButton.GONE);
            }else{
                et_pesan.setVisibility(EditText.VISIBLE);
                img_send.setVisibility(ImageButton.VISIBLE);
            }
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
                data.add(e);
            }
            adapter = new AdapterPesan(this, data,username);
            rv.setAdapter(adapter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
        }
        db.close();
        c.close();
    }

    private void checknew(){
        Map<String, String> dataToSend = new HashMap<String, String>();
        DBQuery db = new DBQuery(this);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        username = c.getString(c.getColumnIndex("email"));
        dataToSend.put("api",getResources().getString(R.string.API));
        dataToSend.put("id_order", id_order);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(this);
        client.execute(getResources().getString(R.string.server) + "android/chat", encodedStr);

        try {
            JSONObject j = client.get();
            JSONObject jorder = j.getJSONObject("data_order");
            String status_order = jorder.getString("status_order");
            if(status_order.equalsIgnoreCase("finish") || status_order.equalsIgnoreCase("Not Confirm")){
                et_pesan.setVisibility(EditText.GONE);
                img_send.setVisibility(ImageButton.GONE);
            }else{
                et_pesan.setVisibility(EditText.VISIBLE);
                img_send.setVisibility(ImageButton.VISIBLE);
            }
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
                if (data.size()>0){
                    boolean isexist = false;
                    for (EntityPesan entityPesan: data){
                        if (entityPesan.getId()== e.getId()){
                            isexist = true;
                        }
                    }
                    if (!isexist){
                        data.add(e);
                        adapter.notifyDataSetChanged();
                        rv.scrollToPosition(data.size() - 1);
                    }
                }else {
                    data.add(e);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
        }
        db.close();
        c.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);

    }
}
