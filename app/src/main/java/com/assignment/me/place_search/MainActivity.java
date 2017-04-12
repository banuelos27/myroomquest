package com.assignment.me.place_search;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.toolkit.map.MapViewHelper;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geocode.LocatorSuggestionParameters;
import com.esri.core.tasks.geocode.LocatorSuggestionResult;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {


    private static final String TAG = "PlaceSearch";
    private static final String COLUMN_NAME_ADDRESS = "address";
    private static final String COLUMN_NAME_BUILDING = "Building";
    private static final String COLUMN_NAME_X = "x";
    private static final String COLUMN_NAME_Y = "y";
    private static final String LOCATION_TITLE = "Location";
    private static final String FIND_PLACE = "Find";
    private static final String SUGGEST_PLACE = "Suggest";
    private static boolean suggestClickFlag = false;
    private static boolean searchClickFlag = false;

    //for paw
    private GraphicsLayer mLocationLayer;

    //private MapView mMapView;  DONT FORGET TO UNCOMMENT IF DEL "ADDED CODE"
    private String mMapViewState;
    // Entry point to ArcGIS for Android Toolkit
    private MapViewHelper mMapViewHelper;

    //private Locator mLocator;   DONT FORGET TO UNCOMMENT IF DEL "ADDED CODE"
    private SearchView mSearchView;
    private MenuItem searchMenuItem;
    private MatrixCursor mSuggestionCursor;

    private static ProgressDialog mProgressDialog;
    private LocatorSuggestionParameters suggestParams;
    private LocatorFindParameters findParams;

    private SpatialReference mapSpatialReference;
    private static ArrayList<LocatorSuggestionResult> suggestionsList;


    //***********ADDED CODE ***************

    //MapView mMapView;//leave

    public ArcGISFeatureLayer mFeatureLayer;//leave
    public String mFeatureServiceUrl;//leave
    //public GraphicsLayer mGraphicsLayer;//leave
    // Define ArcGIS Elements

    MapView mMapView;
    final String extern = Environment.getExternalStorageDirectory().getPath();
    //final String tpkPath = "/ArcGIS/samples/OfflineRouting/SanDiego.tpk";


    GraphicsLayer mGraphicsLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.DYNAMIC);

    // RouteTask mRouteTask = null;
    NAFeaturesAsFeature mStops = new NAFeaturesAsFeature();

    Locator mLocator = null;
    View mCallout = null;
    Spinner dSpinner;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    //***********END ADDED CODE ***************

    //original url
   // public String url = "http://roomquest.research.cse:6080/arcgis/rest/services/ThirdFloorLocator/GeocodeServer";

    //new url
    public String Url = "http://roomquest.research.cse.csusb.edu:6080/arcgis/rest/services/SecondPrototype/MapServer";


    //The following maps hold the links for all the floors of campus
    Map<Integer, String> basements    = new HashMap<Integer, String>(); //basement floor links
    Map<Integer, String> firstFloors  = new HashMap<Integer, String>(); //firstfloors links
    Map<Integer, String> secondFloors = new HashMap<Integer, String>(); //secondfloor links
    Map<Integer, String> thirdFloors  = new HashMap<Integer, String>(); //thirdfloor links
    Map<Integer, String> fourthFloors = new HashMap<Integer, String>(); //fourthfloor links
    Map<Integer, String> fifthFloors =  new HashMap<Integer, String>(); //fifthfloor links

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //*******Fill Maps ***********//
        //add basements floors to basements map
       // basements.put(0, getString(R.string.Basement));

        //add first floors to firstfloors map
       // firstFloors.put(0, getString(R.string.FirstFloors));

        //add second floors to secondfloors map
       // secondFloors.put(0,getString(R.string.SecondFloors));

        //add third  floors to thirdfloors map
       // thirdFloors.put(0, getString(R.string.ThirdFloors));

        //add fourth floors to fourthfloors map
       // fourthFloors.put(0, getString(R.string.JB4));

        //add fifth floors to fifthfloors map
       // fifthFloors.put(0, getString(R.string.JB5));

        // Setup and show progress dialog
        mProgressDialog = new ProgressDialog(this) {
            @Override
            public void onBackPressed() {
                // Back key pressed - just dismiss the dialog
                mProgressDialog.dismiss();
            }
        };

        // After the content of this activity is set the map can be accessed from the layout
        mMapView = (MapView) findViewById(R.id.map);

        //for paw
        mLocationLayer = new GraphicsLayer();
        mMapView.addLayer(mLocationLayer);

        // Initialize the helper class to use the Toolkit
        mMapViewHelper = new MapViewHelper(mMapView);
        // Create the default ArcGIS online Locator. If you want to provide your own {@code Locator},
        // user other methods of Locator.
       // String url = "http://roomquest.research.cse:6080/arcgis/rest/services/Rooms_CreateAddressLocator/GeocodeServer";
        //3rd floor locator
       // String locator_url = "http://roomquest.research.cse.csusb.edu:6080/arcgis/rest/services/FirstPrototype/MapServer";
        //this is not the address locator this is the geocode server
        String second_prototype_url = "http://roomquest.research.cse.csusb.edu:6080/arcgis/rest/services/SecondPrototype/MapServer";
        //new url
        String url1 = "http://roomquest.research.cse.csusb.edu:6080/arcgis/rest/services/FirstLocator/GeocodeServer";

        String extern = Environment.getExternalStorageDirectory().getPath();
        mLocator = Locator.createOnlineLocator(url1);
        //mLocator = Locator.createOnlineLocator();

        // set logo and enable wrap around
        mMapView.setEsriLogoVisible(true);
        mMapView.enableWrapAround(true);

        // Setup listener for map initialized
        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

            @Override
            public void onStatusChanged(Object source, STATUS status) {
                if (source == mMapView && status == OnStatusChangedListener.STATUS.INITIALIZED) {
                    mapSpatialReference = mMapView.getSpatialReference();

                    if (mMapViewState == null) {
                        Log.i(TAG, "MapView.setOnStatusChangedListener() status=" + status.toString());
                    } else {
                        mMapView.restoreState(mMapViewState);
                    }

                }
            }
        });

        final Button buttonb = (Button) findViewById(R.id.buttonb);
        buttonb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // displayToast("Basements");
                setBasementFloors();
            }
        });

        final Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // displayToast("1st Floors");
                setFirstFloors();
                //button1.setBackgroundColor(getResources().getColor(R.color.yellow));
            }
        });


        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // displayToast("2nd Floors");
                setSecondFloors();
            }
        });

        final Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // displayToast("3rd Floors");
                setThirdFloors();
            }
        });

        final Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // displayToast("4th Floors");
                setFourthFloors();
            }
        });

        final Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // displayToast("Floor 5");
                setFifthFloors();
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        //on start up set the initial layer to be the basement floor
        mFeatureServiceUrl = "http://roomquest.research.cse.csusb.edu:6080/arcgis/rest/services/SecondPrototype/MapServer/6";
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer);
        //now highlight the button
        //changeButtonBackgroundColor(0);
        //button1.setBackgroundColor(getResources().getColor(R.color.yellow));
        //buttons1.setBackgroundColor(Color.YELLOW);

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
        if (id == R.id.action_search) {
            searchMenuItem = item;
            // Create search view and display on the Action Bar
            initSearchView();
            item.setActionView(mSearchView);
            return true;
        } else if (id == R.id.action_clear) {
            // Remove all the marker graphics
            if (mMapViewHelper != null) {
                mMapViewHelper.removeAllGraphics();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if ((mSearchView != null) && (!mSearchView.isIconified())) {
            // Close the search view when tapping back button
            if (searchMenuItem != null) {
                searchMenuItem.collapseActionView();
                invalidateOptionsMenu();
            }
        } else {
            super.onBackPressed();
        }
    }


    // Initialize suggestion cursor
    private void initSuggestionCursor() {
        //can modify the params here to include other columns from the alias table
        //original
        String[] cols = new String[]{BaseColumns._ID, COLUMN_NAME_ADDRESS, COLUMN_NAME_X, COLUMN_NAME_Y};
        //modified
        //String[] cols = new String[]{BaseColumns._ID, COLUMN_NAME_ADDRESS,COLUMN_NAME_BUILDING, COLUMN_NAME_X, COLUMN_NAME_Y};
        mSuggestionCursor = new MatrixCursor(cols);
    }

    // Set the suggestion cursor to an Adapter then set it to the search view
    private void applySuggestionCursor() {
        //original
        String[] cols = new String[]{COLUMN_NAME_ADDRESS};
        //modified
        //String[] cols = new String[]{COLUMN_NAME_ADDRESS,COLUMN_NAME_BUILDING};
        int[] to = new int[]{R.id.suggestion_item_address};

        SimpleCursorAdapter mSuggestionAdapter = new SimpleCursorAdapter(mMapView.getContext(), R.layout.suggestion_item, mSuggestionCursor, cols, to, 0);
        mSearchView.setSuggestionsAdapter(mSuggestionAdapter);
        mSuggestionAdapter.notifyDataSetChanged();
    }

    // Initialize search view and add event listeners to handle query text changes and suggestion
    private void initSearchView() {
        if (mMapView == null || !mMapView.isLoaded())
            return;

        mSearchView = new SearchView(this);
        mSearchView.setFocusable(true);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryHint(getResources().getString(R.string.search_hint));// search bar hint

        // Open the soft keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!suggestClickFlag && !searchClickFlag) {
                    searchClickFlag = true;
                    onSearchButtonClicked(query);
                    mSearchView.clearFocus();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (mLocator == null)
                    return false;
                getSuggestions(newText); //lets user see suggestions while he types
                return true;
            }
        });

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                // Obtain the content of the selected suggesting place via cursor
                MatrixCursor cursor = (MatrixCursor) mSearchView.getSuggestionsAdapter().getItem(position);
                int indexColumnSuggestion = cursor.getColumnIndex(COLUMN_NAME_ADDRESS);
                final String address = cursor.getString(indexColumnSuggestion);

                //added the toast below that works to display the address searched
                //Toast.makeText(getBaseContext(), address , Toast.LENGTH_SHORT ).show();
                setFloor(address);
                //setFloor(x);
                suggestClickFlag = true;

                // Find the Location of the suggestion
                new FindLocationTask(address).execute(address);


                cursor.close();

                return true;
            }
        });
    }
    //function checks for the address the user types and displays a toast message with the
    // corresponding floor number
    public void setFloor(String address){
        for(int i = 0; i < address.length(); i++){
            char c = address.charAt(i);
            if(c == '1'){
                Toast.makeText(getBaseContext(), "First Floor" , Toast.LENGTH_SHORT ).show();
                setFirstFloors();
                break;
            }
            else if(c == '2'){
                Toast.makeText(getBaseContext(), "Second Floor" , Toast.LENGTH_SHORT ).show();
                setSecondFloors();
                break;
            }
            else if(c == '3'){
                Toast.makeText(getBaseContext(), "Third Floor" , Toast.LENGTH_SHORT ).show();
                setThirdFloors();
                break;
            }
            else if(c == '4'){
                Toast.makeText(getBaseContext(), "Fourth Floor" , Toast.LENGTH_SHORT ).show();
                setFourthFloors();
                break;
            }
            else if(c == '5'){
                Toast.makeText(getBaseContext(), "Fifth Floor" , Toast.LENGTH_SHORT ).show();
                setFifthFloors();
                break;
            }
        }
        return;
    }


    /**
     * Called from search_layout.xml when user presses Search button.
     *
     * @param address The text in the searchbar to be geocoded
     *
     */
    public void onSearchButtonClicked(String address) {
        hideKeyboard();
        mMapViewHelper.removeAllGraphics();
        executeLocatorTask(address);
    }

    private void executeLocatorTask(String address) {

        //Create Locator parameters from single line address string
        locatorParams(FIND_PLACE, address);

        //Execute async task to find the address
        LocatorAsyncTask locatorTask = new LocatorAsyncTask();
        locatorTask.execute(findParams);

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.assignment.me.place_search/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.assignment.me.place_search/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /*
       * This class provides an AsyncTask that performs a geolocation request on a
       * background thread and displays the first result on the map on the UI
       * thread.
       */
    private class LocatorAsyncTask extends
            AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {

        private Exception mException;

        public LocatorAsyncTask() {
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.setMessage(getString(R.string.address_search));
            mProgressDialog.show();
        }

        @Override
        protected List<LocatorGeocodeResult> doInBackground(
                LocatorFindParameters... params) {
            // Perform routing request on background thread
            mException = null;
            List<LocatorGeocodeResult> results = null;

            // Create locator using default online geocoding service and tell it
            // to
            // find the given address

            String url1 = "http://roomquest.research.cse.csusb.edu:6080/arcgis/rest/services/FirstLocator/GeocodeServer";
            //third floor locator
           // String URL = "http://roomquest.research.cse.csusb.edu:6080/arcgis/rest/services/FirstPrototype/MapServer";
            String extern = Environment.getExternalStorageDirectory().getPath();
            Locator locator = Locator.createOnlineLocator(url1);
            try {
                results = locator.find(params[0]);
            } catch (Exception e) {
                mException = e;
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<LocatorGeocodeResult> result) {
            // Display results on UI thread
            mProgressDialog.dismiss();
            if (mException != null) {
                Log.w(TAG, "LocatorSyncTask failed with:");
                mException.printStackTrace();
                Toast.makeText(MainActivity.this,
                        getString(R.string.addressSearchFailed),
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (result.size() == 0) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.noResultsFound), Toast.LENGTH_LONG)
                        .show();
            } else {
                // Use first result in the list
                LocatorGeocodeResult geocodeResult = result.get(0);

                // get return geometry from geocode result
                Point resultPoint = geocodeResult.getLocation();

                double x = resultPoint.getX();
                double y = resultPoint.getY();

                // Get the address
                String address = geocodeResult.getAddress();

                // Display the result on the map
                displaySearchResult(x, y, address);

                hideKeyboard();

            }
        }

    }


    //Fetch the Location from the Map and display it
    private class FindLocationTask extends AsyncTask<String, Void, Point> {
        private Point resultPoint = null;
        private String resultAddress;
        private Point temp = null;

        public FindLocationTask(String address) {
            resultAddress = address;
        }

        @Override
        protected Point doInBackground(String... params) {

            // get the Location for the suggestion from the map
            for (LocatorSuggestionResult result : suggestionsList) {
                if (resultAddress.matches(result.getText())) {
                    try {
                        temp = ((mLocator.find(result, 2, null, mapSpatialReference)).get(0)).getLocation();
                    } catch (Exception e) {
                        Log.e(TAG, "Exception in FIND");
                        Log.e(TAG, e.getMessage());
                    }
                }
            }

            resultPoint = (Point) GeometryEngine.project(temp, mapSpatialReference, SpatialReference.create(4326));

            return resultPoint;
        }

        @Override
        protected void onPreExecute() {
            // Display progress dialog on UI thread
            mProgressDialog.setMessage(getString(R.string.address_search));
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Point resultPoint) {
            // Dismiss progress dialog
            mProgressDialog.dismiss();
            if (resultPoint == null)
                return;

            // Display the result
            displaySearchResult(resultPoint.getX(), resultPoint.getY(), resultAddress);
            hideKeyboard();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mMapViewState = mMapView.retainState();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start the MapView running again
        if (mMapView != null) {
            mMapView.unpause();
            if (mMapViewState != null) {
                mMapView.restoreState(mMapViewState);
            }
        }
    }

    /**
     * When the user types on the search bar, the following code suggests addresses for him
     *
     * @param suggestText String the user typed so far to fetch the suggestions
     */

    protected void getSuggestions(String suggestText) {
        final CallbackListener<List<LocatorSuggestionResult>> suggestCallback = new CallbackListener<List<LocatorSuggestionResult>>() {
            @Override
            public void onCallback(List<LocatorSuggestionResult> locatorSuggestionResults) {
                final List<LocatorSuggestionResult> locSuggestionResults = locatorSuggestionResults;
                if (locatorSuggestionResults == null)
                    return;
                suggestionsList = new ArrayList<>();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int key = 0;
                        if (locSuggestionResults.size() > 0) {
                            // Add suggestion list to a cursor
                            initSuggestionCursor();
                            for (final LocatorSuggestionResult result : locSuggestionResults) {
                                suggestionsList.add(result);

                                // Add the suggestion results to the cursor
                                mSuggestionCursor.addRow(new Object[]{key++, result.getText(), "0", "0"});
                            }

                            applySuggestionCursor();
                        }
                    }

                });

            }


            @Override
            public void onError(Throwable throwable) {
                //Log the error
                Log.e(MainActivity.class.getSimpleName(), "No Results found!!");
                Log.e(MainActivity.class.getSimpleName(), throwable.getMessage());
            }
        };

        try {
            // Initialize the LocatorSuggestion parameters
            locatorParams(SUGGEST_PLACE, suggestText);

            mLocator.suggest(suggestParams, suggestCallback);

        } catch (Exception e) {
            Log.e(MainActivity.class.getSimpleName(), "No Results found");
            Log.e(MainActivity.class.getSimpleName(), e.getMessage());
        }
    } //END getSuggestion()

    /**
     * Initialize the LocatorSuggestionParameters or LocatorFindParameters
     *
     * @param query The string for which the locator parameters are to be initialized
     */
    protected void locatorParams(String TYPE, String query) {

        if (TYPE.contentEquals(SUGGEST_PLACE)) {
            suggestParams = new LocatorSuggestionParameters(query);
            // Use the centre of the current map extent as the suggest location point
            suggestParams.setLocation(mMapView.getCenter(), mMapView.getSpatialReference());
            // Set the radial search distance in meters
            suggestParams.setDistance(500.0);
        } else if (TYPE.contentEquals(FIND_PLACE)) {
            findParams = new LocatorFindParameters(query);
            //Use the center of the current map extent as the find point
            findParams.setLocation(mMapView.getCenter(), mMapView.getSpatialReference());
            // Set the radial search distance in meters
            findParams.setDistance(500.0);
        }


    }

    /**
     * Display the search location on the map
     *
     * @param x       Longitude of the place
     * @param y       Latitude of the place
     * @param address The address of the location
     *
     */
    protected void displaySearchResult(double x, double y, String address) {
        // Add a marker at the found place. When tapping on the marker, a Callout with the address
        // will be displayed


        // Use first result in the list
        // get return geometry from geocode result
        //Point resultPoint = geocodeResult.getLocation();
        // create marker symbol to represent location
        // Use first result in the list
        //LocatorGeocodeResult geocodeResult = result.get(0);

        // get return geometry from geocode result
        //for adding paw
        /*Point resultPoint = geocodeResult.getLocation();

        double x = resultPoint.getX();
        double y = resultPoint.getY();
        SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(
                Color.RED, 10, SimpleMarkerSymbol.STYLE.CROSS);
        // create graphic object for resulting location
        Graphic resultLocGraphic = new Graphic(resultPoint,
                resultSymbol);
        // add graphic to location layer
        mLocationLayer.addGraphic(resultLocGraphic);
      */

        mMapViewHelper.addMarkerGraphic(y, x, LOCATION_TITLE, address, android.R.drawable.ic_menu_myplaces, null, false, 1);
       // mMapViewHelper.addMarkerGraphic(y, x, LOCATION_TITLE, address, R.drawable.black_paw_csusb, null, false, 1);
        mMapView.centerAndZoom(y, x, 21);
        mSearchView.setQuery(address, true);
        searchClickFlag = false;
        suggestClickFlag = false;

    }

    protected void hideKeyboard() {

        // Hide soft keyboard
        mSearchView.clearFocus();
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
    }

    //BEGIN ADDING FLOORS TO MAP
    public void setBasementFloors(){
        mMapView.removeLayer(mFeatureLayer);
        //mFeatureServiceUrl = basements.get(0);//set all floors in the basement level, one by one
        mFeatureServiceUrl = "http://roomquest.research.cse.csusb.edu:6080/arcgis/rest/services/SecondPrototype/MapServer/8";
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer);
        //now highlight the button
        //changeButtonBackgroundColor(0);

    }

    //set 1st floors
    public void setFirstFloors(){
        mMapView.removeLayer(mFeatureLayer);
        mFeatureServiceUrl = "http://roomquest.research.cse.csusb.edu:6080/arcgis/rest/services/SecondPrototype/MapServer/6";
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer);
        //now highlight the button
        //changeButtonBackgroundColor(1);

    }//END setFirstFloors()


    //set 2nd floors
    public void setSecondFloors(){

        mMapView.removeLayer(mFeatureLayer);
        //mMapView.removeLayer(mFeatureLayer);
        mFeatureServiceUrl = "http://roomquest.research.cse.csusb.edu:6080/arcgis/rest/services/SecondPrototype/MapServer/13";//set all floors in the basement level, one by one
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer);
        //now highlight the button
        //changeButtonBackgroundColor(2);



    }//END setSecondFloors()


    //set 3rd floors
    public void setThirdFloors(){
        mMapView.removeLayer(mFeatureLayer);
        //mMapView.removeLayer(mFeatureLayer);
        mFeatureServiceUrl ="http://roomquest.research.cse.csusb.edu:6080/arcgis/rest/services/SecondPrototype/MapServer/7";
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer);
        //now highlight the button
        //changeButtonBackgroundColor(3);



    }//END setThirdFloors()


    //set 4th floors
    public void setFourthFloors(){
        mMapView.removeLayer(mFeatureLayer);
        //mMapView.removeLayer(mFeatureLayer);
        mFeatureServiceUrl = fourthFloors.get(0);//set all floors in the basement level, one by one
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer);
        //now highlight the button
        //changeButtonBackgroundColor(4);


    }//END setFourthFloors()


    //set 5th floors
    public void setFifthFloors(){
        mMapView.removeLayer(mFeatureLayer);
        //set floors
        //mMapView.removeLayer(mFeatureLayer);
        mFeatureServiceUrl = fifthFloors.get(0);//set all floors in the basement level, one by one
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer);
        //now highlight the button
        //changeButtonBackgroundColor(5);

    }//END setFifthFloors()

    //Jose Banuelos
    //changing the background color of the floor buttons
    //can be used along with the function that switches layers
    //bases on what the user types in
