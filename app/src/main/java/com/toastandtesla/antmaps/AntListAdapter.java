package com.toastandtesla.antmaps;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.squareup.picasso.Picasso;
import com.toastandtesla.antmaps.data.AntSpecies;

/**
 * A RecyclerView adapter that can display a list of ant species. Each will have a picture and
 * a label.
 */
final class AntListAdapter extends RecyclerView.Adapter<AntListAdapter.AntViewHolder> {
  static final class AntViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView nameView;

    AntViewHolder(View itemView) {
      super(itemView);
    }
  }

  private ImmutableList<AntSpecies> antSpecies = ImmutableList.of();
  private final Picasso picasso;

  AntListAdapter(Picasso picasso) {
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
