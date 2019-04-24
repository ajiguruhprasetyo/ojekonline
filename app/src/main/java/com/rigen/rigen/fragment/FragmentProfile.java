package com.rigen.rigen.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.rigen.rigen.LoginActivity;
import com.rigen.rigen.R;
import com.rigen.rigen.db.DBQuery;
import com.rigen.rigen.db.EntityAccount;

/**
 * Created by ILHAM HP on 25/11/2016.
 */

public class FragmentProfile extends Fragment {
    DBQuery db;
    EntityAccount e;
    private EditText et_email, et_nama, et_password, et_phone, et_alamat;
    private Button btn_simpan, btn_logout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        et_email = (EditText) view.findViewById(R.id.email);
        et_nama = (EditText) view.findViewById(R.id.nama);
        et_alamat = (EditText) view.findViewById(R.id.alamat);
        et_phone = (EditText) view.findViewById(R.id.phone);
        et_password = (EditText) view.findViewById(R.id.password);
        btn_simpan = (Button) view.findViewById(R.id.btn_simpan);
        btn_logout = (Button) view.findViewById(R.id.btn_logout);
        db = new DBQuery(getActivity());
        loadProfile();
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.open();
                db.clear();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });
        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Simpan();
            }
        });
    }
    private void loadProfile(){
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        et_email.setText(c.getString(c.getColumnIndex("email")));
        et_nama.setText(c.getString(c.getColumnIndex("nama")));
        et_alamat.setText(c.getString(c.getColumnIndex("alamat")));
        et_phone.setText(c.getString(c.getColumnIndex("no_telp")));
        c.close();
        db.close();
    }
    private boolean CheckRequired(EditText et, int min, int max){
        if (et.getText().length()<min){
            et.setError("Minimal "+min+" sampai "+max+" karakter.");
            et.requestFocus();
            return false;
        }else{
            return true;
        }
    }
    private void Simpan(){
        boolean bname = CheckRequired(et_nama,5,50);
        boolean balamat = CheckRequired(et_alamat,10,100);
        boolean bphone  = CheckRequired(et_phone,10,15);
        if (bname && balamat && bphone){

        }
    }
}
