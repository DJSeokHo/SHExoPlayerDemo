package kr.co.dotv365.shexoplayerdemo.framework.util.bitmaps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by seokho on 10/11/2016.
 */

public class BitmapUtil {

    /**
     * must in thread
     *
     * @param urlString
     * @return
     */
    public static Bitmap getBitmapFromUrl(String urlString) {

        try {

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        }
        catch (IOException e) {
            // Log exception
            e.printStackTrace();
            return null;
        }

    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ?Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565;// 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmapFromDrawableResource(Context context, int resourceId) {
        Drawable drawable = context.getResources().getDrawable(resourceId);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ?Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565;// 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmapFromFile(File file) {

        Bitmap bitmap;

        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        return bitmap;

    }

    public static Bitmap getBitmapFromPath(String path) {
        Bitmap bitmap;

        bitmap = BitmapFactory.decodeFile(path);

        return bitmap;
    }


    public static byte[] bitmapToByte(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bitmapByte = byteArrayOutputStream.toByteArray();

        return bitmapByte;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFilePath(String imagePath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath,options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static void rotateImage(String file, int angle){

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, bounds);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(file, opts);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;


        // for OOM
        float ratio = 1.0f;

        Matrix matrix = new Matrix();
        matrix.setRotate(angle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, (int) (bounds.outWidth * ratio), (int) (bounds.outHeight * ratio), matrix, true);
        saveBitmapToJpeg(rotatedBitmap, file);
    }


    public static void rotateImage(String file){

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, bounds);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(file, opts);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;


        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

        // for OOM
        float ratio = 1.0f;

        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, (int) (bounds.outWidth * ratio), (int) (bounds.outHeight * ratio), matrix, true);
        saveBitmapToJpeg(rotatedBitmap, file);
    }

    public static void saveBitmapToJpeg(Bitmap bitmap, String path){
        FileOutputStream out = null;
        try{

            out = new FileOutputStream(path);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        }
        catch(FileNotFoundException exception){
            exception.printStackTrace();
        }
        finally{
            if(out != null)
                try {
                    out.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
        }
    }

    public static void ResizeImages(String path, int quality) {

        Bitmap photo = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, quality, bytes);

        File f = new File(path);
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getScaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();
        return resizedBitmap;
    }

    public static Bitmap getBitmapFromByte(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }


    public static Bitmap extractFrame(float ms, MediaMetadataRetriever mediaMetadataRetriever) {

        //OPTION_CLOSEST ,在给定的时间，检索最近一个帧,这个帧不一定是关键帧。
        //OPTION_CLOSEST_SYNC   在给定的时间，检索最近一个同步与数据源相关联的的帧（关键帧）
        //OPTION_NEXT_SYNC 在给定时间之后检索一个同步与数据源相关联的关键帧。
        //OPTION_PREVIOUS_SYNC 在给定时间之前检索一个同步与数据源相关联的关键帧。

        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime((long) (ms * 1000), MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        return bitmap;
    }

    public static Bitmap concatBitmaps(int margin, List<Bitmap> bitmaps) {
        int width = 0;
        int height = 0;
        int leng = bitmaps.size();
        for (int i = 0; i < leng; i++) {
            width += bitmaps.get(i).getWidth();
            width += margin;
            height = Math.max(height, bitmaps.get(i).getHeight());
        }
        width -= margin;
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        int left = 0;
        for (int i = 0; i < leng; i++) {
            if (i > 0) {
                left += bitmaps.get(i - 1).getWidth();
                left += margin;
            }
            canvas.drawBitmap(bitmaps.get(i), left, (height - bitmaps.get(i).getHeight()), null);
        }
        return result;
    }



}
