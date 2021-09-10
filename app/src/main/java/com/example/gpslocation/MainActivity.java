package com.example.gpslocation;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    public static String saveStorage = ""; //저장된 파일 경로
    public static String saveData = ""; //저장된 파일 내용

    long now = System.currentTimeMillis(); //TODO 현재시간 받아오기
    Date date = new Date(now); //TODO Date 객체 생성
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
    String nowTime = sdf.format(date);

    String textFileName = "/FindMeDrone"+nowTime+".txt";
    File storedFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SaveStorage"+textFileName);

    TextView tv;
    ToggleButton tb;
    TextView sigtv;
    TelephonyManager tm;
    SensorManager sm;
    Sensor pressen;
    SensorEventListener SEL;
    TextView alt_tv;
    LocationManager lm;

    //textWriterFunction
    public void mySaveText(String data, int appDataType){
        try {
            saveData = data;
            File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SaveStorage"); //TODO 저장 경로
            //TODO 폴더 생성
            if(!storageDir.exists()){ //TODO 폴더 없을 경우
                storageDir.mkdir(); //TODO 폴더 생성
            }

            long now2 = System.currentTimeMillis(); //TODO 현재시간 받아오기
            Date date2 = new Date(now2); //TODO Date 객체 생성
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            String nowTime2 = sdf.format(date2);

            BufferedWriter buf = new BufferedWriter(new FileWriter(storedFile, true));
            if(appDataType == 1){
                buf.append("["+nowTime2+"]" + "\nRSRP: ["+saveData+"]"); //TODO 날짜 쓰기
            }
            else if (appDataType==2){
                buf.append("["+nowTime2+"]" + "\nAltitude: ["+saveData+"]"); //TODO 날짜 쓰기
            }
            else if (appDataType ==3){
                buf.append("["+nowTime2+"]" + "\nGPS: ["+saveData+"]"); //TODO 날짜 쓰기
            }
            buf.newLine(); //TODO 개행
            buf.close();

            saveStorage = String.valueOf(storageDir+textFileName); //TODO 경로 저장 /storage 시작
            //saveStorage = String.valueOf(storageDir.toURI()+textFileName); //TODO 경로 저장 file:/ 시작

            Toast.makeText(getApplication(),"텍스트 파일이 저장되었습니다",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Location 제공자에서 정보를 얻어오기(GPS)
        // 1. Location을 사용하기 위한 권한을 얻어와야한다 AndroidManifest.xml
        //     ACCESS_FINE_LOCATION : NETWORK_PROVIDER, GPS_PROVIDER
        //     ACCESS_COARSE_LOCATION : NETWORK_PROVIDER
        // 2. LocationManager 를 통해서 원하는 제공자의 리스너 등록
        // 3. GPS 는 에뮬레이터에서는 기본적으로 동작하지 않는다
        // 4. 실내에서는 GPS_PROVIDER 를 요청해도 응답이 없다.  특별한 처리를 안하면 아무리 시간이 지나도
        //    응답이 없다.
        //    해결방법은
        //     ① 타이머를 설정하여 GPS_PROVIDER 에서 일정시간 응답이 없는 경우 NETWORK_PROVIDER로 전환
        //     ② 혹은, 둘다 한꺼번헤 호출하여 들어오는 값을 사용하는 방식.

        //add same things for sensors
        tv = (TextView) findViewById(R.id.textView2);
        tv.setText("위치정보 미수신중");

        tb = (ToggleButton)findViewById(R.id.toggle1);
        sigtv = (TextView) findViewById(R.id.textView3);
        sigtv.setText("신호세기 수신 대기중");

        alt_tv = (TextView) findViewById(R.id.textViewAlt);
        alt_tv.setText("고도 센서 수신 대기중");

        // LocationManager 객체를 얻어온다
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        tm =  (TelephonyManager) getSystemService (TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        pressen = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);

        SEL = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d("ttttest","this2");
                float sval = event.values[0];
                String altNow = String.valueOf(mgetAlt(sval));
                alt_tv.setText("고도: " + altNow);
                mySaveText(altNow, 2);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };


        tb.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {
                try{
                    if(tb.isChecked()){
                        tv.setText("수신중..");
                        // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                                100, // 통지사이의 최소 시간간격 (miliSecond)
                                1, // 통지사이의 최소 변경거리 (m)
                                mLocationListener);
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                                100, // 통지사이의 최소 시간간격 (miliSecond)
                                1, // 통지사이의 최소 변경거리 (m)
                                mLocationListener);
                    }else{
                        tv.setText("위치정보 미수신중");
                        lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                    }
                }catch(SecurityException ex){
                }
            }
        });
    } // end of onCreate

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("ttttest","this");
        sm.registerListener(SEL, pressen, sm.SENSOR_DELAY_UI);
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
        } catch(SecurityException ex){
        }
    }

    public float mgetAlt(float ssval){
        return sm.getAltitude(sm.PRESSURE_STANDARD_ATMOSPHERE, ssval);
    }

    PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            //RSRP (Reference Signal Received Power) - 단위 dBm (절대크기). - 단말에 수신되는 Reference Signal의 Power
            String strSignal = signalStrength.toString();
            Log.d("SignalStrength", strSignal);
            Log.d("tttest","here");
            CellSignalStrengthLte ltesig = (CellSignalStrengthLte)  signalStrength.getCellSignalStrengths().get(0);
            String rsrpNow = String.valueOf(ltesig.getRsrp());
            sigtv.setText("RSRP : "+ rsrpNow);
            mySaveText(rsrpNow,1);
        }
    };

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            tv.setText("위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude
                    + "\n정확도 : "  + accuracy);
            String gpsNowConcat = "위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude
                    + "\n정확도 : "  + accuracy;
            mySaveText(gpsNowConcat,3);
        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };
} // end of class