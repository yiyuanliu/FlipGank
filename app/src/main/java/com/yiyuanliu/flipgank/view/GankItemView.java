package com.yiyuanliu.flipgank.view;

import com.yiyuanliu.flipgank.data.GankItem;

/**
 * Created by YiyuanLiu on 2016/12/20.
 */
public interface GankItemView {
    void bind(GankItem gankItem, Listener listener);

    interface Listener {
        void open(GankItem gankItem);
        void showBottomSheet(GankItem gankItem);
        void like(GankItem gankItem);
    }
}