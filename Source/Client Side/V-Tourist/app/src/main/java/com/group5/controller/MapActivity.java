package com.group5.controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.group5.model.MyItem;
import com.group5.model.Place;
import com.group5.service.PlaceServices;
import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public GoogleMap googleMap; //googlemap object
    public List<Place> placeList = null;
    private LatLng destonClick;
    private ClusterManager<MyItem> mClusterManager;
    EditText txtSearch;
    Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Config for Drawer navigation - start
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        txtSearch = (EditText) findViewById(R.id.txtSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtSearch.getText().toString().equals("")) {
                    HttpAsyncTask httpAsyncTask = new HttpAsyncTask();
                    httpAsyncTask.execute(txtSearch.getText().toString());
                }
            }
        });
        //goolgemap
        if (googleMap == null) {
            googleMap = ((com.google.android.gms.maps.MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        } else {
            setupMap();
        }

    }

    public void setupMap() {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.setBuildingsEnabled(true);
        LatLng dest = new LatLng(10.7715007, 106.6959833);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dest, 14));
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                destonClick = latLng;
                placeList = null;
                googleMap.clear();

                addMaker(latLng);
                drawCircle(latLng);

                DownloadPlaceTask downloadPlaceTask = new DownloadPlaceTask();
                downloadPlaceTask.execute(latLng);
            }
        });
    }

    private class DownloadPlaceTask extends AsyncTask<LatLng, Void, Void> implements ClusterManager.OnClusterItemInfoWindowClickListener<MyItem> {
        @Override
        protected Void doInBackground(LatLng... params) {
            try {
                placeList = PlaceServices.getPlacesListByGeo(params[0].latitude, params[0].longitude, 2.0);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void places) {
            super.onPostExecute(places);
            //Update map
            if (placeList.size() == 0) {
                Toast.makeText(getApplication(), "Không tìm thấy địa điểm nào !", Toast.LENGTH_LONG).show();
            }

            mClusterManager = new ClusterManager<MyItem>(getApplicationContext(), googleMap);
            mClusterManager.setRenderer(new MyClusterRenderer(getApplicationContext(), googleMap, mClusterManager));

            googleMap.setOnCameraChangeListener(mClusterManager);
            googleMap.setOnMarkerClickListener(mClusterManager);
            googleMap.setOnInfoWindowClickListener(mClusterManager);
            mClusterManager.setOnClusterItemInfoWindowClickListener(this);

            List<MyItem> items = new ArrayList<MyItem>();
            for (Place p : placeList) {
                MyItem myItem = new MyItem(p.getLatitude(), p.getLongitude(), p.getPlaceName());
                items.add(myItem);
            }
            mClusterManager.addItems(items);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destonClick, 14));
            googleMap.setOnMarkerClickListener(mClusterManager);
            mClusterManager.cluster();

        }

        @Override
        public void onClusterItemInfoWindowClick(MyItem myItem) {
            for (int j = 0; j < placeList.size(); j++) {
                if (placeList.get(j).getLatitude() == myItem.getPosition().latitude && placeList.get(j).getLongitude() == myItem.getPosition().longitude) {
                    GlobalVariable.idGlobalPlaceCurrent = placeList.get(j).getPlaceId();
                    GlobalVariable.latitute = placeList.get(j).getLatitude();
                    GlobalVariable.longtitute = placeList.get(j).getLongitude();
                    GlobalVariable.name = placeList.get(j).getPlaceName();

                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }
            }
        }

        private class MyClusterRenderer extends DefaultClusterRenderer<MyItem> {

            public MyClusterRenderer(Context context, GoogleMap map,
                                     ClusterManager<MyItem> clusterManager) {
                super(context, map, clusterManager);
            }

            @Override
            protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
                super.onBeforeClusterItemRendered(item, markerOptions);

                markerOptions.title(item.getNamePlace() + " | Chỉ đường");
            }

            @Override
            protected void onClusterItemRendered(MyItem clusterItem, Marker marker) {
                super.onClusterItemRendered(clusterItem, marker);

                //here you have access to the marker itself
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        } else {
            setupMap();
        }
    }

    @Override
    protected void onDestroy() {
        if (googleMap != null) {
            googleMap = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                //navigate to home activity
                Intent intentHome = new Intent(MapActivity.this, MainActivity.class);
                startActivity(intentHome);
                break;
            case R.id.nav_favourite_place:
                //navigate to favourite place activity
                Intent intentFavorite = new Intent(MapActivity.this, FavoriteActivity.class);
                startActivity(intentFavorite);
                break;
            case R.id.nav_map:
                break;
            default:
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        switch (id) {
            case R.id.action_settings:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String GET(String query) {
        HttpURLConnection con = null;
        InputStream is = null;
        String url = "http://maps.googleapis.com/maps/api/geocode/json?address=";
        url = url + Uri.encode(query);
        Log.i("abc", url);
        try {
            /**
             * Create Connection
             */
            con = (HttpURLConnection) (new URL(url)).openConnection();
            //Set Request Method
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            //Connect
            con.connect();
            // Let's read the response
            StringBuffer buffer = new StringBuffer();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null)
                buffer.append(line + "\r\n");
            //Close Connection and Stream
            is.close();
            con.disconnect();
            //Return Data
            return buffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Throwable t) {
            }
            try {
                con.disconnect();
            } catch (Throwable t) {
            }
        }
        return null;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... querys) {
            String res = GET(querys[0]);
            Log.i("icuud", res);
            return res;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            JSONObject json = null; // convert String to JSONObject
            try {
                Log.i("icuud", result);
                json = new JSONObject(result);
                JSONArray places = json.getJSONArray("results"); // get articles array
                if (places.length() == 0)// --> 2
                {
                    Toast.makeText(getBaseContext(), "Không tìm thấy địa điểm nào!", Toast.LENGTH_LONG).show();
                    return;
                }
                JSONObject p = places.getJSONObject(0).getJSONObject("geometry").getJSONObject("location"); // get first article in the array
                Double longtitude, latitude;
                longtitude = p.getDouble("lng");
                latitude = p.getDouble("lat");
                String title = places.getJSONObject(0).getString("formatted_address");
                Log.i("title", title);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtitude), 15));
                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_default)).anchor(0.0f,
                        0.0f).position(new LatLng(latitude, longtitude)).title(title));
            } catch (JSONException e) {
                Log.d("parse", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void drawCircle(LatLng location) {
        CircleOptions options = new CircleOptions();
        options.center(location);
        //Radius in meters
        options.radius(2000);
        options.fillColor(getResources()
                .getColor(R.color.fill_color));
        options.strokeColor(getResources()
                .getColor(R.color.black));
        options.strokeWidth(0);
        googleMap.addCircle(options);
    }

    private void addMaker(LatLng location) {
        MarkerOptions options = new MarkerOptions().position(location);
        options.title("Trung tâm");

        options.icon(BitmapDescriptorFactory.defaultMarker());
        googleMap.addMarker(options);
    }
}