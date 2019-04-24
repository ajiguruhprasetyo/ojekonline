package com.rigen.rigen.db;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rigen.rigen.DetailOrderActivity;
import com.rigen.rigen.R;
import com.rigen.rigen.WaitingOrder;
import com.rigen.rigen.lib.ClientConnectPost;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ILHAM HP on 25/11/2016.
 */

public class AdapterOrder extends RecyclerView.Adapter<AdapterOrder.VH> {
    private ArrayList<EntityOrder> data;
    private Context context;

    public AdapterOrder(Context c,ArrayList<EntityOrder> d) {
        this.data = d;
        context= c;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_history, parent, false);

        return new VH(itemView);
    }

    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        DecimalFormat df = new DecimalFormat("#.#");
        NumberFormat formatKurensi = NumberFormat.getCurrencyInstance(new Locale("in", "id"));
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = fmt.parse(data.get(position).getTgl_order());
            SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MM-yyyy hh:mm");
            holder.date.setText(fmtOut.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.jarak.setText("Harga ("+df.format(data.get(position).getJarak())+" Km)");
        holder.harga.setText(formatKurensi.format(data.get(position).getBayar()));
        holder.from.setText(data.get(position).getAlamat_jemput());
        holder.to.setText(data.get(position).getAlamat_tujuan());
        if (data.get(position).getStatus_order().equalsIgnoreCase("Finish")){
            holder.btn_cancel.setVisibility(Button.GONE);
        }
        holder.btn_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.get(position).getStatus_order().equalsIgnoreCase("Not Confirm")){
                    Intent intent = new Intent(context, WaitingOrder.class);
                    intent.putExtra("id", data.get(position).getId_order());
                    context.startActivity(intent);
                }else{
                    Intent intent = new Intent(context, DetailOrderActivity.class);
                    intent.putExtra("id", data.get(position).getId_order());
                    context.startActivity(intent);
                }
            }
        });
        holder.btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setMessage("Hapus order "+data.get(position).getAlamat_tujuan());
                alert.setNegativeButton("Tidak",null);
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String, String> dataToSend = new HashMap<String, String>();
                        dataToSend.put("api",context.getString(R.string.API));
                        dataToSend.put("id",data.get(position).getId_order()+"");
                        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
                        ClientConnectPost client = new ClientConnectPost(context);
                        client.execute(context.getString(R.string.server) + "android/cancel" , encodedStr);
                        data.remove(position);
                        notifyDataSetChanged();
                    }
                });
                alert.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class VH extends RecyclerView.ViewHolder {
        public TextView date, jarak,harga, from, to;
        public Button btn_cancel, btn_detail;

        public VH(View view) {
            super(view);
            date = (TextView) view.findViewById(R.id.txt_date);
            jarak = (TextView) view.findViewById(R.id.txt_jarak);
            harga = (TextView) view.findViewById(R.id.txt_harga);
            from = (TextView) view.findViewById(R.id.txt_from);
            to = (TextView) view.findViewById(R.id.txt_to);
            btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
            btn_detail = (Button) view.findViewById(R.id.btn_detail);
        }
    }

}
