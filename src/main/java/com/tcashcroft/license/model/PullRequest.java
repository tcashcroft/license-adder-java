package com.tcashcroft.license.model;

public class PullRequest {

  private String title;

  private String body;

  private String head;

  private String base;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getHead() {
    return head;
  }

  public void setHead(String head) {
    this.head = head;
  }

  public String getBase() {
    return base;
  }

  public void setBase(String base) {
    this.base = base;
  }
}
