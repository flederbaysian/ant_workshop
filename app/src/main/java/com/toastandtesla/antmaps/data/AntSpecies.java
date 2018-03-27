package com.toastandtesla.antmaps.data;

import android.net.Uri;

import javax.annotation.concurrent.Immutable;

/**
 * A simple data object with information about an ant species.
 */
@Immutable
public final class AntSpecies {
  public final String name;
  public final Uri imageUrl;

  public AntSpecies(String name, Uri imageUrl) {
    this.name = name;
    this.imageUrl = imageUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AntSpecies that = (AntSpecies) o;

    if (!name.equals(that.name)) return false;
    return imageUrl.equals(that.imageUrl);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + imageUrl.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "AntSpecies{" +
        "name='" + name + '\'' +
        ", imageUrls=" + imageUrl +
        '}';
  }
}
