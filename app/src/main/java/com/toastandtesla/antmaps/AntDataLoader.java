package com.toastandtesla.antmaps;

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

/**
 * A loader which uses the AntWeb API to download a list of nearby ant species, each with
 * a list of photos.
 */
public final class AntDataLoader extends AsyncTaskLoader<ImmutableList<AntSpecies>> {
  private static final String TAG = "AntDataLoader";

  private final RequestQueue requestQueue;

  public AntDataLoader(Context context, RequestQueue requestQueue) {
    super(context);
    this.requestQueue = requestQueue;
  }

  @Override
  public ImmutableList<AntSpecies> loadInBackground() {
    SpecimensJson specimensJson = getSpecimensJson();
    if (specimensJson == null) {
      return ImmutableList.of();
    }
    Set<String> taxonNames = getTaxonNames(specimensJson, 10);
    return getAntSpecies(taxonNames);
  }

  private SpecimensJson getSpecimensJson() {
    String url = "http://api.antweb.org/v3/geoSpecimens?coords=52,%200&limit=100&radius=2&dateMin=2017-03-01&dateMax=2018-03-25";
    RequestFuture<SpecimensJson> future = RequestFuture.newFuture();
    GsonRequest<SpecimensJson> request = new GsonRequest<>(
        url,
        SpecimensJson.class,
        null,
        future, future);
    requestQueue.add(request);

    try {
      return Futures.getChecked(future, Exception.class);
    } catch (Exception e) {
      Log.w(TAG, "Failed to load nearby specimen", e);
      return null;
    }
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
      imagesJsonList = Futures.getChecked(
          Futures.allAsList(futuresBuilder.build()), Exception.class);
    } catch (Exception e) {
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
}
