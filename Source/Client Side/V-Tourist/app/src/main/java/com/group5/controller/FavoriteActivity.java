package com.group5.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.group5.model.Bookmark;
import com.group5.model.User;
import com.group5.service.BookmarkServices;
import com.group5.service.UserServices;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private List<Bookmark> listItem;
    private TextView txtNote;
	FavoriteListAdapter adapter;

    MenuItem loginMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        txtNote = (TextView) findViewById(R.id.txt_LoginRequire);
        setSupportActionBar(toolbar);
        this.setTitle("Địa điểm yêu thích");

        //Config for Drawer navigation - start
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){


            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                GlobalVariable.setLoginTitle(loginMenuItem);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loginMenuItem = navigationView.getMenu().getItem(3);


    }

    public void setItemClickOfRecyclerView() {
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(FavoriteActivity.this, recyclerView, new ClickListener() {
            @Override
            public void onClick(View v, int position) {
                try {
                    Bookmark itemSelected = listItem.get(position);
                    showDialog(itemSelected);
                } catch (Exception ex) {}
            }

            @Override
            public void onLongClick(View v, int position) {

            }
        }));
    }

    public void showDialog(final Bookmark item) {
        AlertDialog.Builder b = new AlertDialog.Builder(FavoriteActivity.this);
        b.setTitle("Chọn chức năng");
        b.setNeutralButton("Xem chi tiết", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                GlobalVariable.idGlobalPlaceCurrent = item.getPlace().getPlaceId();
                GlobalVariable.longtitute = item.getPlace().getLongitude();
                GlobalVariable.latitute = item.getPlace().getLatitude();
                GlobalVariable.name = item.getPlace().getPlaceName();
                GlobalVariable.firstImageUrl = item.getPlace().firstImageURL;
                Intent intent = new Intent(FavoriteActivity.this, DetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        b.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        b.setNegativeButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    BookmarkServices.deleteBookmark(item.getId());
					adapter.data.remove(item);
					adapter.notifyDataSetChanged();
                } catch (Exception ex) {}
            }
        });
        b.create().show();
    }

    private void getPlacefromParse(User currentUser)
    {
        GetDataThread getTask = new GetDataThread(FavoriteActivity.this, currentUser.getId());
        getTask.execute();

    }

    private class GetDataThread extends AsyncTask<Void, Void, List<Bookmark>> {

        Activity activity;
        String currentUser;
        ProgressDialog Loading;

        public GetDataThread(Activity context, String CurrentUser) {
            activity = context;
            currentUser = CurrentUser;
            Loading = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Loading.setTitle("Loading...");
            Loading.setCanceledOnTouchOutside(false);
            Loading.show();
        }

        @Override
        protected List<Bookmark> doInBackground(Void... params) {
            List<Bookmark> bm = new ArrayList<>();
            try {
                //bm = BookmarkServices.getBookmarkList(currentUser);
                bm = BookmarkServices.getBookmarkList();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return bm;
        }

        @Override
        protected void onPostExecute(List<Bookmark> result) {
            super.onPostExecute(result);
            if(result.size() != 0) {
                listItem = result;
                RecyclerView lv = (RecyclerView) activity.findViewById(R.id.list_Favorite);
                adapter = new FavoriteListAdapter(activity, listItem);
                Loading.dismiss();
                lv.setAdapter(adapter);
            } else {
                Loading.dismiss();
                txtNote.setText("Bạn chưa có Bookmark nào.");
                txtNote.setVisibility(View.VISIBLE);
            }
        }
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
        switch (id){
            case R.id.nav_home:
                //navigate to home activity
                Intent intentHome = new Intent(FavoriteActivity.this,MainActivity.class);
                startActivity(intentHome);
                break;
            case R.id.nav_favourite_place:
                break;
            case R.id.nav_map:
                Intent intentMap = new Intent(FavoriteActivity.this,MapActivity.class);
                startActivity(intentMap);
                break;
            case R.id.nav_login:
                loginMenuItem = item;
                if (UserServices.getCurrentUser() != null)
                {
                    ParseUser.logOut();
                    adapter.data.clear();
                    adapter.notifyDataSetChanged();
                    txtNote.setText("Bạn cần đăng nhập để xem mục này.");
                    txtNote.setVisibility(View.VISIBLE);
                    item.setTitle("Đăng nhập");
                }else {
                    ParseLoginBuilder builder = new ParseLoginBuilder(FavoriteActivity.this);
                    startActivityForResult(builder.build(), 0);
                    //item.setTitle("Đăng xuất");
                }
                break;
            case R.id.nav_about:

                Intent intentAbout = new Intent(FavoriteActivity.this, AboutActivity.class);
                startActivity(intentAbout);
                break;
            default:
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                loginMenuItem.setTitle("Đăng xuất");
            }
            if (resultCode == RESULT_CANCELED) {
                // Change title

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(UserServices.getCurrentUser() != null) {
            txtNote.setVisibility(View.GONE);
            recyclerView  = (RecyclerView) findViewById(R.id.list_Favorite);
            setItemClickOfRecyclerView();
            getPlacefromParse(UserServices.getCurrentUser());
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

        } else {
            if (listItem != null) {
                adapter.data.clear();
                adapter.notifyDataSetChanged();
            }
            txtNote.setText("Bạn cần đăng nhập để xem mục này.");
            txtNote.setVisibility(View.VISIBLE);
        }
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
        switch (id){

            case R.id.action_map:
                Intent intentMap = new Intent(FavoriteActivity.this, MapActivity.class);
                startActivity(intentMap);
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    //RecyclerView.OnItemTouchListener
    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private ClickListener clickListener;
        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener){
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clickListener!=null ){
                        clickListener.onLongClick(child,recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(),e.getY());
            if((child != null) && (clickListener != null) && gestureDetector.onTouchEvent(e)){
                clickListener.onClick(child,rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public static interface ClickListener{
        public void onClick(View v, int position);
        public void onLongClick(View v, int position);
    }

}