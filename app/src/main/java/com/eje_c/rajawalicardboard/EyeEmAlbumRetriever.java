package com.eje_c.rajawalicardboard;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by whateverhuis on 28/11/15.
 */
public class EyeEmAlbumRetriever {
    public class Album {
        final public String[] photoURLs;

        public Album(String[] photoURLs) {
            this.photoURLs = photoURLs;
        }

        public String firstPhotoURL() {
            return photoURLs[0];
        }

        public String[] otherPhotoURLs() {
            return Arrays.copyOfRange(photoURLs, 1, photoURLs.length);
        }

        public Bitmap getPhoto(int pos) {
            if (photoURLs.length < pos) return null;

            try {
                String filename = getFilename(photoURLs[pos]);
                FileInputStream fis = context.openFileInput(filename);
                BufferedInputStream bis = new BufferedInputStream(fis);
                Bitmap bm = BitmapFactory.decodeStream(bis);
                bis.close();
                fis.close();
                return bm;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    RequestQueue mRequestQueue;
    Album[] albums = new Album[8];
    Context context;

    int photosToCache = 0;
    int photosCached = 0;

    public EyeEmAlbumRetriever(Context context) {
        this.context = context;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();
    }

    public void getAlbums() {
        String url = "https://api.eyeem.com/v2/albums?access_token=0ed006490704660a8b8c22fc1deac5adc4be79b6&trending=1&detailed=0";
        // String url = "https://api.eyeem.com/v2/albums?access_token=0ed006490704660a8b8c22fc1deac5adc4be79b6&detailed=0&ids=13216,1349750,1540,626,33,4604,10548559,11645128";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray items = response.getJSONObject("albums").getJSONArray("items");
                            int albumsToRetrieve = Math.min(items.length(), 8);
                            albums = new Album[albumsToRetrieve];
                            for (int i = 0; i < albumsToRetrieve; i++) {
                                JSONObject item = items.getJSONObject(i);
                                JSONArray photos = item.getJSONObject("photos").getJSONArray("items");
                                String[] urls = new String[photos.length()];
                                for (int j = 0; j < photos.length(); j++) {
                                    JSONObject photo = photos.getJSONObject(j);
                                    String photoURL = photo.getString("photoUrl");
                                    photoURL = photoURL.replace("640/480/", "1024/1024/");
                                    urls[j] = photoURL;
                                }
                                albums[i] = new Album(urls);
                            }
                            cacheImagesFromAlbums();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        System.out.println("BOOOH!!!11");

                    }
                });
        mRequestQueue.add(jsObjRequest);
    }

    public void cacheImagesFromAlbums() {
        String[] alreadyDownloaded = context.fileList();
        for(Album album : albums) {
            cachePhoto(album.firstPhotoURL(), alreadyDownloaded);
        }
        for(Album album : albums) {
            for (final String photo : album.otherPhotoURLs()) {
                cachePhoto(photo, alreadyDownloaded);
            }
        }
    }

    private void cachePhoto(final String photo, String[] alreadyDownloaded) {
        final String filename = getFilename(photo);

        for (String d : alreadyDownloaded) {
            if (filename.equals(d)) {
                System.out.println("Photo found in cache!");
                return;
            }
        }

        photosToCache++;
        /*
        StringRequest request = new StringRequest(Request.Method.GET, photo,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String bitmap) {
                        String filename = getFilename(photo);
                        System.out.println(filename + " cached!! " + bitmap.length());
                        System.out.println(bitmap.substring(0, 100));
                        try {
                            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                            outputStream.write(bitmap.getBytes());
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        photosCached++;
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("BOOOH!!!11");
                        System.out.println(error);
                        photosCached++;
                    }
                });
                */
        ImageRequest request = new ImageRequest(photo, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    try {
                        FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                        outputStream.flush();
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    photosCached++;
                }
            },
            1024, 1024, ImageView.ScaleType.CENTER, null,

            new Response.ErrorListener()

            {
                public void onErrorResponse (VolleyError error){
                System.out.println("BOOOH!!!11");
                System.out.println(error);
                photosCached++;
            }
            });

        mRequestQueue.add(request);
    }

    private static String getFilename(String photo) {
        return photo.substring(photo.lastIndexOf("/") + 1) + ".jpg";
    }
}
