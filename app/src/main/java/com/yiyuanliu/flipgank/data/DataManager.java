package com.yiyuanliu.flipgank.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by YiyuanLiu on 2016/12/16.
 */
public class DataManager {
    private static final String TAG = "DataManager";

    private static volatile DataManager sInstance;

    public static DataManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DataManager.class) {
                if (sInstance == null) {
                    sInstance = new DataManager(context);
                }
            }
        }

        return sInstance;
    }

    private DataManager(Context context) {
        Context context1 = context.getApplicationContext();
        GankDbHelper gankDbHelper = new GankDbHelper(context1);
        sqLiteDatabase = gankDbHelper.getWritableDatabase();
    }

    private SQLiteDatabase sqLiteDatabase;
    private Api api;

    public Observable<List<String>> loadCategory() {

        return Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                Cursor cursor = sqLiteDatabase.query(GankDbHelper.Contract.TABLE_CATEGORY, null, null, null, null, null, null);
                boolean hasMore = cursor.moveToFirst();
                List<String> categorys = new ArrayList<>();
                while (hasMore) {
                    int categoryPos = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_CATEGORY);
                    String category = cursor.getString(categoryPos);
                    categorys.add(category);
                    hasMore = cursor.moveToNext();
                }

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(categorys);
                }

                cursor.close();
            }
        });
    }

    /**
     * 从当前时间加载数据
     * 调用者应通过返回数据中的时间判断是否有更新的内容
     *
     * @return Observable
     */
    public Observable<List<GankItem>> loadNew() {
        return loadFromDay(getToday());
    }

    /**
     * 加载更多内容
     * 在分页时 loadMore 从上次加载到的位置继续
     * 第一次调用从数据库缓存中最新时间开始加载
     * 如果返回的数据为空，意味着已经没有更多内容
     *
     * @param lastLoaded 上次加载到的日期，用于分页加载
     * @return Observable
     */
    public Observable<List<GankItem>> loadMore(String lastLoaded) {
        String day;
        if (lastLoaded == null) {
            History history = getLatestUpdate(true);
            if (history == null) {
                day = getToday();
            } else {
                day = history.day;
            }
        } else {
            // 日期向前一天
            day = dayBack(lastLoaded);
        }

        return loadFromDay(day);
    }

    /**
     * 从 day 开始进行加载任务，发现新的内容后停止
     * 如果没有内容，加载前一天的内容,如果返回的数据为空，意味着日期以前已经没有更多内容
     *
     * @param day 加载日期
     */
    private Observable<List<GankItem>> loadFromDay(final String day) {
        Log.d(TAG, "loadFromDay: " + day);
        if (isOver(day)) {
            List<GankItem> emptyList = new ArrayList<>();
            return Observable.just(emptyList);
        } else return load(day).flatMap(new Func1<List<GankItem>, Observable<List<GankItem>>>() {
            @Override
            public Observable<List<GankItem>> call(List<GankItem> gankItemList) {
                // 检查是否获得了数据
                if (gankItemList.size() == 0) {
                    String dayBack = dayBack(day);
                    return loadFromDay(dayBack);
                } else {
                    return Observable.just(gankItemList);
                }
            }
        });
    }

    /**
     * 加载某一天的数据，首先检查数据库，否则从网络中加载并更新数据库
     *
     * @param dayStr 日期
     * @return Observable
     */
    private Observable<List<GankItem>> load(final String dayStr) {
        Log.d(TAG, "load: " + dayStr);

        History history = checkLoadHistory(dayStr);
        if (history == null) {

            return loadFromGank(dayStr).map(new Func1<List<GankItem>, List<GankItem>>() {
                @Override
                public List<GankItem> call(List<GankItem> gankItemList) {
                    updateDb(gankItemList, dayStr);
                    return gankItemList;
                }
            });
        } else {
            return loadFormDb(dayStr);
        }
    }

    /**
     * 从 gank.io 加载某一天的内容
     * @param dayStr 日期
     * @return 返回加载到的内容
     */
    private Observable<List<GankItem>> loadFromGank(final String dayStr) {
        Log.d(TAG, "loadFromGank: " + dayStr);

        String[] parts = dayStr.split("/");
        int year = Integer.valueOf(parts[0]);
        int month = Integer.valueOf(parts[1]);
        int day = Integer.valueOf(parts[2]);

        if (api == null) {
            Retrofit.Builder builder = new Retrofit.Builder();
            api = builder.baseUrl(Api.BASE_URL)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(Api.class);
        }

        return api.loadData(year, month, day)
                .map(new Func1<GankResponse, List<GankItem>>() {
                    @Override
                    public List<GankItem> call(GankResponse gankResponse) {
                        if (gankResponse.error) {
                            throw new RuntimeException("gank error");
                        }
                        List<GankItem> gankItemList = new ArrayList<>();
                        if (gankResponse.hasData()) {
                            for (Map.Entry<String, List<GankItem>> entry: gankResponse.results.entrySet()) {
                                gankItemList.addAll(entry.getValue());
                            }
                        }
                        for (GankItem gankItem: gankItemList) {
                            gankItem.day = dayStr;
                        }
                        return gankItemList;
                    }
                });
    }

    public void updateLike(GankItem gankItem) {
        ContentValues item = new ContentValues();
        item.put(GankDbHelper.Contract.COLUMN_ID, gankItem.id);
        item.put(GankDbHelper.Contract.COLUMN_CATEGORY, gankItem.type);
        item.put(GankDbHelper.Contract.COLUMN_DEST, gankItem.desc);
        item.put(GankDbHelper.Contract.COLUMN_DAY, gankItem.day);
        item.put(GankDbHelper.Contract.COLUMN_URL, gankItem.url);
        item.put(GankDbHelper.Contract.COLUMN_WHO, gankItem.who);
        item.put(GankDbHelper.Contract.COLUMN_LIKE, gankItem.like ? 1 : 0);
        item.put(GankDbHelper.Contract.COLUMN_IMAGE, gankItem.getImage());

        sqLiteDatabase.update(GankDbHelper.Contract.TABLE_DATA, item,
                GankDbHelper.Contract.COLUMN_ID + "=?", new String[]{ gankItem.id });
    }

    /**
     * 从 dataBase 中加载某天的内容
     * @param day 日期
     * @return 内容
     */
    private Observable<List<GankItem>> loadFormDb(final String day) {
        Log.d(TAG, "loadFormDb: " + day);

        return Observable.create(new Observable.OnSubscribe<List<GankItem>>() {
            @Override
            public void call(Subscriber<? super List<GankItem>> subscriber) {
                Log.d(TAG, "call: " + day);

                final List<GankItem> gankItemList = new ArrayList<>();

                Cursor cursor = sqLiteDatabase.query(GankDbHelper.Contract.TABLE_DATA, null,
                        GankDbHelper.Contract.COLUMN_DAY + " = ?", new String[]{day},
                        null, null, null);

                boolean hasMore = cursor.moveToFirst();
                while (hasMore) {
                    GankItem gankItem = new GankItem();
                    int columnDesc = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_DEST);
                    int columnUrl = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_URL);
                    int columnWho = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_WHO);
                    int columnType = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_CATEGORY);
                    int columnDay = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_DAY);
                    int columnImage = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_IMAGE);
                    int columnLike = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_LIKE);
                    int columnId = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_ID);

                    gankItem.desc = cursor.getString(columnDesc);
                    gankItem.url = cursor.getString(columnUrl);
                    gankItem.who = cursor.getString(columnWho);
                    gankItem.type = cursor.getString(columnType);
                    gankItem.day = cursor.getString(columnDay);
                    gankItem.image = cursor.getString(columnImage);
                    gankItem.like = cursor.getInt(columnLike) != 0;
                    gankItem.id = cursor.getString(columnId);
                    gankItemList.add(gankItem);

                    hasMore = cursor.moveToNext();
                }

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(gankItemList);
                    subscriber.onCompleted();
                }

                cursor.close();

                Log.d(TAG, "call: end " + day);
            }
        });

    }

    /**
     * 最近一次的更新时间
     * @return 最近更新，日期形式 2016/12/16
     */
    private History getLatestUpdate(boolean shouldHasData) {
        History ans = null;
        int ansInt = Integer.MIN_VALUE;

        Cursor cursor = sqLiteDatabase.query(GankDbHelper.Contract.TABLE_LOAD_HISTORY,
                null, null, null, null, null, null);
        boolean hasMore = cursor.moveToFirst();
        while (hasMore) {
            int columnDay = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_DAY);
            int columnHasData = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_HAS_DATA);
            String day = cursor.getString(columnDay);
            boolean hasData = cursor.getInt(columnHasData) == 1;

            int dayInt = Integer.valueOf(day.replaceAll("/", ""));
            if (dayInt > ansInt && (hasData || !shouldHasData)) {
                if (ans == null) {
                    ans = new History();
                }
                ans.day = day;
                ans.hasData = hasData;
                ansInt = dayInt;
            }

            hasMore = cursor.moveToNext();
        }

        cursor.close();

        return ans;
    }

    private void updateDb(List<GankItem> gankItemList, String day) {

        final boolean isToday = isToday(day);
        if (gankItemList.size() == 0) {
            // 返回的列表中没有消息
            if (isToday) {
                // do nothing, 可能代码家还没干活，等等再说
            } else {
                // 那就是说今天没东西了
                updateLoadHistory(day, false);
            }
        } else {
            sqLiteDatabase.beginTransaction();
            updateLoadHistory(day, true);
            updateDataDb(gankItemList, day);
            updateCategory(gankItemList);
            sqLiteDatabase.setTransactionSuccessful();
            sqLiteDatabase.endTransaction();
        }
    }

    private void updateDataDb(List<GankItem> gankItemList, String day) {
        if (gankItemList == null || gankItemList.size() == 0) {
            Log.d(TAG, "updateDataDb return for no data");
            return;
        }

        for (GankItem gankItem: gankItemList) {
            ContentValues item = new ContentValues();
            item.put(GankDbHelper.Contract.COLUMN_ID, gankItem.id);
            item.put(GankDbHelper.Contract.COLUMN_CATEGORY, gankItem.type);
            item.put(GankDbHelper.Contract.COLUMN_DEST, gankItem.desc);
            item.put(GankDbHelper.Contract.COLUMN_DAY, day);
            item.put(GankDbHelper.Contract.COLUMN_URL, gankItem.url);
            item.put(GankDbHelper.Contract.COLUMN_WHO, gankItem.who);
            item.put(GankDbHelper.Contract.COLUMN_IMAGE, gankItem.getImage());
            item.put(GankDbHelper.Contract.COLUMN_LIKE, gankItem.like ? 1 : 0);
            sqLiteDatabase.insert(GankDbHelper.Contract.TABLE_DATA, null, item);
        }
    }

    /**
     * 更新 category 列表
     * @param gankItemList 输入数据
     */
    private void updateCategory(List<GankItem> gankItemList) {
        if (gankItemList == null || gankItemList.size() == 0) {
            return;
        }

        Set<String> category = new HashSet<>();
        for (GankItem gankItem: gankItemList) {
            category.add(gankItem.type);
        }
        for (String item: category) {
            Cursor cursor = sqLiteDatabase.query(GankDbHelper.Contract.TABLE_CATEGORY, null,
                    GankDbHelper.Contract.COLUMN_CATEGORY + "=?", new String[]{item}, null, null, null );
            if (!cursor.moveToFirst()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(GankDbHelper.Contract.COLUMN_CATEGORY, item);
                sqLiteDatabase.insert(GankDbHelper.Contract.TABLE_CATEGORY, null, contentValues);
            }

            cursor.close();
        }
    }

    /**
     * 将日期添加到 load_history 表之中，表示当天的消息已经加载过了
     * @param day 日期
     */
    private void updateLoadHistory(String day, boolean hasData) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(GankDbHelper.Contract.COLUMN_DAY, day);
        contentValues.put(GankDbHelper.Contract.COLUMN_HAS_DATA, hasData ? 1 : 0);
        sqLiteDatabase.insert(GankDbHelper.Contract.TABLE_LOAD_HISTORY, null, contentValues);
    }

    /**
     * 检查当天是否被加载过
     * @param day 所需查询的日期
     * @return 是否已经加载, null 表示未加载，反之表示加载情况
     */
    private History checkLoadHistory(String day) {
        Cursor cursor = sqLiteDatabase.query(GankDbHelper.Contract.TABLE_LOAD_HISTORY, null,
                GankDbHelper.Contract.COLUMN_DAY + " = ?", new String[]{day},
                null, null, null);

        if (cursor.moveToFirst()) {
            History history = new History();
            int columnDay = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_DAY);
            int columnHasData = cursor.getColumnIndex(GankDbHelper.Contract.COLUMN_HAS_DATA);

            history.day = cursor.getString(columnDay);
            history.hasData = cursor.getInt(columnHasData) == 1;
            cursor.close();

            return history;
        } else {
            cursor.close();
            return null;
        }
    }

    private static boolean isToday(String dayStr) {
        int[] dayInt = dayStr2Int(dayStr);

        return isToday(dayInt[0], dayInt[1], dayInt[2]);
    }

    private static boolean isToday(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        int yearToday = calendar.get(Calendar.YEAR);
        int monthToday = calendar.get(Calendar.MONTH) + 1;
        int dayToday = calendar.get(Calendar.DAY_OF_MONTH);

        return year == yearToday && monthToday == month && dayToday == day;
    }

    private static int[] dayStr2Int(String dayStr) {
        String[] parts = dayStr.split("/");
        int year = Integer.valueOf(parts[0]);
        int month = Integer.valueOf(parts[1]);
        int day = Integer.valueOf(parts[2]);

        return new int[]{year, month, day};
    }

    private static String dayInt2Str(int year, int month, int day) {
        return year + "/" + month + "/" + day;
    }

    private static final long A_DAY = 24 * 60 * 60 * 1000;

    /**
     * 获取前一天的日期字符串, 例如输入 2016/12/16
     * @return 前一天的时间 2016/12/15
     */
    private static String dayBack(String dayStr) {
        int[] dayInts = dayStr2Int(dayStr);
        Calendar calendar = Calendar.getInstance();
        calendar.set(dayInts[0], dayInts[1] - 1, dayInts[2]);
        Date date = calendar.getTime();
        long time = date.getTime() - A_DAY;
        date.setTime(time);
        calendar.setTime(date);

        return dayInt2Str(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    private static String dayNext(String dayStr) {
        int[] dayInts = dayStr2Int(dayStr);
        Calendar calendar = Calendar.getInstance();
        calendar.set(dayInts[0], dayInts[1] - 1, dayInts[2]);
        Date date = calendar.getTime();
        long time = date.getTime() + A_DAY;
        date.setTime(time);
        calendar.setTime(date);

        return dayInt2Str(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    private static String getToday() {
        Calendar calendar = Calendar.getInstance();
        return dayInt2Str(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * gank.io 最早的时间应该是 2015/05/18,
     * @param dayStr 日期
     * @return 是否超过了 gank 的初始时间
     */
    private static boolean isOver(String dayStr) {
        int[] day = dayStr2Int(dayStr);
        Calendar gankStart = Calendar.getInstance();
        gankStart.set(2015, 4, 18);
        Calendar checkTime = Calendar.getInstance();
        checkTime.set(day[0], day[1] - 1, day[2]);

        return checkTime.before(gankStart);
    }
}
