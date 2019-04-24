package com.rigen.rigen;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rigen.rigen.db.DBQuery;
import com.rigen.rigen.db.EntityOrder;
import com.rigen.rigen.lib.ClientConnectPost;
import com.rigen.rigen.lib.DirectionsJSONParser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by ILHAM HP on 25/11/2016.
 */

public class DetailOrderActivity extends AppCompatActivity implements OnMapReadyCallback{
    private ClientConnectPost connectPost;
    private TextView txt_date, txt_from, txt_destination,txt_jarak, txt_harga, txt_time, txt_timedriver, txt_drivername, txt_email,txt_phone, txt_address;
    private TextView txt_no_kendaraan, txt_merek, txt_jenis, txt_nama_kendaraan, txt_tahun,txt_cc,txt_warna;
    private  EntityOrder eo;
    private GoogleMap gMap;
    private ImageView img_driver, img_kendaraan;

    private DBQuery db;
    private String email;
    private LatLng from, to,drv;
    private Marker mrk_drv;

    private Timer timer;
    private boolean hidemenu=true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("MyBookings");
        setContentView(R.layout.activity_detailorder);
        txt_date = (TextView) findViewById(R.id.txt_date);
        txt_from = (TextView) findViewById(R.id.txt_from);
        txt_destination = (TextView) findViewById(R.id.txt_to);
        txt_jarak = (TextView) findViewById(R.id.txt_jarak);
        txt_harga = (TextView) findViewById(R.id.txt_harga);
        txt_time = (TextView) findViewById(R.id.txt_time);
        txt_timedriver = (TextView) findViewById(R.id.txt_drivertime);
        txt_drivername = (TextView) findViewById(R.id.txt_drivername);
        txt_email = (TextView) findViewById(R.id.txt_email);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
        txt_address = (TextView) findViewById(R.id.txt_address);
        img_driver = (ImageView) findViewById(R.id.img_driver);
        img_kendaraan = (ImageView) findViewById(R.id.img_motor);
        txt_no_kendaraan = (TextView) findViewById(R.id.txt_nopol);
        txt_merek = (TextView) findViewById(R.id.txt_merek);
        txt_jenis = (TextView) findViewById(R.id.txt_jenis_kendaraan);
        txt_nama_kendaraan = (TextView) findViewById(R.id.txt_nama_kendaraan);
        txt_tahun = (TextView) findViewById(R.id.txt_tahun_kendaraan);
        txt_cc = (TextView) findViewById(R.id.txt_cc);
        txt_warna = (TextView) findViewById(R.id.txt_warna);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);

        db = new DBQuery(this);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        email = c.getString(c.getColumnIndex("email"));
        Reload();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_order, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.mn_pesan).setVisible(hidemenu);
        menu.findItem(R.id.mn_call).setVisible(hidemenu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.mn_pesan:
                Intent i = new Intent(this, PesanActivity.class);
                i.putExtra("id", getIntent().getIntExtra("id",0)+"");
                i.putExtra("username",  eo.getUsername_driver());
                i.putExtra("nama",  eo.getNama_driver());
                startActivity(i);
                break;
            case R.id.mn_call:
                String uri = "tel:" + txt_phone.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                if (ActivityCompat.checkSelfPermission(DetailOrderActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {}
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void Reload() {
        try {
            NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(this.NOTIFICATION_SERVICE);
            nMgr.cancel(getIntent().getIntExtra("id",0));
        }catch (Exception e){}

        Map<String, String> dataToSend = new HashMap<String, String>();
        dataToSend.put("api",getResources().getString(R.string.API));
        dataToSend.put("id", getIntent().getIntExtra("id",0)+"");
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        connectPost = new ClientConnectPost(this);
        connectPost.execute(getResources().getString(R.string.server) + "android/checkdetailorder", encodedStr);
        try {
            DecimalFormat df = new DecimalFormat("#.#");
            NumberFormat formatKurensi = NumberFormat.getCurrencyInstance(new Locale("in", "id"));
            JSONObject j = connectPost.get();
            JSONObject ja = j.getJSONObject("data");
            eo = new EntityOrder();
            eo.setId_order(ja.getInt("id_order"));
            eo.setBayar(ja.getInt("bayar"));
            eo.setTgl_order(ja.getString("tgl_order"));
            eo.setJarak(ja.getDouble("jarak"));
            eo.setLat_jemput(ja.getDouble("lat_jemput"));
            eo.setLong_jemput(ja.getDouble("long_jemput"));
            eo.setLat_tujuan(ja.getDouble("lat_tujuan"));
            eo.setLong_tujuan(ja.getDouble("long_tujuan"));
            eo.setNama_driver(ja.getString("nama_driver"));
            eo.setUsername_driver(ja.getString("username_driver"));
            eo.setAlamat_jemput(ja.getString("alamat_jemput"));
            eo.setAlamat_tujuan(ja.getString("alamat_tujuan"));
            eo.setStatus_order(ja.getString("status_order"));
            drv = new LatLng(ja.getDouble("lat_last_position"),ja.getDouble("lng_last_position"));

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = fmt.parse(eo.getTgl_order());
            SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MM-yyyy hh:mm");
            txt_date.setText(fmtOut.format(date));
            txt_harga.setText(formatKurensi.format(eo.getBayar()));
            txt_jarak.setText("Harga ("+df.format(eo.getJarak())+" Km)");
            txt_from.setText(eo.getAlamat_jemput());
            txt_destination.setText(eo.getAlamat_tujuan());
            txt_drivername.setText(eo.getNama_driver());
            txt_email.setText(ja.getString("email"));
            txt_phone.setText(ja.getString("no_telp"));
            txt_address.setText(ja.getString("alamat"));
            Picasso.with(this)
                    .load(getResources().getString(R.string.server)+ja.getString("foto"))
                    .placeholder(R.drawable.ic_action_profile)
                    .error(R.drawable.ic_action_no)
                    .into(img_driver);
            txt_nama_kendaraan.setText(": "+ja.getString("nama_kendaraan"));
            txt_no_kendaraan.setText(": "+ja.getString("no_kendaraan"));
            txt_merek.setText(": "+ja.getString("merek"));
            txt_jenis.setText(": "+ja.getString("jenis"));
            txt_tahun.setText(": "+ja.getString("tahun"));
            txt_warna.setText(": "+ja.getString("warna"));
            txt_cc.setText(": "+ja.getString("isi_silinder")+" cc");
            Picasso.with(this)
                    .load(getResources().getString(R.string.server)+ja.getString("foto_kendaraan"))
                    .placeholder(R.drawable.ic_action_motor)
                    .error(R.drawable.ic_action_no)
                    .into(img_kendaraan);
        } catch (InterruptedException e) {} catch (ExecutionException e) {} catch (JSONException e) {} catch (ParseException e) {}
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        Reload();
        from =new LatLng(eo.getLat_jemput(), eo.getLong_jemput());
        to = new LatLng(eo.getLat_tujuan(),eo.getLong_tujuan());
        GetDistance(from,to,txt_time);
        GetDistance(drv,from,txt_timedriver);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_posisi);
        gMap.addMarker(new MarkerOptions().position(from).icon(descriptor));
        descriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_drop);
        gMap.addMarker(new MarkerOptions().position(to).icon(descriptor));
        descriptor = BitmapDescriptorFactory.fromResource(R.drawable.img_motor);
        mrk_drv = gMap.addMarker(new MarkerOptions().position(drv).icon(descriptor));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(from);
        builder.include(to);
        builder.include(drv);
        LatLngBounds bounds = builder.build();
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
        gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                gMap.moveCamera(cu);
            }
        });
        String url = getDirectionsUrl(from,to);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
        if (eo.getStatus_order().equalsIgnoreCase("Finish")){
            txt_timedriver.setVisibility(TextView.GONE);
            mrk_drv.remove();
        }else{
            setTimer();
        }
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
    }
    private void GetDistance(LatLng from, LatLng to, TextView txt){
        String url = "http://maps.googleapis.com/maps/api/directions/json?origin="+from.latitude+","+from.longitude+"&destination="+to.latitude+","+to.longitude+"&sensor=false&units=metric&mode=driving";
        connectPost = new ClientConnectPost(this);
        connectPost.execute(url, "");
        try {
            JSONObject j = connectPost.get();
            JSONArray array = j.getJSONArray("routes");
            JSONObject routes = array.getJSONObject(0);
            JSONArray legs = routes.getJSONArray("legs");
            JSONObject steps = legs.getJSONObject(0);
            JSONObject time = steps.getJSONObject("duration");
            int t = time.getInt("value")/60;
            txt.setText(t + " Menit");
        } catch (InterruptedException e) {} catch (ExecutionException e) {} catch (JSONException e) {}
    }
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.RED);

            }
            gMap.addPolyline(lineOptions);
        }
    }
    private void setTimer() {
        timer = new Timer();
        TimerTask tt = new CustomTimerTask();
        timer.scheduleAtFixedRate(tt, 5000, 5000);
    }

    private void FinishOrder(){
        timer.cancel();
        timer.purge();
        invalidateOptionsMenu();

        setContentView(R.layout.activity_finish);
        TextView txt_byr = (TextView) findViewById(R.id.txt_cash);
        NumberFormat formatKurensi = NumberFormat.getCurrencyInstance(new Locale("in", "id"));
        txt_byr.setText(formatKurensi.format(eo.getBayar()));
        Button btn_keluar = (Button) findViewById(R.id.btn_keluar);
        btn_keluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public class CustomTimerTask extends TimerTask {
        private Handler mHandler = new Handler();
        @Override
        public void run(){
            new Thread(new Runnable(){
                @Override
                public void run(){
                    mHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            connectPost = new ClientConnectPost(DetailOrderActivity.this);
                            Map<String, String> dataToSend = new HashMap<String, String>();
                            dataToSend.put("api",getResources().getString(R.string.API));
                            dataToSend.put("id", getIntent().getIntExtra("id",0)+"");
                            String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
                            connectPost.execute(getResources().getString(R.string.server)+"android/checkdetailorder",encodedStr);
                            try {
                                JSONObject j = connectPost.get();
                                JSONObject ja = j.getJSONObject("data");
                                if (ja.getString("status_order").equalsIgnoreCase("Pickup")){
                                    txt_timedriver.setText("Sedang mengantar anda.");
                                    if (mrk_drv != null) {
                                        mrk_drv.remove();
                                    }
                                }else if (ja.getString("status_order").equalsIgnoreCase("Finish")){
                                    txt_timedriver.setText("Order Selesai.");
                                    if (mrk_drv != null) {
                                        mrk_drv.remove();
                                    }
                                    FinishOrder();
                                }else {
                                    drv = new LatLng(ja.getDouble("lat_last_position"), ja.getDouble("lng_last_position"));
                                    if (mrk_drv != null) {
                                        mrk_drv.remove();
                                    }
                                    BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.img_motor);
                                    mrk_drv = gMap.addMarker(new MarkerOptions().position(drv).icon(descriptor));
                                    GetDistance(drv, from, txt_timedriver);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).start();
        }
    }
}
