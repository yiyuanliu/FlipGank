package com.yiyuanliu.flipgank.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yiyuanliu.flipgank.R;
import com.yiyuanliu.flipgank.data.GankItem;

import java.nio.channels.NonReadableChannelException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.http.Path;

/**
 * Created by YiyuanLiu on 2016/12/17.
 */
public class MainAdapter extends RecyclerView.Adapter {

    private List<Page> pageList = new ArrayList<>();

    public void clear() {
        int size = pageList.size();
        pageList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addData(List<GankItem> gankItemList) {
        BigImagePage bigImagePage = BigImagePage.gen(gankItemList);
        if (bigImagePage != null) {
            pageList.add(bigImagePage);
            notifyItemInserted(pageList.size() - 1);
        }

        while (gankItemList.size() > 0) {
            NormalPage normal = NormalPage.gen(gankItemList);
            pageList.add(normal);
            notifyItemInserted(pageList.size() - 1);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == Page.TYPE_BIG_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_big_image, parent, false);
            return new BigImageVh(view);
        }

        if (viewType == Page.TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_normal, parent, false);
            return new NormalVh(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);

        if (type == Page.TYPE_BIG_IMAGE) {
            BigImageVh imageVh = (BigImageVh) holder;
            imageVh.bind(pageList.get(position));
        }

        if (type == Page.TYPE_NORMAL) {
            NormalVh normalVh = (NormalVh) holder;
            normalVh.bind(pageList.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return pageList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return pageList.size();
    }

    private class BigImageVh extends RecyclerView.ViewHolder {

        ImageView image;
        TextView[] titles;
        TextView[] types;
        ViewGroup otherItems;
        View items[];

        public BigImageVh(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            titles = new TextView[2];
            items = new View[2];

            otherItems = (ViewGroup) itemView.findViewById(R.id.other_items);

            items[0] = itemView.findViewById(R.id.item1);
            items[1] = itemView.findViewById(R.id.item2);

            titles[0] = (TextView) itemView.findViewById(R.id.title1);
            titles[1] = (TextView) itemView.findViewById(R.id.title2);

            types = new TextView[2];
            types[0] = (TextView) itemView.findViewById(R.id.type1);
            types[1] = (TextView) itemView.findViewById(R.id.type2);
        }

        void bind(Page page) {
            bind((BigImagePage) page);
        }

        void bind(BigImagePage bigImagePage) {

            if (bigImagePage.getSize() == 1) {
                otherItems.setVisibility(View.GONE);
            } else {
                otherItems.setVisibility(View.VISIBLE);
            }

            if (bigImagePage.getSize() == 2) {
                items[1].setVisibility(View.GONE);
            } else {
                items[1].setVisibility(View.VISIBLE);
            }

            Glide.with(image.getContext())
                    .load(bigImagePage.imageItem.url)
                    .into(image);

            for (int i = 0;i < bigImagePage.getSize() - 1; i ++) {
                titles[i].setText(bigImagePage.otherItems[i].desc);
                types[i].setText(bigImagePage.otherItems[i].type);
            }
        }
    }

    private class NormalVh extends RecyclerView.ViewHolder {
        TextView titles[];
        TextView types[];
        ImageView images[];


        public NormalVh(View itemView) {
            super(itemView);
            titles = new TextView[3];
            types = new TextView[3];
            images = new ImageView[3];

            titles[0] = (TextView) itemView.findViewById(R.id.title1);
            titles[1] = (TextView) itemView.findViewById(R.id.title2);
            titles[2] = (TextView) itemView.findViewById(R.id.title3);

            types[0] = (TextView) itemView.findViewById(R.id.type1);
            types[1] = (TextView) itemView.findViewById(R.id.type2);
            types[2] = (TextView) itemView.findViewById(R.id.type3);

            images[0] = (ImageView) itemView.findViewById(R.id.image1);
            images[1] = (ImageView) itemView.findViewById(R.id.image2);
            images[2] = (ImageView) itemView.findViewById(R.id.image3);
        }

        void bind(Page page) {
            bind((NormalPage) page);
        }

        void bind(NormalPage normalPage) {
            for (int i = 0;i < normalPage.getSize();i ++) {
                titles[i].setText(normalPage.items[i].desc);
                types[i].setText(normalPage.items[i].type);
                if (TextUtils.isEmpty(normalPage.items[i].getImage())) {
                    images[i].setVisibility(View.GONE);
                } else {
                    images[i].setVisibility(View.VISIBLE);
                    Glide.with(images[i].getContext())
                            .load(normalPage.items[i].getImage())
                            .centerCrop()
                            .into(images[i]);
                }
            }
        }
    }

    private abstract static class Page {
        static final int TYPE_BIG_IMAGE = 1;
        static final int TYPE_NORMAL = 2;

        abstract int getType();
    }

    /**
     * 通常为某天信息的第一页
     * 样式为上方一个大图片，下方最多两个内容并排排列
     *
     */
    private static class BigImagePage extends Page {
        static BigImagePage gen(List<GankItem> gankItemList) {
            GankItem imageItem = null;

            Iterator<GankItem> gankItemIterator = gankItemList.iterator();
            while (gankItemIterator.hasNext()) {
                GankItem item = gankItemIterator.next();
                if (item.type.equals("福利")) {
                    imageItem = item;
                    gankItemIterator.remove();
                    break;
                }
            }

            if (imageItem == null) {
                return null;
            }

            int count = gankItemList.size() % 3;
            int i = 0;
            GankItem[] otherItems = new GankItem[count];
            gankItemIterator = gankItemList.iterator();
            while (gankItemIterator.hasNext() && count > i) {
                GankItem item = gankItemIterator.next();
                if (TextUtils.isEmpty(item.getImage())) {
                    otherItems[i] = item;
                    i ++;
                    gankItemIterator.remove();
                }
            }
            while (gankItemIterator.hasNext() && count > i) {
                GankItem item = gankItemIterator.next();
                otherItems[i] = item;
                i ++;
                gankItemIterator.remove();
            }

            BigImagePage bigImagePage = new BigImagePage();
            bigImagePage.imageItem = imageItem;
            bigImagePage.otherItems = otherItems;
            return bigImagePage;
        }

        GankItem imageItem;
        GankItem[] otherItems;

        private int getSize() {
            return otherItems == null ? 1 : otherItems.length + 1;
        }

        @Override
        int getType() {
            return TYPE_BIG_IMAGE;
        }
    }

    /**
     * 这是通常形式的 page
     * 样式为三个内容竖直排列
     */
    private static class NormalPage extends Page {
        static NormalPage gen(List<GankItem> gankItemList) {
            if (gankItemList == null || gankItemList.isEmpty()) {
                return null;
            }

            int size = gankItemList.size() > 3 ? 3 : gankItemList.size();
            GankItem[] items = new GankItem[size];
            gankItemList.subList(0, size).toArray(items);
            gankItemList.removeAll(gankItemList.subList(0, size));

            NormalPage normalPage = new NormalPage();
            normalPage.items = items;

            return normalPage;
        }

        GankItem[] items;

        private int getSize() {
            return items.length;
        }

        @Override
        int getType() {
            return TYPE_NORMAL;
        }
    }
}
