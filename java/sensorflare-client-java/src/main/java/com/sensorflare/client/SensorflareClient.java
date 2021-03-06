package com.sensorflare.client;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * <p>Java Client for Sensorflare API</p> <p>Created by amaxilatis on 5/4/14.</p>
 *
 * @author Dimitrios Amaxilatis
 * @author Dimitris Zarras <zarras.dim@gmail.com>
 */
public class SensorflareClient {

    /**
     * <p>The default base Url for the Sensorflare API</p>
     */
    public static final String DEFAULT_BASE_URL = "http://www.sensorflare.com/api/";

    /**
     * <p>The base url to use.</p>
     */
    private String sensorflareApiBaseUrl;

    /**
     * <p>The connection Base64 encoded Basic Authorization token.</p>
     */
    private String authorizationToken;

    /**
     * <p>Boolean value to indicate if this client has successfully authenticated.</p>
     */
    private boolean authenticated;

    /**
     * Default Constructor.
     */
    public SensorflareClient() {
        authorizationToken = null;
        sensorflareApiBaseUrl = DEFAULT_BASE_URL;
        authenticated = false;
    }

    /**
     * <p>Creates a SensorflareClient for the given base url</p>
     *
     * @param baseUrl The base url to use for the API calls.
     * @throws java.lang.IllegalArgumentException if baseUrl is null or empty or not a valid HTTP(S) URL.
     */
    public SensorflareClient(final String baseUrl) {
        sensorflareApiBaseUrl = null;
        authorizationToken = null;
        authenticated = false;

        setBaseUrl(baseUrl);
    }

    /**
     * <p>Returns the current base url for the client requests</p>
     *
     * @return The base url
     */
    public String getBaseUrl() {
        return sensorflareApiBaseUrl;
    }

