package com.example.tesknotam;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import android.os.Handler;
import android.widget.TextView;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PaintingActivity extends AppCompatActivity {

    private static final long SCAN_PERIOD = 10 * 1000;
    private static final long TIME_WAIT_BETWEEN_READINGS_UPDATES = 10;
    //Necessary to make bluetooth readings work properly! Do Not Touch!
    private static final long HARDWARE_TIME_BETWEEN_READINGS = 5;

    private static final int mINDEX_SILENCE_SOUND = 7;
    private static final int mMAX_VOLUME = 60;

    boolean mstartedRepeatingTask = false;
    boolean recently_changed = false;
    Button mbuttonBack;
    ImageView mimageViewPainting;
    TextView mtextViewImageCaption;
    MediaPlayer mmediaPlayer;
    BluetoothConnection mbluetoothConnection;
    Common mcommon = new Common();
    Handler mhandler;
    Handler mRSSIRunnableHandler;
    int currentIndex = -1;
    int change_delay_counter = 0;


    public ArrayList<Integer> mpaintings = new ArrayList<Integer>() {
        {
            add(R.drawable.p1);
            add(R.drawable.p2);
            add(R.drawable.p3);
            add(R.drawable.p4);
            add(R.drawable.p5);
            add(R.drawable.p6);
            add(R.drawable.p7);
            add(R.drawable.p8);
        }
    };

    public ArrayList<Integer> msounds = new ArrayList<Integer>() {
        {
            add(R.raw.sound1);
            add(R.raw.sound2);
            add(R.raw.sound3);
            add(R.raw.sound4);
            add(R.raw.sound5);
            add(R.raw.sound6);
            add(R.raw.sound7);
        }
    };

    public ArrayList<String> mpaintingCaptions = new ArrayList<String>() {
        {
            add("1");
            add("2");
            add("3");
            add("4");
            add("5");
            add("6");
            add("7");
            add("8");
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting);

        mbluetoothConnection = new BluetoothConnection(this);
        checkBluetoothPermissions();

        mbuttonBack = (Button) findViewById(R.id.button_back);
        mimageViewPainting = (ImageView) findViewById(R.id.imageView_painting);
        mtextViewImageCaption = (TextView) findViewById(R.id.textView_imageCaption);

        mhandler = new Handler();

        mRSSIRunnableHandler = new Handler();

        scanLeDevice(this);

        mbuttonBack.setOnClickListener(v -> {
            stopPlayer();
            finish();
            System.exit(0);
        });
    }


    private void scanLeDevice(Context context) {
        if (!mstartedRepeatingTask) {
            mhandler.postDelayed(() -> {
                mbluetoothConnection.mbluetoothLeScanner.stopScan(mbluetoothConnection.mLeScanCallback);
                Log.d("DONE BLE", String.valueOf(mbluetoothConnection.mbluetoothDevices.size()));
                if (mbluetoothConnection.mbluetoothDevices.size() >= mcommon.NUMBER_OF_INODE_DEVICES &&
                        mbluetoothConnection.mbluetoothGattDevices.size() < mcommon.NUMBER_OF_INODE_DEVICES)
                {
                    mbluetoothConnection.connectDevicesToGatt(context);
                    Log.d("DONE GATT", String.valueOf(mbluetoothConnection.mbluetoothGattDevices.size()));
                }
                if (mbluetoothConnection.mbluetoothGattDevices.size() == mcommon.NUMBER_OF_INODE_DEVICES)
                {
                    startRepeatingTask();
                }
                else
                {
                    scanLeDevice(context);
                }
            }, SCAN_PERIOD);
        }
        mbluetoothConnection.mbluetoothLeScanner.startScan(mbluetoothConnection.mLeScanCallback);
    }

    void startRepeatingTask() {
        mstartedRepeatingTask = true;
        mRSSIRunnable.run();
    }

    Runnable mRSSIRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread get_inode_rssi = new Thread(mStatusChecker);
                get_inode_rssi.start();
                try {
                    get_inode_rssi.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!recently_changed)
                {
                    setPainting();
                    recently_changed = true;
                }
                else
                {
                    if (change_delay_counter == 10) {
                        recently_changed = false;
                        change_delay_counter = 0;
                    }
                    else
                    {
                        ++change_delay_counter;
                    }
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mhandler.postDelayed(mRSSIRunnable, TIME_WAIT_BETWEEN_READINGS_UPDATES);
            }
        }
    };

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            for (BluetoothGatt inode : mbluetoothConnection.mbluetoothGattDevices) {
                if (inode.readRemoteRssi() == false)
                {
                    Log.d("RUNNABLE", "ReadRemoteRssi() returns false");
                }
                SystemClock.sleep(HARDWARE_TIME_BETWEEN_READINGS);
            }
            mbluetoothConnection.incrementIndex();
        }
    };

    @Override
    protected void onDestroy() {
        stopPlayer();
        unregisterReceiver(mbluetoothConnection.mBroadcastReceiver1);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
    }

    public void playSound(int index, int maxRSSI) {
        if (mmediaPlayer == null ) {
            mmediaPlayer = MediaPlayer.create(this, msounds.get(index));
            mmediaPlayer.setLooping(true);
            adjustSoundVolume(maxRSSI);
            mmediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlayer();
                }
            });
        }
        mmediaPlayer.start();
    }

    public void adjustSoundVolume(int maxRSSI) {
        if (mmediaPlayer != null) {
            int maxRSSIWithBias = (Math.abs(maxRSSI) / mbluetoothConnection.RSSI_HISTORY_LENGTH) - 20;
            int volume = maxRSSIWithBias > mMAX_VOLUME ? mMAX_VOLUME : Math.max(maxRSSIWithBias, 0);

            if (volume > 40) {
                volume += 5;
            } else if (volume <= 40 && volume > 30) {
                volume -= 5;
            } else {
                volume -= 10;
            }

//            float log1=(float)(Math.log(mMAX_VOLUME - volume)/Math.log(mMAX_VOLUME));
            float log1 = (float)(mMAX_VOLUME - volume) / mMAX_VOLUME;
            mmediaPlayer.setVolume(log1,log1);
        }
    }

    public void pauseSound() {
        if (mmediaPlayer != null) {
            mmediaPlayer.pause();
        }
    }

    public void stopSound() {
        stopPlayer();
    }

    public void stopPlayer() {
        if (mmediaPlayer != null) {
            mmediaPlayer.release();
            mmediaPlayer = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setPainting() {
        if (mbluetoothConnection.rrsi_average_signal == null) return;

        int maxRSSI = 0;
        int maxRSSIIndex = 0;
        for(int i = 0; i < mbluetoothConnection.rrsi_average_signal.length; i++) {
            int RSSI = mbluetoothConnection.rrsi_average_signal[i];
            if(RSSI != 0) {
                if (maxRSSI == 0) {
                    maxRSSI = RSSI;
                    maxRSSIIndex = i;
                } else {
                    if (RSSI > maxRSSI) {
                        maxRSSI = RSSI;
                        maxRSSIIndex = i;
                    }
                }
            }
        }

        if (maxRSSI != 0) {
            if (currentIndex != maxRSSIIndex) {
                currentIndex = maxRSSIIndex;
                stopSound();
                if (maxRSSIIndex != mINDEX_SILENCE_SOUND) {
                    playSound(maxRSSIIndex, maxRSSI);
                }
                Log.d("MAXSSIINDEX: ", String.valueOf(maxRSSIIndex));
                mimageViewPainting.setImageResource(mpaintings.get(maxRSSIIndex));
                mtextViewImageCaption.setText(mpaintingCaptions.get(maxRSSIIndex));
            } else {
                adjustSoundVolume(maxRSSI);
            }
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }
        tryEnableLocation();
    }

    public void tryEnableLocation() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do poprawnego działania aplikacji potrzebny jest GPS, czy chcesz włączyć?")
                .setCancelable(false)
                .setPositiveButton("Tak", (dialog, id) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("Nie", (dialog, id) -> {
                    finish();
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}