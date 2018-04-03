package com.toastandtesla.antmaps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.toastandtesla.antmaps.data.AntImageUrlLoader;

public class SearchActivity extends AppCompatActivity {

  private EditText editLatitude;
  private EditText editLongitude;
  private EditText editRadius;
  private EditText editMaxSpecies;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);

    // Use the IDs defined in activity_search.xml to locate all the widgets in the activity
    editLatitude = findViewById(R.id.editLatitude);
    editLongitude = findViewById(R.id.editLongitude);
    editRadius = findViewById(R.id.editRadius);
    editMaxSpecies = findViewById(R.id.editMaxSpecies);

    // Define what to do when the button is pressed
    Button button = findViewById(R.id.buttonSubmit);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startNearbyAntsActivity();
      }
    });
  }

  private void startNearbyAntsActivity() {
    // Read all the values from the text boxes in the activity.
    // Float.valueOf and Integer.valueOf are needed to convert form
    AntImageUrlLoader.Parameters parameters = new AntImageUrlLoader.Parameters();
    parameters.latitude = Float.valueOf(editLatitude.getText().toString());
    parameters.longitude = Float.valueOf(editLongitude.getText().toString());
    parameters.radiusKm = Integer.valueOf(editRadius.getText().toString());
    parameters.maxSpecies = Integer.valueOf(editMaxSpecies.getText().toString());
    parameters.fakeResults = true;

    Intent intent = new Intent(this, NearbyAntsActivity.class);
    intent.putExtra("parameters", parameters.toBundle());
    startActivity(intent);
  }
}
