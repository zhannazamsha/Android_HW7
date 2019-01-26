package com.example.den.lesson7;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.den.lesson7.DataSources.NetworkingManagerFavorites;
import com.example.den.lesson7.Interfaces.NetworkingManager;
import com.example.den.lesson7.Interfaces.PhotoItem;
import com.example.den.lesson7.Interfaces.PhotoItemsPresenter;
import com.example.den.lesson7.Interfaces.PhotoItemsPresenterCallbacks;
import com.example.den.lesson7.Presenters.PhotoItemPresenterGridView;

public class FavoritesActivity extends Activity implements PhotoItemsPresenterCallbacks {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
       showFavorites();
    }

    private void showFavorites(){
        NetworkingManager networkingManager = new NetworkingManagerFavorites();

        PhotoItemsPresenter presenter = new PhotoItemPresenterGridView();
        networkingManager.getPhotoItems(photoItems ->
                runOnUiThread(()-> {
                    presenter.setupWithPhotoItems(photoItems,this, this);
                })
        );
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
                showMainActivity(MainActivity.ImgServices.UNSPLASH);
                return true;
            }
        });
        final MenuItem showGiphyMenuItem = menu.findItem(R.id.action_show_giphy);
        showGiphyMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showMainActivity(MainActivity.ImgServices.GIPHY);
                return true;
            }
        });

        return true;
    }

    private void showMainActivity(MainActivity.ImgServices service) {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra(MainActivity.SERVICE_KEY,service.name());
        startActivity(mainIntent);
    }

    @Override
    public void onItemSelected(PhotoItem item) {
        Intent shareIntent = new Intent(this, ShareActivityWithFragments.class);
        shareIntent.putExtra(ShareActivity.PHOTO_ITEM_KEY,item);
        startActivity(shareIntent);
    }

    @Override
    public void onItemToggleFavorite(PhotoItem item) {
        if(item.isSavedToDatabase()) {
            item.deleteFromDatabase();
        } else {
            item.saveToDatabase();

        }

        recreate();
    }
}
