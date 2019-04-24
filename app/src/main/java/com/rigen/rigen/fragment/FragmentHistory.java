package com.rigen.rigen.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rigen.rigen.R;
import com.rigen.rigen.db.AdapterOrder;
import com.rigen.rigen.db.DBQuery;
import com.rigen.rigen.db.EntityOrder;
import com.rigen.rigen.lib.ClientConnectPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by ILHAM HP on 25/11/2016.
 */

public class FragmentHistory extends Fragment{
    private RecyclerView rv;
    private SwipeRefreshLayout srl;
    private LinearLayout lnotif;
    private ArrayList<EntityOrder> data;
    private AdapterOrder adapterOrder;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rv = (RecyclerView) view.findViewById(R.id.recycleview);
        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        lnotif = (LinearLayout) view.findViewById(R.id.lnotif);
        LoadData();
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadData();
            }
        });
    }
    private void LoadData(){
        refresh(getActivity());
        if (data.size()>0) {
            adapterOrder = new AdapterOrder(getActivity(),data);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            rv.setLayoutManager(mLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
            rv.setAdapter(adapterOrder);
            lnotif.setVisibility(LinearLayout.GONE);
        }else {
            lnotif.setVisibility(LinearLayout.VISIBLE);
        }
        srl.setRefreshing(false);
    }
    private void refresh(Activity activity){
        data = new ArrayList<>();
        Map<String, String> dataToSend = new HashMap<String, String>();

        DBQuery db = new DBQuery(activity);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        String username = c.getString(c.getColumnIndex("email"));
        dataToSend.put("api",getResources().getString(R.string.API));
        dataToSend.put("email",username);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(activity);
        client.execute(getString(R.string.server) + "android/history" , encodedStr);
        try {
            JSONObject j = client.get();
            JSONArray ja = j.getJSONArray("data");
            for (int i=0; i<ja.length();i++){
                JSONObject jo = ja.getJSONObject(i);
                EntityOrder e = new EntityOrder();
                e.setId_order(jo.getInt("id_order"));
                e.setAlamat_tujuan(jo.getString("alamat_tujuan"));
                e.setTgl_order(jo.getString("tgl_order"));
                e.setAlamat_jemput(jo.getString("alamat_jemput"));
                e.setBayar(jo.getInt("bayar"));
                e.setJarak(jo.getDouble("jarak"));
                e.setStatus_order(jo.getString("status_order"));
                data.add(e);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
