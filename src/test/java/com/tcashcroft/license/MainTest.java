package com.tcashcroft.license;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class MainTest {

  private static final Path credentialsPath = Paths.get("src/test/credentials");
  private static final Path badPath = Paths.get("some/bad/path");
  @Test
  public void testIsBlank() {
    String blank = "";
    String nullString = null;
    String notBlank = "Not blank";

    assertTrue(Main.isBlank(blank));
    assertTrue(Main.isBlank(nullString));
    assertFalse(Main.isBlank(notBlank));
  }

  @Test
  public void tesetAssertIsNotBlank() {
    String blank = "";
    String notBlank = "Not blank";
    String message = "Some message";

    try {
      Main.assertIsNotBlank(blank, message);
      fail("An exception should throw");
    } catch (IllegalArgumentException e) {
      assertEquals(message, e.getMessage());
    }

    try {
      Main.assertIsNotBlank(notBlank, message);
    } catch (IllegalArgumentException e) {
      fail("An exception should not have thrown");
    }
  }

  @Test
  public void testGetUsernameFromCredentials() {
    String expected = "username";
    try {
      String actual = Main.getUsernameFromCredentials(credentialsPath);
      assertEquals(expected, actual);
    } catch (IOException e) {
      fail("An exception should not have thrown from reading the credentials file");
    }

    try {
      Main.getUsernameFromCredentials(null);
      fail("A NullPointerException should throw");
    } catch (NullPointerException e) {
      // do nothing
    } catch (IOException e) {
      fail("An IO exception should not throw on a null path");
    }

    try {
      Main.getUsernameFromCredentials(badPath);
      fail("An IO Exception should throw if there is a problem reading from credentials");
    } catch (IOException e) {
      // do nothing
    }
  }

  @Test
  public void testGetPasswordFromCredentials() {
    String expected = "password";
    try {
      String actual = Main.getPasswordFromCredentials(credentialsPath);
      assertEquals(expected, actual);
    } catch (IOException e) {
      fail("An exception should not have thrown from reading the credentials file");
    }

    try {
      Main.getPasswordFromCredentials(null);
      fail("A NullPointerException should throw");
    } catch (NullPointerException e) {
      // do nothing
    } catch (IOException e) {
      fail("An IO exception should not throw on a null path");
    }

    try {
      Main.getPasswordFromCredentials(badPath);
      fail("An IO Exception should throw if there is a problem reading from credentials");
    } catch (IOException e) {
      // do nothing
    }
  }


}
