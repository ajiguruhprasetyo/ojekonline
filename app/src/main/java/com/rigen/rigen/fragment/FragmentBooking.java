package com.rigen.rigen.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
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
import com.rigen.rigen.R;
import com.rigen.rigen.WaitingOrder;
import com.rigen.rigen.db.DBQuery;
import com.rigen.rigen.lib.ClientConnectPost;
import com.rigen.rigen.lib.Direction;
import com.rigen.rigen.lib.DirectionsJSONParser;
import com.rigen.rigen.lib.PlacesAutoCompleteActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FragmentBooking extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private GoogleMap googleMap;
    private Location mLastLocation;
    private TextView txt_from, txt_destination, txt_jarak, txt_durasi, txt_harga, txt_title;
    private ImageView img_pick, img_cfrom, img_cdest;
    private Button btn_order;
    private LinearLayout ltop, lbottom;

    private Activity activity;
    private GoogleApiClient mGoogleApiClient;

    private Double flat, flng,dlat=0.0, dlng=0.0, jarak;
    private int harga, total, fee_persen, fee;
    private String alamat_jemput, alamat_tujuan;
    private Marker markerfrom, markerdestination;
    private Boolean fixlocation=false;
    private boolean chosefrom = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = getActivity();
        return inflater.inflate(R.layout.fragmen_booking, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ltop.setVisibility(LinearLayout.VISIBLE);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 2526) {
                txt_from.setText(data.getStringExtra("place"));
                alamat_jemput = data.getStringExtra("address");
                flat = data.getDoubleExtra("lat", 0);
                flng = data.getDoubleExtra("lng", 0);
                img_pick.setVisibility(ImageView.GONE);
                txt_title.setVisibility(TextView.GONE);
                googleMap.clear();
                ShowMarker();
                getDriver();
            } else if (requestCode == 2527) {
                txt_destination.setText(data.getStringExtra("place"));
                alamat_tujuan = data.getStringExtra("address");
                dlat = data.getDoubleExtra("lat", 0);
                dlng = data.getDoubleExtra("lng", 0);
                googleMap.clear();
                img_pick.setVisibility(ImageView.GONE);
                txt_title.setVisibility(TextView.GONE);
                ShowMarker();
                getDriver();
            }
        }
        check();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
        ltop = (LinearLayout) view.findViewById(R.id.ltop);
        lbottom = (LinearLayout) view.findViewById(R.id.lbottom);
        txt_from = (TextView) view.findViewById(R.id.txt_from);
        txt_destination = (TextView) view.findViewById(R.id.txt_destination);
        txt_durasi = (TextView) view.findViewById(R.id.txt_durasi);
        txt_jarak = (TextView) view.findViewById(R.id.txt_jarak);
        txt_harga = (TextView) view.findViewById(R.id.txt_harga);
        txt_title = (TextView) view.findViewById(R.id.txt_title);
        btn_order = (Button) view.findViewById(R.id.btn_order);
        img_pick = (ImageView) view.findViewById(R.id.img_pick);
        img_cfrom = (ImageView) view.findViewById(R.id.img_change_from);
        img_cdest = (ImageView) view.findViewById(R.id.img_change_destination);
        lbottom.setVisibility(LinearLayout.GONE);

        img_cdest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosefrom = false;
                clearDest();
            }
        });
        img_cfrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosefrom = true;
                clearJemput();
            }
        });
        img_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chosefrom) {
                    setJemput();
                }else{
                    setDest();
                }
            }
        });
        txt_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chosefrom) {
                    setJemput();
                }else{
                    setDest();
                }
            }
        });
        txt_destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ltop.setVisibility(LinearLayout.GONE);
                lbottom.setVisibility(LinearLayout.GONE);
                Intent i = new Intent(activity, PlacesAutoCompleteActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("address", alamat_tujuan);
                i.putExtra("icon", "destination");
                startActivityForResult(i, 2527);
            }
        });
        txt_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ltop.setVisibility(LinearLayout.GONE);
                lbottom.setVisibility(LinearLayout.GONE);
                Intent i = new Intent(activity, PlacesAutoCompleteActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("address", alamat_tujuan);
                i.putExtra("icon", "location");
                startActivityForResult(i, 2526);
            }
        });
        getHarga();
        btn_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookingNow();
            }
        });
    }
    private void setJemput(){
        LatLng now = googleMap.getCameraPosition().target;
        flat = now.latitude;
        flng = now.longitude;
        alamat_jemput = getAddress(flat,flng);
        txt_from.setText(alamat_jemput);
        img_pick.setVisibility(ImageView.GONE);
        txt_title.setVisibility(TextView.GONE);
        googleMap.clear();
        ShowMarker();
        getDriver();
        check();
        if (dlng ==0.0 && dlat ==0.0){
            chosefrom = false;
            clearDest();
        }
    }
    private void clearJemput(){
        flat = 0.0;
        flng = 0.0;
        alamat_jemput = "";
        txt_from.setText("Wait . . .");
        img_pick.setVisibility(ImageView.VISIBLE);
        img_pick.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_posisi));
        txt_title.setText("Lokasi Jemput");
        txt_title.setVisibility(TextView.VISIBLE);
        try {
            if (markerfrom.isVisible()){
                markerfrom.remove();
            }
        }catch (Exception e){}
        /*googleMap.clear();
        ShowMarker();*/
    }
    private void setDest(){
        LatLng now = googleMap.getCameraPosition().target;
        dlat = now.latitude;
        dlng = now.longitude;
        alamat_tujuan = getAddress(dlat,dlng);
        txt_destination.setText(alamat_tujuan);
        img_pick.setVisibility(ImageView.GONE);
        txt_title.setVisibility(TextView.GONE);
        googleMap.clear();
        ShowMarker();
        getDriver();
        check();
    }
    private void clearDest(){
        dlat = 0.0;
        dlng = 0.0;
        alamat_tujuan = "";
        txt_destination.setText("Wait . . .");
        img_pick.setVisibility(ImageView.VISIBLE);
        img_pick.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(),R.drawable.ic_drop));
        txt_title.setText("Lokasi Tujuan");
        txt_title.setVisibility(TextView.VISIBLE);
        try {
            if (markerdestination.isVisible()){
                markerdestination.remove();
            }
        }catch (Exception e){}
        /*ShowMarker()*/;
    }

    private void ShowMarker(){
        if (flng !=0.0 && flat !=0.0){
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_posisi);
            markerfrom = googleMap.addMarker(new MarkerOptions().position(new LatLng(flat, flng)).icon(descriptor));
        }
        if (dlng !=0.0 && dlat !=0.0){
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_drop);
            markerdestination = googleMap.addMarker(new MarkerOptions().position(new LatLng(dlat, dlng)).icon(descriptor));
        }
    }
    private void check(){
        if (flat != 0.0 && flng != 0.0 && dlat != 0.0 && dlng !=0.0){
            fixlocation = true;
            GetDistance(new LatLng(flat,flng), new LatLng(dlat,dlng));
            lbottom.setVisibility(View.VISIBLE);
            String url = getDirectionsUrl(new LatLng(flat, flng), new LatLng(dlat, dlng));
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
            getDriver();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(flat,flng));
            builder.include(new LatLng(dlat,dlng));
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
            googleMap.moveCamera(cu);
        }else {
            fixlocation = false;
            lbottom.setVisibility(View.GONE);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap gm) {
        googleMap = gm;
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (!fixlocation) {
                    try {
                        if (chosefrom) {
                            clearJemput();
                        }else {
                            clearDest();
                        }
                    }catch (Exception e){}
                }
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            flat = mLastLocation.getLatitude();
            flng = mLastLocation.getLongitude();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(flat, flng), 16));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void getHarga(){
        ClientConnectPost client = new ClientConnectPost(activity);
        Map<String, String> dataToSend = new HashMap<String, String>();
        dataToSend.put("id", "1");
        dataToSend.put("api", getResources().getString(R.string.API));
        String str = client.getEncodedData(dataToSend);
        client.execute(getResources().getString(R.string.server)+"android/harga", str);
        try {
            JSONObject j = client.get();
            try {
                harga = j.getInt("harga");
                fee_persen = j.getInt("fee");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    private void getDriver(){
        Map<String, String> dataToSend = new HashMap<String, String>();
        dataToSend.put("api", getResources().getString(R.string.API));
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(activity);
        client.execute(getString(R.string.server) + "android/finddriver" , encodedStr);
        try {
            JSONObject j = client.get();
            JSONArray ja = j.getJSONArray("data");
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.img_motor);
            for (int i=0; i<ja.length();i++){
                JSONObject jo = ja.getJSONObject(i);
                LatLng d = new LatLng(jo.getDouble("lat_last_position"),jo.getDouble("lng_last_position"));
                googleMap.addMarker(new MarkerOptions().position(d).icon(descriptor));
            }
        }catch (Exception e){
        }
    }
    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getSubLocality()).append(", ");
                result.append(address.getLocality()).append(", ").append(address.getSubAdminArea());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        return result.toString();
    }

    public void BookingNow(){
        DBQuery db = new DBQuery(getActivity());
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        String email = c.getString(c.getColumnIndex("email"));
        Map<String, String> dataToSend = new HashMap<String, String>();
        dataToSend.put("api", getResources().getString(R.string.API));
        dataToSend.put("lat_jemput", flat + "");
        dataToSend.put("long_jemput", flng + "");
        dataToSend.put("lat_tujuan", dlat + "");
        dataToSend.put("long_tujuan", dlng + "");
        dataToSend.put("alamat_jemput", alamat_jemput + "");
        dataToSend.put("alamat_tujuan", alamat_tujuan + "");
        dataToSend.put("jarak", jarak + "");
        dataToSend.put("fee_driver", fee + "");
        dataToSend.put("id_service", "1");
        dataToSend.put("bayar", total + "");
        dataToSend.put("email", email);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        final ClientConnectPost client = new ClientConnectPost(getActivity());
        client.execute(getString(R.string.server) + "android/booking", encodedStr);
        try {
            JSONObject j = client.get();
            JSONArray ja = j.getJSONArray("data");
            j = ja.getJSONObject(0);
            try {
                int id = j.getInt("id_order");
                Intent i = new Intent(getActivity(), WaitingOrder.class);
                i.putExtra("id",id);
                getActivity().startActivity(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        c.close();
        db.close();
    }
    private void GetDistance(LatLng from, LatLng to){
        String url = "http://maps.googleapis.com/maps/api/directions/json?origin="+from.latitude+","+from.longitude+"&destination="+to.latitude+","+to.longitude+"&sensor=false&units=metric&mode=driving";
        ClientConnectPost client = new ClientConnectPost(activity);
        client.execute(url, "");
        try {
            JSONObject j = client.get();
            JSONArray array = j.getJSONArray("routes");
            JSONObject routes = array.getJSONObject(0);
            JSONArray legs = routes.getJSONArray("legs");
            JSONObject steps = legs.getJSONObject(0);
            JSONObject distance = steps.getJSONObject("distance");
            JSONObject time = steps.getJSONObject("duration");
            int t = time.getInt("value")/60;
            txt_durasi.setText(t + " Menit");
            getKM(distance.getDouble("value"));

        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (JSONException e) {
        }
    }
    private void getKM(Double d){
        Double km = d/1000;
        jarak = km;
        DecimalFormat df = new DecimalFormat("#.#");
        txt_jarak.setText(df.format(km)+" Km");
        Double h = km*harga;
        total =  h.intValue();
        /*if (total < 4000){
            btn_order.setEnabled(false);
            Snackbar.make(lbottom,"Minimum order Rp4.000,-", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }else{btn_order.setEnabled(true);}*/
        if (total<4000){total =4000;}
        fee =  (total *fee_persen/100);
        NumberFormat formatKurensi = NumberFormat.getCurrencyInstance(new Locale("in","id"));
        txt_harga.setText(formatKurensi.format(total));
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

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
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
            googleMap.addPolyline(lineOptions);
        }
    }
}
