package com.eje_c.rajawalicardboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.vrtoolkit.cardboard.HeadTransform;

import org.rajawali3d.Object3D;
import org.rajawali3d.WorldParameters;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.AnimationQueue;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.cardboard.RajawaliCardboardRenderer;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.Sphere;

public class MyRenderer extends RajawaliCardboardRenderer {

    public Plane[] pictureCircle = new Plane[8];

    public Plane picture = null;
    public Plane oldPicture = null;
    public int picturePos = 0;
    public float distance = 65;
    public float distance2 = 46;

    int[] imgs = new int[] {
            R.drawable.picture00,
            R.drawable.picture01, R.drawable.picture02,
            R.drawable.picture03, R.drawable.picture04,
            R.drawable.picture05, R.drawable.picture06,
            R.drawable.picture07
    };
    int imgPointer = -1;

    Bitmap bm;
    Bitmap[] bmCircle = new Bitmap[8];

    public float[] fwd = new float[3];

    public MyRenderer(Context context) {
        super(context);
    }

    public void moveDown(Object3D oldPicture, double limit) {
        if (oldPicture != null) {
            Vector3 pos = oldPicture.getPosition();
            if (pos.y > limit) {
                Vector3 newPos = pos.add(0, -4d, 0);
                oldPicture.setPosition(newPos);
            }
        }
    }

    public void setPicturePos() {
        picturePos = (int)((Math.atan2(fwd[0], -fwd[2]) / Math.PI * 4d) + 4.5d) % 8;
        imgPointer = picturePos - 1;
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        super.onNewFrame(headTransform);

        double oldLimit = -100d;
        moveDown(oldPicture, oldLimit);
        moveDown(picture, 0d);
        if (oldPicture != null && oldPicture.getPosition().y <= oldLimit) {
            getCurrentScene().removeChild(oldPicture);
            oldPicture = null;
        }

        headTransform.getForwardVector(fwd, 0);
    }

    @Override
    protected void initScene() {
        loadCircleOfImages();

        getCurrentCamera().setPosition(Vector3.ZERO);
        getCurrentCamera().setFieldOfView(75);
    }

    public void loadCircleOfImages() {
        imgPointer = -1;
        for (int i = 0; i < 8; i++) {
            bmCircle[i] = getNextCircleBitmap(i);
            if (bmCircle[i] == null) continue;
            pictureCircle[i] = createPhotoSphereWithTexture(bmCircle[i], 50f);
            pictureCircle[i].setPosition(position(i));
            pictureCircle[i].setRotation(Vector3.Axis.Y, rotation(i));
            getCurrentScene().addChild(pictureCircle[i]);
        }
    }

    public synchronized void unloadCircleOfImages() {
        for (int i = 0; i < 8; i++) {
            if (bmCircle[i] != null) {
                bmCircle[i].recycle();
                bmCircle[i] = null;
            }
            if (pictureCircle[i] != null) {
                getCurrentScene().removeChild(pictureCircle[i]);
                pictureCircle[i] = null;
            }
        }
        getCurrentScene().clearChildren();
    }

    private Vector3 position(int i) {
        if (i == 0) return new Vector3(0, 0, distance);
        if (i == 1) return new Vector3(distance2, 0, distance2);
        if (i == 2) return new Vector3(distance, 0, 0);
        if (i == 3) return new Vector3(distance2, 0, -distance2);
        if (i == 4) return new Vector3(0, 0, -distance);
        if (i == 5) return new Vector3(-distance2, 0, -distance2);
        if (i == 6) return new Vector3(-distance, 0, 0);
        else        return new Vector3(-distance2, 0, distance2);
    }

    private double rotation(int i) {
        if (i == 0) return 180;
        if (i == 1) return 135;
        if (i == 2) return 90;
        if (i == 3) return 45;
        if (i == 4) return 0;
        if (i == 5) return -45;
        if (i == 6) return -90;
        else        return -135;
//       return 180 + ((360 / 8) * -i);
//        System.out.println(2d / 8d * i);
//        return (2d / 8d * (double)i) - 1d;
//        return 180;
    }

    public synchronized void loadImage() {
        if (oldPicture != null) {
            // ignore
            return;
        }
        oldPicture = picture;

        if (bm != null) bm.recycle();

        bm = getNextAlbumBitmap();
        if (bm == null) return;
        picture = createPhotoSphereWithTexture(bm, 100f);

        picture.setPosition(position(picturePos));
        picture.setRotation(Vector3.Axis.Y, rotation(picturePos));
        if (oldPicture != null) {
            float height = oldPicture.mHeight / 2 + picture.mHeight / 2 + 2;
            picture.setPosition(picture.getPosition().add(0, height, 0));
        }
        getCurrentScene().addChild(picture);
    }

    public synchronized void unloadImage() {
        unloadImage(oldPicture);
        oldPicture = null;
        unloadImage(picture);
        picture = null;
    }

    public boolean hasMoreImages() {
        return imgPointer + 1 < imgs.length;
    }

    private void unloadImage(Plane oldPicture) {
        if (oldPicture != null) getCurrentScene().removeChild(oldPicture);
    }

    protected Bitmap getNextCircleBitmap(int pos) {
        return getNextBitmap();
    }

    protected Bitmap getNextAlbumBitmap() {
        return getNextBitmap();
    }

    private Bitmap getNextBitmap() {
        imgPointer = (imgPointer + 1) % imgs.length;
//        System.out.println(imgPointer);
        return BitmapFactory.decodeResource(mContext.getResources(), imgs[imgPointer]);
    }

    private static Plane createPhotoSphereWithTexture(Bitmap bm, float maxWidth) {
        Texture texture = new Texture("photo", bm);
        float scale = 0f;
        if (bm.getHeight() > bm.getWidth()) {
            scale = maxWidth / bm.getHeight();
        }
        else {
            scale = maxWidth / bm.getWidth();
        }

        Material material = new Material();
        material.setColor(0);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Plane sphere = new Plane(bm.getWidth() * scale, bm.getHeight() * scale, 1, 1);
        sphere.setMaterial(material);

        return sphere;
    }

}
