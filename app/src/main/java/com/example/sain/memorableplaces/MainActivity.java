package com.example.sain.memorableplaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    ArrayList<String> list = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    ArrayList<LatLng> latLngs = new ArrayList<>();
    ArrayList<String> identifiers = new ArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences("com.example.sain.memorableplaces", Context.MODE_PRIVATE);

        try {
            ArrayList<String> latitudes = (ArrayList<String>) ObjectSerializer.deserialize(
                    sharedPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<>())));
            ArrayList<String> longitudes = (ArrayList<String>) ObjectSerializer.deserialize(
                    sharedPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<>())));

            for (int i = 0; i < Math.min(latitudes.size(), longitudes.size()); i++) {
                latLngs.add(new LatLng(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i))));
            }

            identifiers = (ArrayList<String>) ObjectSerializer.deserialize(
                    sharedPreferences.getString("identifiers", ObjectSerializer.serialize(new ArrayList<>())));
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        updateListView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            ArrayList<LatLng> latLngArrayList = data.getParcelableArrayListExtra("newLocations");
            latLngs.addAll(latLngArrayList);

            ArrayList<String> stringArrayList = data.getStringArrayListExtra("identifiers");
            identifiers.addAll(stringArrayList);

            updateListView();
            updateSharedPreferences();
        }
    }

    private void updateListView() {
        list.clear();
        list.add("Add a new place...");
        list.addAll(identifiers);

        Button button = findViewById(R.id.button);
        if (list.size() == 1) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
        }

        arrayAdapter.notifyDataSetChanged();
    }

    private void updateSharedPreferences() {
        try {
            ArrayList<String> latitudes = new ArrayList<>();
            ArrayList<String> longitudes = new ArrayList<>();
            for (LatLng latLng : latLngs) {
                latitudes.add(String.valueOf(latLng.latitude));
                longitudes.add(String.valueOf(latLng.longitude));
            }

            sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply();
            sharedPreferences.edit().putString("identifiers", ObjectSerializer.serialize(identifiers)).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickDelete(View view) {
        ListView listView = findViewById(R.id.listView);
        ListView listViewRemove = findViewById(R.id.listViewRemove);
        final Button button = findViewById(R.id.button);

        if (listViewRemove.getVisibility() == View.GONE) {
            listView.setVisibility(View.GONE);
            listViewRemove.setVisibility(View.VISIBLE);

            button.setText("Return");

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, identifiers);
            listViewRemove.setAdapter(arrayAdapter);
            listViewRemove.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            listViewRemove.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (((ListView) parent).isItemChecked(position)) {
                        ((ListView) parent).setItemChecked(position, true);
                    } else {
                        ((ListView) parent).setItemChecked(position, false);
                    }

                    Log.i("qwertyuiop", String.valueOf(((ListView) parent).getCheckedItemCount()));

                    if (((ListView) parent).getCheckedItemCount() == 0) {
                        button.setText("Return");
                    } else {
                        button.setText("Delete");
                    }
                }
            });
        } else {
            SparseBooleanArray sparseBooleanArray = listViewRemove.getCheckedItemPositions();
            for (int i = 0; i < sparseBooleanArray.size(); i++) {
                int position = sparseBooleanArray.keyAt(i);
                if (sparseBooleanArray.get(position)) {
                    listViewRemove.setItemChecked(position, false);
                    latLngs.remove(position - i);
                    identifiers.remove(position - i);
                }
            }

            if (button.getText().toString().equals("Delete")) {
                Toast.makeText(this, "Selection Deleted", Toast.LENGTH_SHORT).show();
            }
            button.setText("Delete");

            updateListView();
            updateSharedPreferences();
            listView.setVisibility(View.VISIBLE);
            listViewRemove.setVisibility(View.GONE);
        }
    }
}
