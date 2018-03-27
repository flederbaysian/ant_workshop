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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A loader which uses the AntWeb API to download a list of nearby ant species, each with
 * a list of photos.
 */
public final class AntDataLoader extends AsyncTaskLoader<ImmutableList<AntSpecies>> {
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
  public ImmutableList<AntSpecies> loadInBackground() {
    SpecimensJson specimensJson = getSpecimensJson();
    if (specimensJson == null) {
      return ImmutableList.of();
    }
    Set<String> taxonNames = getTaxonNames(specimensJson, parameters.maxSpecies);
    return getAntSpecies(taxonNames);
  }

  private SpecimensJson getSpecimensJson() {
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

  private Set<String> getTaxonNames(SpecimensJson specimens, int maxSize) {
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

  private ImmutableList<AntSpecies> getAntSpecies(Set<String> taxonNames) {
    ImmutableList.Builder<ListenableFuture<TaxaImagesJson>> futuresBuilder = ImmutableList.builder();
    for (String taxon : taxonNames) {
      String url = "http://api.antweb.org/v3/taxaImages?shotType=h&taxonName=" + URLEncoder.encode(taxon);
      RequestFuture<TaxaImagesJson> future = RequestFuture.newFuture();
      GsonRequest<TaxaImagesJson> request = new GsonRequest<>(
          url, TaxaImagesJson.class, null, future, future);
      requestQueue.add(request);
      futuresBuilder.add(JdkFutureAdapters.listenInPoolThread(future));
    }
    List<TaxaImagesJson> imagesJsonList;
    try {
      imagesJsonList = Futures.allAsList(futuresBuilder.build()).get();
    } catch (InterruptedException e) {
      return ImmutableList.of();
    } catch (ExecutionException e) {
      Log.w(TAG, "Failed to load taxa image URLs", e);
      return ImmutableList.of();
    }

    ImmutableList.Builder<AntSpecies> resultBuilder = ImmutableList.builder();
    for (TaxaImagesJson imagesJson : imagesJsonList) {
      Uri imageUrl = imagesJson.getUrl();
      if (imageUrl != null) {
        resultBuilder.add(new AntSpecies(imagesJson.getTaxonName(), imageUrl));
      }
    }
    return resultBuilder.build();
  }

  public static final class Parameters {
    public int maxSpecies = 10;
    public float latitude = 52;
    public float longitude = 0;
    public int radiusKm = 2;

    static Parameters copy(Parameters parameters) {
      Parameters result = new Parameters();
      result.maxSpecies = parameters.maxSpecies;
      result.latitude = parameters.latitude;
      result.longitude = parameters.longitude;
      result.radiusKm = parameters.radiusKm;
      return result;
    }
  }
}
