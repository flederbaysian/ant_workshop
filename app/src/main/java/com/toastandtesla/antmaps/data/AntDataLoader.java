package com.toastandtesla.antmaps.data;

import android.content.Context;
import android.net.Uri;
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
public final class AntDataLoader extends AsyncTaskLoader<List<AntSpecies>> {

  /** An object with parameters that control the loader. */
  public static final class Parameters {
    public int maxSpecies = 10;
    public float latitude;
    public float longitude;
    public int radiusKm = 2;
    public boolean fakeResults = false;

    static Parameters copy(Parameters parameters) {
      Parameters result = new Parameters();
      result.maxSpecies = parameters.maxSpecies;
      result.latitude = parameters.latitude;
      result.longitude = parameters.longitude;
      result.radiusKm = parameters.radiusKm;
      result.fakeResults = parameters.fakeResults;
      return result;
    }
  }

  private static final String TAG = "AntDataLoader";

  private final RequestQueue requestQueue;
  private final Parameters parameters;

  public AntDataLoader(
      Context context,
      RequestQueue requestQueue,
      Parameters parameters) {
    super(context);
    this.requestQueue = requestQueue;
    this.parameters = Parameters.copy(parameters);
  }

  @Override
  public List<AntSpecies> loadInBackground() {
    if (parameters.fakeResults) {
      return fakeResults();
    }
    SpecimensJson specimensJson = fetchSpecimensJson();
    if (specimensJson == null) {
      return ImmutableList.of();
    }
    Set<String> taxonNames = extractTaxonNames(specimensJson, parameters.maxSpecies);

    // EXERCISE: Sort the species by name
    return fetchAntSpeciesData(taxonNames);
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
  private Set<String> extractTaxonNames(SpecimensJson specimens, int maxSize) {
    Set<String> taxonNames = new HashSet<>();
    for (SpecimensJson.SingleSpecimenJson specimen : specimens.specimens) {
      if (!taxonNames.contains(specimen.antwebTaxonName)) {
        taxonNames.add(specimen.antwebTaxonName);
        if (taxonNames.size() == maxSize) {
          break;
        }
      }
    }
    return taxonNames;
  }

  /**
   * Makes several network requests to get image URLs for the given taxonNames. The returned
   * species will be in the same order as passed into this method, but only species where an image
   * was found will be returned.
   */
  private List<AntSpecies> fetchAntSpeciesData(Iterable<String> taxonNames) {
    List<ListenableFuture<TaxaImagesJson>> futures = new ArrayList<>();
    for (String taxon : taxonNames) {
      String url = "http://api.antweb.org/v3/taxaImages?shotType=h&taxonName=" + URLEncoder.encode(taxon);
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

    List<AntSpecies> results = new ArrayList<>();
    for (TaxaImagesJson imagesJson : imagesJsonList) {
      Uri imageUrl = imagesJson.getUrl();
      if (imageUrl != null) {
        results.add(new AntSpecies(imagesJson.getTaxonName(), imageUrl));
      }
    }
    return results;
  }

  private ImmutableList<AntSpecies> fakeResults() {
    ImmutableList.Builder<AntSpecies> resultBuilder = ImmutableList.builder();
    for (int i = 0; i < parameters.maxSpecies; i++) {
      resultBuilder.add(new AntSpecies("Ant #" + i, null));
    }
    return resultBuilder.build();
  }
}
