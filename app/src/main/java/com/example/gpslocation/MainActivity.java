package com.example.gpslocation;

import static java.lang.String.valueOf;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    FileWriter writer;
    final int PERMISSIONS_REQUEST_CODE = 1;
    String whereDir;
    String thisFile;

    private void requestPermission() {
        boolean shouldProviceRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);//사용자가 이전에 거절한적이 있어도 true 반환

        if (shouldProviceRationale) {
            //앱에 필요한 권한이 없어서 권한 요청
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            //권한있을때.
            //오레오부터 꼭 권한체크내에서 파일 만들어줘야함
            makeDir();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //권한 허용 선택시
                    //오레오부터 꼭 권한체크내에서 파일 만들어줘야함
                    makeDir();
                } else {
                    //사용자가 권한 거절시
                    denialDialog();
                }
                return;
            }
        }
    }

    public void denialDialog() {
    }
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    PERMISSIONS_REQUEST_CODE
            );
        }
    }

    public void makeDir() {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath(); //내장에 만든다
        String directoryName = "DroneStorage";
        final File myDir = new File(root + "/" + directoryName);
        if (!myDir.exists()) {
            boolean wasSuccessful = myDir.mkdir();
            if (!wasSuccessful) {
                System.out.println("file: was not successful.");
            } else {
                System.out.println("file: first create files." + root + "/" + directoryName);
            }
        } else {
            System.out.println("file: " + root + "/" + directoryName +"already exists");
        }
        whereDir = root + "/" + directoryName;
        long now = System.currentTimeMillis(); //TODO 현재시간 받아오기
        Date date = new Date(now); //TODO Date 객체 생성
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        String nowTime = sdf.format(date);
        String textFileName = "FindMeDrone " + nowTime + ".txt";
        thisFile = textFileName;
        File file = new File(myDir+textFileName);
        try{
            if(!file.exists()){
                file.createNewFile();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


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
            String saveData = data;
            verifyStoragePermissions(this);
            File filereal = new File(whereDir+"/"+thisFile);
            writer = new FileWriter(filereal,true);
            Log.d("texttest","WWWriterON!");

            long now2 = System.currentTimeMillis(); //TODO 현재시간 받아오기
            Date date2 = new Date(now2); //TODO Date 객체 생성
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            String nowTime2 = sdf.format(date2);

            if(appDataType == 1){
                writer.write("["+nowTime2+"]" + "\nRSRP: ["+saveData+"]"); //TODO 날짜 쓰기
            }
            else if (appDataType==2){
                writer.write("["+nowTime2+"]" + "\nAltitude: ["+saveData+"]"); //TODO 고도 쓰기
                Log.d("texttest","alttttitudesavvvvvved");
            }
            else if (appDataType ==3){
                writer.write("["+nowTime2+"]" + "\nGPS: ["+saveData+"]"); //TODO GPS 쓰기
            }
            writer.write("\n");
            writer.flush();
            writer.close();
            Log.d("texttest","saveddddeeed");

//            Toast.makeText(getApplication(),"텍스트 파일이 저장되었습니다",Toast.LENGTH_SHORT).show();
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
//                Log.d("ttttest","this2");
                float sval = event.values[0];
                String altNow = valueOf(mgetAlt(sval));
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
//                    mySaveText("thisisfstTest",2);
                }catch(SecurityException ex){
                }
            }
        });
        requestPermission();
    } // end of onCreate

    @Override
    protected void onResume(){
        super.onResume();
//        Log.d("ttttest","this");
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
//            Log.d("SignalStrength", strSignal);
//            Log.d("tttest","here");
            CellSignalStrengthLte ltesig = (CellSignalStrengthLte)  signalStrength.getCellSignalStrengths().get(0);
            String rsrpNow = valueOf(ltesig.getRsrp());
            sigtv.setText("RSRP : "+ rsrpNow);
            mySaveText(rsrpNow,1);
        }
    };

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

//            Log.d("test", "onLocationChanged, location:" + location);
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
//            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
//            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
//            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };
} // end of class
