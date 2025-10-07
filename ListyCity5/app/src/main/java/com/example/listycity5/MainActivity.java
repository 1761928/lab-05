package com.example.listycity5;

import static java.security.AccessController.getContext;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.content.Context;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    ListView cityList;
    ArrayList<City> cityDataList;
    CityArrayAdapter cityArrayAdapter;
    private Button addCityButton;
    private Button deleteCityButton;
    private EditText addCityEditText;
    private EditText addProvinceEditText;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    int selectedCity = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        cityDataList = new ArrayList<>();


        addCityEditText = findViewById(R.id.city_name_edit);
        addProvinceEditText = findViewById(R.id.province_name_edit);
        addCityButton = findViewById(R.id.add_city_button);
        deleteCityButton = findViewById(R.id.delete_city_button);
        cityList = findViewById(R.id.city_list);

        cityArrayAdapter = new CityArrayAdapter(this, cityDataList);
        cityList.setAdapter(cityArrayAdapter);

        addCityButton.setOnClickListener(v -> {
            String cityName = addCityEditText.getText().toString();
            String provinceName = addProvinceEditText.getText().toString();
            City city = new City(cityName, provinceName);
            addNewCity(city);
        });

        cityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = position;
            }
        });

        deleteCityButton.setOnClickListener(v -> {
            cityList = findViewById(R.id.city_list);
            if (selectedCity != -1 ) {
                City city = cityDataList.get(selectedCity);
                cityDataList.remove(selectedCity);
                cityArrayAdapter = new CityArrayAdapter(this, cityDataList);
                cityList.setAdapter(cityArrayAdapter);
                selectedCity = -1;
                deleteCity(city);
            }
        });

        citiesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    cityDataList.clear();
                    for (QueryDocumentSnapshot doc: querySnapshots) {
                        String city = doc.getId();
                        String province = doc.getString("Province");
                        Log.d("Firestore", String.format("City(%s, %s) fetched", city, province));
                        cityDataList.add(new City(city, province));
                    }
                    cityArrayAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void addNewCity(City city) {
        // Add the city to the local list
        cityDataList.add(city);
        cityArrayAdapter.notifyDataSetChanged();
        // Add the city to the Firestore collection with the city name as the document Id
        HashMap<String, String> data = new HashMap<>();
        data.put("Province", city.getProvinceName());
        citiesRef.document(city.getCityName()).set(data);
    }

    private void deleteCity(City city) {
        HashMap<String, String> data = new HashMap<>();
        data.remove(city.getCityName());
        citiesRef.document(city.getCityName()).delete();
    }



}