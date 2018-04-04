package com.toastandtesla.antmaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.toastandtesla.antmaps.data.AntImageUrlLoader;
import com.toastandtesla.antmaps.data.AntImageUrl;

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
  }

  @Override
  protected void onStart() {
    super.onStart();
    Bundle parametersBundle;
    if (activityWasStartedFromLauncher()) {
      AntImageUrlLoader.Parameters parameters = createLoaderParameters();
      parametersBundle = parameters.toBundle();
    } else {
      // Read the parameters from the intent passed to this activity.
      parametersBundle = getIntent().getBundleExtra("parameters");
      if (parametersBundle == null) {
        parametersBundle = createLoaderParameters().toBundle();
      }
    }
    startLoadingAntData(parametersBundle);
  }

  private static AntImageUrlLoader.Parameters createLoaderParameters() {
    AntImageUrlLoader.Parameters parameters = new AntImageUrlLoader.Parameters();
    // Your coordinates - no GPS required! (if you're in OIST)
    parameters.latitude = 26;
    parameters.longitude = 128;
    parameters.maxSpecies = 12;
    parameters.radiusKm = 100;
    return parameters;
  }

  /** Returns true if this activity was started by the user tapping the icon in the launcher. */
  private boolean activityWasStartedFromLauncher() {
    return Intent.ACTION_MAIN.equals(getIntent().getAction());
  }

  private void startLoadingAntData(Bundle parametersBundle) {
    Loader<List<AntImageUrl>> loader =
        getSupportLoaderManager().initLoader(0, parametersBundle, loaderCallbacks);
    loader.forceLoad();
  }

  private final class AntDataLoaderCallbacks
      implements LoaderManager.LoaderCallbacks<List<AntImageUrl>> {

    @Override
    public Loader<List<AntImageUrl>> onCreateLoader(int id, Bundle args) {
      NearbyAntsActivity context = NearbyAntsActivity.this;
      AntImageUrlLoader.Parameters parameters = AntImageUrlLoader.Parameters.fromBundle(args);
      return new AntImageUrlLoader(context, RequestQueueSingleton.getInstance(context), parameters);
    }

    @Override
    public void onLoadFinished(Loader<List<AntImageUrl>> loader, List<AntImageUrl> data) {
      antListAdapter.setAntSpecies(data);
    }

    @Override
    public void onLoaderReset(Loader<List<AntImageUrl>> loader) {}
  }
}
