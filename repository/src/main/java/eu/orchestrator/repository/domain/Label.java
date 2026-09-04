package eu.orchestrator.repository.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "label")
public class Label implements Serializable, Comparable<Label> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long labelID;

  @Column(nullable = true)
  private String name;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public Label() {
  }

  public Long getLabelID() {
    return labelID;
  }

  public void setLabelID(Long labelID) {
    this.labelID = labelID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  @Override
  public int compareTo(Label compareLabel) {
    String compareQuantity = ((Label) compareLabel).getName();

    return this.name.compareTo(compareQuantity);
  }
}
