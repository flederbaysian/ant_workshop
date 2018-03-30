package com.toastandtesla.antmaps;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.toastandtesla.antmaps.data.AntDataLoader;
import com.toastandtesla.antmaps.data.AntSpecies;

import java.util.List;

/** An activity which presents a list of nearby ant species (as long as you're in OIST). */
public final class NearbyAntsActivity extends AppCompatActivity {

  private final AntDataLoaderCallbacks loaderCallbacks = new AntDataLoaderCallbacks();
  private AntListAdapter antListAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_nearby_ants);
    RecyclerView antListView = findViewById(R.id.ant_list);

    antListAdapter = new AntListAdapter(Picasso.with(this));
    antListView.setAdapter(antListAdapter);
    antListView.setLayoutManager(new LinearLayoutManager(this));
    startLoadingAntData();
  }

  private void startLoadingAntData() {
    Loader<List<AntSpecies>> loader =
        getSupportLoaderManager().initLoader(0, null, loaderCallbacks);
    loader.forceLoad();
  }

  private final class AntDataLoaderCallbacks
      implements LoaderManager.LoaderCallbacks<List<AntSpecies>> {
    @Override
    public Loader<List<AntSpecies>> onCreateLoader(int id, Bundle args) {
      NearbyAntsActivity context = NearbyAntsActivity.this;
      AntDataLoader.Parameters parameters = new AntDataLoader.Parameters();

      // Your coordinates - no GPS required! (if you're in OIST)
      parameters.latitude = 26;
      parameters.longitude = 128;
      parameters.maxSpecies = 12;
      parameters.radiusKm = 100;
      return new AntDataLoader(context, Volley.newRequestQueue(context), parameters);
    }

    @Override
    public void onLoadFinished(Loader<List<AntSpecies>> loader, List<AntSpecies> data) {
      antListAdapter.setAntSpecies(data);
    }

    @Override
    public void onLoaderReset(Loader<List<AntSpecies>> loader) {}
  }
}
