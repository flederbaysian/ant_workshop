package com.toastandtesla.antmaps.data;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

/**
 * Object representing the response from:
 * http://api.antweb.org/v3/taxaImages
 */
final class TaxaImagesJson {
  private TaxaImagesInner[] taxaImages;

  Uri getUrl() {
    if (taxaImages == null
        || taxaImages.length == 0
        || taxaImages[0].specimen == null
        || taxaImages[0].specimen.length == 0
        || taxaImages[0].specimen[0].images == null
        || taxaImages[0].specimen[0].images.length == 0
        || taxaImages[0].specimen[0].images[0].urls == null
        || taxaImages[0].specimen[0].images[0].urls.length == 0) {
      return null;
    }
    String[] urlsArray = taxaImages[0].specimen[0].images[0].urls;
    return Uri.parse(urlsArray[Math.min(2, urlsArray.length)]);
  }

  String getTaxonName() {
    if (taxaImages == null
        || taxaImages.length == 0) {
      return null;
    }
    return taxaImages[0].taxonName;
  }

  private static final class TaxaImagesInner {
    SpecimenJson[] specimen;
    String taxonName;

    private static class SpecimenJson {
      Images[] images;

      private static class Images {
        @SerializedName("urls:")
        private String[] urls;
      }
    }
  }
}
