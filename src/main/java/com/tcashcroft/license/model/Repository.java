package com.tcashcroft.license.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Repository {

  private String id;

  private String name;

  private String url;

  private License license;

  @JsonProperty("commits_url")
  private String commitsUrl;

  @JsonProperty("git_refs_url")
  private String gitRefsUrl;

  @JsonProperty("branches_url")
  private String branchesUrl;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public License getLicense() {
    return license;
  }

  public void setLicense(License license) {
    this.license = license;
  }

  public String getCommitsUrl() {
    return commitsUrl;
  }

  public void setCommitsUrl(String commitsUrl) {
    this.commitsUrl = commitsUrl;
  }

  public String getGitRefsUrl() {
    return gitRefsUrl;
  }

  public void setGitRefsUrl(String gitRefsUrl) {
    this.gitRefsUrl = gitRefsUrl;
  }

  public String getBranchesUrl() {
    return branchesUrl;
  }

  public void setBranchesUrl(String branchesUrl) {
    this.branchesUrl = branchesUrl;
  }
}
