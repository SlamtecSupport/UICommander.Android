package com.slamtec.slamware.uicommander.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.slamtec.slamware.action.MoveDirection;
import com.slamtec.slamware.action.Path;
import com.slamtec.slamware.geometry.Line;
import com.slamtec.slamware.geometry.PointF;
import com.slamtec.slamware.robot.CompositeMap;
import com.slamtec.slamware.robot.LaserScan;
import com.slamtec.slamware.robot.Location;
import com.slamtec.slamware.robot.Map;
import com.slamtec.slamware.robot.Pose;
import com.slamtec.slamware.sdp.CompositeMapHelper;
import com.slamtec.slamware.uicommander.R;
import com.slamtec.slamware.uicommander.event.ActionStatusGetEvent;
import com.slamtec.slamware.uicommander.event.GetCompositeMapEvent;
import com.slamtec.slamware.uicommander.event.RobotInfoEvent;
import com.slamtec.slamware.uicommander.event.MapUpdataEvent;
import com.slamtec.slamware.uicommander.event.RemainingMilestonesGetEvent;
import com.slamtec.slamware.uicommander.event.RemainingPathGetEvent;
import com.slamtec.slamware.uicommander.event.RobotHealthInfoEvent;
import com.slamtec.slamware.uicommander.event.RobotPoseGetEvent;
import com.slamtec.slamware.uicommander.SlamwareAgent;
import com.slamtec.slamware.uicommander.event.ConnectionLostEvent;
import com.slamtec.slamware.uicommander.event.HomePoseGetEvent;
import com.slamtec.slamware.uicommander.event.LaserScanGetEvent;
import com.slamtec.slamware.uicommander.event.MapGetEvent;
import com.slamtec.slamware.uicommander.event.RobotStatusGetEvent;
import com.slamtec.slamware.uicommander.event.TrackGetEvent;
import com.slamtec.slamware.uicommander.event.WallGetEvent;
import com.slamtec.slamware.uicommander.mapview.utils.RadianUtil;
import com.slamtec.slamware.uicommander.widget.LongClickButton;
import com.slamtec.slamware.uicommander.mapview.MapView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Date;
import java.util.List;

import static com.slamtec.slamware.uicommander.PathUtil.getPath;

public class MonitorCenterActivity extends BaseActivity implements View.OnClickListener, LongClickButton.LongClickRepeatListener {
    private static final String TAG = "MonitorCenterActivity";
    private static final String MAP_SDORE_PATH = Environment.getExternalStorageDirectory() + "/slamware/";
    final static String[] sNavigationModes = new String[]{"自由导航", "轨道导航", "轨道优先"};

    private MapView mMapView;
    private TextView mBatteryPercentage;
    private TextView mLocalizationQuality;
    private TextView mActionStatus;
    private TextView mSoftwareVersion;
    private TextView mRobotLocation;
    private AlertDialog mRobotHealthDialog;
    private AlertDialog mConnectFailedDialog;
    private Button mMapUpdata;
    private Button mNavigationMode;
    private Button mGoHome;
    private Button mButtonStop;
    private Button mButtonDisconnect;
    private Button mButtonSetMap;
    private Button mButtonSaveMap;

    private LongClickButton mButtonTurnLeft;
    private LongClickButton mButtonTurnRight;
    private LongClickButton mButtonForward;
    private LongClickButton mButtonBackward;

    private SlamwareAgent mAgent;

    private Runnable mRobotStateUpdateRunnable = new Runnable() {
        int cnt;

        @Override
        public void run() {
            cnt = 0;
            mAgent.getGetRobotInfo();

            while (true) {
                if (mRobotStateUpdateRunnable == null || !mRobotStateUpdateThread.isAlive() || mRobotStateUpdateThread.isInterrupted()) {
                    break;
                }

                if ((cnt % 3) == 0) {
                    mAgent.getRobotPose();
                    mAgent.getLaserScan();
                }

                if ((cnt % 20) == 0) {
                    mAgent.getMap();
                    mAgent.getWalls();
                    mAgent.getTracks();
                    mAgent.getMoveAction();
                    mAgent.getRobotStatus();
                }

                if ((cnt % 30) == 0) {
                    mAgent.getHomePose();
                    mAgent.getRobotHealth();
                    mAgent.getMapUpdata();
                }

                SystemClock.sleep(33);
                cnt++;
            }
        }
    };

