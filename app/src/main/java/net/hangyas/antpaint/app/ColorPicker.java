package net.hangyas.antpaint.app;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class ColorPicker extends ActionBarActivity {

    com.larswerkman.holocolorpicker.ColorPicker picker;
    SaturationBar sBar;
    ValueBar vBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        Intent intent = getIntent();

        picker = (com.larswerkman.holocolorpicker.ColorPicker)findViewById(R.id.picker);
        sBar = (SaturationBar)findViewById(R.id.saturationbar);
        vBar = (ValueBar)findViewById(R.id.valuebar);
        
        int lastColor = intent.getIntExtra("lastColor", 0);

        picker.setOldCenterColor(lastColor);
        picker.addSaturationBar(sBar);
        picker.addValueBar(vBar);
        picker.setColor(lastColor);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_color_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //nincs is ilyen
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void ok(View view) {
        Intent resultData = new Intent();
        resultData.putExtra("color", picker.getColor());
        setResult(Activity.RESULT_OK, resultData);
        finish();
    }

    public void cancel(View view) {
        Intent resultData = new Intent();
        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }
}
