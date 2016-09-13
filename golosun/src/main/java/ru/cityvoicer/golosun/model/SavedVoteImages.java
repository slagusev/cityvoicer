package ru.cityvoicer.golosun.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import ru.cityvoicer.golosun.GolosunApp;

public class SavedVoteImages {
    private static String TAG = "SavedVoteImages";

    private int mImageEndIndex;
    private int mImagesCount;

    private static int MaxImagesCount = 5;

    public int getImagesCount() {
        return mImagesCount;
    }

    public Bitmap loadImagesByNumber(int n) {
        if (n >= mImagesCount)
            return null;
        int startIndex = (mImageEndIndex - mImagesCount + MaxImagesCount) % MaxImagesCount;
        int i = (n + startIndex) % MaxImagesCount;
        String path = imageFilePath(i);
        Bitmap bm = BitmapFactory.decodeFile(path);
        return bm;
    }

    public void pushImage(Bitmap bitmap) {
        int i = mImageEndIndex;
        mImageEndIndex++;
        if (mImageEndIndex >= MaxImagesCount) {
            mImageEndIndex -= MaxImagesCount;
        }
        mImagesCount ++;
        if (mImagesCount > MaxImagesCount) {
            mImagesCount = MaxImagesCount;
        }
        String path = imageFilePath(i);
        try {
            File file = new File(path);
            OutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.close();
            save();
        } catch (Exception ex) {
        }
    }

    static private String filePath() {
        return GolosunApp.getBaseFilePath() + "saved_vote_images.json";
    }

    static private String imageFilePath(int index) {
        return GolosunApp.getBaseFilePath() + "image" + index + ".png";
    }

    private void save() {
        try {
            Writer writer = new FileWriter(filePath());
            Gson gson = new GsonBuilder().create();
            gson.toJson(this, writer);
            writer.close();
        } catch(Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    static private SavedVoteImages gInstance;

    static public SavedVoteImages getInstance() {
        if (gInstance != null)
            return gInstance;

        if ((new File(filePath())).exists()) {
            try {
                Reader reader = new FileReader(filePath());
                Gson gson = new GsonBuilder().create();
                gInstance = gson.fromJson(reader, SavedVoteImages.class);
            } catch(Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }

        if (gInstance == null) {
            gInstance = new SavedVoteImages();
        }

        return gInstance;
    }
}
