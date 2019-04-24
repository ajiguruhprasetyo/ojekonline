package com.rigen.rigen.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.rigen.rigen.R;
import com.rigen.rigen.lib.ClientConnectPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by ILHAM HP on 25/11/2016.
 */

public class FragmentHelp extends Fragment{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help,container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final WebView webView = (WebView) view.findViewById(R.id.webView);
        final SwipeRefreshLayout sfr = (SwipeRefreshLayout) view.findViewById(R.id.sr);
        getActivity().setTitle("Bantuan");
        sfr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ClientConnectPost c = new ClientConnectPost(getActivity());
                c.execute(getString(R.string.server)+"android/help", "");
                try {
                    JSONObject j = c.get();
                    String d = j.getString("value");
                    webView.loadData(d, "text/html", "charset=UTF-8");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sfr.setRefreshing(false);
            }
        });
        ClientConnectPost c = new ClientConnectPost(getActivity());
        Map<String, String> dataToSend = new HashMap<String, String>();
        dataToSend.put("api", getResources().getString(R.string.API));
        String str = c.getEncodedData(dataToSend);
        c.execute(getString(R.string.server)+"android/help", str);
        try {
            JSONObject j = c.get();
            String d = j.getString("value");
            webView.loadData(d, "text/html", "charset=UTF-8");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
