package com.grandartisans.advert.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.grandartisans.advert.interfaces.ElevatorEventListener;

/*电梯状态管理类*/
public class ElevatorStatusManager implements SensorEventListener {
    private final String TAG = "ElevatorStatusManager";
    static final float THRESHOLD = 0.2f; //0.08f;
    static final int LIFT_STATE_INIT = 0;
    static final int LIFT_STATE_STOP = 1;
    static final int LIFT_STATE_UP = 2;
    static final int LIFT_STATE_DOWN = 3;
    static final int LIFT_STATE_UP_WAITING_STOP = 4;
    static final int LIFT_STATE_DOWN_WAITING_STOP = 5;
    static final int LIFT_STATE_PRE_STOP =6;

    private boolean AccSensorEnabled = false;
    private int mLiftState=0;
    private int mChanging=0;
    private int mUpChanging = 0;
    private int mDownChanging = 0;
    private float mInitZ = 0;
    private float mLastZ;

    private SensorManager mSensorManager;
    private Sensor mAccSensor;
    private ElevatorEventListener mEventListener;

    public ElevatorStatusManager(Context context,String mMode,float defaultValue){
        initAccSensor(context,mMode,defaultValue);
    }

    public void setAccSensorDefaultValue(float value){
        mInitZ = value;
    }
    private void initAccSensor(Context context,String mMode,float defaultValue){
        LogToFile.init(context);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mMode.equals("GAPEDS4A2")||mMode.equals("GAPEDS4A4") || mMode.equals("GAPEDS4A6")||
                mMode.equals("GAPADS4A1") || mMode.equals("GAPADS4A2") || mMode.equals("GAPEDS4A3")){
            AccSensorEnabled = true;
        }
        else AccSensorEnabled = false;
        Log.i(TAG, "initAccSensor AccSensorEnabled = " + AccSensorEnabled  + " mInitZ = " + mInitZ);

        if(AccSensorEnabled) {
            mSensorManager.registerListener(this, mAccSensor, 200000);
            mInitZ = defaultValue;
        }
    }
    public int getStatus(){
        return mLiftState;
    }
    public boolean isStop(){
        if(mLiftState==LIFT_STATE_INIT || mLiftState==LIFT_STATE_STOP)
            return true;
        else return false;
    }
    public void setStatusDefault(){
        mLiftState = LIFT_STATE_INIT;
    }
    public void  registerListener(ElevatorEventListener eventListener){
        if(eventListener!=null)
            mEventListener = eventListener;
    }
    /* Filter positive direction*/
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor eSensor= sensorEvent.sensor;
        if (eSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //float acc = sensorEvent.values[0];
            //float acc = sensorEvent.values[1];
            float acc = sensorEvent.values[2];
            //Log.i(TAG, "time:" + sensorEvent.timestamp + "acc_z:" + acc  + "  " +"mLiftState=" + mLiftState);
            // " X Y Z: " + acc_x + " " + acc_y + " " + acc_z);
            //Log.i(TAG,"state: " + mLiftState  + "acc_z = " + acc);
            //LogToFile.i(TAG,"state: " + mLiftState  + "acc_z = " + acc);
            if(CommonUtil.getGsensorEnabled()==0){
                setLiftState(LIFT_STATE_INIT);
                return;
            }
            switch (mLiftState) {
                case LIFT_STATE_INIT:
                    //setLiftState(LIFT_STATE_STOP);
                    //mInitZ = acc;
                    //break;
                case LIFT_STATE_STOP: {
                    float deltaz = acc - mInitZ;
                    if (Math.abs(deltaz) > THRESHOLD && deltaz >0) {
                        mDownChanging = 0;
                        if (++mUpChanging == 8) {
                            mUpChanging = 0;
                            setLiftState(LIFT_STATE_UP);
                        }
                    }else if(Math.abs(deltaz) > THRESHOLD && deltaz <0){
                        mUpChanging = 0;
                        if (++mDownChanging == 8) {
                            mDownChanging = 0;
                            setLiftState(LIFT_STATE_DOWN);
                        }
                    } else {
                        if (mChanging != 0) mChanging = 0;
                        mDownChanging = 0;
                        mUpChanging = 0;
                    }
                    break;
                }

                case LIFT_STATE_DOWN: {
                    //check decelerate
                    float deltaz = acc - mInitZ;
                    if (deltaz > THRESHOLD) {
                        if (++mChanging == 8) {
                            mChanging = 0;
                            setLiftState(LIFT_STATE_PRE_STOP);
                        }
                    } else {
                        if (mChanging != 0) mChanging = 0;
                    }
                    break;
                }

                case LIFT_STATE_PRE_STOP: {
                    float deltaz = acc - mInitZ;
                    if (Math.abs(deltaz) <= 0.08) {
                        if (++mChanging == 4) {
                            mChanging = 0;
                            setLiftState(LIFT_STATE_STOP);
                        }
                    } else {
                        if (mChanging != 0) mChanging = 0;
                    }
                    break;
                }
                case LIFT_STATE_UP_WAITING_STOP: {
                    float deltaz = acc - mInitZ;
                    if (acc > mLastZ) {
                        if (++mChanging == 4) {
                            mChanging = 0;
                            setLiftState(LIFT_STATE_PRE_STOP);
                        }
                    } else {
                        if (mChanging != 0) mChanging = 0;
                    }
                    break;
                }

                case LIFT_STATE_DOWN_WAITING_STOP: {
                    float deltaz = acc - mInitZ;
                    if (acc < mLastZ) {
                        if (++mChanging == 4) {
                            mChanging = 0;
                            setLiftState(LIFT_STATE_PRE_STOP);
                        }
                    } else {
                        if (mChanging != 0) mChanging = 0;
                    }
                    break;
                }
                case LIFT_STATE_UP: {
                    //check decelerate
                    float deltaz = acc - mInitZ;
                    if (deltaz < 0) {
                        if (Math.abs(deltaz) > THRESHOLD) {
                            if (++mChanging == 8) {
                                mChanging = 0;
                                setLiftState(LIFT_STATE_PRE_STOP);
                            }
                        } else {
                            if (mChanging != 0) mChanging = 0;
                        }
                    }
                    break;
                }
            }

            mLastZ = acc;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void setLiftState(int liftState) {
        mLiftState = liftState;
        Log.i(TAG, "state: " + liftState);
        //LogToFile.i(TAG,"state: " + liftState);
        switch (liftState) {
            case LIFT_STATE_INIT:
                break;
            case LIFT_STATE_PRE_STOP:
            case LIFT_STATE_STOP:
                if(mEventListener!=null) mEventListener.onElevatorStop();
                break;
            case LIFT_STATE_UP:
                if(mEventListener!=null) mEventListener.onElevatorUp();
                break;
            case LIFT_STATE_DOWN:
                if(mEventListener!=null) mEventListener.onElevatorDown();
                break;
            default:
                break;
        }
    }
}
