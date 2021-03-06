package com.eje_c.rajawalicardboard;

import android.content.Context;
import android.graphics.Bitmap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import ly.kite.catalogue.Asset;

/**
 * Created by whateverhuis on 29/11/15.
 */
public class EyemAlbumRenderer extends MyRenderer {
    EyeEmAlbumRetriever eear;

    private int albumPointer = 0;
    private int inAlbumPos = -1;

    public EyemAlbumRenderer(Context context, EyeEmAlbumRetriever eear) {
        super(context);
        this.eear = eear;
    }

    public void setPicturePos() {
        super.setPicturePos();
        albumPointer = (imgPointer + 1) % 8;
        inAlbumPos = -1;
    }

    public boolean hasMoreImages() {
        EyeEmAlbumRetriever.Album album = eear.albums[albumPointer];
        if (album == null) return false;
        return inAlbumPos + 1 < album.photoURLs.length;
    }

    protected Bitmap getNextCircleBitmap(int pos) {
        EyeEmAlbumRetriever.Album album = eear.albums[pos];
        if (album == null) return null;
        return album.getPhoto(0);
    }

    protected Bitmap getNextAlbumBitmap() {
        if (albumPointer == -1) {
            System.out.println("LOLWUT?");
            return null;
        }
        EyeEmAlbumRetriever.Album album = eear.albums[albumPointer];
        if (album == null) {
            System.out.println("Album is null :(");
            return null;
        }
        inAlbumPos++;
        return album.getPhoto(inAlbumPos);
    }

    public ArrayList<Asset> assets() {
        ArrayList<Asset> assets = new ArrayList<>();
        EyeEmAlbumRetriever.Album album = eear.albums[albumPointer];
        if (album == null) return assets;
        try {
            for (String photo : album.photoURLs) assets.add(new Asset(new URL( photo + ".jpg" )));
        } catch (MalformedURLException ex) {/* ignore */}
        return assets;
    }
}
