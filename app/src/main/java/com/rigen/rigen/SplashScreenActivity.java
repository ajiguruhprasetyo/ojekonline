package com.rigen.rigen;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.RelativeLayout;

import com.rigen.rigen.R;
import com.rigen.rigen.db.DBQuery;
import com.rigen.rigen.db.EntityAccount;
import com.rigen.rigen.utils.ConnectionUtils;

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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ILHAM HP on 24/11/2016.
 */

public class SplashScreenActivity extends AppCompatActivity {
    private DBQuery db;
    private RelativeLayout mylayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        mylayout = (RelativeLayout) findViewById(R.id.primaryLayout);

        db = new DBQuery(this);
        db.open();
        check();
    }
    private boolean check(){
        if (ConnectionUtils.isConnected(this)){
            logindata();
            return true;
        }else{
            Snackbar.make(mylayout, "No Internet Connection!",Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            check();
                        }
                    })
                    .show();
            return false;
        }
    }
    private void logindata(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                db = new DBQuery(SplashScreenActivity.this);
                db.open();
                Cursor c = db.getAll();
                if (c.getCount()!=0){
                    c.moveToFirst();
                    UserLoginTask userLoginTask = new UserLoginTask(c.getString(c.getColumnIndex("email")), c.getString(c.getColumnIndex("password")));
                    userLoginTask.execute((Void) null);
                }else {
                    startActivity(new Intent(SplashScreenActivity.this,
                            LoginActivity.class));
                    finish();
                }

            }
        }, 5000);
    }
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private String msg;
        private EntityAccount e;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject response = new JSONObject();
            BufferedReader reader = null;
            try {
                URL url = new URL(getResources().getString(R.string.server)+"android/logincheck");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                Map<String, String> dataToSend = new HashMap<String, String>();
                dataToSend.put("api", getResources().getString(R.string.API));
                dataToSend.put("email", mEmail);
                dataToSend.put("password", mPassword);
                writer.write(getEncodedData(dataToSend));
                writer.flush();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String responseString = readStream(con.getInputStream());
                response = new JSONObject(responseString);
                int res = response.getInt("r");
                msg =response.getString("msg");
                JSONObject jo = response.getJSONObject("data");
                e = new EntityAccount();
                if (res==1){
                    e.setNama(jo.getString("nama"));
                    e.setPassword(jo.getString("password"));
                    e.setEmail(jo.getString("email"));
                    e.setNo_telp(jo.getString("no_telp"));
                    e.setAlamat(jo.getString("alamat"));

                    return true;
                }else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            } else {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                finish();
            }
        }
        private String getEncodedData(Map<String,String> data) {
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
}
