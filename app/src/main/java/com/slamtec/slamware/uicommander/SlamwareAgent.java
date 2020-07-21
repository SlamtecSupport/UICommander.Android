package com.slamtec.slamware.uicommander;

import android.graphics.RectF;
import android.util.Log;

import com.slamtec.slamware.AbstractSlamwarePlatform;
import com.slamtec.slamware.action.ActionStatus;
import com.slamtec.slamware.action.IMoveAction;
import com.slamtec.slamware.action.MoveDirection;
import com.slamtec.slamware.action.Path;
import com.slamtec.slamware.discovery.DeviceManager;
import com.slamtec.slamware.geometry.Line;
import com.slamtec.slamware.robot.CompositeMap;
import com.slamtec.slamware.robot.HealthInfo;
import com.slamtec.slamware.robot.LaserScan;
import com.slamtec.slamware.robot.Location;
import com.slamtec.slamware.robot.Map;
import com.slamtec.slamware.robot.MapKind;
import com.slamtec.slamware.robot.MoveOption;
import com.slamtec.slamware.robot.Pose;
import com.slamtec.slamware.uicommander.event.ActionStatusGetEvent;
import com.slamtec.slamware.uicommander.event.ConnectedEvent;
import com.slamtec.slamware.uicommander.event.ConnectionLostEvent;
import com.slamtec.slamware.uicommander.event.GetCompositeMapEvent;
import com.slamtec.slamware.uicommander.event.HomePoseGetEvent;
import com.slamtec.slamware.uicommander.event.MapUpdataEvent;
import com.slamtec.slamware.uicommander.event.LaserScanGetEvent;
import com.slamtec.slamware.uicommander.event.MapGetEvent;
import com.slamtec.slamware.uicommander.event.RemainingMilestonesGetEvent;
import com.slamtec.slamware.uicommander.event.RemainingPathGetEvent;
import com.slamtec.slamware.uicommander.event.RobotHealthInfoEvent;
import com.slamtec.slamware.uicommander.event.RobotInfoEvent;
import com.slamtec.slamware.uicommander.event.RobotPoseGetEvent;
import com.slamtec.slamware.uicommander.event.RobotStatusGetEvent;
import com.slamtec.slamware.uicommander.event.TrackGetEvent;
import com.slamtec.slamware.uicommander.event.WallGetEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static com.slamtec.slamware.action.ActionStatus.FINISHED;
import static com.slamtec.slamware.robot.ArtifactUsage.ArtifactUsageVirtualTrack;
import static com.slamtec.slamware.robot.MapType.BITMAP_8BIT;

public class SlamwareAgent {
    private final static String TAG = "SlamwareAgent";

    private final static int ROBOT_PORT = 1445;
    private final static int NAVIGATION_MODE_FREE = 0;
    private final static int NAVIGATION_MODE_TRACK = 1;
    private final static int NAVIGATION_MODE_TRACK_OA = 2;

    private AbstractSlamwarePlatform mRobotPlatform;
    private String mIp;
    private int mNavigationMode;

    private ThreadManager mManager;
    private ThreadManager.ThreadPoolProxy mPoolProxy;

    private static TaskConnect sTaskConnect;
    private static TaskCancelAllActions sTaskCancelAllActions;
    private static TaskGetMoveAction sTaskGetMoveAction;
    private static TaskDisconnect sTaskDisconnect;
    private static TaskGoHome sTaskGoHome;
    private static TaskMoveBy sTaskMoveBy;
    private static TaskMoveTo sTaskMoveTo;
    private static TaskGetLaserScan sTaskGetLaserScan;
    private static TaskGetMapUpdata sTaskGetIsMapUpdata;
    private static TaskToggleMapUpdata sTaskToggleMapUpdata;
    private static TaskGetMap sTaskGetMap;
    private static TaskGetRobotPose sTaskGetRobotPose;
    private static TaskGetHomePose sTaskGetHomePose;
    private static TaskGetStatus sTaskGetStatus;
    private static TaskGetRobotInfo sTaskGetRobotInfo;
    private static TaskGetWalls sTaskGetWalls;
    private static TaskGetTracks sTaskGetTracks;
    private static TaskGetRobotHealth sTaskGetRobotHealth;
    private static TaskClearRobotHealth sTaskClearRobotHealth;
    private static TaskgetCompositeMap sTaskgetCompositeMap;
    private static TaskSetCompositeMap sTaskSetCompositeMap;

