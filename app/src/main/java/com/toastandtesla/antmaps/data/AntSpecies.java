package com.toastandtesla.antmaps.data;

import android.net.Uri;
import android.support.annotation.DrawableRes;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A simple data object with information about an ant species.
 */
@Immutable
public final class AntSpecies {
  public final String name;
  /** Image URI for the species, or null if the image resource should be used intead. */
  @Nullable public final Uri imageUrl;
  @DrawableRes public final int imageResourceId;

  public AntSpecies(String name, Uri imageUrl, int imageResourceId) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.imageResourceId = imageResourceId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AntSpecies that = (AntSpecies) o;

    if (imageResourceId != that.imageResourceId) return false;
    if (!name.equals(that.name)) return false;
    return imageUrl != null ? imageUrl.equals(that.imageUrl) : that.imageUrl == null;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
    result = 31 * result + imageResourceId;
    return result;
  }

  @Override
  public String toString() {
    return "AntSpecies{" +
        "name='" + name + '\'' +
        ", imageUrl=" + imageUrl +
        ", imageResourceId=" + imageResourceId +
        '}';
  }
}
