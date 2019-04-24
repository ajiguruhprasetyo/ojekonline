package com.rigen.rigen.lib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rigen.rigen.R;
import com.rigen.rigen.lib.adapters.PlacesAutoCompleteAdapter;
import com.rigen.rigen.lib.listeners.RecyclerItemClickListener;


public class PlacesAutoCompleteActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    protected GoogleApiClient mGoogleApiClient;

    private static LatLngBounds BOUNDS = new LatLngBounds(
            new LatLng(-8.200867, 109.943382),new LatLng(-7.494885, 110.896385));

    private EditText mAutocompleteView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    ImageView delete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        setContentView(R.layout.activity_search);

        mAutocompleteView = (EditText)findViewById(R.id.autocomplete_places);

        delete=(ImageView)findViewById(R.id.cross);

        mAutoCompleteAdapter =  new PlacesAutoCompleteAdapter(this, R.layout.searchview_adapter,
                mGoogleApiClient, BOUNDS, null);

        mRecyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        mLinearLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAutoCompleteAdapter);
        delete.setOnClickListener(this);
        mAutocompleteView.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.toString().equals("") && mGoogleApiClient.isConnected()) {
                    mAutoCompleteAdapter.getFilter().filter(s.toString());
                }else if(!mGoogleApiClient.isConnected()){}
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });

        mAutocompleteView.setText(getIntent().getStringExtra("address"));
        if (getIntent().getStringExtra("icon").equals("location")){
            mAutocompleteView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_posisi,0, 0,  0);
        }else {
            mAutocompleteView.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_drop,0, 0, 0);
        }
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final PlacesAutoCompleteAdapter.PlaceAutocomplete item = mAutoCompleteAdapter.getItem(position);
                        final String placeId = String.valueOf(item.placeId);
                        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                                .getPlaceById(mGoogleApiClient, placeId);
                        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if(places.getCount()==1){
                                    Intent intent = new Intent();
                                    intent.putExtra("address", String.valueOf(places.get(0).getAddress()));
                                    intent.putExtra("place", String.valueOf(places.get(0).getName()));
                                    intent.putExtra("lat",places.get(0).getLatLng().latitude);
                                    intent.putExtra("lng", places.get(0).getLatLng().longitude);
                                    setResult(Activity.RESULT_OK, intent);
                                    finish();
                                }
                            }
                        });
                    }
                })
        );
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {}
    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onClick(View v) {
        if(v==delete){
            mAutocompleteView.setText("");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()){
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
