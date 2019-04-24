package com.rigen.rigen.db;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rigen.rigen.R;
import com.rigen.rigen.lib.ClientConnectPost;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ILHAM HP on 19/12/2016.
 */

public class AdapterPesan extends RecyclerView.Adapter<AdapterPesan.VH> {
    private Context c;
    private ArrayList<EntityPesan> data;
    private String username;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("in","ID"));
    SimpleDateFormat sdft = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("in","ID"));
    public AdapterPesan(Context cx, ArrayList<EntityPesan> dt, String u) {
        c= cx;
        data = dt;
        username = u;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(c);
        View v = null;
        switch (viewType){
            case 0:
                v= inflater.inflate(R.layout.adapter_chat, parent, false);
                break;
            case 1:
                v= inflater.inflate(R.layout.adapter_chat_to, parent, false);
                break;
        }
        VH viewHolder = new VH(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.txt_msg.setText(data.get(position).getPesan());
        try {
            Date d = sdf.parse(data.get(position).getDate());
            holder.txt_date.setText(sdft.format(d));
        } catch (ParseException e) {}
        if (data.get(position).getStatus().equals("D") && !data.get(position).getFrom().equalsIgnoreCase(username)){
            status("R", data.get(position).getId()+"" );
        }else if (data.get(position).getStatus().equals("P") && !data.get(position).getFrom().equalsIgnoreCase(username)){
            status("R", data.get(position).getId()+"" );
        }
    }

    private void status(String status, String id){
        Map<String, String> dataToSend = new HashMap<String, String>();
        dataToSend.put("api",c.getResources().getString(R.string.API));
        dataToSend.put("id_messages", id);
        dataToSend.put("status_messages", status);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(c);
        client.execute(c.getResources().getString(R.string.server) + "android/statuschat", encodedStr);
    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).getFrom().equalsIgnoreCase(username)){
            return 0;
        }else {
            return 1;
        }
    }

    public static class VH extends RecyclerView.ViewHolder {
        private TextView txt_msg;
        private TextView txt_date;

        public VH(View itemView) {
            super(itemView);
            txt_msg = (TextView) itemView.findViewById(R.id.psn);
            txt_date = (TextView) itemView.findViewById(R.id.txt_date);
        }
    }
}
