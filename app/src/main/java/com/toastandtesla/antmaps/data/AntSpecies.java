package com.toastandtesla.antmaps.data;

import android.net.Uri;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

/** A data object with information about an ant species. */
@Immutable
public final class AntSpecies {

  private final String name;
  @Nullable private final Uri imageUrl;

  public AntSpecies(String name, @Nullable Uri imageUrl) {
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
