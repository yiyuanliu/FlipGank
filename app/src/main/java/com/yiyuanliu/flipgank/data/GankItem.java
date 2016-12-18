package com.yiyuanliu.flipgank.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by YiyuanLiu on 2016/12/15.
 */

public class GankItem {

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

    public String getImage() {
        if (image != null) {
            return image;
        } else {
            return images == null || images.size() == 0 ? null : images.get(0);
        }
    }
}
