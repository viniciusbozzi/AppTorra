package com.viniciusbozzi.project.activity;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Set;

public class ListaDispositivos extends ListActivity {

    public BluetoothAdapter bluetoothAdapterPareados;
    public static String enderecoMAC;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> adapterBluetooth = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);

        bluetoothAdapterPareados = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> dispPareados = bluetoothAdapterPareados.getBondedDevices();

        if(dispPareados.size() > 0){
            for(BluetoothDevice disp: dispPareados){
                String nome =  disp.getName();
                String mac = disp.getAddress();
                adapterBluetooth.add(nome + "\n"+ mac);
            }
        }
        setListAdapter(adapterBluetooth);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String info = ((TextView) v).getText().toString();
        String enderecoMac = info.substring(info.length() -17);

        Intent retorna = new Intent();
        retorna.putExtra(enderecoMAC,enderecoMac);
        setResult(RESULT_OK,retorna);
        finish();

    }
}
