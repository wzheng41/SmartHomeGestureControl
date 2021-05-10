package com.example.smarthomegesturecontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // to Creat a Spinner with given array elemements
        Spinner smartHomeGesturesSpinner = findViewById(R.id.smartHomeGesterList);
        smartHomeGesturesSpinner.setOnItemSelectedListener(this);

        // Get all elements from String.xml resource and set as the drop down.
        ArrayAdapter<String> smartHomeGesturesMyAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.smart_home_gestures));
        smartHomeGesturesMyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        smartHomeGesturesSpinner.setAdapter(smartHomeGesturesMyAdapter);
    }

    /**
     * Description: on select any item from the drop down list, it will drive you to the WatchGesture Activity.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        if (!item.equals("Select a Gesture")) {
            Intent watchGestureActivityIntent = new Intent(MainActivity.this, WatchGestureActivity.class);
            watchGestureActivityIntent.putExtra("gesture_name", item);
            startActivity(watchGestureActivityIntent);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

}