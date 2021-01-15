package com.spotifycompanion.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.spotifycompanion.R;
import com.spotifycompanion.management.ManagementConnector;
import com.spotifycompanion.models.Playlist;

public class MainActivity extends AppCompatActivity {
    private final ManagementConnector gManagementConnector = new ManagementConnector(MainActivity.this);
    private static final String ADD_LIST = "addList", ADD_LIKED = "addLiked", REMOVE_LIST = "removeList", REMOVE_LIKED = "removeLiked", ORIGIN = "origin", DESTINATION = "destination";
    Toolbar gToolbarTop;
    DrawerLayout gDrawerLayout;
    ImageView gImageView;
    Switch gDeleteFromList, gDeleteFromLiked, gAddToList, gAddToLiked;
    Spinner gOrigin, gDestination;

    SharedPreferences gPreferences;
    SharedPreferences.Editor gEditor;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getAllByID();

        gPreferences = getSharedPreferences("spotifyCompanion", MODE_PRIVATE);
        gEditor = getSharedPreferences("spotifyCompanion", MODE_PRIVATE).edit();

        startNotification();
    }

    private void startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel lChannel = new NotificationChannel(getString(R.string.notification_channelID), "name", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager lManager = getSystemService(NotificationManager.class);
            lManager.createNotificationChannel(lChannel);

            Intent lIntent = new Intent(this, ManagementConnector.class);
            startService(lIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //allow synchronous web calls in main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        gManagementConnector.connectRemote();
        gManagementConnector.authorizeAccess();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gManagementConnector.disconnectRemote();
        gManagementConnector.disallowAccess();
    }

    @Override
    public void onBackPressed() {
        if (gDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            gDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }

    private void getAllByID() {
        gImageView = findViewById(R.id.iv_mainCover);

        gToolbarTop = findViewById(R.id.tb_top);
        setSupportActionBar(gToolbarTop);
        gDrawerLayout = findViewById(R.id.main_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, gDrawerLayout, gToolbarTop, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setDrawerSettings();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                gDeleteFromList = findViewById(R.id.sw_rmList);
                gDeleteFromLiked = findViewById(R.id.sw_rmLiked);
                gAddToList = findViewById(R.id.sw_addList);
                gAddToLiked = findViewById(R.id.sw_addLiked);

                gEditor.putBoolean(REMOVE_LIST, gDeleteFromList.isChecked());
                gEditor.putBoolean(REMOVE_LIKED, gDeleteFromLiked.isChecked());
                gEditor.putBoolean(ADD_LIST, gAddToList.isChecked());
                gEditor.putBoolean(ADD_LIKED, gAddToLiked.isChecked());

                gOrigin = findViewById(R.id.sp_srcList);
                gDestination = findViewById(R.id.sp_dstList);

                gEditor.putInt(ORIGIN, gOrigin.getSelectedItemPosition());
                gEditor.putInt(DESTINATION, gDestination.getSelectedItemPosition());

                gEditor.apply();

                gManagementConnector.setPlaylist(((Playlist) gOrigin.getSelectedItem()).uri);
            }
        };
        gDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    public ImageView getCoverView() {
        return gImageView;
    }

    public TextView getNameView() {
        return findViewById(R.id.tw_trackTitle);
    }

    public TextView getArtistView() {
        return findViewById(R.id.tw_trackArtist);
    }

    public TextView getSkipView() {
        return findViewById(R.id.tw_trackSkips);
    }

    public boolean getDeleteFromLikedValue() {
        return gPreferences.getBoolean(REMOVE_LIKED, false);
    }

    public boolean getDeleteFromListValue() {
        return gPreferences.getBoolean(REMOVE_LIST, true);
    }

    public boolean getAddToLikedValues() {
        return gPreferences.getBoolean(ADD_LIKED, true);
    }

    public boolean getAddToListValues() {
        return gPreferences.getBoolean(ADD_LIST, true);
    }

    private void setDrawerSettings() {
        gDeleteFromList = findViewById(R.id.sw_rmList);
        gDeleteFromLiked = findViewById(R.id.sw_rmLiked);
        gAddToList = findViewById(R.id.sw_addList);
        gAddToLiked = findViewById(R.id.sw_addLiked);

        gDeleteFromLiked.setChecked(gPreferences.getBoolean(REMOVE_LIKED, false));
        gDeleteFromList.setChecked(gPreferences.getBoolean(REMOVE_LIST, true));
        gAddToLiked.setChecked(gPreferences.getBoolean(ADD_LIKED, false));
        gAddToList.setChecked(gPreferences.getBoolean(ADD_LIST, true));

        gOrigin = findViewById(R.id.sp_srcList);
        gDestination = findViewById(R.id.sp_dstList);
        gManagementConnector.fillPlaylistsSelection(gOrigin, gDestination);

        gOrigin.setSelection(gManagementConnector.getPlaylistPosition());
        gDestination.setSelection(gPreferences.getInt(DESTINATION, 0));
    }

    public Playlist getOriginList() {
        setDrawerSettings();
        gOrigin = findViewById(R.id.sp_srcList);
        return (Playlist) gOrigin.getSelectedItem();
    }

    public Playlist getDestinationList() {
        setDrawerSettings();
        gDestination = findViewById(R.id.sp_dstList);
        return (Playlist) gDestination.getSelectedItem();
    }

    public void skipForward(View view) {
        gManagementConnector.skipForward();
    }

    public void skipBackward(View view) {
        gManagementConnector.skipBackward();
    }

    public void clearSkipped(View view) {
        gManagementConnector.clearSkipped();
    }

    public void togglePlayback(View v) {
        gManagementConnector.togglePlayback();
    }


    public void rest(View view) {
        try {
            Button lBt = findViewById(R.id.bt_logInOut);
            Log.e("UI", "Button clicked");


            if (gManagementConnector.isgAuthorized()) {
                //logout
                gManagementConnector.disallowAccess(); //comment out if testing the examples
                lBt.setText(R.string.drawer_logIn);

//              EXAMPLE usage of the handler: getAllLists, getSpecificList, copyTrackToAnotherList and deleteTrackFromList
//              Todo: Remove examples
//              Playlists lists = gManagementConnector.gRESTHandler.getUserPlaylists();
//              Playlist list = gManagementConnector.gRESTHandler.getPlaylist(lists.items[5].id);
//              list = gManagementConnector.gRESTHandler.getPlaylist(lists.items[3].id);
//              gManagementConnector.gRESTHandler.addToPlaylist(lists.items[5].id, new String[]{list.tracks[0].track.uri});
//              gManagementConnector.gRESTHandler.removeFromPlaylist(lists.items[5].id, new String[]{list.tracks[0].track.uri});
//              SavedTracks favorites = gManagementConnector.gRESTHandler.getSavedTracks();
//              Playlist favourites_playlist = gManagementConnector.gRESTHandler.getSavedTracksAsPlaylist();
            } else {
                //login
                gManagementConnector.authorizeAccess();
                //lBt.setText(R.string.drawer_logOut); //button text change on successful login (onActivityResult)
            }
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT);
        }
    }

    public void onClearCredentialsClicked() {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Button lBt = findViewById(R.id.bt_logInOut);
        if (requestCode == gManagementConnector.gRESTHandler.authConfig.AUTH_TOKEN_REQUEST_CODE) {
            boolean hasAccess = gManagementConnector.authorizeCallback(requestCode, resultCode, data);
            if (hasAccess) lBt.setText(R.string.drawer_logOut);
        }
    }
}
