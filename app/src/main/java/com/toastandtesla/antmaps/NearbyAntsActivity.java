package com.toastandtesla.antmaps;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.toolbox.Volley;
import com.google.common.collect.ImmutableList;
import com.squareup.picasso.Picasso;
import com.toastandtesla.antmaps.data.AntDataLoader;
import com.toastandtesla.antmaps.data.AntSpecies;

/** An activity which presents a list of nearby ant species. */
public class NearbyAntsActivity extends AppCompatActivity {

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
    Loader<ImmutableList<AntSpecies>> loader =
        getSupportLoaderManager().initLoader(0, null, loaderCallbacks);
    loader.forceLoad();
  }

  private final class AntDataLoaderCallbacks
      implements LoaderManager.LoaderCallbacks<ImmutableList<AntSpecies>> {
    @Override
    public Loader<ImmutableList<AntSpecies>> onCreateLoader(int id, Bundle args) {
      NearbyAntsActivity context = NearbyAntsActivity.this;
      AntDataLoader.Parameters parameters = new AntDataLoader.Parameters();
      parameters.maxSpecies = 12;
      return new AntDataLoader(context, Volley.newRequestQueue(context), parameters);
    }

    @Override
    public void onLoadFinished(
        Loader<ImmutableList<AntSpecies>> loader, ImmutableList<AntSpecies> data) {
      antListAdapter.setAntSpecies(data);
    }

    @Override
    public void onLoaderReset(Loader<ImmutableList<AntSpecies>> loader) {}
  }
}