    /**
     * <p>Sets the current base url for the clients requests to baseUrl.</p>
     *
     * @param baseUrl The base url to use.
     * @throws java.lang.IllegalArgumentException if baseUrl is null or empty or not a valid HTTP(S) URL.
     */
    public void setBaseUrl(final String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("baseUrl cannot be null or empty");
        }

        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            throw new IllegalArgumentException("baseUrl must be a valid Http URL");
        }

        if (baseUrl.endsWith("/")) {
            sensorflareApiBaseUrl = baseUrl;

        } else {
            sensorflareApiBaseUrl = baseUrl + "/";
        }
    }

    /**
     * Authenticates the user.
     *
     * @param username username as String.
     * @param password password as String.
     * @return true if authentication succeeded, false if authentication failed.
     * @throws IOException in case of failed or interrupted I/O operations.
     */
    public final boolean authenticate(final String username, final String password) throws IOException {

        authorizationToken = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
        authenticated = testConnection();

        return authenticated;
    }

    /**
     * <p>Check if the client is authenticated for the given username and password</p>
     *
     * @param username username as String.
     * @param password password as String.
     * @return true if the client is authenticated for the given username and password, false otherwise.
     */
    public final boolean isClientFor(final String username, final String password) {
        final String authToken = new String(Base64.encodeBase64((username + ":" + password).getBytes()));

        return authToken.equals(authorizationToken);
    }

    /**
     * <p>Checks if the client is authenticated.</p>
     *
     * @return Return true for yes, false for no.
     */
    public final boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Shows all Dashboards.
     *
     * @return a String List with all Dashboards.
     */
    public final List<String> dashboards() throws IOException, JSONException {
        JSONArray dashboards = new JSONArray(getPage("dashboard"));
        List<String> dashboardsList = new ArrayList<>();
        for (int i = 0; i < dashboards.length(); i++) {
            dashboardsList.add(dashboards.getString(i));
        }
        return dashboardsList;
    }

    /**
     * <p>Returns a List<Long> of the authenticated User's Dashboard ids.</p>
     *
     * @return A List<Long> containing the User's Dashboards.
     *
     * @throws java.io.IOException if the connection cannot be established.
     * @throws org.json.JSONException  in case the server's response cannot be parsed.
     * @throws java.lang.IllegalStateException if the connection has not been authenticated.
     */
    public final List<Long> getDashboards() throws IOException, JSONException {
        //The map of the dashboards
        final List<Long> dashboards = new ArrayList<>();

        //Do the api call and parse the response into a JSONObject
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForGetRequest("dashboards");
        final JSONObject response = new JSONObject(getApiCallResponse(connection));

        //Get the JSONArray of dashboards
        final JSONArray dashboardsJsonArray = response.getJSONArray("dashboards");

        //Iterate through the JSONObjects in the JSONArray and parse them as a Map.Entry<Long, String>
        for (int i = 0; i < dashboardsJsonArray.length(); i++) {
            dashboards.add(dashboardsJsonArray.getLong(i));
        }

        return dashboards;
    }

    /**
     * <p>Returns a Map<String, String> with the details of the requested dashboard.</p>
     * <p>The Map will contains the following keys and values: <br />
     * <ul>
     *     <li>id</li>
     *     <li>name</li>
     *     <li>latitude</li>
     *     <li>longitude</li>
     * </ul>
     * </p>
     *
     * @param dashboardId The id of the dashboard.
     * @return A Map<String,String> with the details of the Dashboard.
     *
     * @throws java.io.IOException if the connection cannot be established.
     * @throws org.json.JSONException  in case the server's response cannot be parsed.
     * @throws java.lang.IllegalStateException if the connection has not been authenticated.
     */
    public final Map<String, String> getDashboardDetails(final Long dashboardId) throws IOException, JSONException {
        if (dashboardId == null || dashboardId <= 0) {
            throw new IllegalArgumentException("dashboardId cannot be null or less than 0");
        }

        final Map<String, String> dashboardDetails = new HashMap<>(); //The Map<> to return
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForGetRequest(String.format("dashboard/%d", dashboardId)); //Do the API call
        final JSONObject dashbaordDetailsJSONObject = new JSONObject(getApiCallResponse(connection)); //Parse the API call response

        dashboardDetails.put("id", String.valueOf(dashbaordDetailsJSONObject.getLong("id"))); //Get the id
        dashboardDetails.put("name", dashbaordDetailsJSONObject.getString("name")); //Get the name
        dashboardDetails.put("latitude", String.valueOf(dashbaordDetailsJSONObject.getDouble("latitude"))); //Get the latitude
        dashboardDetails.put("longitude", String.valueOf(dashbaordDetailsJSONObject.getDouble("longtitude"))); //Get the latitude

        return dashboardDetails;
    }

    /**
     * <p>Returns a HashMap<Long, String> (id, uri) of the resources for the given dashboard id.</p>
     *
     * @param dashboardId The id of the dashboard whose resources we want
     * @return A Map<Long, String> with the result.
     * @throws java.io.IOException             if the connection cannot be established.
     * @throws org.json.JSONException          in case the server's response cannot be parsed.
     * @throws java.lang.IllegalStateException if the connection has not been authenticated.
     */
    public final Map<Long, String> getDashboardResources(final Long dashboardId) throws IOException, JSONException {
        final String dashboardResourcesUrl = String.format("dashboards/%d/resources", dashboardId); //Construct the URL
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForGetRequest(dashboardResourcesUrl); //Authorized connection for the url
        final JSONObject response = new JSONObject(getApiCallResponse(connection)); //The API call response JSONObject
        final JSONArray resourcesJsonArray; //The JSONArray that contains the resources return from the API
        final Map<Long, String> dashboardResources; //The resources of the dashboard

        //Get the resources JSONArray
        resourcesJsonArray = response.getJSONArray("resources");

        //Iterate through the JSONArray and populate the result
        dashboardResources = new HashMap<>();
        for (int i = 0; i < resourcesJsonArray.length(); i++) {
            final JSONObject resourceJSONObject = resourcesJsonArray.getJSONObject(i);

            dashboardResources.put(resourceJSONObject.getLong("id"), resourceJSONObject.getString("uri"));
        }

        return dashboardResources;
    }

    /**
     * <p>Returns a List<Long> of Intelligence ids for a given Dashboard.</p>
     *
     * @param dashboardId The id of the dashboard.
     * @return a List<Long> with Intelligence ids for the given Dashboard.
     *      The returned List<Long> may be empty if the Dashboard doesn't have any Intelligence or the Dashboard doesn't
     *      belong or is shared with the authenticated User.
     *
     * @throws java.io.IOException if a connection cannot be established with the server.
     * @throws org.json.JSONException if the server returned an unexpected response.
     * @throws java.lang.IllegalArgumentException if dashboardId is null or less than 0.
     * @throws java.lang.IllegalStateException if the connection has not been authenticated.
     */
    public final List<Long> getDashboardIntelligence(final Long dashboardId) throws IOException, JSONException {
        //Check if dashboardId is valid
        if (dashboardId == null || dashboardId <= 0) {
            throw new IllegalArgumentException("dashboardId cannot be null or less than 0.");
        }

        final String dashboardIntelligenceUrl = String.format("dashboards/%d/intelligence", dashboardId); //Construct the URL
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForGetRequest(dashboardIntelligenceUrl); //Authorized connection for the url
        final JSONObject response = new JSONObject(getApiCallResponse(connection)); //The API call response JSONObject
        final JSONArray intelligenceIdsJSONArray; //JSONArray that contains the Intelligence ids
        final List<Long> intelligenceIds = new ArrayList<>(); //The List<Long> of the Intelligence ids for the given Dashboard


        //Populate the intelligenceIds only if responseStatusCode is 200
        if (response.getInt("code") == 200) {
            //Status code is 200.

            //Get the intelligenceIdsJSONArray iterate through it and populate the intelligenceIds List<Long>
            intelligenceIdsJSONArray = response.getJSONArray("intelligence");

            for (int i = 0; i < intelligenceIdsJSONArray.length(); i++) {
                intelligenceIds.add(intelligenceIdsJSONArray.getLong(i));
            }
        }

        //Return the null or List<Long>
        return intelligenceIds;
    }

    /**
     * <p>Returns a Map<String, String> with the details of a given resource</p>
     *
     * @param resourceUri The resource URI
     * @return The Map<String, String> with the resource details.
     * @throws java.io.IOException if the connection cannot be established.
     * @throws org.json.JSONException in case the server's response cannot be parsed.
     * @throws java.lang.IllegalStateException if the connection has not been authenticated.
     */
    public final Map<String, String> getResourceDetails(final String resourceUri) throws IOException, JSONException {
        final String resourceDetailsUrl = String.format("resource/%s/description", resourceUri.replace(" ", "%20")); //Construct the url
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForGetRequest(resourceDetailsUrl); //Do the api call
        final JSONObject resourceDetailsJSONObject = new JSONObject(getApiCallResponse(connection)); //Parse the response of the api call
        final Map<String, String> resourceDetails = new HashMap<>(); //The hmap with the resource details
        final boolean hasName; //Does the resource have a name
        final String[] uriParts; //The uri parts split at /
        final StringBuilder uriName; //The name created base on the resource uri


        //Get the name if it exists and it is not null
        if (resourceDetailsJSONObject.has("name") && !resourceDetailsJSONObject.isNull("name")) {
            resourceDetails.put("name", resourceDetailsJSONObject.getString("name"));
            hasName = true;

        } else {
            hasName = false;
        }

        //Get the resource type
        if (resourceDetailsJSONObject.has("isa")) {
            resourceDetails.put("isa", resourceDetailsJSONObject.getString("isa"));

        } else {
            //Throw a JSONException if the resources doesn't have an isa field
            throw new JSONException("resource doesn't have an isa field");
        }

        //Get what the resource observer if it's a sensor
        if (resourceDetailsJSONObject.has("observes")) {
            resourceDetails.put("observes", resourceDetailsJSONObject.getString("observes"));

        } else {
            resourceDetails.put("observes", "");
        }

        //Get what the resource controls if it's an actuator
        if (resourceDetailsJSONObject.has("controls")) {
            resourceDetails.put("controls", resourceDetailsJSONObject.getString("controls"));

        } else {
            resourceDetails.put("controls", "");
        }

        //Get the unit of measurement for the resource
        if (resourceDetailsJSONObject.has("uom")) {
            resourceDetails.put("uom", resourceDetailsJSONObject.getString("uom"));

        } else {
            resourceDetails.put("uom", "");
        }

        //Set a name based on the resource type or uri if the resource doesn't have a name
        if (!hasName) {
            if (resourceDetails.get("isa").equalsIgnoreCase("sensor") && resourceDetailsJSONObject.has("observes")) {
                //Set the observes value as a name
                resourceDetails.put("name", resourceDetailsJSONObject.getString("observes"));

            } else if (resourceDetails.get("isa").equalsIgnoreCase("actuator") && resourceDetailsJSONObject.has("controls")) {
                //Set the controls value as a name
                resourceDetails.put("name", resourceDetailsJSONObject.getString("controls"));

            } else {
                //Create a name based on the resource uri
                uriName = new StringBuilder();

                //Iterate through the parts and create the name
                uriParts = resourceUri.split("/");
                for (int i = 1; i < uriParts.length; i++) {
                    uriName.append(uriParts[i]);

                    if (i != (uriParts.length - 1)) {
                        uriName.append("/");
                    }
                }

                //Set the name
                resourceDetails.put("name", uriName.toString());
            }
        }

        //Return the details
        return resourceDetails;
    }

    /**
     * <p>Returns a Map<String, String> containing the details of a given Intelligence.</p>
     * <p>The Map<String, String> returned here is compatible with the one returned by getResourceDetails(String). <br />
     * It contains though more information.</p>
     *
     * @param intelligenceId The id of the Intelligence.
     * @return A Map<String, String> with the details of the Intelligence or null if the logged in User doens't have access to the Intelligence object.
     *
     * @throws java.io.IOException if a connection with the server cannot be established.
     * @throws org.json.JSONException if the server returned an unexpected response.
     * @throws java.lang.IllegalArgumentException if intelligenceId is null or less than 0.
     * @throws java.lang.IllegalStateException if the connection has not been authenticated.
     */
    public final Map<String, String> getIntelligenceDetails(final Long intelligenceId) throws IOException, JSONException {
        if (intelligenceId == null || intelligenceId <= 0) {
            throw new IllegalArgumentException("intelligenceId cannot be null");
        }

        final String intelligenceUrl = String.format("intelligence/%d", intelligenceId); //Construct the url
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForGetRequest(intelligenceUrl); //Do the API call
        final JSONObject response = new JSONObject(getApiCallResponse(connection)); //Parse the API call response
        final JSONObject intelligenceJSONObject; //The Intelligence info as a JSONObject
        final Map<String, String> intelligenceDetails; //The Map<String, String> with the intelligence details.

        if (response.getInt("code") != 200) {
            //Intelligence not found or doesn't belong to the logged in User
            intelligenceDetails = null;

        } else {
            //Intelligence found. Populate the HashMap<> and return the details.
            intelligenceJSONObject = response.getJSONObject("intelligence");
            intelligenceDetails = new HashMap<>();
            intelligenceDetails.put("id", String.valueOf(intelligenceJSONObject.getLong("id")));
            intelligenceDetails.put("uri", String.format("sensorflare-application-%d/execution/bypass", intelligenceJSONObject.getLong("id")));
            intelligenceDetails.put("name", intelligenceJSONObject.getString("name"));

            //Set the type if we have one
            if (intelligenceJSONObject.has("type") && !intelligenceJSONObject.isNull("type")) {
                //We have a type that is not null
                intelligenceDetails.put("type", intelligenceJSONObject.getJSONObject("type").getString("name"));

            } else {
                //Type not found
                intelligenceDetails.put("type", "Unknown");
            }

            intelligenceDetails.put("isa", "Intelligence");
            intelligenceDetails.put("observes", "");
            intelligenceDetails.put("controls", "");
            intelligenceDetails.put("uom", "");
        }

        return intelligenceDetails;
    }

    /**
     * <p>Returns the refresh hash for a given dashboard</p>
     *
     * @param dashboardId The id of the dashboard
     * @return The refresh hash of the dashboard
     * @throws java.io.IOException             if the connection cannot be established.
     * @throws java.lang.IllegalStateException if the connection has not been authenticated.
     */
    public final String getDashboardRefreshHash(final Long dashboardId) throws IOException {
        final String urlBase = "dashboard/%d/resources/description/hash";
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForGetRequest(String.format(urlBase, dashboardId));

        return getApiCallResponse(connection);
    }

    /**
     * Creates new Dashboard.
     *
     * @param name the name for the new @{see Dashboard}.
     * @return a @{see JSONObject} with information about the created Dashboard.
     */
    public final JSONObject createDashboard(final String name) throws IOException, JSONException {
        return new JSONObject(getPage("dashboard/create/" + name));
    }

    /**
     * Sets the location of a Dashboard.
     *
     * @param id         the unique id of the @{see Dashboard}.
     * @param longtitude longtitude as Double.
     * @param latitude   latitude as Double.
     * @return a @{see JSONObject} with information about the updated Dashboard.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final JSONObject dashboardSetLocation(final Long id, final double longtitude, final double latitude) throws IOException, JSONException {
        Map<String, String> param = new HashMap<>();
        param.put("longitude", String.valueOf(longtitude));
        param.put("latitude", String.valueOf(latitude));
        return new JSONObject(postPage("dashboard/" + id + "/location/", param));
    }

    /**
     * Adds a Resource to Dashboard.
     *
     * @param id          the unique id of the @{see Dashboard}.
     * @param resourceUri resource Uri as STring.
     * @return a String value with information about the specified Dashboard.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final String dashboardAddResource(final long id, final String resourceUri) throws IOException, JSONException {
        JSONObject dashboard = new JSONObject(getPage("dashboard/" + id + "/add/resource/" + resourceUri));
        return dashboard.toString();
    }


    /**
     * Removes a Resource from Dashboard.
     *
     * @param id          the unique id of the @{see Dashboard}.
     * @param resourceUri resource Uri as String.
     * @return true if removal succeed, false if removal failed.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final Boolean dashboardRemoveResource(final long id, final String resourceUri) throws IOException, JSONException {
        JSONObject obj = new JSONObject(getPage("dashboard/" + id + "/disconnect/" + resourceUri));

        JSONObject response = new JSONObject();
        response.put("status", "Ok");
        response.put("code", 200);

        if (obj.toString().equals(response.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Deletes Dashboard.
     *
     * @param id the unique id of the @{see Dashboard}.
     * @return true if deleted, false otherwise.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final boolean dashboardDelete(final Long id) throws IOException, JSONException {
        return new JSONObject(getPage("dashboard/delete/" + id)).getInt("code") == 200;
    }

    /**
     * Sets a Resource featured in a Dashboard.
     *
     * @param dashboardId dashboardId as Long.
     * @param resourceUri resource Uri as String.
     * @return true if the specified Resource is featured successfully, false if not.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final Boolean dashboardSetFeaturedResource(final long dashboardId, final String resourceUri) throws IOException, JSONException {
        JSONObject obj = new JSONObject(getPage("dashboard/" + dashboardId + "/featured/" + resourceUri));

        JSONObject response = new JSONObject();
        response.put("status", "Ok");
        response.put("code", 200);

        if (obj.toString().equals(response.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shows information about a Dashboard.
     *
     * @param dashboardId dashboardId as String.
     * @return a String value with information about the specified Dashboard.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final JSONObject dashboard(final Long dashboardId) throws IOException, JSONException {
        return new JSONObject(getPage("dashboard/" + dashboardId));
    }

    /**
     * Show all Resources of a Dashboard.
     *
     * @param dashboardName dashboardName as String.
     * @return a String List with all the Resources of the specified dashboard.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final List<String> dashboardResources(final String dashboardName) throws IOException, JSONException {
        JSONArray resources = new JSONArray(getPage("dashboard/" + dashboardName + "/resource"));
        List<String> resourcesList = new ArrayList<>();
        for (int i = 0; i < resources.length(); i++) {
            resourcesList.add(resources.getString(i));
        }
        return resourcesList;
    }

//    /**
//     * Shows all Applications of a Dashboard.
//     *
//     * @param dashboardName dashboardName as String.
//     * @return a String list with all the Applications of the specified Dashboard.
//     * @throws IOException   in case of failed or interrupted I/O operations.
//     * @throws JSONException in case of exception during JSON processing.
//     */
//    public final List<String> dashboardApplications(final String dashboardName) throws IOException, JSONException {
//        JSONArray applications = new JSONArray(getPage("dashboard/" + dashboardName + "/application"));
//        List<String> applicationsList = new ArrayList<>();
//        for (int i = 0; i < applications.length(); i++) {
//            applicationsList.add(applications.getString(i));
//        }
//        return applicationsList;
//    }

    /**
     * Shows all IoT Resources deployed by the User.
     *
     * @return a String List with all the Resources.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final Map<String, String> resources() throws IOException, JSONException {
        JSONArray resources = new JSONArray(getPage("resource"));
        Map<String, String> resourcesList = new HashMap<>();
        for (int i = 0; i < resources.length(); i++) {
            resourcesList.put(
                    resources.getString(i).split(":")[0].replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\"", ""),
                    resources.getString(i).split(":")[1].replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\"", ""));
        }
        return resourcesList;
    }

    /**
     * Shows the Description of a Resource.
     *
     * @param resourceUri resource Uri as String.
     * @return a String value with the description of the specified Resource.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final String resourceDescription(final String resourceUri) throws IOException, JSONException {
        return getPage("resource/" + resourceUri + "/description");
    }

    /**
     * Removes Property from a Resource.
     *
     * @param resourceId resourceId as Long.
     * @param propertyId propertyId as Long.
     * @return true if removal succeed, false if removal failed.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final Boolean resourceRemoveProperty(final Long resourceId, final Long propertyId) throws IOException, JSONException {
        JSONObject obj = new JSONObject(getPage("resource/" + resourceId + "/property/" + propertyId));

        JSONObject response = new JSONObject();
        response.put("status", "Ok");
        response.put("code", 200);

        if (obj.toString().equals(response.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Clears all Properties from a Resource.
     *
     * @param resourceId resourceId as Long.
     * @return true if all Properties are successfully removed from the specified Resource, false if not.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final Boolean resourceClearProperty(final Long resourceId) throws IOException, JSONException {
        JSONObject obj = new JSONObject(getPage("resource/" + resourceId + "/property"));

        JSONObject response = new JSONObject();
        response.put("status", "Ok");
        response.put("code", 200);

        if (obj.toString().equals(response.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>Sets the value of the specified resource</p>
     *
     * @param resourceUri   The resource URI
     * @param resourceValue The value to set
     * @return true on success, false on error
     * @throws java.io.IOException             if the connection cannot be established.
     * @throws org.json.JSONException          in case the server's response cannot be parsed.
     * @throws java.lang.IllegalStateException if the connection has not been authenticated.
     */
    public final boolean setResourceValue(final String resourceUri, final double resourceValue) throws IOException, JSONException {
        final boolean result;
        final String apiEndpoint = "report/set/%s/%s"; // {value}, {resourceUri}
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForPostRequest(String.format(apiEndpoint, resourceValue, resourceUri));
        final JSONObject response = new JSONObject(getApiCallResponse(connection));

        if (response.has("status") && response.getString("status").compareToIgnoreCase("ok") == 0) {
            result = true;

        } else {
            result = false;
        }

        return result;
    }

    /**
     * <p>Set the value of an Intelligence.</p>
     *
     * @param intelligenceId The id of the Intelligence.
     * @param resourceValue The new value for the Intelligence.
     * @return true on success, false on error
     *
     * @throws java.io.IOException if a connection cannot be established with the server
     * @throws org.json.JSONException if the server sent an unexpected response
     * @throws java.lang.IllegalStateException if the connection has not been authenticated.
     */
    public final boolean setIntelligenceValue(final Long intelligenceId, final double resourceValue) throws IOException, JSONException {
        final String url = String.format("intelligence/%d/bypass", intelligenceId); //The url where to post the new value
        final Map<String, String> parameters = new HashMap<>(); //The parameters of the post request
        final JSONObject response; //The response of the API
        final boolean result; //The result of the request

        //Populate the parameters
        parameters.put("status", String.valueOf(resourceValue));

        //Get the response
        response = new JSONObject(postPage(url, parameters));

        if (response.has("status") && response.getString("status").equalsIgnoreCase("ok")) {
            //Intelligence value successfully set
            result = true;

        } else {
            //Error setting the Intelligence value
            result = false;
        }

        return result;
    }

    /**
     * Shows all the Devices.
     *
     * @return a String List with all the Devices.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final List<String> devices() throws IOException, JSONException {
        JSONArray devices = new JSONArray(getPage("device"));
        List<String> devicesList = new ArrayList<>();
        for (int i = 0; i < devices.length(); i++) {
            devicesList.add(devices.getString(i));
        }
        return devicesList;
    }

//    /**
//     * Shows all User Applications.
//     *
//     * @return a String List with all the User Applications.
//     * @throws IOException   in case of failed or interrupted I/O operations.
//     * @throws JSONException in case of exception during JSON processing.
//     */
//    public final List<String> applications() throws IOException, JSONException {
//        JSONArray applications = new JSONArray(getPage("application"));
//        List<String> applicationsList = new ArrayList<>();
//        for (int i = 0; i < applications.length(); i++) {
//            applicationsList.add(applications.getString(i));
//        }
//        return applicationsList;
//    }

