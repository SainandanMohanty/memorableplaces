package com.example.sain.memorableplaces;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> list = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    ArrayList<LatLng> latLngs = new ArrayList<>();
    HashMap<LatLng, Date> timestamps = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list.add("Add a new place...");

        ListView listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("viewPosition", position);
                if (position != 0) {
                    intent.putExtra("LatLng", latLngs.get(position - 1));
                }
                startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            ArrayList<LatLng> arrayList = data.getParcelableArrayListExtra("newLocations");
            latLngs.addAll(arrayList);

            @SuppressWarnings("unchecked")
            HashMap<LatLng, Date> hashMap = (HashMap<LatLng, Date>) data.getSerializableExtra("timestamps");
            timestamps.putAll(hashMap);

            updateListView();
        }
    }

    private void updateListView() {
        for (LatLng latLng : latLngs) {
            String title = null;

            if (timestamps.containsKey(latLng)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
                title = simpleDateFormat.format(timestamps.get(latLng));
            } else {
                Geocoder geocoder = new Geocoder(this);
                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList.size() > 0) {
                        title = addressList.get(0).getAddressLine(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!list.contains(title)) {
                list.add(title);
            }
        }

        arrayAdapter.notifyDataSetChanged();
    }
}
