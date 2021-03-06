package com.example.den.lesson7;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.den.lesson7.DataSources.Giphy.NetworkingManagerGiphy;
import com.example.den.lesson7.DataSources.Giphy.PhotoItemsGiphy;
import com.example.den.lesson7.DataSources.Local.SecureDataHelper;
import com.example.den.lesson7.DataSources.Local.SharedPreferencesHelper;
import com.example.den.lesson7.DataSources.NetworkingManagerFavorites;
import com.example.den.lesson7.DataSources.Unsplash.NetworkingManagerUnsplash;
import com.example.den.lesson7.DataSources.Unsplash.PhotoItemUnsplash;
import com.example.den.lesson7.Interfaces.NetworkingManager;
import com.example.den.lesson7.Interfaces.PhotoItem;
import com.example.den.lesson7.Interfaces.PhotoItemsPresenter;
import com.example.den.lesson7.Interfaces.PhotoItemsPresenterCallbacks;
import com.example.den.lesson7.Presenters.PhotoItemPresenterGridView;
import com.orm.SugarRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends Activity implements PhotoItemsPresenterCallbacks {

    //TODO 1: Add new button to top menu - "Show Giphy"
    //TODO 2: Add to that button ability to change service to Giphy (like its done with Unsplash)
    //TODO 3: Add new activity "FavoriteActivity" to show Favotie button
    //TODO 4: Show on "FavoriteActivity" favorited items by PhotoItemPresenter :)
    //*TODO 5 EXTRA: handle removing favorites from "FavoriteActivity"
    //*TODO 6 EXTRA EXTRA: Implement save remove (do not delete anything from database)

    public enum ImgServices {
        UNSPLASH,
        GIPHY,
        FAVORITES
    }

    public final static String SERVICE_KEY = "SERVICE_KEY";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String service = getIntent().getStringExtra(SERVICE_KEY);
        if(service == null){
           service =  ImgServices.GIPHY.name();
        }
        showImgService(ImgServices.valueOf(service));
    }

    private void showImgService(ImgServices service) {

        NetworkingManager networkingManager = null;

        switch (service) {
            case GIPHY:
                networkingManager = new NetworkingManagerGiphy();
                break;
            case UNSPLASH:
                networkingManager = new NetworkingManagerUnsplash();
                break;
            case FAVORITES:
                networkingManager = new NetworkingManagerFavorites();
                break;
        }

        PhotoItemsPresenter presenter = new PhotoItemPresenterGridView();
        networkingManager.getPhotoItems(photoItems ->
                runOnUiThread(()-> {
                    presenter.setupWithPhotoItems(photoItems,this, this);
                })
        );
    }



    @Override
    public void onItemSelected(PhotoItem item) {
        Intent shareIntent = new Intent(this, ShareActivityWithFragments.class);
        shareIntent.putExtra(ShareActivity.PHOTO_ITEM_KEY,item);
        startActivity(shareIntent);
    }

    @Override
    public void onItemToggleFavorite(PhotoItem item) {
        testFavoriteORM(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);

        final MenuItem favoriteMenuItem = menu.findItem(R.id.action_show_favorites);
        favoriteMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showFavorites();
                return true;
            }
        });
        final MenuItem showUnsplashMenuItem = menu.findItem(R.id.action_show_unslash);
        showUnsplashMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showImgService(ImgServices.UNSPLASH);
                return true;
            }
        });
        final MenuItem showGiphyMenuItem = menu.findItem(R.id.action_show_giphy);
        showGiphyMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showImgService(ImgServices.GIPHY);
                return true;
            }
        });

        return true;
    }

    private void showFavorites() {
        Intent favoriteIntent = new Intent(this, FavoritesActivity.class);
        startActivity(favoriteIntent);
    }

    // *****************************************************
    // *****************************************************
    // ************************ ORM ************************
    // *****************************************************
    // *****************************************************

    private void testFavoriteORM(PhotoItem item) {

        if(item.isSavedToDatabase()) {
            item.deleteFromDatabase();
        } else {
            item.saveToDatabase();

        }
    }

    private PhotoItem[] getAllSavedPhotoItems() {

        List<PhotoItem>allFavoritedItems = new ArrayList<PhotoItem>();

        allFavoritedItems.addAll(SugarRecord.listAll(PhotoItemsGiphy.class));
        allFavoritedItems.addAll(SugarRecord.listAll(PhotoItemUnsplash.class));


        return allFavoritedItems.toArray(new PhotoItem[allFavoritedItems.size()]);
    }

    // *****************************************************
    // *****************************************************
    // **************** Shared Preferences *****************
    // *****************************************************
    // *****************************************************

    private void testFavoriteSharedPreferences(PhotoItem item) {
        if(SharedPreferencesHelper.isFavorited(item.getID(), this)) {
            SharedPreferencesHelper.removeFromFavorite(item.getID(),this);
        } else {
            SharedPreferencesHelper.saveFavorite(item.getID(),this);
        }
    }

    private void testSensitiveData() {
        String password_key = "password_key";

        SecureDataHelper.saveSensitiveData("aaaaaaaabbbbbbcccccc", password_key, this);
        SecureDataHelper.getSensitiveData(password_key,this);
    }

    // *****************************************************
    // *****************************************************
    // ****************** Write to file ********************
    // *****************************************************
    // *****************************************************

    private void testWriteToExternalFile() {
        File path = getExternalStorageDirectory();
//        dont forget to add <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

        if(isStoragePermissionGranted()) {
            File file = new File(path, "my_awesome_file.txt");
            try {
                FileOutputStream stream = new FileOutputStream(file);
                stream.write("EXTERNAL: Today is a great day!".getBytes());
                stream.close();
            } catch (Exception ex) {
                Log.v("i",ex.getLocalizedMessage());
            }
        }
    }

    private void testReadFromExternalFile() {

        File path = getExternalStorageDirectory();
        File file = new File(path, "my_awesome_file.txt");

        int length = (int) file.length();

        byte[] bytes = new byte[length];

        try {
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        } catch (Exception ex) {
            Log.v("i",ex.getLocalizedMessage());
        }

        String contents = new String(bytes);
        Log.i("i", contents);
    }

    private void testWriteToLocalFile() {

        // Local phone
        File path = getFilesDir();

        // Local SD cards
//        File path = getExternalFilesDir("super_private");

        File file = new File(path, "my_awesome_file.txt");
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write("LOCAL: Today is a great day!".getBytes());
            stream.close();
        } catch (Exception ex) {
            Log.v("i",ex.getLocalizedMessage());
        }
    }

    private void testReadFromLocalFile() {

        File path = getFilesDir();
        File file = new File(path, "my_awesome_file.txt");

        int length = (int) file.length();

        byte[] bytes = new byte[length];

        try {
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        } catch (Exception ex) {
            Log.v("i",ex.getLocalizedMessage());
        }

        String contents = new String(bytes);
        Log.i("i", contents);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Storage","Permission is granted");
                return true;
            } else {

                Log.v("Storage","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Storage","Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("STORAGE","Permission: "+permissions[0]+ "was "+grantResults[0]);
            testWriteToExternalFile();
        }
    }
}
