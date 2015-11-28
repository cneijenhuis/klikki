package com.eje_c.rajawalicardboard;

import android.app.Activity;
import android.os.Bundle;

import com.google.vrtoolkit.cardboard.CardboardActivity;

import org.rajawali3d.cardboard.RajawaliCardboardRenderer;
import org.rajawali3d.cardboard.RajawaliCardboardView;


public class MainActivity extends CardboardActivity {
    MyRenderer renderer;

    boolean displaysCircle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RajawaliCardboardView view = new RajawaliCardboardView(this);
        setContentView(view);
        setCardboardView(view);

        renderer = new MyRenderer(this);
        view.setRenderer(renderer);
        view.setSurfaceRenderer(renderer);
    }

    @Override
    public void onCardboardTrigger() {
        System.out.println("CARDBOARD TRIGGERED");
        if (displaysCircle) {
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
