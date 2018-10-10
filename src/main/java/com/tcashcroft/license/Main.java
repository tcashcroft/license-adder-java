package com.tcashcroft.license;

import com.tcashcroft.license.model.Branch;
import com.tcashcroft.license.model.Repository;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** */
public class Main {

  /**
   * The main entry point for the license adder program.
   * @param args String[]
   * @throws ParseException throws if there is a problem parsing the command line arguments
   */
  public static void main(String[] args) throws ParseException {

    Options options = new Options();
    options.addOption("o", "--organization", true, "The Github organization name");
    options.addOption("u", "--username", true, "The Github username");
    options.addOption("p", "--password", true, "The Github password");
    options.addOption("c", "--credentials", true, "The path to a credentials file");
    options.addOption("l", "--license", true, "The path to a license file");
    options.addOption("b", "--base", true, "The name of the base branch to branch from");
    options.addOption(
        "n", "--new-branch", true, "The name of the new branch for adding the license");
    CommandLineParser parser = new DefaultParser();
    CommandLine commandLine = parser.parse(options, args);

    // initialize variables to null to facilitate error checking
    String username = null;
    String password = null;
    String organizationName = null;
    String baseBranchName = null; // master should be default
    String newBranchName = null; // license should be default
    Path licensePath = null; // this project's license as default?
    Path credentialsPath = null;

    // parse options and assertIsNotBlank on applicable options
    // note that password and username may be blank if credentials is provided but must not be blank after parsing credentials
    organizationName = commandLine.getOptionValue("o");
    assertIsNotBlank(organizationName, "Organization name must be provided");
    username = commandLine.getOptionValue("u");
    password = commandLine.getOptionValue("p");
    credentialsPath = Paths.get(commandLine.getOptionValue("c"));
    String licensePathString = commandLine.getOptionValue("l");
    if (isBlank(licensePathString)) {
      licensePathString = "LICENSE.md";
    }
    licensePath = Paths.get(licensePathString);

    baseBranchName = commandLine.getOptionValue("b");
    if (isBlank(baseBranchName)) {
      baseBranchName = "master";
    }

    newBranchName = commandLine.getOptionValue("n");
    if (isBlank(newBranchName)) {
      newBranchName = "license";
    }

    if (isBlank(username) || isBlank(password)) {
      try {
        username = getUsernameFromCredentials(credentialsPath);
        password = getPasswordFromCredentials(credentialsPath);
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (isBlank(username) || isBlank(password)) {
        throw new IllegalArgumentException(
            "Username and password must not be empty or a valid credentials file must be supplied");
      }
    }

    // create Github client, get repos, branch repos without licenses and add license
    GithubClient githubClient = new GithubClient(username, password);
    List<Repository> repositories = githubClient.getRepositoriesInOrganization(organizationName);
    for (Repository repository : repositories) {
      if (repository.getLicense() == null) {
        Branch baseBranch = githubClient.getBranch(organizationName, repository, baseBranchName);
        githubClient.createBranch(organizationName, repository, baseBranch, newBranchName);
        githubClient.addFile(organizationName, repository, newBranchName, licensePath);
        githubClient.createPullRequest(organizationName, repository, baseBranchName, newBranchName);
      }
    }
  }

  /**
   * Asserts that the given string is not blank and throws an exception with the given message if it is.
   * @param string String - the string to check
   * @param message String - the message in the exception
   * @throws IllegalArgumentException throws if the string is blank
   */
  private static void assertIsNotBlank(String string, String message)
      throws IllegalArgumentException {
    if (isBlank(string)) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * A utility method for checking if a string is null or empty
   * @param string String - the string to check
   * @return boolean - true if the string is null or empty, false otherwise
   */
  private static boolean isBlank(String string) {
    if (string == null) {
      return true;
    } else {
      return string.isEmpty();
    }
  }

  /**
   * Reads the username from the given credentials file. The username must appear on the first line
   * of the credentials file.
   * @param file {@link Path} - the file path
   * @return String - the username
   * @throws IOException throws if an error occurs reading from the file
   */
  private static String getUsernameFromCredentials(Path file) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(file)) {
      String username = reader.readLine();
      return username;
    }
  }

  /**
   * Reads the password from the given credentials file. The password must appear on the secondd line
   * of the credentials file.
   * @param file (@link Path) - the file path
   * @return String - the password
   * @throws IOException throws if an error occurs reading from the file
   */
  private static String getPasswordFromCredentials(Path file) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(file)) {
      reader.readLine();
      String password = reader.readLine();
      return password;
    }
  }
}
