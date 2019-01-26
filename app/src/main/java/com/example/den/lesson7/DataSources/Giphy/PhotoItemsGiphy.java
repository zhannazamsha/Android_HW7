package com.example.den.lesson7.DataSources.Giphy;

import com.example.den.lesson7.Interfaces.PhotoItem;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

import java.io.Serializable;

@Table
public class PhotoItemsGiphy implements PhotoItem  {

    @SerializedName("db_id")
    private transient Long id;

    @SerializedName("id")
    @Unique
    private String imgID;

    @SerializedName("deleted")
    private Boolean deleted = false;

    ImagesContainer images;
    String username;

    private String imagesForORM;

    public PhotoItemsGiphy() { }

    @Override
    public String getID() {return this.imgID;}

    @Override
    public String getImgUrl() {
        if(images == null) {
            this.images = new Gson().fromJson(this.imagesForORM, ImagesContainer.class);
        }

        return images.getMediumSize();
    }

    @Override
    public String getAuthorName() {
        return username;
    }

    @Override
    public void saveToDatabase() {
        this.imagesForORM = images.toString();
        if(this.isDeleted()){
            this.deleted = false;
            SugarRecord.update(this);
        } else {
            SugarRecord.save(this);
        }
    }

    public boolean isDeleted() {
        return SugarRecord.find(PhotoItemsGiphy.class,"img_id = ?", this.imgID).size() > 0;
    }

    @Override
    public void deleteFromDatabase() {
      //  SugarRecord.deleteAll(PhotoItemsGiphy.class,"id = ?", this.imgID);
        this.deleted = true;
        SugarRecord.update(this);
    }

    @Override
    public boolean isSavedToDatabase() {
        return SugarRecord.find(PhotoItemsGiphy.class,"img_id = ? AND NOT deleted", this.imgID).size() > 0;
    }

    public class ImagesContainer implements Serializable {

        public String toString() {
            return new Gson().toJson(this);
        }

        DownsizedMedium downsized_medium;

        public String getMediumSize() {
            return downsized_medium.url;
        }
    }

    public class DownsizedMedium implements Serializable {
        String url;
    }
}
