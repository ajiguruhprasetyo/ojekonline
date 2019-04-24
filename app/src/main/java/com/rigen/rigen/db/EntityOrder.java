package com.rigen.rigen.db;

/**
 * Created by ILHAM HP on 17/11/2016.
 */

public class EntityOrder {
    private String tgl_order,alamat_jemput, alamat_tujuan,status_order,username_driver, nama_driver;
    private Double lat_jemput, long_jemput, lat_tujuan, long_tujuan, jarak;
    private int bayar,id_order;

    public int getId_order() {
        return id_order;
    }

    public String getUsername_driver() {
        return username_driver;
    }

    public void setUsername_driver(String username_driver) {
        this.username_driver = username_driver;
    }

    public String getNama_driver() {
        return nama_driver;
    }

    public void setNama_driver(String nama_driver) {
        this.nama_driver = nama_driver;
    }

    public void setId_order(int id_order) {
        this.id_order = id_order;
    }

    public String getTgl_order() {
        return tgl_order;
    }

    public void setTgl_order(String tgl_order) {
        this.tgl_order = tgl_order;
    }

    public String getAlamat_jemput() {
        return alamat_jemput;
    }

    public void setAlamat_jemput(String alamat_jemput) {
        this.alamat_jemput = alamat_jemput;
    }

    public String getAlamat_tujuan() {
        return alamat_tujuan;
    }

    public void setAlamat_tujuan(String alamat_tujuan) {
        this.alamat_tujuan = alamat_tujuan;
    }

    public Double getLat_jemput() {
        return lat_jemput;
    }

    public void setLat_jemput(Double lat_jemput) {
        this.lat_jemput = lat_jemput;
    }

    public Double getLong_jemput() {
        return long_jemput;
    }

    public void setLong_jemput(Double long_jemput) {
        this.long_jemput = long_jemput;
    }

    public Double getLat_tujuan() {
        return lat_tujuan;
    }

    public void setLat_tujuan(Double lat_tujuan) {
        this.lat_tujuan = lat_tujuan;
    }

    public Double getLong_tujuan() {
        return long_tujuan;
    }

    public void setLong_tujuan(Double long_tujuan) {
        this.long_tujuan = long_tujuan;
    }

    public Double getJarak() {
        return jarak;
    }

    public void setJarak(Double jarak) {
        this.jarak = jarak;
    }

    public int getBayar() {
        return bayar;
    }

    public void setBayar(int bayar) {
        this.bayar = bayar;
    }

    public String getStatus_order() {
        return status_order;
    }

    public void setStatus_order(String status_order) {
        this.status_order = status_order;
    }
}
