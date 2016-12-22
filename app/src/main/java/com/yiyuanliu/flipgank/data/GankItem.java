package com.yiyuanliu.flipgank.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by YiyuanLiu on 2016/12/15.
 */

public class GankItem implements Serializable {

    @SerializedName("_id")
    public String id;
    public String createAt;
    public String desc;
    public String publishedAt;
    public String type;
    public String url;
    public String who;
    public String day;
    public List<String> images;
    public String image;
    public boolean like;

    public String getImage() {
        if (image != null) {
            return image;
        } else if (!type.equals("福利")){
            return images == null || images.size() == 0 ? null : images.get(0);
        } else {
            return url;
        }
    }
}
