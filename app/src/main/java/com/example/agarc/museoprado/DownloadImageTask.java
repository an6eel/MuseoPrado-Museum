package com.example.agarc.museoprado;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {

    Context context;
    ImageView imageView;
    Bitmap bitmap;
    InputStream in = null;
    int responseCode = -1;

    //constructor.
    public DownloadImageTask(Context context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    protected void onPreExecute() {


    }

    @Override
    protected Bitmap doInBackground(String... params) {

        URL url = null;
        try {

            url = new URL(params[0]);

           /* HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();
            responseCode = httpURLConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                in = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
    */
            try {
                InputStream in = url.openStream();
                bitmap= BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Bitmap bmpWithBorder = Bitmap.createBitmap(bitmap.getWidth() + 20 * 2, bitmap.getHeight() + 20 * 2, bitmap.getConfig());
            Canvas canvas = new Canvas(bmpWithBorder);
            canvas.drawColor(Color.parseColor("#3d1e00"));
            canvas.drawBitmap(bitmap,20, 20, null);
            bitmap = bmpWithBorder;
                /*in.close();
            }*/
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    @Override
    protected void onPostExecute(Bitmap data) {
        imageView.setImageBitmap(data);
    }



}

