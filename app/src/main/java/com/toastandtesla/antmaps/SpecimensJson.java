package com.toastandtesla.antmaps;

/**
 * An object representing the JSON response from:
 * http://api.antweb.org/v3/geoSpecimens
 */
final class SpecimensJson {
  SingleSpecimenJson[] specimens;

  static final class SingleSpecimenJson {
    String antwebTaxonName;
  }
}
