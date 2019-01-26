package com.example.den.lesson7.DataSources.Unsplash;

import com.example.den.lesson7.DataSources.Giphy.PhotoItemsGiphy;
import com.example.den.lesson7.Interfaces.PhotoItem;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

import java.io.Serializable;

@Table
public class PhotoItemUnsplash implements PhotoItem {

    @SerializedName("db_id")
    private transient Long id = null;

    @SerializedName("id")
    @Unique
    private String imgID;

    @SerializedName("deleted")
    private Boolean deleted = false;


    URLs urls;
    User user;

    private String URLsFromORM;
    private String UserFromORM;

    public PhotoItemUnsplash() {}


    public String getID () {return this.imgID;}

    public String getImgUrl() {
        if (urls == null) {
            this.urls = new Gson().fromJson(this.URLsFromORM, URLs.class);
        }

        return this.urls.regular;
    }

    public String getAuthorName() {
        if (user == null) {
            this.user = new Gson().fromJson(this.UserFromORM, User.class);
        }

        return this.user.name;
    }

    @Override
    public void saveToDatabase() {
        this.URLsFromORM = urls.toString();
        this.UserFromORM = user.toString();
        if(this.isDeleted()){
            this.deleted = false;
            SugarRecord.update(this);
         } else {
            SugarRecord.save(this);
        }


    }

    @Override
    public void deleteFromDatabase() {
       // SugarRecord.deleteAll(PhotoItemUnsplash.class,"id = ?", this.imgID);
        this.deleted = true;
        SugarRecord.update(this);
    }

    @Override
    public boolean isSavedToDatabase() {
        return SugarRecord.find(PhotoItemUnsplash.class,"img_id = ? AND NOT deleted", this.imgID).size() > 0;
    }

    public boolean isDeleted() {
        return SugarRecord.find(PhotoItemUnsplash.class,"img_id = ?", this.imgID).size() > 0;
    }

    public class User implements Serializable {
        String name;

        public String toString() {
            return new Gson().toJson(this);
        }
    }

    public class URLs implements Serializable {
        String regular;

        public String toString() {
            return new Gson().toJson(this);
        }

    }
}