    public SlamwareAgent() {
        mManager = ThreadManager.getInstance();
        mPoolProxy = mManager.createLongPool();

        sTaskConnect = new TaskConnect();
        sTaskCancelAllActions = new TaskCancelAllActions();
        sTaskDisconnect = new TaskDisconnect();
        sTaskGoHome = new TaskGoHome();
        sTaskMoveBy = new TaskMoveBy();
        sTaskGetLaserScan = new TaskGetLaserScan();
        sTaskGetIsMapUpdata = new TaskGetMapUpdata();
        sTaskToggleMapUpdata = new TaskToggleMapUpdata();
        sTaskGetMap = new TaskGetMap();
        sTaskGetRobotPose = new TaskGetRobotPose();
        sTaskGetHomePose = new TaskGetHomePose();
        sTaskGetStatus = new TaskGetStatus();
        sTaskGetRobotInfo = new TaskGetRobotInfo();
        sTaskGetWalls = new TaskGetWalls();
        sTaskGetTracks = new TaskGetTracks();
        sTaskGetRobotHealth = new TaskGetRobotHealth();
        sTaskClearRobotHealth = new TaskClearRobotHealth();
        sTaskGetMoveAction = new TaskGetMoveAction();
        sTaskMoveTo = new TaskMoveTo();
        sTaskgetCompositeMap = new TaskgetCompositeMap();
        sTaskSetCompositeMap = new TaskSetCompositeMap();

        mNavigationMode = NAVIGATION_MODE_FREE;
    }


    public void connectTo(String ip) {
        mIp = ip;
        pushTask(sTaskConnect);
    }

    public void reconnect() {
        String ip;

        synchronized (this) {
            ip = mIp;
        }

        if (ip.isEmpty()) return;

        connectTo(ip);
    }

    public void setNavigationMode(int mode) {
        mNavigationMode = mode;
    }

    public int getNavigationMode() {
        return mNavigationMode;
    }

    public void disconnect() {
        pushTask(sTaskDisconnect);
    }

    public void getRobotPose() {
        pushTask(sTaskGetRobotPose);
    }

    public void getMap() {
        pushTask(sTaskGetMap);
    }

    public void getHomePose() {
        pushTask(sTaskGetHomePose);
    }

    public void getLaserScan() {
        pushTask(sTaskGetLaserScan);
    }

    public void getRobotStatus() {
        pushTask(sTaskGetStatus);
    }

    public void getMoveAction() {
        pushTask(sTaskGetMoveAction);
    }

    public void clearRobotHealth(List<Integer> errors) {
        sTaskClearRobotHealth.setErrorCodes(errors);
        pushTask(sTaskClearRobotHealth);
    }

    public void moveTo(Location location) {
        sTaskMoveTo.setlocation(location);
        pushTaskHead(sTaskMoveTo);
    }

    public void moveBy(MoveDirection direction) {
        sTaskMoveBy.setMoveDirection(direction);
        pushTask(sTaskMoveBy);
    }

    public void getWalls() {
        pushTask(sTaskGetWalls);
    }

    public void getTracks() {
        pushTask(sTaskGetTracks);
    }

    public void goHome() {
        pushTaskHead(sTaskGoHome);
    }

    public void toggleMapUpdata() {
        pushTaskHead(sTaskToggleMapUpdata);
    }

    public void cancelAllActions() {
        pushTaskHead(sTaskCancelAllActions);
    }