//    /**
//     * Show information about a User Application.
//     *
//     * @param applicationId applicationId as Long.
//     * @return a String with information about the specified Application.
//     * @throws IOException   in case of failed or interrupted I/O operations.
//     * @throws JSONException in case of exception during JSON processing.
//     */
//    public final String application(final Long applicationId) throws IOException, JSONException {
//        JSONObject application = new JSONObject(getPage("application/" + applicationId));
//        return application.toString();
//    }

//    /**
//     * Deletes a User Application.
//     *
//     * @param applicationId applicationId as Long.
//     * @return true if deletion succeed, false if deletion failed.
//     * @throws IOException   in case of failed or interrupted I/O operations.
//     * @throws JSONException in case of exception during JSON processing.
//     */
//    public final Boolean applicationDelete(final Long applicationId) throws IOException, JSONException {
//        JSONObject obj = new JSONObject(getPage("application/delete/" + applicationId));
//
//        JSONObject response = new JSONObject();
//        response.put("status", "Ok");
//        response.put("code", 200);
//
//        if (obj.toString().equals(response.toString())) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    /**
     * Shows all Gateways.
     *
     * @return a String List with all the Gateways.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final Map<Long, String> gateways() throws IOException, JSONException {
        JSONArray gateways = new JSONArray(getPage("gateway"));
        Map<Long, String> gatewaysList = new HashMap<>();
        for (int i = 0; i < gateways.length(); i++) {
            gatewaysList.put(((JSONObject) gateways.get(i)).getLong("id"), ((JSONObject) gateways.get(i)).getString("name"));
        }
        return gatewaysList;
    }

    /**
     * Show information about a Gateway.
     *
     * @param gatewayName gatewayName as String.
     * @return a String value with information about a Gateway.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final JSONObject gatewayViewInfo(final String gatewayName) throws IOException, JSONException {
        JSONObject gateway = new JSONObject(getPage("gateway/" + gatewayName));
        return gateway;
    }

    /**
     * Adds Property in a Gateway.
     *
     * @param key   key as a String.
     * @param value value as a String.
     * @return true if the addition succeeded, false if the addition failed.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final Boolean gatewayAddProperty(final String key, final String value) throws IOException, JSONException {
        JSONObject obj = new JSONObject(getPage("gateway/" + key + "/" + value));

        JSONObject response = new JSONObject();
        response.put("status", "Ok");
        response.put("code", 200);

        if (obj.toString().equals(response.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shows all Schedules.
     *
     * @return a String List with all Schedules.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final List<String> schedules() throws IOException, JSONException {
        JSONArray schedules = new JSONArray(getPage("schedule"));
        List<String> schedulesList = new ArrayList<>();
        for (int i = 0; i < schedules.length(); i++) {
            schedulesList.add(schedules.getString(i));
        }
        return schedulesList;
    }

    /**
     * Creates a Schedule.
     *
     * @param keyName      keyName as String.
     * @param cronSchedule cronSchedule as String.
     * @param payload      payload as String.
     * @param after        after as Long.
     * @param payload2     payload2 as String.
     * @return a String value with the created Schedule.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final String scheduleCreate(final String keyName, final String cronSchedule, final String payload, final Long after, final String payload2) throws IOException, JSONException {
        JSONObject schedule = new JSONObject(getPage("schedule/" + keyName + "/" + cronSchedule + "/" + payload + "/" + after + "/" + payload2));
        return schedule.toString();
    }

    /**
     * Deletes a Schedule.
     *
     * @param scheduleId scheduleId as Long.
     * @return true if deletion succeeded, false if deletion failed.
     * @throws IOException   in case of failed or interrupted I/O operations.
     * @throws JSONException in case of exception during JSON processing.
     */
    public final Boolean scheduleDelete(final Long scheduleId) throws IOException, JSONException {
        JSONObject obj = new JSONObject(getPage("schedule/" + scheduleId));

        JSONObject response = new JSONObject();
        response.put("status", "Ok");
        response.put("code", 200);

        if (obj.toString().equals(response.toString())) {
            return true;
        } else {
            return false;
        }
    }

    public final String summary(final String uri) throws IOException, JSONException {
        return getPage("report/latest/" + uri);
    }

    public final String stringSummary(final String uri) throws IOException, JSONException {
        return getPage("report/latest/" + uri);
    }

    /**
     * <p>Private method that returns an authorized HttpURLConnection.</p>
     *
     * @param apiEndpoint The API endpoint to access
     * @return An HttpURLConnection with the Authorization header set
     * @throws java.net.MalformedURLException     if apiEndpoint does not result in a valid URL
     * @throws java.io.IOException                if the connection to the resulting URL cannot be established
     * @throws java.lang.IllegalStateException    if no authentication has been made prior to the use of this method
     * @throws java.lang.IllegalArgumentException if apiEndpoint is null or empty
     */
    private HttpURLConnection getAuthorizedHttpURLConnectionFor(final String apiEndpoint) throws IOException {
        //Check if a User is authenticated. If not throw IllegalStateException.
        if (authorizationToken == null) {
            throw new IllegalStateException("Can't get authorized connection. User authenticate(String, String) first.");
        }

        //Check if the apiEndpoint is not empty
        if (apiEndpoint == null || apiEndpoint.isEmpty()) {
            throw new IllegalArgumentException("apiEndpoint must not be null or empty");
        }

        //Get the URL and HttpURLConnection
        final URL url = new URL(sensorflareApiBaseUrl + apiEndpoint);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        //Set the User-Agent and Authorization headers
        connection.setRequestProperty("User-Agent", "sensorflare-java-client");
        connection.setRequestProperty("Authorization", "Basic " + authorizationToken);

        //Return the HttpURLConnection
        return connection;
    }

    /**
     * <p>Protected method that returns an HttpURLConnection with the Authorization header set, for a GET request,<br />
     * for the apiEndpoint.</p>
     *
     * @param apiEndpoint The API endEndpoint to access via GET
     * @return An HttpUrlConnection object
     *
     * @throws java.net.MalformedURLException     if apiEndpoint does not result in a valid URL
     * @throws java.io.IOException                if the connection to the resulting URL cannot be established
     * @throws java.lang.IllegalStateException    if no authentication has been made prior to the use of this method
     * @throws java.lang.IllegalArgumentException if apiEndpoint is null or empty
     */
    protected final HttpURLConnection getAuthorizedHttpUrlConnectionForGetRequest(final String apiEndpoint) throws IOException {
        //Get the authorized HttpURLConnection
        final HttpURLConnection connection = getAuthorizedHttpURLConnectionFor(apiEndpoint);
        connection.setRequestMethod("GET");
        return connection;
    }

    /**
     * <p>Protected method that returns an HttpURLConnection with the Authorization header set, for a POST request,<br />
     * for the apiEndpoint.</p>
     *
     * @param apiEndpoint The API endEndpoint to access via GET
     * @return An HttpUrlConnection object
     *
     * @throws java.net.MalformedURLException     if apiEndpoint does not result in a valid URL
     * @throws java.io.IOException                if the connection to the resulting URL cannot be established
     * @throws java.lang.IllegalStateException    if no authentication has been made prior to the use of this method
     * @throws java.lang.IllegalArgumentException if apiEndpoint is null or empty
     */
    protected final HttpURLConnection getAuthorizedHttpUrlConnectionForPostRequest(final String apiEndpoint) throws IOException {
        //Get the authorized HttpURLConnection
        final HttpURLConnection connection = getAuthorizedHttpURLConnectionFor(apiEndpoint);
        connection.setRequestMethod("POST");
        return connection;
    }

    /**
     * <p>Protected method that returns an HttpURLConnection with the Authorization header set, for a PUT request,<br />
     * for the apiEndpoint.</p>
     *
     * @param apiEndpoint The API endEndpoint to access via GET
     * @return An HttpUrlConnection object
     *
     * @throws java.net.MalformedURLException     if apiEndpoint does not result in a valid URL
     * @throws java.io.IOException                if the connection to the resulting URL cannot be established
     * @throws java.lang.IllegalStateException    if no authentication has been made prior to the use of this method
     * @throws java.lang.IllegalArgumentException if apiEndpoint is null or empty
     */
    protected final HttpURLConnection getAuthorizedHttpUrlConnectionForPutRequest(final String apiEndpoint) throws IOException {
        //Get the authorized HttpURLConnection
        final HttpURLConnection connection = getAuthorizedHttpURLConnectionFor(apiEndpoint);
        connection.setRequestMethod("PUT");
        return connection;
    }

    /**
     * <p>Protected method that returns an HttpURLConnection with the Authorization header set, for a DELETE request,<br />
     * for the apiEndpoint.</p>
     *
     * @param apiEndpoint The API endEndpoint to access via GET
     * @return An HttpUrlConnection object
     *
     * @throws java.net.MalformedURLException     if apiEndpoint does not result in a valid URL
     * @throws java.io.IOException                if the connection to the resulting URL cannot be established
     * @throws java.lang.IllegalStateException    if no authentication has been made prior to the use of this method
     * @throws java.lang.IllegalArgumentException if apiEndpoint is null or empty
     */
    protected final HttpURLConnection getAuthorizedHttpUrlConnectionForDeleteRequest(final String apiEndpoint) throws IOException {
        //Get the authorized HttpURLConnection
        final HttpURLConnection connection = getAuthorizedHttpURLConnectionFor(apiEndpoint);
        connection.setRequestMethod("DELETE");
        return connection;
    }

    /**
     * <p>Returns the response of an API call via an HttpURLConnection.</p>
     *
     * @param connection The HttpURLConnection to the API Endpoint.
     * @return The response of the API call.
     * @throws java.io.IOException if the response cannot be read.
     */
    protected final String getApiCallResponse(HttpURLConnection connection) throws IOException {
        final InputStream connectionInputStream = (connection.getResponseCode() < 400) ? connection.getInputStream() : connection.getErrorStream();
        final BufferedReader inputReader = new BufferedReader(new InputStreamReader(connectionInputStream));
        final StringBuilder responseBuilder = new StringBuilder();

        String line;
        while ((line = inputReader.readLine()) != null) {
            responseBuilder.append(line);
        }

        inputReader.close();

        return responseBuilder.toString();
    }

    /**
     * <p>Tests if the current authorization token is valid.</p>
     *
     * @return true on success, false on failure.
     * @throws java.io.IOException                if the validity of the authorization token cannot be checked.
     * @throws java.lang.IllegalArgumentException if no authentication has been made prior to the use of this method
     */
    private boolean testConnection() throws IOException {
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForGetRequest("dashboard");

        return connection.getResponseCode() == 200;
    }

    /**
     * <p>Do a GET request for the given API path and return the response as a String.</p>
     *
     * @param path The API path to do the GET request
     * @return The API call response as a String
     * @throws java.io.IOException             if a connection cannot be established
     * @throws java.lang.IllegalStateException if the connection cannot be authorized
     */
    protected final String getPage(final String path) throws IOException {

        //Create connection
        final HttpURLConnection con = getAuthorizedHttpUrlConnectionForGetRequest(path);

        //Return the response
        return getApiCallResponse(con);
    }

    /**
     * <p>Do a POST request for the given API path and return the response as a String.</p>
     *
     * @param path  The API path to do the GET request
     * @param params A map that contains the values to post
     * @return The API call response as a String
     * @throws java.io.IOException             if a connection cannot be established
     * @throws java.lang.IllegalStateException if the connection cannot be authorized
     */
    protected final String postPage(final String path, final Map<String, String> params) throws IOException {
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForPostRequest(path); //Create connection

        //Do the POST request and return the response
        return doPostOrPut(connection, params);
    }

    /**
     * <p>Do a PUT request for the given API path and return the response as a String.</p>
     *
     * @param path  The API path to do the PUT request
     * @param params A map that contains the values to put
     * @return The API call response as a String
     * @throws java.io.IOException if a connection cannot be established
     * @throws java.lang.IllegalStateException if the connection cannot be authorized
     */
    protected final String putPage(final String path, final Map<String, String> params) throws IOException {
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForPutRequest(path); //Create the connection

        //Do the PUT request and return the response
        return doPostOrPut(connection, params);
    }

    /**
     * <p>Do a DELETE request for the given API path and return the response as a String.</p>
     *
     * @param path  The API path to do the DELETE request
     * @return The API call response as a String
     * @throws java.io.IOException if a connection cannot be established
     * @throws java.lang.IllegalStateException if the connection cannot be authorized
     */
    protected final String deletePage(final String path) throws IOException {
        final HttpURLConnection connection = getAuthorizedHttpUrlConnectionForDeleteRequest(path); //Create the connection

        //Do the PUT request and return the response
        return getApiCallResponse(connection);
    }

    /**
     * <p>Do a POST or PUT request given the initialized HttpURLConnection and the Map<String, String> parameters map.</p>
     *
     * @param connection The HttpURLConnection to the API Endpoint.
     * @param params A map that contains the parameters to send.
     * @return A String with the API call response.
     *
     * @throws java.io.IOException if a connection cannot be established
     * @throws java.lang.IllegalStateException if the connection cannot be authorized
     */
    private String doPostOrPut(final HttpURLConnection connection, final Map<String, String> params) throws IOException {
        final StringBuilder postParams = new StringBuilder(); //Parameters string builder
        final BufferedWriter postParamsWriter; //Buffered writer connected to the output stream of the connection, where we write the parameters

        connection.setDoInput(true); //Enable input so that we can get the response
        connection.setDoOutput(true); //Enable output so that we can send the request parameters

        //Parse the parameters to a single encoded string
        for (final Map.Entry<String, String> param : params.entrySet()) {
            if (postParams.length() != 0) {
                postParams.append("&");
            }

            postParams.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postParams.append("=");
            postParams.append(URLEncoder.encode(param.getValue(), "UTF-8"));
        }

        postParamsWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
        postParamsWriter.write(postParams.toString());
        postParamsWriter.close();

        return getApiCallResponse(connection);
    }
}
