package com.tcashcroft.license;

import com.fasterxml.jackson.databind.JsonNode;
import com.tcashcroft.license.model.Branch;
import com.tcashcroft.license.model.CreateFileEntity;
import com.tcashcroft.license.model.PullRequest;
import com.tcashcroft.license.model.Ref;
import com.tcashcroft.license.model.RefResponse;
import com.tcashcroft.license.model.Repository;
import com.tcashcroft.license.model.User;
import edu.byu.hbll.json.ObjectMapperFactory;
import edu.byu.hbll.json.UncheckedObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

/**
 * A client class that provides helper function for performing basic operations against the Gihub API.
 */
public class GithubClient {

  private final Client client;

  private static final UncheckedObjectMapper mapper = ObjectMapperFactory.newDefault();

  private static final URI BASE_URI = UriBuilder.fromUri("https://api.github.com").build();

  private static final URI ORGS_URI = UriBuilder.fromUri(BASE_URI).path("orgs").build();

  private final String ORGANIZATION_LABEL_TEMPLATE = "organizationName";

  private final String REPOSITORY_NAME_TEMPLATE = "repositoryName";

  private final String BRANCH_NAME_TEMPLATE = "branchName";

  private final String username;

  private final String password;

  private final User user;

  /**
   * Constructs a GithubClient. Requires a username and password. Authentication is handled via
   * Basic Auth.
   * @param username String - the user's Github username
   * @param password String - the user's Github password
   */
  public GithubClient(String username, String password) {
    HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder().credentials(username, password).build();
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.register(feature);
    client = ClientBuilder.newClient(clientConfig);
    this.username = username;
    this.password = password;

    URI userUri = UriBuilder.fromUri(BASE_URI).path("user").build();
    Response response = client.target(userUri).request().get();
    String responseString = response.readEntity(String.class);
    System.out.println(responseString);
    user = mapper.readValue(responseString, User.class);
  }

  /**
   * Gets a list of all the repositories in the given organization
   * @param organizationName String - the organization name
   * @return List of {@link Repository} - the repositories in the given organization
   */
  public List<Repository> getRepositoriesInOrganization(String organizationName) {
    URI organizationReposUri = UriBuilder.fromUri(ORGS_URI).path(organizationName).path("repos").build();
    Response response = client.target(organizationReposUri).request().get();
    String responseString = response.readEntity(String.class);
    JsonNode responseNode = mapper.readTree(responseString);

    Repository[] repositories = mapper.treeToValue(responseNode, Repository[].class);

    return Arrays.asList(repositories);
  }

  /**
   * Retrieves the given branch name from the given repository
   * @param organizationName String - the organization name
   * @param repository {@link Repository} - the target repository
   * @param branchName String - the name of the target branch
   * @return {@link Branch} - the repository branch
   */
  public Branch getBranch(String organizationName, Repository repository, String branchName) {
    URI branchUri = createBranchUri(organizationName, repository.getName(), branchName);
    Response response = client.target(branchUri).request().get();
    String responseString = response.readEntity(String.class);
    Branch branch = mapper.readValue(responseString, Branch.class);
    return branch;
  }

  /**
   * Creates a branch from the given base branch in the given repository
   * @param organizationName String - the organization name
   * @param repository {@link Repository} - the target repository
   * @param base {@link Branch} - the base branch to branch from
   * @param newBranchName String - the name of the new branch
   * @return {@link} RefResponse - the Github API response
   */
  public RefResponse createBranch(String organizationName, Repository repository, Branch base, String newBranchName) {
    URI refsUri = UriBuilder.fromUri(BASE_URI)
        .path("repos/{" + ORGANIZATION_LABEL_TEMPLATE + "}/{" + REPOSITORY_NAME_TEMPLATE + "}/git/refs")
        .resolveTemplate(ORGANIZATION_LABEL_TEMPLATE, organizationName)
        .resolveTemplate(REPOSITORY_NAME_TEMPLATE, repository.getName())
        .build();

    Ref ref = new Ref();
    ref.setSha(base.getCommit().getSha());
    ref.setRef("refs/heads/" + newBranchName);
    Response response = client.target(refsUri)
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(mapper.writeValueAsString(ref), MediaType.APPLICATION_JSON));
    String responseString = response.readEntity(String.class);
    RefResponse refResponse = mapper.readValue(responseString, RefResponse.class);

