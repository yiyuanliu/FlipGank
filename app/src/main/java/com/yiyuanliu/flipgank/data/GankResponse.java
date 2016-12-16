package com.yiyuanliu.flipgank.data;

import java.util.List;
import java.util.Map;

/**
 * Created by YiyuanLiu on 2016/12/15.
 */

public class GankResponse {
    public boolean error;
    public Map<String, List<GankItem>> results;
    public List<String> category;

    public boolean hasData() {
        return results != null && results.size() > 0;
    }
}
