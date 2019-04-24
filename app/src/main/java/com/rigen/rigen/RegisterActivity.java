package com.rigen.rigen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * Created by ILHAM HP on 25/11/2016.
 */

public class RegisterActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int REQUEST_READ_CONTACTS = 0;

    private LinearLayout layoutRegister, layoutLoading;
    private AutoCompleteTextView et_email;
    private EditText et_password, et_nama,et_alamat,et_telp;
    private DBQuery db;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DBQuery(this);

        layoutLoading = (LinearLayout) findViewById(R.id.layoutLoading);
        layoutRegister = (LinearLayout) findViewById(R.id.layoutRegister);
        et_password = (EditText) findViewById(R.id.password);
        et_nama     = (EditText) findViewById(R.id.nama);
        et_alamat   = (EditText) findViewById(R.id.address);
        et_telp     = (EditText) findViewById(R.id.phone);
        et_email = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        Button btnRegister  = (Button) findViewById(R.id.btn_register);
        Button btnLogin     = (Button) findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUser();
            }
        });
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
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
    private void RegisterUser(){
        boolean balamat = CheckRequired(et_alamat,10,100);
        boolean btelp = CheckRequired(et_telp,10,12);
        boolean bnama = CheckRequired(et_nama,5,30);
        boolean bpass= false;
        if(et_password.getText().toString().isEmpty() || et_password.getText().toString().length()<4 ){
            et_password.setError("Password minimal 4 hingga 20 karakter.");bpass=false;et_password.requestFocus();
        }else {bpass =true;}
        boolean bemail = false;
        if (et_email.getText().toString().contains("@")){
            bemail = true;
        }else {et_email.setError("Format email salah!");bemail=false;et_password.requestFocus();}
        if (balamat & btelp & bnama & bpass & bemail){
            EntityAccount e = new EntityAccount();
            e.setEmail(et_email.getText().toString());
            e.setNama(et_nama.getText().toString());
            e.setNo_telp(et_telp.getText().toString());
            e.setAlamat(et_alamat.getText().toString());
            e.setPassword(et_password.getText().toString());
            new RegiterTask(e).execute((Void) null);
        }
    }
    class RegiterTask extends AsyncTask<Void,Void, Boolean>{
        private EntityAccount e;
        private String msg;
        public  RegiterTask(EntityAccount entityAccount){
            e = entityAccount;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            layoutLoading.setVisibility(LinearLayout.VISIBLE);
            layoutRegister.setVisibility(LinearLayout.GONE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject response = new JSONObject();
            BufferedReader reader = null;
            try {
                URL url = new URL(getResources().getString(R.string.server)+"android/register");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                Map<String, String> dataToSend = new HashMap<String, String>();
                dataToSend.put("api", getResources().getString(R.string.API));
                dataToSend.put("email", e.getEmail());
                dataToSend.put("password", e.getPassword());
                dataToSend.put("nama", e.getNama());
                dataToSend.put("telp", e.getNo_telp());
                dataToSend.put("alamat", e.getAlamat());
                writer.write(getEncodedData(dataToSend));
                writer.flush();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String responseString = readStream(con.getInputStream());
                response = new JSONObject(responseString);
                int res = response.getInt("r");
                msg =response.getString("msg");
                if (res==1){
                    e.setPassword(ConnectionUtils.md5(e.getPassword()));
                    db.open();
                    db.clear();
                    db.insert(e);
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
        protected void onPostExecute(Boolean registeresult) {
            if (registeresult){
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }else {
                layoutLoading.setVisibility(LinearLayout.GONE);
                layoutRegister.setVisibility(LinearLayout.VISIBLE);
                Snackbar.make(layoutRegister,msg,Snackbar.LENGTH_LONG).show();
            }
            super.onPostExecute(registeresult);
        }
        @NonNull
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
        @NonNull
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


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (Build.VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(et_email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<>();
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        et_email.setAdapter(adapter);
    }
    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}