    Thread mRobotStateUpdateThread = new Thread(mRobotStateUpdateRunnable);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_activity);

        initView();
        mAgent = getSlamwareAgent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUpdate();
    }

    private void startUpdate() {
        mRobotStateUpdateThread = new Thread(mRobotStateUpdateRunnable);
        mRobotStateUpdateThread.start();
    }

    private void stopUpdate() {
        if (mRobotStateUpdateThread != null && !mRobotStateUpdateThread.isInterrupted()) {
            mRobotStateUpdateThread.interrupt();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exit();
    }

    private void exit() {
        mAgent.disconnect();
        stopUpdate();
        finish();
    }

    private void initView() {
        mBatteryPercentage = findViewById(R.id.battery_percentage);
        mLocalizationQuality = findViewById(R.id.localization_quality);
        mActionStatus = findViewById(R.id.action_status);
        mSoftwareVersion = findViewById(R.id.software_version);
        mRobotLocation = findViewById(R.id.robot_location);
        mMapUpdata = findViewById(R.id.map_updata);
        mNavigationMode = findViewById(R.id.navigation_mode);
        mGoHome = findViewById(R.id.go_home);
        mMapView = findViewById(R.id.map_view);
        mButtonTurnLeft = findViewById(R.id.button_turn_left);
        mButtonTurnRight = findViewById(R.id.button_turn_right);
        mButtonForward = findViewById(R.id.button_forward);
        mButtonBackward = findViewById(R.id.button_backward);
        mButtonStop = findViewById(R.id.button_stop);
        mButtonDisconnect = findViewById(R.id.disconnect);
        mButtonSetMap = findViewById(R.id.set_map);
        mButtonSaveMap = findViewById(R.id.save_map);

        mMapUpdata.setOnClickListener(this);
        mNavigationMode.setOnClickListener(this);
        mGoHome.setOnClickListener(this);
        mButtonStop.setOnClickListener(this);
        mButtonDisconnect.setOnClickListener(this);
        mButtonSetMap.setOnClickListener(this);
        mButtonSaveMap.setOnClickListener(this);
        mButtonTurnLeft.setLongClickRepeatListener(this);
        mButtonTurnRight.setLongClickRepeatListener(this);
        mButtonForward.setLongClickRepeatListener(this);
        mButtonBackward.setLongClickRepeatListener(this);

        mMapView.setSingleTapListener(event -> {
            PointF target = mMapView.widgetCoordinateToMapCoordinate(event.getX(), event.getY());
            if (target == null) return;
            Location location = new Location(target.getX(), target.getY(), 0);
            mAgent.moveTo(location);
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MapGetEvent event) {
        Map map = event.getMap();
        mMapView.setMap(map);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LaserScanGetEvent event) {
        LaserScan laserScan = event.getLaserScan();
        mMapView.setLaserScan(laserScan);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(WallGetEvent event) {
        List<Line> walls = event.getWalls();
        mMapView.setVwalls(walls);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(TrackGetEvent event) {
        List<Line> tracks = event.getTracks();
        mMapView.setVtracks(tracks);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RobotPoseGetEvent event) {
        Pose pose = event.getPose();
        mMapView.setRobotPose(pose);
        if (pose != null) {
            String s = String.format("机器位姿  [%.2f, %.2f, %.2f]", pose.getX(), pose.getY(), RadianUtil.toAngel(pose.getYaw()));
            mRobotLocation.setText(s);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HomePoseGetEvent event) {
        Pose pose = event.getHomePose();
        mMapView.setHomePose(pose);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RemainingMilestonesGetEvent event) {
        Path remainingMilestones = event.getRemainingMilestones();
        mMapView.setRemainingMilestones(remainingMilestones);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RemainingPathGetEvent event) {
        Path remainingPath = event.getRemainingPath();
        mMapView.setRemainingPath(remainingPath);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ActionStatusGetEvent event) {
        mActionStatus.setText("运动状态  " + event.getActionStatus());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MapUpdataEvent event) {
        if (event.isMapUpdata()) {
            mMapUpdata.setText("暂停建图");
        } else {
            mMapUpdata.setText("继续建图");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RobotHealthInfoEvent event) {
        if (mRobotHealthDialog == null) {
            mRobotHealthDialog = new AlertDialog.Builder(this).setTitle("底盘健康信息").setMessage(event.getErrorMsg()).setCancelable(false).setNegativeButton("退出", (in, i) -> {
                in.dismiss();
                exit();
            }).setPositiveButton("清除错误", (in, i) -> {
                in.dismiss();
                mAgent.clearRobotHealth(event.getErrorList());
            }).show();
        } else {
            if (mRobotHealthDialog.isShowing()) {
                return;
            } else {
                mRobotHealthDialog.show();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RobotStatusGetEvent event) {
        mBatteryPercentage.setText("剩余电量  " + event.getBatteryPercentage() + "% (" + (event.isCharging() ? "正在充电" : "未充电") + ")");
        mLocalizationQuality.setText("定位质量  " + event.getLocalizationQuality());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RobotInfoEvent event) {
        mSoftwareVersion.setText("软件版本  " + event.getSoftwareVersion());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ConnectionLostEvent event) {
        if (mConnectFailedDialog == null) {
            mConnectFailedDialog = new AlertDialog.Builder(this).setMessage("连接失败").setNegativeButton("退出", (in, i) -> {
                in.dismiss();
                exit();
            }).setPositiveButton("重连", (in, i) -> {
                in.dismiss();
                mAgent.reconnect();
            }).show();
        } else {
            if (mConnectFailedDialog.isShowing()) {
                return;
            } else {
                mConnectFailedDialog.show();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBackThread(GetCompositeMapEvent event) {
        File jf = new File(MAP_SDORE_PATH);
        if (!jf.exists()) {
            jf.mkdirs();
        }

        String fileName = MAP_SDORE_PATH + new Date() + ".stcm";
        CompositeMapHelper compositeMapHelper = new CompositeMapHelper();
        String filePath = compositeMapHelper.saveFile(fileName, event.getCompositeMap());
        if (filePath == null) {
            Toast.makeText(this, "地图保存在" + fileName, Toast.LENGTH_SHORT).show();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.map_updata:
                mAgent.toggleMapUpdata();
                break;
            case R.id.navigation_mode:
                int mode = (mAgent.getNavigationMode() + 1) % 3;
                mAgent.setNavigationMode(mode);
                mNavigationMode.setText(sNavigationModes[mode]);
                break;
            case R.id.go_home:
                mAgent.goHome();
                break;
            case R.id.button_stop:
                mAgent.cancelAllActions();
                break;

            case R.id.save_map:
                mAgent.saveCompositeMap();
                break;

            case R.id.set_map:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;

            case R.id.disconnect:
                exit();
                break;
            default:
                break;
        }
    }

    @Override
    public void repeatAction(View view) {
        switch (view.getId()) {
            case R.id.button_turn_left:
                mAgent.moveBy(MoveDirection.TURN_LEFT);
                break;
            case R.id.button_turn_right:
                mAgent.moveBy(MoveDirection.TURN_RIGHT);
                break;
            case R.id.button_forward:
                mAgent.moveBy(MoveDirection.FORWARD);
                break;
            case R.id.button_backward:
                mAgent.moveBy(MoveDirection.BACKWARD);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                String path = getPath(this, uri);
                if (path.endsWith(".stcm")) {
                    CompositeMapHelper compositeMapHelper = new CompositeMapHelper();
                    CompositeMap compositeMap1 = compositeMapHelper.loadFile(path);
                    Pose pose = new Pose();
                    getSlamwareAgent().setCompositeMap(compositeMap1, pose);
                } else {
                    Toast.makeText(this, "文件类型不对", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
