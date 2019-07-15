package net.hangyas.antpaint.app;

import android.util.Log;
import com.loopj.android.http.*;
import org.apache.http.Header;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Created by hangyas on 2015-07-02
 */
public class AcStore {

//    final static String url = "http://192.168.12.141:8080/app-content/antpaint/canvases/";
    final static String url = "http://hangyas.net/app-content/antpaint/canvases/";

    static AsyncHttpClient client = new AsyncHttpClient();

    static {
        client.setTimeout(20000);
    }

    public static void upload(String id, File f){
        MainActivity.msg("uploading...");
        Log.v("upload", id);
        RequestParams req = new RequestParams();

        req.put("id", id);
        try {
            req.put("file", f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            MainActivity.msg("File uploading failed (1)");
            return;
        }

        client.post(url, req, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //valamiert mindig hibas pedig feltolti
                MainActivity.msg("File uploading failed (" + statusCode + ")");
            }
        });
    }

    /**
     * megpróbáljuk mappából megnyitni, arról lesz egy bool
     * ha nem sikerült akkor szerver
     *
     * mindkét módszer saját maga tölti majd be és tünteti el a loading képernyőt
     * */
    public static void get(String id){
        if (!getFromLocal(id)) {
            //csak akkor jelenítjük meg ah entről szedjük mert különben gyors
            MainActivity.showLoading();
            getFromServer(id);
        }
    }

    private static boolean getFromLocal(String id){
        File file = ShareUtils.getAntCanvasFile(id);

        if (file.exists()){
            try {
                ShareUtils.openCanvas(MainActivity.self.antCanvas, file);
            } catch (Exception e) {
                MainActivity.msg("Failed to open image (0)");
                e.printStackTrace();
            }
            return true;
        }else{
            return false;
            //megzünk leszedjük netről
        }
    }

    private static void getFromServer(final String id){
        Log.v("download", url + id);
        client.get(url + id, new FileAsyncHttpResponseHandler(MainActivity.self.getApplicationContext()) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                MainActivity.msg("Failed to load image (" + statusCode + ")");
                MainActivity.hideLoading();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File response) {
                Log.v("getFromServer", "success");
                try {
                    File file = ShareUtils.getAntCanvasFile(id);

                    //lementjuk mert a serverrol eltunik
                    copyFile(response, file);

                    //betoltjuk
                    ShareUtils.openCanvas(MainActivity.self.antCanvas, file);

                    MainActivity.hideLoading();
                } catch (Exception e) {
                    MainActivity.msg("Failed to load image (1)");
                    e.printStackTrace();
                    MainActivity.hideLoading();
                }
            }
        });
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

}
