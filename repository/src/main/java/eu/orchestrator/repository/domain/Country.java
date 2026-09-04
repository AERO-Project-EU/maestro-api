package eu.orchestrator.repository.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "country")
public class Country implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = true, name = "alpha_2")
  private String alpha2;

  @Column(nullable = true, name = "alpha_3")
  private String alpha3;

  @Column(nullable = true, name = "country_code")
  private String countryCode;

  @Column(nullable = true, name = "iso_3166")
  private String iso3166;

  @Column(nullable = true, name = "region")
  private String region;

  @Column(nullable = true, name = "sub_region")
  private String subRegion;

  @Column(nullable = true, name = "region_code")
  private String regionCode;

  @Column(nullable = true, name = "sub_region_code")
  private String subRegionCode;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
  private Date dateCreated;

  public Country() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAlpha2() {
    return alpha2;
  }

  public void setAlpha2(String alpha2) {
    this.alpha2 = alpha2;
  }

  public String getAlpha3() {
    return alpha3;
  }

  public void setAlpha3(String alpha3) {
    this.alpha3 = alpha3;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getIso3166() {
    return iso3166;
  }

  public void setIso3166(String iso3166) {
    this.iso3166 = iso3166;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getSubRegion() {
    return subRegion;
  }

  public void setSubRegion(String subRegion) {
    this.subRegion = subRegion;
  }

  public String getRegionCode() {
    return regionCode;
  }

  public void setRegionCode(String regionCode) {
    this.regionCode = regionCode;
  }

  public String getSubRegionCode() {
    return subRegionCode;
  }

  public void setSubRegionCode(String subRegionCode) {
    this.subRegionCode = subRegionCode;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }
}
