package com.toastandtesla.antmaps.data;

import static com.google.common.base.Preconditions.checkNotNull;

import android.net.Uri;
import javax.annotation.Nullable;

/** A data object with information about an ant species. */
public final class AntImageUrl {

  // The name of the ant species
  private final String name;
  @Nullable private final Uri imageUrl;

  public AntImageUrl(String name, @Nullable Uri imageUrl) {
    this.name = checkNotNull(name);
    this.imageUrl = imageUrl;
  }

  /** Name of the ant species, like "myrmicinaecataulacus oberthueri". */
  public String getName() {
    return name;
  }

  /** Internet URL for an image of a specimen, or null if no image is available. */
  @Nullable
  public Uri getImageUrl() {
    return imageUrl;
  }
}
