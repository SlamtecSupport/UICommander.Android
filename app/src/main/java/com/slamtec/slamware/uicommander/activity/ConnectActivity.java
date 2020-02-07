package com.slamtec.slamware.uicommander.activity;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.slamtec.slamware.uicommander.R;
import com.slamtec.slamware.uicommander.SlamwareAgent;
import com.slamtec.slamware.uicommander.event.ConnectedEvent;
import com.slamtec.slamware.uicommander.event.ConnectionLostEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ConnectActivity extends BaseActivity {
    private final static String TAG = "ConnectActivity";
    private static final String SP_FILE_NAME = "device_info";
    private static final String KEY_IP_ADDRESS = "ip_address";
    private static final int CODE_FOR_WRITE_PERMISSION = 1234;

    private String mIpAddress;
    private SlamwareAgent mAgent;

    private Button mButtonConnect;
    private EditText mEditTextAddress;

    private ProgressDialog mDialogConnecting;
    private AlertDialog mDialogConnectFailed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        initViews();
        mAgent = getSlamwareAgent();
    }

    private void initViews() {
        mButtonConnect = (Button) findViewById(R.id.btn_connect_robot);
        mEditTextAddress = (EditText) findViewById(R.id.et_ip_address);
        mIpAddress = getLastDeviceInfo();
        mEditTextAddress.setText(mIpAddress);

        mButtonConnect.setOnClickListener(view -> {
            mIpAddress = mEditTextAddress.getText().toString();
            if (TextUtils.isEmpty(mIpAddress)) return;

            saveDeviceInfo(mIpAddress);
            connectToDevice(mIpAddress);
        });

        if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_FOR_WRITE_PERMISSION);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        hideConnectingDialog();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void connectToDevice(String ip) {
        mDialogConnecting = new ProgressDialog(this);
        mDialogConnecting.setMessage("正在连接...");
        mDialogConnecting.setCancelable(false);
        mDialogConnecting.setCanceledOnTouchOutside(false);
        mDialogConnecting.show();

        mAgent.connectTo(ip);
    }

    private void showConnectFailedDialog() {
        if (mDialogConnectFailed != null && mDialogConnectFailed.isShowing()) return;

        mDialogConnectFailed = new AlertDialog.Builder(this).setMessage("连接失败").setPositiveButton("确认", ((in, i) -> in.dismiss())).show();
    }

    private void hideConnectingDialog() {
        if (mDialogConnecting != null && mDialogConnecting.isShowing()) {
            mDialogConnecting.dismiss();
            mDialogConnecting = null;
        }
    }

    private void gotoMonitorCenterActivity() {
        hideConnectingDialog();
        startActivity(new Intent(this, MonitorCenterActivity.class));
    }

    private String getLastDeviceInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(SP_FILE_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_IP_ADDRESS, "192.168.11.1");
    }

    private void saveDeviceInfo(String ipAddress) {
        SharedPreferences sharedPreferences = getSharedPreferences(SP_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_IP_ADDRESS, ipAddress);
        editor.commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectionLostEvent event) {
        hideConnectingDialog();
        showConnectFailedDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectedEvent event) {
        hideConnectingDialog();
        gotoMonitorCenterActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CODE_FOR_WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "获取权限失败，无法使用该软件", Toast.LENGTH_SHORT).show();
                SystemClock.sleep(200);
                finish();
            }
        }
    }


}
