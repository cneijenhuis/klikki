package com.eje_c.rajawalicardboard;

import android.app.Activity;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.google.vrtoolkit.cardboard.CardboardActivity;

import org.rajawali3d.cardboard.RajawaliCardboardRenderer;
import org.rajawali3d.cardboard.RajawaliCardboardView;


public class MainActivity extends CardboardActivity {
    String token = "0ed006490704660a8b8c22fc1deac5adc4be79b6";

    MyRenderer renderer;

    boolean displaysCircle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final RajawaliCardboardView view = new RajawaliCardboardView(this);
        setContentView(view);
        setCardboardView(view);

//        renderer = new MyRenderer(this);
//        view.setRenderer(renderer);
//        view.setSurfaceRenderer(renderer);

        final MainActivity thiis = this;

        final EyeEmAlbumRetriever eear = new EyeEmAlbumRetriever(getApplicationContext());
        eear.getAlbums();

        renderer = new EyemAlbumRenderer(thiis, eear);
        view.setRenderer(renderer);
        view.setSurfaceRenderer(renderer);

        eear.mRequestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {
                System.out.println("Request finished!");
                if (eear.photosCached == 9) {
                    onSomeImagesLoaded();
                }
            }
        });
    }

    public synchronized void onSomeImagesLoaded() {
        renderer.unloadImage();
        renderer.loadCircleOfImages();
        displaysCircle = true;
    }

    @Override
    public synchronized void onCardboardTrigger() {
//        System.out.println("CARDBOARD TRIGGERED");
        if (displaysCircle) {
            renderer.setPicturePos();
            renderer.unloadCircleOfImages();
            renderer.loadImage();
            displaysCircle = false;
        }
        else if (renderer.hasMoreImages()) {
            renderer.loadImage();
        }
        else {
            renderer.unloadImage();
            renderer.loadCircleOfImages();
            displaysCircle = true;
        }
    }
}