    public void getMapUpdata() {
        pushTaskHead(sTaskGetIsMapUpdata);
    }

    public void getRobotHealth() {
        pushTask(sTaskGetRobotHealth);
    }

    public void getGetRobotInfo() {
        pushTask(sTaskGetRobotInfo);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private synchronized void pushTaskHead(Runnable Task) {
        mPoolProxy.execute(Task);
    }

    private synchronized void pushTask(Runnable Task) {
        mPoolProxy.execute(Task);
    }

    private void onRequestError(Exception e) {
        Log.e(TAG, e.getMessage());

        synchronized (this) {
            mPoolProxy.cancleAll();
            mRobotPlatform = null;
        }

        EventBus.getDefault().post(new ConnectionLostEvent());
    }

    public void saveCompositeMap() {
        pushTaskHead(sTaskgetCompositeMap);
    }

    public void setCompositeMap(CompositeMap compositeMap, Pose pose) {
        sTaskSetCompositeMap.setCompositeMap(compositeMap);
        sTaskSetCompositeMap.setPose(pose);
        pushTaskHead(sTaskSetCompositeMap);
    }

    //////////////////////////////////// Runnable //////////////////////////////////////////////////
    private class TaskConnect implements Runnable {

        @Override
        public void run() {
            try {
                if (mIp == null || mIp.isEmpty()) {
                    onRequestError(new Exception("robot ip is empty"));
                    return;
                }

                synchronized (this) {
                    mRobotPlatform = DeviceManager.connect(mIp, ROBOT_PORT);
                }
            } catch (Exception exception) {
                onRequestError(exception);
                return;
            }

            EventBus.getDefault().post(new ConnectedEvent());
        }
    }

    private class TaskDisconnect implements Runnable {
        @Override
        public void run() {
            synchronized (this) {
                if (mRobotPlatform == null) {
                    return;
                }
                mPoolProxy.cancleAll();
                mRobotPlatform.disconnect();
                mRobotPlatform = null;
            }
        }
    }

    private class TaskGetMap implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;

            synchronized (this) {
                platform = mRobotPlatform;
            }

            if (platform == null) {
                return;
            }

            Map map = null;

            try {
                RectF area = platform.getKnownArea(BITMAP_8BIT, MapKind.EXPLORE_MAP);
                map = platform.getMap(BITMAP_8BIT, MapKind.EXPLORE_MAP, area);
            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            EventBus.getDefault().post(new MapGetEvent(map));
        }
    }

