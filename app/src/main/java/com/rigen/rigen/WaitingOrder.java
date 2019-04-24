package com.rigen.rigen;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.rigen.rigen.db.EntityOrder;
import com.rigen.rigen.lib.ClientConnectPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by ILHAM HP on 25/11/2016.
 */

public class WaitingOrder extends AppCompatActivity {
    private TextView txt_date, txt_harga, txt_jarak, txt_alamat, txt_tujuan;
    private Button btn_batal, btn_baru;
    private ClientConnectPost connectPost;

    EntityOrder eo;
    Timer timer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);
        txt_date = (TextView) findViewById(R.id.txt_date);
        txt_harga = (TextView) findViewById(R.id.txt_harga);
        txt_jarak = (TextView) findViewById(R.id.txt_jarak);
        txt_alamat = (TextView) findViewById(R.id.txt_from);
        txt_tujuan = (TextView) findViewById(R.id.txt_to);
        btn_baru = (Button) findViewById(R.id.btn_order_baru);
        btn_batal = (Button) findViewById(R.id.btn_cancel);
        Reload();
        btn_baru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_batal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(WaitingOrder.this);
                alert.setMessage("Batalkan pesanan ?");
                alert.setNegativeButton("Tidak",null);
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String, String> dataToSend = new HashMap<String, String>();
                        dataToSend.put("api",getString(R.string.API));
                        dataToSend.put("id",eo.getId_order()+"");
                        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
                        ClientConnectPost client = new ClientConnectPost(WaitingOrder.this);
                        client.execute(getString(R.string.server) + "android/cancel" , encodedStr);
                        finish();
                    }
                });
                alert.show();
            }
        });
    }

    void setTimer() {
        timer = new Timer();
        TimerTask tt = new CustomTimerTask();
        timer.scheduleAtFixedRate(tt, 5000, 5000);
    }
    private void Reload() {
        Map<String, String> dataToSend = new HashMap<String, String>();
        dataToSend.put("api", getString(R.string.API));
        dataToSend.put("id", getIntent().getIntExtra("id",0)+"");
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        connectPost = new ClientConnectPost(this);
        connectPost.execute(getResources().getString(R.string.server) + "android/detailorder", encodedStr);
        try {
            DecimalFormat df = new DecimalFormat("#.#");
            NumberFormat formatKurensi = NumberFormat.getCurrencyInstance(new Locale("in", "id"));
            JSONObject j = connectPost.get();
            JSONArray ja = j.getJSONArray("data");
            j = ja.getJSONObject(0);
            eo = new EntityOrder();
            eo.setId_order(j.getInt("id_order"));
            eo.setBayar(j.getInt("bayar"));
            eo.setTgl_order(j.getString("tgl_order"));
            eo.setJarak(j.getDouble("jarak"));
            eo.setLat_jemput(j.getDouble("lat_jemput"));
            eo.setLong_jemput(j.getDouble("long_jemput"));
            eo.setLat_tujuan(j.getDouble("lat_tujuan"));
            eo.setLong_tujuan(j.getDouble("long_tujuan"));
            eo.setAlamat_jemput(j.getString("alamat_jemput"));
            eo.setAlamat_tujuan(j.getString("alamat_tujuan"));
            eo.setStatus_order(j.getString("status_order"));

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = fmt.parse(eo.getTgl_order());
            SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MM-yyyy hh:mm");
            txt_date.setText(fmtOut.format(date));
            txt_harga.setText(formatKurensi.format(eo.getBayar()));
            txt_jarak.setText("Harga ("+df.format(eo.getJarak())+" Km)");
            txt_alamat.setText(eo.getAlamat_jemput());
            txt_tujuan.setText(eo.getAlamat_tujuan());
            setTimer();
        } catch (InterruptedException e) {
            finish();
        } catch (ExecutionException e) {
            finish();
        } catch (JSONException e) {
            finish();
        } catch (ParseException e) {
            finish();
        }
    }
    private void confirm(){
        timer.cancel();
        Intent i = new Intent(WaitingOrder.this, DetailOrderActivity.class);
        i.putExtra("id", getIntent().getIntExtra("id",0));
        startActivity(i);
        finish();
    }

    private class CustomTimerTask extends TimerTask {
        @Override
        public void run() {
            Map<String, String> dataToSend = new HashMap<String, String>();
            dataToSend.put("api", getString(R.string.API));
            dataToSend.put("id", getIntent().getIntExtra("id",0)+"");
            String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
            connectPost = new ClientConnectPost(WaitingOrder.this);
            connectPost.execute(getResources().getString(R.string.server) + "android/detailorder", encodedStr);
            try {
                JSONObject j = connectPost.get();
                JSONArray ja = j.getJSONArray("data");
                j = ja.getJSONObject(0);
                EntityOrder eo = new EntityOrder();
                eo.setStatus_order(j.getString("status_order"));
                if (!eo.getStatus_order().equalsIgnoreCase("Not Confirm")){
                    confirm();
                }
            } catch (InterruptedException e) {} catch (ExecutionException e) {} catch (JSONException e) {}
        }
    }
}