    return refResponse;
  }

  /**
   * Adds the contents of the provided file to the new branch of the given repository.
   * @param organizationName String - the organization name
   * @param repository {@link Repository} - the repository needing a license
   * @param branchName String - the name of the branch to add the license to
   * @param path {@link Path} - the path to the license file
   */
  public void addFile(String organizationName, Repository repository, String branchName, Path path) {
    URI createFileUri = UriBuilder.fromUri(BASE_URI)
        .path("repos/{" + ORGANIZATION_LABEL_TEMPLATE + "}/{" + REPOSITORY_NAME_TEMPLATE + "}/contents/LICENSE")
        .resolveTemplate(ORGANIZATION_LABEL_TEMPLATE, organizationName)
        .resolveTemplate(REPOSITORY_NAME_TEMPLATE, repository.getName())
        .build();

    CreateFileEntity createFileEntity = new CreateFileEntity();
    createFileEntity.setMessage("Adding LICENSE file");
    createFileEntity.setBranch(branchName);
    try {
      createFileEntity.setContent(Base64.getEncoder().encodeToString(readFileAsString(path).getBytes()));
    } catch (IOException e) {
      System.out.println("An error occurred reading the license file at " + path.toString());
      System.out.println(e.getMessage());
    }

    Response response = client.target(createFileUri).request().put(Entity.entity(mapper.writeValueAsString(createFileEntity), MediaType.APPLICATION_JSON));
    if (response.getStatus() != Status.CREATED.getStatusCode()) {
      System.out.println("An error occurred creating the license file.");
      System.out.println("Status Code: " + response.getStatus());
      System.out.println(response.readEntity(String.class));
    }
  }

  /**
   * Creates a pull request in the given repository. Branch head is to be pulled into branch base.
   * @param organizationName String - the organization name
   * @param repository {@link Repository} - the repository
   * @param base String - the base branch name
   * @param head String - the head branch name
   */
  public void createPullRequest(String organizationName, Repository repository, String base, String head) {
    URI pullRequestUri = UriBuilder.fromUri(BASE_URI).path("repos/{" + ORGANIZATION_LABEL_TEMPLATE + "}/{" + REPOSITORY_NAME_TEMPLATE + "}/pulls")
        .resolveTemplate(ORGANIZATION_LABEL_TEMPLATE, organizationName)
        .resolveTemplate(REPOSITORY_NAME_TEMPLATE, repository.getName())
        .build();
    PullRequest pullRequest = new PullRequest();
    pullRequest.setTitle("LICENSE added");
    pullRequest.setBody("Added a license file to this repository.");
    pullRequest.setBase(base);
    pullRequest.setHead(head);
    Response response = client.target(pullRequestUri).request().post(Entity.json(mapper.writeValueAsString(pullRequest)));

    if (response.getStatus() != Status.CREATED.getStatusCode()) {
      System.out.println("An error occurred creating the pull request.");
      System.out.println("Status Code: " + response.getStatus());
      System.out.println(response.readEntity(String.class));
    }
  }

  /**
   * Reads the contents of the given file as a single string
   * @param path {@link Path} - the path to the file
   * @return String - the file contents
   * @throws IOException throws if there is an error reading the file
   */
  private String readFileAsString(Path path) throws IOException {
    BufferedReader reader = Files.newBufferedReader(path);
    StringBuilder builder = new StringBuilder();

    String line;
    while ((line = reader.readLine()) != null) {
      builder.append(line);
    }
    return builder.toString();
  }

  /**
   * A helper function for creating the branches URI
   * @param organizationName String - the organization name
   * @param repositoryName String - the repository name
   * @param branchName String - the branch name
   * @return {@link URI} the branch uri for the given branch
   */
  private URI createBranchUri(String organizationName, String repositoryName, String branchName) {
    URI branchUri = UriBuilder.fromUri(BASE_URI)
        .path("/repos/{" + ORGANIZATION_LABEL_TEMPLATE + "}/{" + REPOSITORY_NAME_TEMPLATE + "}/branches/{" + BRANCH_NAME_TEMPLATE + "}")
        .resolveTemplate(ORGANIZATION_LABEL_TEMPLATE, organizationName)
        .resolveTemplate(REPOSITORY_NAME_TEMPLATE, repositoryName)
        .resolveTemplate(BRANCH_NAME_TEMPLATE, branchName)
        .build();

    return branchUri;
  }

}
