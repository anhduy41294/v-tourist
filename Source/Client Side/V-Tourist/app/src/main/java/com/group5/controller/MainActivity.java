package com.group5.controller;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.group5.model.Place;
import com.group5.parser.DataParser;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /*
    View pager on top
     */
    ViewPager viewPagerHome;

    /*
    Two recyclerView that it's content suggest user about some place.
     */
    RecyclerView recyclerNewUpdate;
    MyHomeRecyclerAdapter myHomeRecyclerAdapterNewUpdate;
    RecyclerView.LayoutManager layoutManagerNewUpdate;
    ArrayList<Place> arrayListNewUpdates = new ArrayList<Place>();


    RecyclerView recyclerHistory;
    MyHomeRecyclerAdapter myHomeRecyclerAdapterHistory;
    RecyclerView.LayoutManager layoutManagerHistory;
    ArrayList<Place> arrayListHistory = new ArrayList<Place>();//array list string list menu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupSlider();

        //getData();
        LoadData loadData = new LoadData();
        loadData.execute();

        //Config for Drawer navigation - start
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

//    private Place p;
//    private void getData() {
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("Place");
//        query.orderByDescending("createdAt").setLimit(5);
//        query.findInBackground(new FindCallback<ParseObject>() {
//            public void done(List<ParseObject> scoreList, ParseException e) {
//                if (e == null) {
//                    for (ParseObject score : scoreList) {
//                        try {
//                            p = DataParser.parsePlace(score);
//                            ParseRelation<ParseObject> relation = score.getRelation("images");
//                            // generate a query based on that relation
//                            ParseQuery query = relation.getQuery();
//                            query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
//                            query.getFirstInBackground(new GetCallback<ParseObject>() {
//                                @Override
//                                public void done(ParseObject object, ParseException e) {
//                                    if (object != null)
//                                    {
//                                        ParseFile imageFile = object.getParseFile("img");
//                                        p.firstImageURL = imageFile.getUrl();
//                                    }
//                                }
//                            });
//                            arrayListNewUpdates.add(p);
//                            arrayListHistory.add(p);
//                            Log.d("a", p.getPlaceName());
//                        } catch (ParseException e1) {
//                            e1.printStackTrace();
//                        }
//                    }
//                    setupListNewUpdate();
//                    setupListHistory();
//                } else {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    private class LoadData extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Place");
            query.orderByDescending("createdAt").setLimit(10);
            try {
                List<ParseObject> places = query.find();
                for (ParseObject place : places)
                {
                    if (place != null)
                    {
                        Place p = DataParser.parsePlace(place);
                        ParseRelation<ParseObject> relation = place.getRelation("images");
                        ParseQuery query2 = relation.getQuery();
                        ParseObject imageObject = query2.getFirst();
                        if (imageObject != null)
                        {
                            ParseFile imageFile = imageObject.getParseFile("img");
                            p.firstImageURL = imageFile.getUrl();
                        }
                        arrayListNewUpdates.add(p);
                        arrayListHistory.add(p);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setupListHistory();
            setupListNewUpdate();
        }
    }

    private void setupListNewUpdate() {
        recyclerNewUpdate = (RecyclerView) findViewById(R.id.recyclerNewUpdate);
        //use linear layout manager
        layoutManagerNewUpdate = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerNewUpdate.setLayoutManager(layoutManagerNewUpdate);

        myHomeRecyclerAdapterNewUpdate = new MyHomeRecyclerAdapter(getApplicationContext(), R.layout.custom_list_home, arrayListNewUpdates);
        recyclerNewUpdate.setAdapter(myHomeRecyclerAdapterNewUpdate);
    }

    private void setupListHistory() {
        recyclerHistory = (RecyclerView) findViewById(R.id.recyclerHistory);
        //use linear layout manager
        layoutManagerHistory = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerHistory.setLayoutManager(layoutManagerHistory);

        myHomeRecyclerAdapterHistory = new MyHomeRecyclerAdapter(getApplicationContext(), R.layout.custom_list_home, arrayListHistory);
        recyclerHistory.setAdapter(myHomeRecyclerAdapterHistory);

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
            case R.id.action_map:
                Intent intentMap = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intentMap);
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
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
                //not do somthing
                break;
            case R.id.nav_favourite_place:
                //navigate to favourite place activity
                Intent intentFavorite = new Intent(MainActivity.this, FavoriteActivity.class);
                startActivity(intentFavorite);
                break;
            case R.id.nav_map:
                Intent intentMap = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intentMap);
                break;
            default:
                break;

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private SliderLayout mDemoSlider;

    private void setupSlider() {
        mDemoSlider = (SliderLayout) findViewById(R.id.slider);

        HashMap<String, String> url_maps = new HashMap<String, String>();
        url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
        url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
        url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
        url_maps.put("Game of Thrones", "http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");

        HashMap<String, Integer> file_maps = new HashMap<String, Integer>();
        file_maps.put("Hồ Chí Minh", R.drawable.slide1);
        file_maps.put("Nha Trang", R.drawable.slide2);
        file_maps.put("Hạ Long", R.drawable.slide3);
        file_maps.put("Hội An", R.drawable.slide4);

        for (String name : file_maps.keySet()) {
            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra", name);

            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);

    }
}
