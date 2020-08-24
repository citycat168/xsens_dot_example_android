//  Copyright (c) 2003-2020 Xsens Technologies B.V. or subsidiaries worldwide.
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without modification,
//  are permitted provided that the following conditions are met:
//
//  1.      Redistributions of source code must retain the above copyright notice,
//           this list of conditions, and the following disclaimer.
//
//  2.      Redistributions in binary form must reproduce the above copyright notice,
//           this list of conditions, and the following disclaimer in the documentation
//           and/or other materials provided with the distribution.
//
//  3.      Neither the names of the copyright holders nor the names of their contributors
//           may be used to endorse or promote products derived from this software without
//           specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
//  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
//  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
//  THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//  SPECIAL, EXEMPLARY OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
//  OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY OR
//  TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.THE LAWS OF THE NETHERLANDS
//  SHALL BE EXCLUSIVELY APPLICABLE AND ANY DISPUTES SHALL BE FINALLY SETTLED UNDER THE RULES
//  OF ARBITRATION OF THE INTERNATIONAL CHAMBER OF COMMERCE IN THE HAGUE BY ONE OR MORE
//  ARBITRATORS APPOINTED IN ACCORDANCE WITH SAID RULES.
//

package com.xsens.dot.android.example.views;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.xsens.dot.android.example.R;
import com.xsens.dot.android.example.databinding.ActivityMainBinding;
import com.xsens.dot.android.example.interfaces.ScanClickInterface;
import com.xsens.dot.android.example.utils.Utils;
import com.xsens.dot.android.example.viewmodels.BluetoothViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BLUETOOTH = 1001;
    private static final int REQUEST_PERMISSION_LOCATION = 1002;

    public static final String FRAGMENT_TAG_SCAN = "scan";
    public static final String FRAGMENT_TAG_CHART = "chart";

    private ActivityMainBinding mBinding;
    private BluetoothViewModel mBluetoothViewModel;

    private boolean mIsScanning = false;
    private ScanClickInterface mScanListener;

    public static String sCurrentFragment = FRAGMENT_TAG_SCAN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());

        setupContainer();
        bindViewModel();
        checkBluetoothAndPermission();

        registerReceiver(mBluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onPostResume() {

        super.onPostResume();

        bindViewModel();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        unregisterReceiver(mBluetoothStateReceiver);
    }

    @Override
    public void onBackPressed() {

        FragmentManager manager = getSupportFragmentManager();

        if (manager.getBackStackEntryCount() > 0) manager.popBackStack();
        else super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() - requestCode = " + requestCode + ", resultCode = " + resultCode);

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {

            if (resultCode == RESULT_OK) checkBluetoothAndPermission();
            else Toast.makeText(this, getString(R.string.hint_turn_on_bluetooth), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult() - requestCode = " + requestCode);

        if (requestCode == REQUEST_PERMISSION_LOCATION) {

            for (int i = 0; i < grantResults.length; i++) {

                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {

                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) checkBluetoothAndPermission();
                    else Toast.makeText(this, getString(R.string.hint_allow_location), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem scanItem = menu.findItem(R.id.action_scan);
        MenuItem measureItem = menu.findItem(R.id.action_measure);

        if (mIsScanning) scanItem.setTitle(getString(R.string.menu_stop_scan));
        else scanItem.setTitle(getString(R.string.menu_start_scan));

        if (sCurrentFragment.equals(FRAGMENT_TAG_SCAN)) {

            scanItem.setVisible(true);
            measureItem.setVisible(true);

        } else if (sCurrentFragment.equals(FRAGMENT_TAG_CHART)) {

            scanItem.setVisible(false);
            measureItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.action_scan:

                if (mScanListener != null && checkBluetoothAndPermission()) {

                    if (mIsScanning) mScanListener.onScanTriggered(false);
                    else mScanListener.onScanTriggered(true);
                }
                break;

            case R.id.action_measure:

                Fragment chartFragment = ChartFragment.newInstance();
                addFragment(chartFragment, FRAGMENT_TAG_CHART);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupContainer() {

        if (null != getIntent()) {

            ScanFragment fragment = ScanFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG_SCAN).commit();
        }
    }

    private void addFragment(Fragment fragment, String tag) {

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, tag).addToBackStack(null).commit();
    }

    /**
     * Check the state of Bluetooth adapter and location permission.
     */
    private boolean checkBluetoothAndPermission() {

        boolean isBluetoothEnabled = Utils.isBluetoothAdapterEnabled(this);
        boolean isPermissionGranted = Utils.isLocationPermissionGranted(this);

        if (isBluetoothEnabled) {
            if (!isPermissionGranted) Utils.requestLocationPermission(this, REQUEST_PERMISSION_LOCATION);
        } else {
            Utils.requestEnableBluetooth(this, REQUEST_ENABLE_BLUETOOTH);
        }

        boolean status = isBluetoothEnabled && isPermissionGranted;
        Log.i(TAG, "checkBluetoothAndPermission() - " + status);

        mBluetoothViewModel.updateBluetoothEnableState(status);
        return status;
    }

    private void bindViewModel() {

        mBluetoothViewModel = BluetoothViewModel.getInstance(this);

        mBluetoothViewModel.isScanning().observe(this, new Observer<Boolean>() {

            @Override
            public void onChanged(Boolean scanning) {

                mIsScanning = scanning;
                invalidateOptionsMenu();
            }
        });
    }

    public void setScanTriggerListener(ScanClickInterface listener) {

        mScanListener = listener;
    }

    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (action != null) {

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                    switch (state) {

                        case BluetoothAdapter.STATE_OFF:
                            mBluetoothViewModel.updateBluetoothEnableState(false);
                            break;

                        case BluetoothAdapter.STATE_ON:
                            mBluetoothViewModel.updateBluetoothEnableState(true);
                            break;
                    }
                }
            }
        }
    };
}
