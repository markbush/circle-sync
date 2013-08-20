package com.ocado.plus.api.circlesync;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Circle;
import com.google.api.services.plus.model.CircleFeed;
import com.google.api.services.plus.model.PeopleFeed;
import com.google.api.services.plus.model.Person;
import com.google.common.collect.Lists;
import com.ocado.plus.api.Authoriser;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CircleSync {
  private static final String APPLICATION_NAME = "Ocado-CircleSync/1.0";
  private static HttpTransport HTTP_TRANSPORT;
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static Plus plus;
  // Set the source loader to one appropriate for your source data
  private static final SourceLoader sourceLoader = new ActiveDirectoryLoader();

  public static void main(String[] args) throws IOException, GeneralSecurityException {
    String filename = "circle-sync.conf";
    if (args.length > 0) {
      filename = args[0];
    }

    // Authorise to Google
    String[] scopes = {"https://www.googleapis.com/auth/plus.me",
                       "https://www.googleapis.com/auth/plus.circles.write"};
    HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Credential credential = Authoriser.authorise(JSON_FACTORY, HTTP_TRANSPORT, scopes);
    plus = new Plus.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    Plus.People people = plus.people();
    Plus.Circles circles = plus.circles();

    // Read source database
    List<CircleMapping> mappingsToAction = new ArrayList<CircleMapping>();
    for (CircleMapping mapping : Config.loadConfig(filename)) {
      // Load this mapping, converting email addresses to Google+ IDs
      try {
        List<String> groupEmails = sourceLoader.getMembersForGroup(mapping.getSourceName());
        if (groupEmails != null && !groupEmails.isEmpty()) {
          List<String> userIds = new ArrayList<String>();
          for (String email : groupEmails) {
            try {
              Person person = people.get(email).execute();
              userIds.add(person.getId());
            } catch (Throwable t) {
              System.out.println("Cannot find Google+ profile for: " + email);
            }
          }
          mapping.setUsers(userIds);
          mappingsToAction.add(mapping);
        }
      } catch (GoogleJsonResponseException e) {
        System.out.println("Lookup of Person by email address not available!");
        System.exit(1);
      } catch (Throwable t) {
        System.out.println("Google+ API failure: " + t.getMessage());
        t.printStackTrace();
        System.exit(1);
      }
    }

    // Read available Google Circles names
    // as map from circle name to ID
    Map<String,String> circleMap = getExistingCircles();

    // Action each match
    for (CircleMapping mapping : mappingsToAction) {
      String circleId = circleMap.get(mapping.getCircleName());
      if (circleId == null) {
        // New Circle - need to create
        Circle circle = new Circle();
        System.out.println("Creating Circle: " + mapping.getCircleName());
        circle.setDisplayName(mapping.getCircleName());
        Plus.Circles.Insert addCircle = circles.insert("me", circle);
        Circle newCircle = addCircle.execute();
        circleId = newCircle.getId();
      }
      // Get the IDs of the people currently in the Circle
      Set<String> existingCircleMembers = getCircleUsers(circleId);
      List<String> requiredUserIds = mapping.getUsers();
      // Add missing users
      List<String> peopleToAdd = new ArrayList<String>();
      for (String userId : requiredUserIds) {
        if (!existingCircleMembers.contains(userId)) {
          // Add user to Circle
          peopleToAdd.add(userId);
        }
      }
      // The API only allows up to 10 people to be added/removed at a time.
      List<List<String>> peopleToAddPartitions = Lists.partition(peopleToAdd, 10);
      for (List<String> peopleSubset : peopleToAddPartitions) {
        Plus.Circles.AddPeople addPeople = circles.addPeople(circleId);
        addPeople.setUserId(peopleSubset);
        addPeople.setCircleId(circleId);
        addPeople.execute();
      }
      // Remove extra users
      List<String> peopleToRemove = new ArrayList<String>();
      for (String userId : existingCircleMembers) {
        if (!requiredUserIds.contains(userId)) {
          // Remove from Circle
          peopleToRemove.add(userId);
        }
      }
      // The API only allows up to 10 people to be added/removed at a time.
      List<List<String>> peopleToRemovePartitions = Lists.partition(peopleToRemove, 10);
      for (List<String> peopleSubset : peopleToRemovePartitions) {
        Plus.Circles.RemovePeople removePeople = circles.removePeople(circleId);
        removePeople.setUserId(peopleSubset);
        removePeople.setCircleId(circleId);
        removePeople.execute();
      }
      System.out.println(String.format("Syncing: %s => %s (adding: %d, removing: %d)", mapping.getSourceName(),
          mapping.getCircleName(), peopleToAdd.size(), peopleToRemove.size()));
    }
  }

  /**
   * Obtains a mapping from Circle name to Circle ID for user's Circles.
   * @return Mapping of Circle name to Circle ID
   * @throws IOException
   */
  private static Map<String,String> getExistingCircles() throws IOException {
    Map<String,String> circles = new HashMap<String,String>();
    // Get all my Circles
    Plus.Circles.List circleList = plus.circles().list("me");
    CircleFeed circleFeed = circleList.execute();
    while (circleFeed.getItems() != null && !circleFeed.getItems().isEmpty()) {
      for (Circle circle : circleFeed.getItems()) {
        // Remember this Circle
        circles.put(circle.getDisplayName(), circle.getId());
      }
      String nextPageToken = circleFeed.getNextPageToken();
      if (nextPageToken == null) {
        break;
      }
      circleList.setPageToken(nextPageToken);
      circleFeed = circleList.execute();
    }
    return circles;
  }

  /**
   * Get the IDs of the people in a given Circle.
   * @param circleId The ID of the Circle to get the members of.
   * @return List of Person IDs.
   * @throws IOException
   */
  private static Set<String> getCircleUsers(String circleId) throws IOException {
    Set<String> users = new HashSet<String>();
    // Get the member list for this Circle
    Plus.People.ListByCircle peopleList = plus.people().listByCircle(circleId);
    PeopleFeed peopleFeed = peopleList.execute();
    while (peopleFeed.getItems() != null && !peopleFeed.getItems().isEmpty()) {
      for (Person person : peopleFeed.getItems()) {
        users.add(person.getId());
      }
      String nextPageToken = peopleFeed.getNextPageToken();
      if (nextPageToken == null) {
        break;
      }
      peopleList.setPageToken(nextPageToken);
      peopleFeed = peopleList.execute();
    }
    return users;
  }
}

