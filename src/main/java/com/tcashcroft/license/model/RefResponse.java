package com.tcashcroft.license.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefResponse {

  private String ref;

  @JsonProperty("node_id")
  private String nodeId;

  private String url;

  @JsonProperty("object")
  private RefObject object;

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public RefObject getObject() {
    return object;
  }

  public void setObject(RefObject object) {
    this.object = object;
  }
}