/*
    public Button buttonb = (Button) findViewById(R.id.buttonb);

    public Button button1 = (Button) findViewById(R.id.button1);

    public Button button2 = (Button) findViewById(R.id.button2);

    public Button button3 = (Button) findViewById(R.id.button3);

    public Button button4 = (Button) findViewById(R.id.button4);

    public Button button5 = (Button) findViewById(R.id.button5);
*/
    /*
    public void changeButtonBackgroundColor(int whatFloor)
    {
        //check what floor your in and change the button color to yellow
        switch(whatFloor){
            case 0:
                buttonsb.setBackgroundColor(getResources().getColor(R.color.yellow));
                //set the other colors back to normal
                buttons1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons5.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 1:
                buttons1.setBackgroundColor(getResources().getColor(R.color.yellow));
                //now set the other colors back to normal
                buttonsb.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons5.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 2:
                buttons2.setBackgroundColor(getResources().getColor(R.color.yellow));
                //normal
                buttonsb.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons5.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 3:
                buttons3.setBackgroundColor(getResources().getColor(R.color.yellow));
                //normal
                buttonsb.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons5.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 4:
                buttons4.setBackgroundColor(getResources().getColor(R.color.yellow));
                //normal
                buttonsb.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons5.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 5:
                buttons5.setBackgroundColor(getResources().getColor(R.color.yellow));
                //normal
                buttonsb.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                buttons4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            default:
                //by default set it to the first floor
                resetButtonBackgroundColor();
                break;
        }
    }

    //now this will reset the floors and by default highlight
    //the first floor
    public void resetButtonBackgroundColor(){
        buttonsb.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        buttons1.setBackgroundColor(getResources().getColor(R.color.yellow));
        buttons2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        buttons3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        buttons4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        buttons5.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }
*/

}
