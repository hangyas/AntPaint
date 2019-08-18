package net.hangyas.antpaint.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.multidex.MultiDex;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    public static MainActivity self; //scala bug miatt csak így érjük el(https://github.com/pfn/android-sdk-plugin/issues/173)

    static final int REQUEST_COLOR_PICKER = 1;
    static final int REQUEST_IMAGE_GET = 2;

    //gui elemek
    AntCanvas antCanvas;
    FloatingActionMenu colorMenu;
    FloatingActionMenu toolMenu;
    RelativeLayout gui;
    PopupMenu popup;
    ImageButton guiShower;
    ProgressDialog progressDialog;
    LockableScrollView toolScroll;
    ActionBar actionBar;
    android.support.v7.widget.Toolbar toolbar;

    private static final int guiShowerDelay = 40;
    private int guiShowerCounter = guiShowerDelay;

    private int[] colors = new int[]{0xff000000, 0xffe91e63, 0xff9c27b0, 0xff03a9f4, 0xff4caf50};

    // fényképezésnél ide elmentjük a nevét, a fényképező ide a képet, azátn megtaláljuk
    private Uri lastPhotoUri = null;

    /**
     * color picker activity megnyitása
     * */
    public void startColorPicker(View v){
        Intent intent = new Intent(this, ColorPicker.class);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("lastColor", antCanvas.activeColor());
        startActivityForResult(intent, REQUEST_COLOR_PICKER);
    }

    public void selectImage() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        lastPhotoUri = Uri.fromFile(ShareUtils.genImageFile());
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, lastPhotoUri);


        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {cameraIntent});

        startActivityForResult(chooserIntent, REQUEST_IMAGE_GET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COLOR_PICKER){
            colorMenu.close(true);
            if (resultCode == Activity.RESULT_OK){
                int color = data.getIntExtra("color", 0);
                antCanvas.setColor(color);
                updateColors(color);
            }
        }

        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            if (data == null){
                //camera - nemtom mert null az intje es hogy meddig marad igy
                //thumbnail : ShareUtils.open(antCanvas, (Bitmap) data.getExtras().get("data"));
                try {
                    ShareUtils.openImage(antCanvas, lastPhotoUri.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                    msg("something went wrong");
                }
            }else{
                ShareUtils.openImage(antCanvas, getFilePath(data.getData()));
            }
        }
    }

    /**
     * beálítja a colorMenuBtn és a palette gomb színét
     * */
    private void setColorBtns(int color){
        FloatingActionButton pickerBtn = (FloatingActionButton) colorMenu.getChildAt(0);
        pickerBtn.setColorNormal(color);
        pickerBtn.setColorPressed(color & 0x00ffffff | 0x99000000);
        colorMenu.setMenuButtonColorNormal(color);
        colorMenu.setMenuButtonColorPressed(color & 0x00ffffff | 0x99000000);
    }

    View.OnClickListener colorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            colorMenu.close(true);
            FloatingActionButton btn = (FloatingActionButton) v;
            antCanvas.setColor(btn.getColorNormal());

            setColorBtns(btn.getColorNormal());
        }
    };

    public void toolClick(View v){
        antCanvas.setTool(v.getId());
        toolMenu.close(true);
    }

     /**
     * eltolja a színpalettát eggyel
     * @param c a paletta tetejére adandó szín
     * */
    void updateColors(int c){
        for (int i = colors.length - 1; i > 0; --i)
            colors[i] = colors[i - 1];
        colors[0] = c;

        for (int i = 0; i < colors.length; ++i){
            FloatingActionButton btn = (FloatingActionButton) colorMenu.getChildAt(i + 1);
            btn.setColorNormal(colors[i]);
            btn.setColorPressed(colors[i] & 0x00ffffff | 0x99000000);
        }

        setColorBtns(c);
    }

    //scala bug(?) miatt csak így érjük el
    static void hideGui() {
        self.toolMenu.close(false);
        self.colorMenu.close(false);
        self.gui.setVisibility(View.INVISIBLE);
        if(self.popup != null){
            self.popup.dismiss();
            self.popup = null;
        }

        self.guiShower.setVisibility(View.INVISIBLE);
        self.guiShowerCounter = guiShowerDelay;

        self.toolScroll.setEnableScrolling(false);

        self.actionBar.hide();
    }

    static void showGui(){
        self.invalidateOptionsMenu();
        self.gui.setVisibility(View.VISIBLE);
        self.actionBar.show();
        self.guiShower.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        self = this;

        MultiDex.install(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setShowHideAnimationEnabled(true);

        antCanvas = (AntCanvas) findViewById(R.id.canvas);
        colorMenu = (FloatingActionMenu) findViewById(R.id.color_menu);
        toolMenu = (FloatingActionMenu) findViewById(R.id.tool_menu);
        gui = (RelativeLayout) findViewById(R.id.gui);
        guiShower = (ImageButton) findViewById(R.id.gui_shower);
        toolScroll = (LockableScrollView) findViewById(R.id.tool_scroll);

        for (int i = 0; i < colors.length; ++i){
            FloatingActionButton btn = new FloatingActionButton(getBaseContext());
            btn.setOnClickListener(colorClickListener);
            btn.setColorNormal(colors[i]);
            btn.setColorPressed(colors[i] & 0x00ffffff | 0x99000000);
            btn.setButtonSize(FloatingActionButton.SIZE_MINI);
            colorMenu.addMenuButton(btn);
        }
        setColorBtns(antCanvas.tool().paint().getColor());

        toolScroll.scrollDown();
        toolScroll.setEnableScrolling(false);

        toolMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean b) {
                toolScroll.toggleEnableScrolling();
                if (!toolScroll.isEnableScrolling()){
                    toolScroll.scrollDown();
                }
            }
        });

        //képmegynitás más appokból

        Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction())){
            ShareUtils.openImage(antCanvas, getFilePath((Uri) intent.getExtras().get(Intent.EXTRA_STREAM)));
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if(keyCode == KeyEvent.KEYCODE_BACK && gui.getVisibility() == View.INVISIBLE) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
                    .setTitle("Close AntPaint?")
                    .setIcon(R.drawable.alert)
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }

                    })
                    .setNegativeButton("Stay", null)
                    .show();

            return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK && gui.getVisibility() == View.VISIBLE) {
            hideGui();
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_MENU && gui.getVisibility() == View.VISIBLE) {
            hideGui();
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_MENU && gui.getVisibility() == View.INVISIBLE) {
            showGui();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        MenuItem undoItem = menu.findItem(R.id.action_undo);
        undoItem.getIcon().setAlpha(HistoryManager.canUndo() ? 255 : 127);

        MenuItem redoItem = menu.findItem(R.id.action_redo);
        redoItem.getIcon().setAlpha(HistoryManager.canRedo() ? 255 : 127);

        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_undo:
                HistoryManager.undo();
                invalidateOptionsMenu();
                break;
            case R.id.action_redo:
                HistoryManager.redo();
                invalidateOptionsMenu();
                break;
            case R.id.action_share:
                ShareUtils.share(antCanvas);
                break;
            case R.id.action_new:
                antCanvas.reset();
                break;
            case R.id.action_import:
                selectImage();
                break;
            case R.id.action_export:
                ShareUtils.export(antCanvas);
                Toast.makeText(getBaseContext(), "saved", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //Messenger

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public static void showLoading(){
        self.progressDialog = ProgressDialog.show(self, "",
                "Loading. Please wait...", true);
    }

    public static void hideLoading(){
        self.progressDialog.hide();
    }

    private static Toast lastToast;
    public static void msg(String s) {
        if (lastToast != null)
            lastToast.cancel();
        lastToast = Toast.makeText(self.getApplicationContext(), s, Toast.LENGTH_LONG);
        lastToast.show();
    }

    public void showGui(View view) {
        showGui();
    }

    public static void handleGuiShower(){
        if(self.gui.getVisibility() == View.INVISIBLE && self.guiShower.getVisibility() == View.INVISIBLE) {
            --self.guiShowerCounter;
            if (self.guiShowerCounter == 0){
                self.guiShower.setVisibility(View.VISIBLE);
            }
        }
    }

    String getFilePath(Uri uri){
        File f = new File(uri.getPath());

        // file: uri volt es meg lehet egyszeruen szerezni
        if (f.exists())
            return f.getAbsolutePath();

        // content: uri

        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                uri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        return filePath;
    }
}
