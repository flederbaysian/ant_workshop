package com.toastandtesla.antmaps;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.squareup.picasso.Picasso;
import com.toastandtesla.antmaps.data.AntImageUrl;

import java.util.List;

/**
 * A RecyclerView adapter that can display a list of ant species. Each will have a picture and a
 * label.
 */
final class AntListAdapter extends RecyclerView.Adapter<AntListAdapter.AntViewHolder> {
  static final class AntViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView nameView;

    AntViewHolder(View itemView) {
      super(itemView);
    }
  }

  private ImmutableList<AntImageUrl> antSpecies = ImmutableList.of();
  private final Picasso picasso;

  AntListAdapter(Picasso picasso) {
    this.picasso = picasso;
  }

  @Override
  public AntViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.ant_species_view, parent, false);
    AntViewHolder holder = new AntViewHolder(view);
    holder.imageView = view.findViewById(R.id.imageView);
    holder.nameView = view.findViewById(R.id.nameView);
    return holder;
  }

  @Override
  public void onBindViewHolder(AntViewHolder holder, int position) {
    AntImageUrl species = antSpecies.get(position);
    // EXERCISE: Capitalize the species name
    String name = species.getName();

    holder.nameView.setText(name);

    // Use Picasso to download the image of the ant
    if (species.getImageUrl() != null) {
      picasso.load(species.getImageUrl())
          .placeholder(R.drawable.placeholder_drawable)
          .into(holder.imageView);
    } else {
      // Load a replacement image from resources
      picasso.load(R.mipmap.ant).into(holder.imageView);
    }
  }

  public void onViewRecycled(AntViewHolder holder) {
    picasso.cancelRequest(holder.imageView);
    holder.imageView.setImageDrawable(null);
  }

  @Override
  public int getItemCount() {
    return antSpecies.size();
  }

  void setAntSpecies(List<AntImageUrl> species) {
    if (!antSpecies.equals(species)) {
      this.antSpecies = ImmutableList.copyOf(species);
      notifyDataSetChanged();
    }
  }
}
