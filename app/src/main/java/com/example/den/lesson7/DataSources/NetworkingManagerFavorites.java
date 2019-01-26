package com.example.den.lesson7.DataSources;

import com.example.den.lesson7.DataSources.Giphy.PhotoItemsGiphy;
import com.example.den.lesson7.DataSources.Unsplash.PhotoItemUnsplash;
import com.example.den.lesson7.Interfaces.NetworkingManager;
import com.example.den.lesson7.Interfaces.NetworkingResultListener;
import com.example.den.lesson7.Interfaces.PhotoItem;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

public class NetworkingManagerFavorites implements NetworkingManager {
    @Override
    public void getPhotoItems(NetworkingResultListener result) {
        List<PhotoItem> allFavoritedItems = new ArrayList<PhotoItem>();
        allFavoritedItems.addAll(SugarRecord.find(PhotoItemsGiphy.class, " NOT deleted"));
        allFavoritedItems.addAll(SugarRecord.find(PhotoItemUnsplash.class, " NOT deleted"));
        result.callback(allFavoritedItems.toArray(new PhotoItem[allFavoritedItems.size()]));

    }
}
