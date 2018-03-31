package com.toastandtesla.antmaps.data;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A loader which uses the AntWeb API to download a list of nearby ant species, each with
 * a list of photos.
 */
public final class AntImageUrlLoader extends AsyncTaskLoader<List<AntImageUrl>> {

  /** An object with parameters that control the loader. */
  public static final class Parameters {
    public int maxSpecies = 10;
    public float latitude;
    public float longitude;
    public int radiusKm = 2;
    /**
     * AntWeb API's "photo type":
     *   h -> head shot
     *   d -> dorsal shot
     *   p -> profile shot
     *   l -> label
     */
    public String photoType = "h";
    public boolean fakeResults = false;

    public static Parameters copy(Parameters parameters) {
      Parameters result = new Parameters();
      result.maxSpecies = parameters.maxSpecies;
      result.latitude = parameters.latitude;
      result.longitude = parameters.longitude;
      result.radiusKm = parameters.radiusKm;
      result.photoType = parameters.photoType;
      result.fakeResults = parameters.fakeResults;
      return result;
    }

    /** Converts a Parameters object to a Bundle. */
    public Bundle toBundle() {
      Bundle bundle = new Bundle();
      bundle.putInt("maxSpecies", maxSpecies);
      bundle.putFloat("latitude", latitude);
      bundle.putFloat("longitude", longitude);
      bundle.putInt("radiusKm", radiusKm);
      bundle.putBoolean("fakeResults", fakeResults);
      return bundle;
    }

    /** Parse a Bundle into a Parameters object. */
    public static Parameters fromBundle(Bundle bundle) {
      Parameters result = new Parameters();
      result.maxSpecies = bundle.getInt("maxSpecies", result.maxSpecies);
      result.latitude = bundle.getFloat("latitude", result.latitude);
      result.longitude = bundle.getFloat("longitude", result.longitude);
      result.radiusKm = bundle.getInt("radiusKm", result.radiusKm);
      result.fakeResults = bundle.getBoolean("fakeResults", result.fakeResults);
      return result;
    }

    @Override
    public String toString() {
      return "Parameters{" +
          "maxSpecies=" + maxSpecies +
          ", latitude=" + latitude +
          ", longitude=" + longitude +
          ", radiusKm=" + radiusKm +
          ", photoType='" + photoType + '\'' +
          ", fakeResults=" + fakeResults +
          '}';
    }
  }

  private static final String TAG = "AntImageUrlLoader";

  private final RequestQueue requestQueue;
  private final Parameters parameters;

  public AntImageUrlLoader(
      Context context,
      RequestQueue requestQueue,
      Parameters parameters) {
    super(context);
    this.requestQueue = requestQueue;
    this.parameters = Parameters.copy(parameters);
  }

  @Override
  public List<AntImageUrl> loadInBackground() {
    if (parameters.fakeResults) {
      return fakeResults();
    }
    SpecimensJson specimensJson = fetchSpecimensJson();
    if (specimensJson == null) {
      return ImmutableList.of();
    }
    List<String> taxonNames = extractTaxonNames(specimensJson, parameters.maxSpecies);

    // EXERCISE: Sort the ant species by name
    List<AntImageUrl> antImageUrls = fetchAntImageUrls(taxonNames);
    return antImageUrls;
  }

  /** Makes a network request to get data about all specimens within the search radius. */
  private SpecimensJson fetchSpecimensJson() {
    String url = specimensUrl();
    RequestFuture<SpecimensJson> future = RequestFuture.newFuture();
    GsonRequest<SpecimensJson> request = new GsonRequest<>(
        url,
        SpecimensJson.class,
        null,
        future, future);
    requestQueue.add(request);

    try {
      return future.get();
    } catch (InterruptedException e) {
      return null;
    } catch (ExecutionException e) {
      Log.w(TAG, "Loading speciment JSON failed", e);
      return null;
    }
  }

  private String specimensUrl() {
    return String.format(
        "http://api.antweb.org/v3/"
            + "geoSpecimens?coords=%s,%s"
            + "&limit=100"
            + "&radius=%s"
            + "&dateMin=2017-03-01&dateMax=2018-03-25",
        (int) parameters.latitude,
        (int) parameters.longitude,
        parameters.radiusKm);
  }

  /** Processes the specimens to get up to maxSize unique taxon names. */
  private List<String> extractTaxonNames(SpecimensJson specimens, int maxSize) {
    Set<String> taxonNames = new HashSet<>();
    for (SpecimensJson.SingleSpecimenJson specimen : specimens.specimens) {
      if (!taxonNames.contains(specimen.antwebTaxonName)) {
        taxonNames.add(specimen.antwebTaxonName);
        if (taxonNames.size() == maxSize) {
          break;
        }
      }
    }
    // Note: in real code you'd probably just return the Set, but this makes the workshop exercises
    // a bit easier.
    return new ArrayList<>(taxonNames);
  }

  /**
   * Makes several network requests to get image URLs for the given taxonNames.
   *
   * <p>The returned species will be in the same order as passed into this method, but only species
   * where an image was found will be returned.
   */
  private List<AntImageUrl> fetchAntImageUrls(Iterable<String> taxonNames) {
    List<ListenableFuture<TaxaImagesJson>> futures = new ArrayList<>();
    for (String taxon : taxonNames) {
      String url = String.format(
          "https://api.antweb.org/v3/taxaImages?shotType=%s&taxonName=%s",
          parameters.photoType,
          URLEncoder.encode(taxon));
      RequestFuture<TaxaImagesJson> future = RequestFuture.newFuture();
      GsonRequest<TaxaImagesJson> request = new GsonRequest<>(
          url, TaxaImagesJson.class, null, future, future);
      requestQueue.add(request);
      futures.add(JdkFutureAdapters.listenInPoolThread(future));
    }
    List<TaxaImagesJson> imagesJsonList;
    try {
      imagesJsonList = Futures.allAsList(futures).get();
    } catch (InterruptedException e) {
      return new ArrayList<>();
    } catch (ExecutionException e) {
      Log.w(TAG, "Failed to load taxa image URLs", e);
      return new ArrayList<>();
    }

    List<AntImageUrl> results = new ArrayList<>();
    for (TaxaImagesJson imagesJson : imagesJsonList) {
      Uri imageUrl = imagesJson.getUrl();
      if (imageUrl != null) {
        results.add(new AntImageUrl(imagesJson.getTaxonName(), imageUrl));
      }
    }
    return results;
  }

  private ImmutableList<AntImageUrl> fakeResults() {
    ImmutableList.Builder<AntImageUrl> resultBuilder = ImmutableList.builder();
    for (int i = 0; i < parameters.maxSpecies; i++) {
      resultBuilder.add(new AntImageUrl("antum falsum #" + i, null));
    }
    return resultBuilder.build();
  }
}
