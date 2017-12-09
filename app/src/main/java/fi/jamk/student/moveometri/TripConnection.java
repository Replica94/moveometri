package fi.jamk.student.moveometri;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

/**
 * Utility for connecting to the external database. Most operations are
 * synchronous and should not be called from UI-threads. These methods are
 * marked with "Sync" postfix.
 */

public class TripConnection {

    public static final int ERROR_INTERNAL_ERROR = 0;
    public static final int ERROR_UNAUTHORIZED = -1;
    public static final int ERROR_CONNECTION_FAILED = -2;
    public static final int ERROR_INTERNAL_SERVER_ERROR = -3;
    public static final int ERROR_BAD_REQUEST = -4;

    //TODO move to strings somehow
    private final static String[] errorMessages =
    {
        "Internal error",
        "Invalid username or password",
        "Connection failed",
        "Internal server error",
        "Bad request"
    };

    /**
     * Return a human readable version of the return codes.
     *
     * @param error error code
     * @return Human readable string representing the error
     */
    public static String getErrorCodeMessage(int error)
    {
        if (error > 0)
        {
            return "Success";
        }
        int errIndex = -error;
        if (errIndex < 0 || errIndex >= errorMessages.length)
            return errorMessages[0];
        return errorMessages[errIndex];
    }

    private static TripConnection instance;

    public static TripConnection getInstance(Context context)
    {
        if (instance == null)
            instance = new TripConnection(context);
        return instance;
    }

    public static class Account
    {
        public int id;
        public long lastModification;
        public String name;

        public Account(JSONObject j) throws JSONException {
            id = j.getInt("id");
            lastModification = j.getLong("lastModification");
            name = j.getString("name");
        }
    }


    private Context mContext;
    private Account loginAccount = null;
    private String loginAccountName = "";
    private String loginAccountPassword = "";
    private String server = "https://skeletorium.com/mresti/";
    //private String server = "http://10.0.2.2:5511/";

    private HttpURLConnection prepareConnection(String url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setUseCaches(false);
        return c;
    }

    private HttpURLConnection prepareConnectionWithCredentials(String url, String name, String password) throws IOException {
        HttpURLConnection c = prepareConnection(url);

        String au = new String(Base64.encode((name + ":" + password).getBytes(), Base64.NO_WRAP));

        c.setRequestProperty("Authorization", "Basic " + au);
        return c;
    }

    private HttpURLConnection prepareConnectionLoggedIn(String url) throws IOException {
        if (loginAccount == null)
            throw new RuntimeException("Not logged in");
        return prepareConnectionWithCredentials(url, loginAccountName, loginAccountPassword);
    }

    private void prepareConnectionPost(HttpURLConnection c, JSONObject obj) throws IOException
    {
        String query = obj.toString();
        Log.d("TripConnection D", query);
        byte[] queryData = query.getBytes();

        c.setDoOutput(true);
        c.setFixedLengthStreamingMode(queryData.length);
        c.setRequestProperty("Content-type", "application/json; charset=UTF-8");

        OutputStream out = new BufferedOutputStream(c.getOutputStream());
        out.write(queryData);
        out.close();
    }

    private TripConnection(Context context)
    {
        mContext = context;
    }

    public Account getAccount()
    {
        return loginAccount;
    }

    public boolean isLoggedIn() { return loginAccount != null; }


    public void logOut() {
        loginAccount = null;
        loginAccountName = "";
        loginAccountPassword = "";
    }

    /**
     * Update current account status synchronously.
     * @return Zero or negative value on failure
     */
    public int updateAccountSync() {
        if (loginAccount == null)
            throw new UnsupportedOperationException("TripConnection updateAccount called when logged out");
        return loginSync(loginAccountName, loginAccountPassword);
    }

    public int getTripsSync(List<Trip> outputList)
    {
        if (loginAccount == null)
            return 0;

        String url = server.concat("trips");
        try {
            HttpURLConnection c = prepareConnectionLoggedIn(url);
            c.connect();
            if (c.getResponseCode() != 200) {
                Log.d("TripConnection FAIL", c.getResponseMessage());
                c.disconnect();
                return ERROR_UNAUTHORIZED;
            }
            Scanner s = new Scanner(c.getInputStream()).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            c.disconnect();

            JSONArray json = new JSONArray(response);

            for (int i = 0; i < json.length(); i++)
            {
                JSONObject obj = json.getJSONObject(i);
                outputList.add(new Trip(obj));
            }
            return 1;
        }
        catch (Exception e) {
            Log.d("TripConnection EX", e.toString());
        }
        return ERROR_INTERNAL_ERROR;
    }

    public int deleteTripSync(int extId)
    {
        String url = server.concat("trips/"+extId);
        try {
            HttpURLConnection c = prepareConnectionLoggedIn(url);
            c.setRequestMethod("DELETE");;
            c.connect();

            if (c.getResponseCode() != 200) {
                Log.d("TripConnection FAIL", c.getResponseMessage());
                c.disconnect();
                return ERROR_UNAUTHORIZED;
            }
            return 1;
        }
        catch (Exception e) {
            Log.d("TripConnection EX", e.toString());
        }
        return ERROR_INTERNAL_ERROR;
    }

    public int addTripSync(Trip t)
    {
        String url = server.concat("trips");
        try {

            JSONObject obj = t.toJSONObject();

            HttpURLConnection c = prepareConnectionLoggedIn(url);
            prepareConnectionPost(c, obj);
            c.connect();

            if (c.getResponseCode() != 200) {
                Log.d("TripConnection FAIL", c.getResponseMessage());
                c.disconnect();
                return ERROR_UNAUTHORIZED;
            }

            Scanner s = new Scanner(c.getInputStream()).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            c.disconnect();

            JSONObject json = new JSONObject(response);
            return json.getInt("id");
        }
        catch (Exception e) {
            Log.d("TripConnection EX", e.toString());
        }
        return ERROR_INTERNAL_ERROR;
    }

    public int loginSync(String name, String password)
    {
        String url = server.concat("account");
        try {
            HttpURLConnection c = prepareConnectionWithCredentials(url, name, password);
            c.connect();
            if (c.getResponseCode() != 200) {
                Log.d("TripConnection FAIL", c.getResponseMessage());
                c.disconnect();
                return ERROR_UNAUTHORIZED;
            }
            Scanner s = new Scanner(c.getInputStream()).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            c.disconnect();

            JSONObject json = new JSONObject(response);
            loginAccount = new Account(json);
            int id = loginAccount.id;
            loginAccountName = name;
            loginAccountPassword = password;
            return id;
        }
        catch (Exception e) {
            Log.d("TripConnection EX", e.toString());
        }
        return ERROR_INTERNAL_ERROR;
    }

    public int registerSync(String name, String password)
    {
        String url = server.concat("account");
        try {

            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("password", password);

            HttpURLConnection c = prepareConnection(url);
            prepareConnectionPost(c, obj);
            c.connect();


            if (c.getResponseCode() != 200) {
                Log.d("TripConnection FAIL", c.getResponseMessage());
                c.disconnect();
                return ERROR_BAD_REQUEST;
            }

            int id = loginSync(name, password);
            if (id <= 0) {

                Log.d("TripConnection FAIL", "Failed to login with previously registered account");
                return ERROR_INTERNAL_ERROR;
            }
            return id;
        }
        catch (Exception e) {
            Log.d("TripConnection EX", e.toString());
        }
        return ERROR_INTERNAL_ERROR;
    }

}
