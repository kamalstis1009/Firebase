package com.subra.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class Session {

    private String TAG = this.getClass().getSimpleName();
    private static Context mContext;
    private static Session mSession;

    public static synchronized Session getInstance(Context context) {
        if (mSession == null) {
            mSession = new Session();
            mContext = context;
        }
        return mSession;
    }

    private String SHARED_PREF_NAME = "my_shared_preference";
    private String BITMAP_DATA = "bitmap_data";

    public void saveData(List<Bitmap> mArrayList) {
        SharedPreferences pref = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(BITMAP_DATA, new Gson().toJson(mArrayList));
        editor.apply();
        editor.commit();
    }

    public List<Bitmap> getData() {
        SharedPreferences pref = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return new Gson().fromJson(pref.getString(BITMAP_DATA, null), new TypeToken<List<Bitmap>>(){}.getType());
    }
}
