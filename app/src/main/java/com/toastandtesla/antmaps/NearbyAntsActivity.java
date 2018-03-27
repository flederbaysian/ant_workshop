package com.toastandtesla.antmaps;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.Volley;
import com.google.common.collect.ImmutableList;
import com.squareup.picasso.Picasso;

/**
 * An activity which presents a list of nearby ant species.
 */
public class NearbyAntsActivity extends AppCompatActivity {

  private final LoaderManager.LoaderCallbacks<ImmutableList<AntSpecies>> loaderCallbacks =
      new MyLoaderCallbacks();
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

  private class MyLoaderCallbacks
      implements LoaderManager.LoaderCallbacks<ImmutableList<AntSpecies>> {
    @Override
    public Loader<ImmutableList<AntSpecies>> onCreateLoader(int id, Bundle args) {
      NearbyAntsActivity context = NearbyAntsActivity.this;
      return new AntDataLoader(context, Volley.newRequestQueue(context));
    }

    @Override
    public void onLoadFinished(
        Loader<ImmutableList<AntSpecies>> loader, ImmutableList<AntSpecies> data) {
      antListAdapter.setAntSpecies(data);
    }

    @Override
    public void onLoaderReset(Loader<ImmutableList<AntSpecies>> loader) {
    }
  }

  private static final class AntListAdapter extends RecyclerView.Adapter<AntViewHolder> {
    ImmutableList<AntSpecies> antSpecies = ImmutableList.of();
    private final Picasso picasso;

    private AntListAdapter(Picasso picasso) {
      this.picasso = picasso;
    }

    @Override
    public AntViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.ant_species_view, parent, false);
      AntViewHolder holder = new AntViewHolder(view);
      holder.imageView = view.findViewById(R.id.imageView);
      holder.nameView = view.findViewById(R.id.nameView);
      return holder;
    }

    @Override
    public void onBindViewHolder(AntViewHolder holder, int position) {
      AntSpecies species = antSpecies.get(position);
      holder.nameView.setText(species.name);
      picasso.load(species.imageUrl)
          .into(holder.imageView);
    }

    public void onViewRecycled(AntViewHolder holder) {
      picasso.cancelRequest(holder.imageView);
      holder.imageView.setImageDrawable(null);
    }

    @Override
    public int getItemCount() {
      return antSpecies.size();
    }

    void setAntSpecies(ImmutableList<AntSpecies> species) {
      if (!antSpecies.equals(species)) {
        this.antSpecies = species;
        notifyDataSetChanged();
      }
    }
  }

  private static final class AntViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView nameView;

    AntViewHolder(View itemView) {
      super(itemView);
    }
  }
}
