package com.rigen.rigen.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

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

public class FragmentCS extends Fragment{
    private  EditText msg;
    private Button btn_send, btn_call;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cs,container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        msg = (EditText) view.findViewById(R.id.txt_messages);
        btn_send = (Button) view.findViewById(R.id.btn_kirim);
        btn_call = (Button) view.findViewById(R.id.btn_call);
        btn_send.setEnabled(false);
        msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (msg.getText().length() > 20){
                    btn_send.setEnabled(true);
                }else{
                    btn_send.setEnabled(false);
                    msg.setError("Minimal 20 karakter.");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uri = "tel:0274411623";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(intent);
            }
        });
    }
}
