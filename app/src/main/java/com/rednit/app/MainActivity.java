package com.rednit.app;

import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.rednit.app.Controller.MyLocation;
import com.rednit.app.Model.FiwareContextJson;
import com.rednit.app.Model.MyFacebook;
import com.rednit.app.Util.MyTwitterApiClient;
import com.rednit.app.Util.Util;
import com.rednit.app.View.HomeFragment;
import com.rednit.app.View.ResultListFragment;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.FavoriteService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends ActionBarActivity
        implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultListFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "MEhrB2Z8cdbUP0P97vnrbFjZy";
    private static final String TWITTER_SECRET = "ULgjeTVKOhAmpUh8zA9guMUU243kqTwj095TR2o6cZnNNeYGww";
    private String TAG = "Twitter";

    private Util utils;

    /*TWITTER VARIABLES*/
    private TwitterLoginButton twitterLoginButton;

    /*FACEBOOK VARIABLES*/
    private List<String> permissions = Arrays.asList("public_profile", "email", "user_likes");
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private LoginButton loginButton;

    /*GOOGLE VARIABLES*/
    private static GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 0;
    private boolean mSignInClicked;
    private boolean mIntentInProgress;

    private String likedPages;
    MyLocation gps;

    private MyFacebook myFacebook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_main);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        FlowManager.init(this);

        callbackManager = CallbackManager.Factory.create();

        utils = new Util();
        //        this.createDefaults();

        findViewById(R.id.main_btn_google).setOnClickListener(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();

        //        facebookLogOut();
        googleLogOut();

        myFacebook = new MyFacebook();

        this.facebookSetup();
//        https://docs.fabric.io/android/twitter/access-rest-api.html
//        https://dev.twitter.com/node/1180/twittercore
//        https://developers.facebook.com/apps/731261337003089/settings/
        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                callLoginLoadingScreen();

                TwitterSession session = Twitter.getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                String token = authToken.token;
                String secret = authToken.secret;
                Log.i("TwitterToken", token);    //211736597-Jkr7pjIVsjzvwT8hEZewTlXT4Sck1HvfvUYfbTXh
                Log.i("TwitterSecret", secret);  //qWnD6KgqoVrQ0NbuqFZev79bkhTHrjaX5r5g09Zt8Hfbc
                Log.i("Id", String.valueOf(session.getId()));
                Log.i("UserId", String.valueOf(session.getUserId()));
                Log.i("Username", session.getUserName());


                MyTwitterApiClient twitterApiClient = new MyTwitterApiClient(session);
//                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                // Can also use Twitter directly: Twitter.getApiClient()

                //Obtem os tweets favoritos
                FavoriteService favoriteService = twitterApiClient.getFavoriteService();
                favoriteService.list(session.getUserId(), null, null, null, null, null, new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> result) {
                        Log.i("Success2", "");
                        List<Tweet> l = result.data;
                        for (int i = 0; i < l.size(); i++) {
                            Log.i("Result:", l.get(i).text);
                        }

                        final JSONObject jo = new JSONObject();
                        try {
                            jo.put("name", "Leo1 From Android");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        postToServer("http://10.1.1.5:3000/api/accounts", jo);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Log.i("Failure2", "");
                    }
                });


                //Obtem a lista de amigos que o usuário segue
                twitterApiClient.getFriendsService().idsByUserId(session.getUserId(), new Callback<MyTwitterApiClient.Ids>() {
                    @Override
                    public void success(Result<MyTwitterApiClient.Ids> result) {
                        //success
                        Log.i("Result3", "");
                        result.data.printIds();
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        //failure
                        Log.i("Failure3", exception.getMessage());

                    }
                });

                //Obtem informações do perfil do usuário
                twitterApiClient.getAccountService().verifyCredentials(true, false, new Callback<User>() {
                    @Override
                    public void success(Result<User> userResult) {
                        Log.i("Result4", "");
                        User u = userResult.data;
                        Log.i("Name", u.name);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Log.i("Failure4", e.getMessage());
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });

    }

    public void postToServer(final String urlParam, final JSONObject jdata){
        new Thread(new Runnable() {
            public void run() {
                URL url = null;
                try {
                    url = new URL(urlParam);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    System.out.println("Dado: "+jdata.get("name") );

                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(jdata.toString().getBytes());

                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("HTTP Response is : ", serverResponseMessage + ": " + serverResponseCode);

                    //Get Response. If you need to get a response you can uncomment this code below
//                    if(serverResponseCode == HttpURLConnection.HTTP_OK){
//                        InputStream is = conn.getInputStream();
//                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//                        String line;
//                        StringBuffer response = new StringBuffer();
//                        while ((line = rd.readLine()) != null) {
//                            response.append(line);
//                            response.append('\r');
//                            Log.i("Line: ", line);
//                        }
//                        rd.close();
//                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    @Override
    public void onResume(){
        super.onResume();
        //        loadingTextView.setText(R.string.main_txt_loading);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Google
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }
            mIntentInProgress = false;
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        }
        //Facebook
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }


        //Twitter
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    //Google
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress) {
            if (mSignInClicked && result.hasResolution()) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                try {
                    result.startResolutionForResult(this, RC_SIGN_IN);
                    mIntentInProgress = true;
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    //Google
    @Override
    public void onConnected(Bundle connectionHint) {
        mSignInClicked = false;
        callLoginLoadingScreen();
    }

    public void onDisconnected() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else{
            if (id == R.id.action_fb_logout) {
                facebookLogOut();
                Fragment fg = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if(fg != null){
                    getSupportFragmentManager().beginTransaction().
                            remove(fg).commit();
                }
            }else{
                if (id == R.id.action_twitter_logout) {
                    twitterLogOut();
                    Fragment fg = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if(fg != null){
                        getSupportFragmentManager().beginTransaction().
                                remove(fg).commit();
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void twitterLogOut() {
        //https://twittercommunity.com/t/fabric-logout/29947/6
        Twitter.getInstance();
        Twitter.logOut();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_btn_google && !mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
        //        if (view.getId() == R.id.main_btn_google) {
        else{
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();
            }
        }
    }

    private void facebookSetup(){
        loginButton = (LoginButton) findViewById(R.id.main_btn_facebook);
        AccessToken.refreshCurrentAccessTokenAsync();

        if(AccessToken.getCurrentAccessToken() == null) {
            if(!utils.checkConnection(MainActivity.this)) {
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "Nao ha conexao", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                loginButton.setReadPermissions(permissions);
                loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        System.out.println("Facebook Success");
                        callLoginLoadingScreen();
                    }

                    @Override
                    public void onCancel() {
                        System.out.println("Facebook Cancel");
                        AccessToken.refreshCurrentAccessTokenAsync();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        System.out.println("Facebook Error");
                    }
                });
            }
        } else {

            Profile profile = Profile.getCurrentProfile();
//            System.out.println(profile.getId());
//            //            callLoginLoadingScreen();
//
//            //            /* make the API call */
//            new GraphRequest(
//                             AccessToken.getCurrentAccessToken(),
//                             "/" + profile.getId() + "/likes",
//                             null,
//                             HttpMethod.GET,
//                             new GraphRequest.Callback() {
//                public void onCompleted(GraphResponse response) {
//                    if(response != null) {
//                        try {
//                            JSONObject json = response.getJSONObject();
//                            System.out.println(json.toString());
//                            JSONArray data = json.getJSONArray("data");
//                            setLikedPages(data.toString());
//                            extractLikes(profile.getId(), json.getJSONObject("paging").getJSONObject("cursors").getString("after"));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//                             ).executeAsync();



//            extractLikes(profile.getId(), "");
            if(profile != null) {
                myFacebook.extractLikes(profile.getId(), "");
            }
            gps = new MyLocation(MainActivity.this);

            if(gps.canGetLocation()){

                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                try {
//                    putDataToServer(new FiwareContextJson(profile.getId()).locationJson(latitude, longitude));
                    myFacebook.putDataToServer(new FiwareContextJson(profile.getId()).locationJson(latitude, longitude));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

                // \n is for new line
                //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            }else{
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
            }


            //
            //            Bundle parameters = new Bundle();
            //            parameters.putString("fields", "id,name,link");
            //
            //            GraphRequest graphRequest = new GraphRequest(
            //                    AccessToken.getCurrentAccessToken(),
            //                    profile.getId(),
            //                    null,
            //                    HttpMethod.GET,
            //                    new GraphRequest.Callback() {
            //                        public void onCompleted(GraphResponse response) {
            //                            if(response != null)
            //                                System.out.println(response.toString());
            //                        }
            //                    }
            //            );
            //            graphRequest.setParameters(parameters);
            //            graphRequest.executeAsync();

            callLoginLoadingScreen();
        }


    }

    public static void facebookLogOut() {
        AccessToken.setCurrentAccessToken(null);
        Profile.setCurrentProfile(null);
    }

    public static void googleLogOut(){
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }

    public static boolean facebookIsConnected(){
        return (AccessToken.getCurrentAccessToken() != null);
    }

    public static boolean googleIsConnected() { return mGoogleApiClient.isConnected(); }

    public static boolean hasSocialConnection(){ return (facebookIsConnected() || googleIsConnected()); }

    public static String whitchIsConnected(){ return facebookIsConnected() ? "facebook" : "google";}

    private void callLoginLoadingScreen(){
        getSupportFragmentManager()
                .beginTransaction()
//                .add(R.id.fragment_container, new HomeFragment())
                .add(R.id.fragment_container, new ResultListFragment())
                .commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}