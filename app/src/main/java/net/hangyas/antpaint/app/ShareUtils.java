package net.hangyas.antpaint.app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by hangyas on 2015-07-02
 */
public class ShareUtils {

    final static int REQUEST_CODE_SHARE_TO_MESSENGER = 2;

    final static SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SS");

    static void share(AntCanvas antCanvas) {
        File file = export(antCanvas);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        intent.setType("image/jpeg");
        MainActivity.self.startActivity(Intent.createChooser(intent, "share"));
    }

    /**
     * PNG-ként elmenti a képet
     * */
    static File export(AntCanvas antCanvas) {
        File file = genImageFile();
        try {
            FileOutputStream out = new FileOutputStream(file);
            antCanvas.bitmap().compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }


    /**
     * üres file generálása megosztáshoz, vagy jsonból ovalsáshoz, vagy fényképezőnek
     * */
    static File genImageFile(){
        Date now = new Date();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Pictures/AntPaint"); // kártya tövében ltérehozza
        myDir.mkdirs();
        String fname = fileNameFormat.format(now) + ".png";
        File file = new File (myDir, fname);
        if (file.exists())
            file.delete();

        return file;
    }

    static File getAntCanvasFile(String id) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/AntPaint/Canvases");
        myDir.mkdirs();
        String fname = id + ".ac";
        File file = new File (myDir, fname);

        return file;
    }

    /**
     * elmenti a szerkeszthető képet
     * formátum
     *      version         int
     *      bitmap.length   int
     *      bitmap          bitmap.length (compressed)
     *      points.length   int
     *      points          points.length
     * */
    private static File save(AntCanvas antCanvas, File file) throws IOException {

        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));

        //versioin
        out.writeInt(1);

        //bitmap
        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
        antCanvas.bitmap().compress(Bitmap.CompressFormat.PNG, 100, bitmapStream );
        byte[] bitmapBytes = bitmapStream.toByteArray();

        out.writeInt(bitmapBytes.length);
        out.write(bitmapBytes);

        //points
        out.writeInt(antCanvas.drawingPoints().length());
        for (int i = 0; i < antCanvas.drawingPoints().length(); ++i){
            Position p = antCanvas.drawingPoints().apply(i);
            out.writeFloat(p.x());
            out.writeFloat(p.y());
        }

        out.flush();
        out.close();

        return file;
    }

    static void openCanvas(AntCanvas antCanvas, File file) throws Exception {
        DataInputStream in = new DataInputStream(new FileInputStream(file));

        //versioin
        int version = in.readInt();
        if (version != 1)
            throw new Exception("unsupported file version");

        //bitmap
        int bitmapSize = in.readInt();
        byte[] bitmapBytes = new byte[bitmapSize];
        in.read(bitmapBytes);

        //points
        int pointsSize = in.readInt();

        Position[] points = new Position[pointsSize];
        for (int i = 0; i < pointsSize; ++i){
            float x = in.readFloat();
            float y = in.readFloat();

            points[i] = new Position(x, y);
        }

        antCanvas.reset();
        antCanvas.drawSaved(BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapSize).copy(Bitmap.Config.ARGB_8888, true), points);
    }

    static void openImage(AntCanvas antCanvas, Bitmap bitmap){
        antCanvas.reset();
        antCanvas.drawSaved(bitmap, null);
    }

    static void openImage(AntCanvas antCanvas, String filePath){
        try {
            openImage(antCanvas, BitmapFactory.decodeFile(filePath).copy(Bitmap.Config.ARGB_8888, true));
        } catch (Exception e) {
            e.printStackTrace();
            MainActivity.msg("Failed to open Picture");
        }
    }

    /**
     * messengeres metadatahoz
     * elmenti a képet(már kiírt fájlból hogy egyszerűbb legyen)
     * és a drawing pointokat
     * vissza tudjuk ovlasni a loadJSON-al
     * */
    private static String makeJSON(String id) throws JSONException, IOException {
        JSONObject metadata = new JSONObject();

        metadata.put("version", 1);
        metadata.put("id", id);

        return metadata.toString();
    }

}