    private class TaskGetRobotPose implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null) {
                    return;
                }
            }

            Pose pose;

            try {
                pose = platform.getPose();
            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            EventBus.getDefault().post(new RobotPoseGetEvent(pose));
        }
    }

    private class TaskGetHomePose implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;

            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null) return;
            }

            Pose homePose;

            try {
                homePose = platform.getHomePose();
            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            EventBus.getDefault().post(new HomePoseGetEvent(homePose));
        }
    }

    private class TaskGetLaserScan implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null) return;
            }

            LaserScan laserScan;

            try {
                laserScan = platform.getLaserScan();
            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            EventBus.getDefault().post(new LaserScanGetEvent(laserScan));
        }
    }

    private class TaskGetMapUpdata implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null) {
                    return;
                }
            }

            boolean isMapUpdata;

            try {
                isMapUpdata = platform.getMapUpdate();
            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            EventBus.getDefault().post(new MapUpdataEvent(isMapUpdata));
        }
    }


    private class TaskToggleMapUpdata implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null) {
                    return;
                }
            }

            try {
                boolean mapUpdate = platform.getMapUpdate();
                platform.setMapUpdate(!mapUpdate);
            } catch (Exception e) {
                onRequestError(e);
                return;
            }
        }
    }

    private class TaskGetStatus implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null) {
                    return;
                }
            }

            boolean isCharging;
            int batteryPercentage;
            int localizationQuality;

            try {
                batteryPercentage = platform.getBatteryPercentage();
                isCharging = platform.getBatteryIsCharging();
                localizationQuality = platform.getLocalizationQuality();
            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            EventBus.getDefault().post(new RobotStatusGetEvent(batteryPercentage, isCharging, localizationQuality));
        }
    }

    private class TaskGetRobotInfo implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null) {
                    return;
                }
            }

            int modelId;
            String hardwareVersion;
            String softwareVersion;
            String modelName;

            try {
                modelId = platform.getModelId();
                hardwareVersion = platform.getHardwareVersion();
                softwareVersion = platform.getSoftwareVersion();
                modelName = platform.getModelName();
            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            EventBus.getDefault().post(new RobotInfoEvent(modelId, hardwareVersion, softwareVersion, modelName));
        }
    }

    private class TaskGetMoveAction implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null) return;
            }

            Path remainingMilestones = null;
            Path remainingPath = null;
            ActionStatus actionStatus = FINISHED;

            try {
                IMoveAction moveAction = platform.getCurrentAction();

                if (moveAction != null) {
                    remainingMilestones = moveAction.getRemainingMilestones();
                    remainingPath = moveAction.getRemainingPath();
                    actionStatus = moveAction.getStatus();
                }

            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            EventBus.getDefault().post(new RemainingMilestonesGetEvent(remainingMilestones));
            EventBus.getDefault().post(new RemainingPathGetEvent(remainingPath));
            EventBus.getDefault().post(new ActionStatusGetEvent(actionStatus));
        }
    }

    private class TaskMoveTo implements Runnable {
        private Location location;

        public TaskMoveTo() {
        }

        public void setlocation(Location location) {
            this.location = location;
        }


        @Override
        public void run() {
            AbstractSlamwarePlatform platform;

            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null || location == null) return;
            }

            try {
                MoveOption moveOption = new MoveOption();
                moveOption.setMilestone(true);
                switch (mNavigationMode) {
                    case NAVIGATION_MODE_FREE:
                        break;

                    case NAVIGATION_MODE_TRACK:
                        moveOption.setKeyPoints(true);
                        break;

                    case NAVIGATION_MODE_TRACK_OA:
                        moveOption.setKeyPoints(true);
                        moveOption.setTrackWithOA(true);
                        break;

                    default:
                        break;
                }

                platform.moveTo(location, moveOption, 0f);

            } catch (Exception e) {
                onRequestError(e);
            }
        }
    }


    private class TaskMoveBy implements Runnable {
        MoveDirection moveDirection;

        public TaskMoveBy() {
            moveDirection = null;
        }

        public void setMoveDirection(MoveDirection moveDirection) {
            this.moveDirection = moveDirection;
        }

        @Override
        public void run() {
            AbstractSlamwarePlatform platform;

            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null || moveDirection == null) {
                    return;
                }
            }

            try {
                platform.moveBy(moveDirection);
            } catch (Exception e) {
                onRequestError(e);
            }
        }
    }

    private class TaskGetTracks implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null) {
                    return;
                }
            }

            List<Line> tracks;
            try {
                tracks = platform.getLines(ArtifactUsageVirtualTrack);
            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            EventBus.getDefault().post(new TrackGetEvent(tracks));
        }
    }

    private class TaskGetWalls implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
                if (platform == null) {
                    return;
                }
            }

            Vector<Line> walls;

            try {
                walls = platform.getWalls();
            } catch (Exception e) {
                onRequestError(e);
                return;
            }

            EventBus.getDefault().post(new WallGetEvent(walls));
        }
    }

    private class TaskGoHome implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
            }
            if (platform == null) return;

            try {
                platform.goHome();
            } catch (Exception e) {
                onRequestError(e);
            }
        }
    }

    private class TaskCancelAllActions implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;

            synchronized (this) {
                platform = mRobotPlatform;
            }

            if (platform == null) return;

            try {
                IMoveAction moveAction = platform.getCurrentAction();
                if (moveAction != null) {
                    moveAction.cancel();
                }
            } catch (Exception e) {
                onRequestError(e);
            }

            mPoolProxy.cancleAll();
        }
    }

    private class TaskGetRobotHealth implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
            }

            if (platform == null) {
                return;
            }

            String errorMsg = "";
            List<Integer> errorList = new ArrayList<>();

            try {
                HealthInfo info = platform.getRobotHealth();

                if (info.isWarning() || info.isError() || info.isFatal() || (info.getErrors() != null && info.getErrors().size() > 0)) {

                    for (HealthInfo.BaseError error : info.getErrors()) {
                        String level;
                        switch (error.getErrorLevel()) {
                            case HealthInfo.BaseError.BaseErrorLevelWarn:
                                level = "Warning";
                                break;
                            case HealthInfo.BaseError.BaseErrorLevelError:
                                level = "Error";
                                break;
                            case HealthInfo.BaseError.BaseErrorLevelFatal:
                                level = "Fatal";
                                break;
                            default:
                                level = "Unknown";
                                break;
                        }
                        String component;
                        switch (error.getErrorComponent()) {
                            case HealthInfo.BaseError.BaseErrorComponentUser:
                                component = "User";
                                break;
                            case HealthInfo.BaseError.BaseErrorComponentMotion:
                                component = "Motion";
                                break;
                            case HealthInfo.BaseError.BaseErrorComponentPower:
                                component = "Power";
                                break;
                            case HealthInfo.BaseError.BaseErrorComponentSensor:
                                component = "Sensor";
                                break;
                            case HealthInfo.BaseError.BaseErrorComponentSystem:
                                component = "System";
                                break;
                            default:
                                component = "Unknown";
                                break;
                        }

                        errorList.add(error.getErrorCode());
                        errorMsg += String.format("Error ID: %d\nError level: %s\nError Component: %s\nError message: %s\nError ErrorCode: %d\n------\n", error.getId(), level, component, error.getErrorMessage(), error.getErrorCode());
                        EventBus.getDefault().post(new RobotHealthInfoEvent(errorMsg, errorList));
                    }
                }
            } catch (Exception e) {
                onRequestError(e);
            }
        }
    }

    private class TaskgetCompositeMap implements Runnable {
        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
            }

            if (platform == null) {
                return;
            }

            CompositeMap compositeMap = null;
            try {
                compositeMap = platform.getCompositeMap();
            } catch (Exception e) {
                onRequestError(e);
            }

            EventBus.getDefault().post(new GetCompositeMapEvent(compositeMap));
        }
    }

    private class TaskSetCompositeMap implements Runnable {

        private CompositeMap compositeMap;
        private Pose pose;

        @Override
        public void run() {
            AbstractSlamwarePlatform platform;
            synchronized (this) {
                platform = mRobotPlatform;
            }

            if (platform == null) {
                return;
            }

            try {
                platform.setCompositeMap(compositeMap, pose);
            } catch (Exception e) {
                onRequestError(e);
            }
        }

        public CompositeMap getCompositeMap() {
            return compositeMap;
        }

        public void setCompositeMap(CompositeMap compositeMap) {
            this.compositeMap = compositeMap;
        }

        public Pose getPose() {
            return pose;
        }

        public void setPose(Pose pose) {
            this.pose = pose;
        }
    }

    private class TaskClearRobotHealth implements Runnable {
        List<Integer> errors;

        @Override
        public void run() {
            AbstractSlamwarePlatform platform;

            synchronized (this) {
                platform = mRobotPlatform;
            }

            if (platform == null) {
                return;
            }

            try {
                for (Integer error : errors) {
                    platform.clearRobotHealth(error);
                }
            } catch (Exception e) {
                onRequestError(e);
            }
        }

        public void setErrorCodes(List<Integer> errors) {
            this.errors = errors;
        }
    }
}
