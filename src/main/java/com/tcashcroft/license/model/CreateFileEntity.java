package com.tcashcroft.license.model;

public class CreateFileEntity {

  private String branch;

  private String message;

  private User committer;

  private User author;

  private String content;

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public User getCommitter() {
    return committer;
  }

  public void setCommitter(User committer) {
    this.committer = committer;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
