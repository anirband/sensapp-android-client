/**
 * Copyright (C) 2012 SINTEF <fabien@fleurey.com>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
import org.sensapp.android.sensappdroid.api.SensAppHelper;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * This is the UI used to run or stop the sensor logging into SensApp.
 */
public class SensorActivity extends Activity{

    protected static final String SERVICE_RUNNING = "pref_service_is_running";
    private static final String TAG = SensorActivity.class.getSimpleName();

    final static int GREY=0xFFCCDDFF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView title = (TextView)findViewById(R.id.app_title);
        title.setText(R.string.app_title);

        SensorLoggerService.initSensorArray();

        final LinearLayout l = (LinearLayout) findViewById(R.id.general_view);

        SensorManager mManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        for(Sensor s: mManager.getSensorList(Sensor.TYPE_ALL)){
            final AndroidSensor as = new AndroidSensor(s, "Android_Tab");
            as.setRefreshRate(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(as.getName(), AndroidSensor.DEFAULT_RATE));
            SensorLoggerService.addSensor(as);
            final Button b = new Button(this);
            final ImageView image = new ImageView(this);
            LinearLayout line = new LinearLayout(this);
            LinearLayout forImage = new LinearLayout(this);

            initButton(b, as, image);
            initImage(image, l);
            initMainAppView(l, line, forImage, image, b);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferences:
                startActivity(new Intent(getApplicationContext(), Preferences.class));
                return true;
        }
        return false;
    }

    private void initMainAppView(LinearLayout l, LinearLayout line, LinearLayout img, ImageView image, Button b){
        img.setPadding(65, 10, 55, 0);
        img.addView(image);
        line.setPadding(0,11,50,0);
        line.addView(img);
        line.addView(b);
        l.addView(line);
        LinearLayout separator = new LinearLayout(this);
        separator.setMinimumHeight(1);
        separator.setMinimumWidth(l.getWidth());
        separator.setBackgroundColor(GREY);
        l.addView(separator);
    }

    private void initButton(final Button b, final AndroidSensor as, final ImageView image){
        b.setText("Start logging " + as.getName());
        b.setBackgroundColor(Color.BLACK);
        b.setMinimumHeight(50);
        b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.LEFT);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (!preferences.getBoolean(SERVICE_RUNNING, false)) {
                    // If service is not running.

                    // Check if SensApp is installed.
                    if (!SensAppHelper.isSensAppInstalled(getApplicationContext())) {
                        // If not suggest to install and return.
                        SensAppHelper.getInstallationDialog(SensorActivity.this).show();
                        return;
                    }
                }

                if (as.isListened()) {
                    as.setListened(false);
                    b.setText("Start logging " + as.getName());
                    image.setImageResource(R.drawable.button_round_red);

                    if(SensorLoggerService.noSensorListened() && preferences.getBoolean(SERVICE_RUNNING, false)){
                        // Service is running so it must stop.
                        // Update the preference.
                        preferences.edit().putBoolean(SERVICE_RUNNING, false).commit();
                        // Request for disable, cancel the alarm.
                        AlarmHelper.cancelAlarm(getApplicationContext());
                    }
                } else {
                    as.setListened(true);
                    b.setText("Stop logging " + as.getName());
                    image.setImageResource(R.drawable.button_round_green);

                    if (!preferences.getBoolean(SERVICE_RUNNING, false)) {
                        // Update the preference. Service is now running.
                        preferences.edit().putBoolean(SERVICE_RUNNING, true).commit();
                        // Schedule a repeating alarm to start the service, which stops itself.
                        AlarmHelper.setAlarm(getApplicationContext());
                    }
                }
            }
        });
    }

    private void initImage(ImageView img, LinearLayout l){
        img.setImageResource(R.drawable.button_round_red);
        img.setMinimumWidth(l.getWidth());
        img.setScaleType(ImageView.ScaleType.FIT_START);
    }
}
