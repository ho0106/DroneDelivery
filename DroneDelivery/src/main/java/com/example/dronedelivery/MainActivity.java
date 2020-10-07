package com.example.dronedelivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PolylineOverlay;

import com.naver.maps.map.util.FusedLocationSource;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.companion.solo.SoloAttributes;
import com.o3dr.services.android.lib.drone.companion.solo.SoloState;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.speech.tts.TextToSpeech.ERROR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, DroneListener, TowerListener, LinkListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static String IP_ADDRESS = "61.33.158.137";

    // NaverMap
    NaverMap mNaverMap;
    private List<Marker> orderMarker = new ArrayList<>();
    private int orderMarkerCount = 0;
    private Marker droneMarker = new Marker();
    private List<LatLng> poly = new ArrayList<>();
    private PolylineOverlay polylineOverlay = new PolylineOverlay();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    boolean mMapLock = true;

    // DroneLogList
    RecyclerView mDroneRecyclerView;
    DroneLog mDroneLog;
    ArrayList mDroneDataLog = new ArrayList();

    // OrderLogList
    RecyclerView mOrderRecyclerView;
    OrderLog mOrderLog;
    ArrayList mOrderDataLog = new ArrayList();
    List<LatLng> mAddress = new ArrayList<>();

    // Drone
    private Drone drone;
    private int droneType = Type.TYPE_UNKNOWN;
    private ControlTower controlTower;
    private Spinner modeSelector;
    private final Handler handler = new Handler();
    private double mDroneAltitude = 5.0;
    private Gps mGps;
    private Attitude mDroneYaw;
    private Float mYaw;

    private TextToSpeech tts;

    // Mission
    private GuideMode mGuideMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Full screen //
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i("Is on?", "Turning immersive mode mode off. ");
        } else {
            Log.i("Is on?", "Turning immersive mode mode on.");
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

        // 네이버 맵 API불러오기 //
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("895cz3v0pt")
        );

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        // GCS 위치표시 //
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // Drone Log //
        mDroneRecyclerView = findViewById(R.id.droneLog);
        LinearLayoutManager mDroneLinerLayoutManager = new LinearLayoutManager(this);

        mDroneLinerLayoutManager.setReverseLayout(true);
        mDroneLinerLayoutManager.setStackFromEnd(true);
        mDroneRecyclerView.setLayoutManager(mDroneLinerLayoutManager);
        mDroneDataLog = new ArrayList<Integer>(10);
        mDroneLog = new DroneLog(mDroneDataLog);
        mDroneRecyclerView.setAdapter(mDroneLog);

        // Order Log //
        mOrderRecyclerView = findViewById(R.id.orderLog);
        LinearLayoutManager mOrderLinerLayoutManager = new LinearLayoutManager(this);

        mOrderLinerLayoutManager.setReverseLayout(true);
        mOrderLinerLayoutManager.setStackFromEnd(true);
        mOrderRecyclerView.setLayoutManager(mOrderLinerLayoutManager);
        mOrderDataLog = new ArrayList<Integer>(10);
        mOrderLog = new OrderLog(mOrderDataLog);
        mOrderRecyclerView.setAdapter(mOrderLog);
        checkOrder();
        delOrder();

        // Drone start //
        final Context context = getApplicationContext();
        this.controlTower = new ControlTower(context);
        this.drone = new Drone(context);

        // Drone Mode Spinner //
        this.modeSelector = (Spinner) findViewById(R.id.modeSelect);
        this.modeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onFlightModeSelected(view);
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        Button deliveryStart = (Button) findViewById(R.id.btnDeliveryStart);
        deliveryStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runGuideMode(mAddress.get(0));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨 //
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {
        this.mNaverMap = naverMap;
        UiSettings uiSettings = naverMap.getUiSettings();
        alertUser("맵 로딩 완료");

        // 최초 위치, 줌 설정 //
        CameraPosition cameraPosition = new CameraPosition(
                new LatLng(35.9424531, 126.6811309), // 대상 지점
                17 // 줌 레벨
        );
        naverMap.setCameraPosition(cameraPosition);

        // UI 숨김 //
        naverMap.setIndoorEnabled(true); // 건물 내부 정보 활성화
        uiSettings.setCompassEnabled(false); // 나침반 숨김
        uiSettings.setZoomControlEnabled(false); // 줌 버튼 숨김

        // GCS 위치 표시 //
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);
    }

    // Drone Log update //

    protected void alertUser(String message) {
        // 기본 로그 //
        mDroneDataLog.add(" ☆ " + message);
        mDroneRecyclerView.smoothScrollToPosition(mDroneDataLog.size()-1);
        mDroneLog.notifyDataSetChanged();
    }

    protected void alertUserError(String message) {
        // 오류 로그 //
        mDroneDataLog.add(" ※ " + message);
        mDroneRecyclerView.smoothScrollToPosition(mDroneDataLog.size()-1);
        mDroneLog.notifyDataSetChanged();
    }

    protected void orderListLog(String message) {
        // 주문 리스트 //
        if (orderMarkerCount <= 50) {
            mOrderDataLog.add(String.format(" %d - " + message, orderMarkerCount + 1));
            orderMarkerCount++;
        }
        mOrderRecyclerView.smoothScrollToPosition(mOrderDataLog.size()-1);
        mOrderLog.notifyDataSetChanged();
    }

    protected void ttsPrint(String message) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }

    // Drone Start //

    @Override
    public void onStart() {
        super.onStart();
        this.controlTower.connect(this);
        updateVehicleModesForType(this.droneType);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.drone.isConnected()) {
            this.drone.disconnect();
            updateUI(false);
        }

        this.controlTower.unregisterDrone(this.drone);
        this.controlTower.disconnect();
    }

    // DroneKit-Android Listener //

    @Override
    public void onTowerConnected() {
        alertUser("DroneKit-Android Connected");
        this.controlTower.registerDrone(this.drone, this.handler);
        this.drone.registerDroneListener(this);
    }

    @Override
    public void onTowerDisconnected() {
        alertUser("DroneKiT-Android Interrupted");
    }

    // Drone Listener //

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        State droneState = drone.getAttribute(AttributeType.STATE);
        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                alertUser("Drone Connected");
                updateUI(this.drone.isConnected());
                checkSoloState();
                break;

            case AttributeEvent.STATE_DISCONNECTED:
                alertUser("Drone Disconnected");
                updateUI(this.drone.isConnected());
                clearValue();
                break;

            case AttributeEvent.STATE_UPDATED:
            case AttributeEvent.STATE_ARMING:
                updateUI(this.drone.isConnected());
                break;

            case AttributeEvent.TYPE_UPDATED:
                Type newDroneType = this.drone.getAttribute(AttributeType.TYPE);
                if (newDroneType.getDroneType() != this.droneType) {
                    this.droneType = newDroneType.getDroneType();
                    updateVehicleModesForType(this.droneType);
                }
                break;

            case AttributeEvent.STATE_VEHICLE_MODE:
                updateVehicleMode();
                break;

            case AttributeEvent.BATTERY_UPDATED:
                updateVoltage();
                break;

            case AttributeEvent.SPEED_UPDATED:
                updateSpeed();
                break;

            case AttributeEvent.ALTITUDE_UPDATED:
                updateAltitude();
                break;

            case AttributeEvent.GPS_COUNT:
                updateSatellitesCount();
                break;

            case AttributeEvent.ATTITUDE_UPDATED:
                updateYaw();
                break;

            case AttributeEvent.HOME_UPDATED:
                //updateDistanceFromHome();
                break;

            case AttributeEvent.GPS_POSITION:
                updateDroneLocation();
                break;

            case AttributeEvent.MISSION_SENT:
                break;

            case AttributeEvent.AUTOPILOT_MESSAGE:

            case AttributeEvent.AUTOPILOT_ERROR:
                extras.putString(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID, droneState.getAutopilotErrorId());
                break;

            default:
                // Log.i("DRONE_EVENT", event); //Uncomment to see events from the drone
                break;
        }
    }

    private void checkSoloState() {
        final SoloState soloState = drone.getAttribute(SoloAttributes.SOLO_STATE);
        if (soloState == null) {
            alertUser("Unable to retrieve the solo state.");
        } else {
            alertUser("Solo state is up to date.");
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }

    // UI Events //

    public void onBtnConnectTap(View view) {
        if (this.drone.isConnected()) {
            this.drone.disconnect();
        } else {
            Spinner connectionSelector = findViewById(R.id.selectConnectionType);
            int selectedConnectionType = connectionSelector.getSelectedItemPosition();

            ConnectionParameter connectionParams = selectedConnectionType == ConnectionType.TYPE_UDP
                    ? ConnectionParameter.newUsbConnection(null)
                    : ConnectionParameter.newUdpConnection(null);

            this.drone.connect(connectionParams);
        }
    }

    public void onFlightModeSelected(View view) {
        final VehicleMode vehicleMode = (VehicleMode) this.modeSelector.getSelectedItem();

        VehicleApi.getApi(this.drone).setVehicleMode(vehicleMode, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                alertUser(String.format("비행 모드 변경 : %s", vehicleMode.getLabel()));
                ttsPrint(String.format("비행 모드 변경 : %s", vehicleMode.getLabel()));
            }

            @Override
            public void onError(int executionError) {
                alertUserError("비행 모드 변경 실패 : " + executionError);
            }

            @Override
            public void onTimeout() {
                alertUser("Vehicle mode change timed out.");
            }
        });
    }

    public void onArmButtonTap(View view) {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);

        // CustomDialog 선언 //
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        // Dialog UI 선언 //
        TextView title = dialogView.findViewById(R.id.title);
        TextView message = dialogView.findViewById(R.id.message);
        LinearLayout addressLayout = dialogView.findViewById(R.id.addressLayout);
        LinearLayout orderDetailLayout = dialogView.findViewById(R.id.orderDetail);

        addressLayout.setVisibility(View.GONE);
        orderDetailLayout.setVisibility(View.GONE);

        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);

        if (vehicleState.isFlying()) {
            onArmButtonFunction(mDroneAltitude);
        } else if (vehicleState.isArmed()) {
            title.setText("이륙 경고");
            message.setText("지정한 이륙고도까지 기체가 상승합니다.\n안전거리를 유지하세요.");
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onArmButtonFunction(mDroneAltitude);
                    alertDialog.dismiss();
                }
            });
            btnNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.show();
        } else if (!vehicleState.isConnected()) {
            alertUserError("드론을 먼저 연결해주세요.");
        } else {
            title.setText("시동 경고");
            message.setText("모터를 가동합니다.\n모터가 고속으로 회전합니다.");
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onArmButtonFunction(mDroneAltitude);
                    alertDialog.dismiss();
                }
            });
            btnNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.show();
        }
    }

    public void onArmButtonFunction(double setAltitude) {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        this.mDroneAltitude = setAltitude;

        if (vehicleState.isFlying()) {
            // Land
            VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_LAND, new SimpleCommandListener() {
                @Override
                public void onError(int executionError) {
                    alertUserError("착륙이 불가능합니다.");
                }

                @Override
                public void onTimeout() {
                    alertUser("시간초과. (Land)");
                }
            });
        } else if (vehicleState.isArmed()) {
            // Take off
            ControlApi.getApi(this.drone).takeoff(setAltitude, new AbstractCommandListener() {

                @Override
                public void onSuccess() {
                    alertUser(String.format("이륙합니다. 설정된 이륙 고도 : %2.1fm", mDroneAltitude));
                }

                @Override
                public void onError(int executionError) {
                    alertUserError("이륙이 불가능 합니다.");
                }

                @Override
                public void onTimeout() {
                    alertUser("시간초과. (Take off)");
                }
            });
        } else if (!vehicleState.isConnected()) {
            // Connect
            alertUser("드론을 연결해주세요.");
        } else {
            // Connected but not Armed
            VehicleApi.getApi(this.drone).arm(true, false, new AbstractCommandListener() {

                @Override
                public void onSuccess() {
                    alertUser("모터 시동");
                }
                @Override
                public void onError(int executionError) {
                    alertUserError("시동을 걸 수 없습니다.");
                }

                @Override
                public void onTimeout() {
                    alertUser("시간초과, (ARM)");
                }
            });
        }
    }

    public void onClearButtonTap(View view) {
        //if (this.drone.isConnected()) {
            alertUser("주문 목록 & 배달 데이터 삭제");
            ttsPrint("모든 데이터를 삭제합니다.");

            poly.removeAll(poly);
            mOrderDataLog.removeAll(mOrderDataLog);
            mOrderLog.notifyDataSetChanged();
            mAddress.removeAll(mAddress);
            polylineOverlay.setMap(null);

            if (orderMarker.size() != 0) {
                for (int i = 0; i < orderMarker.size(); i++) {
                    orderMarker.get(i).setMap(null);
                }
            }
            poly.clear();
            orderMarker.clear();
            mOrderDataLog.clear();
            mAddress.clear();
            orderMarkerCount = 0;
        //} else {
            //alertUserError("먼저 드론을 연결해 주세요.");
        //}
    }

    public void onMapMoveButtonTap(View view) {
        TextView mapMoveValue = findViewById(R.id.btnMapMove);

        if (mMapLock == true) {
            mapMoveValue.setText("경로추적");
            alertUser("추적 해제");
            mMapLock = false;
        } else {
            mapMoveValue.setText("추적 해제");
            alertUser("경로추적");
            mMapLock = true;
        }
    }

    public void onBtnTakeOffAltitudeTap(View view) {
        final Button upAltitudeButton = findViewById(R.id.btnUpAltitude);
        final Button downAltitudeButton = findViewById(R.id.btnDownAltitude);

        if (upAltitudeButton.getVisibility() == view.GONE) {
            upAltitudeButton.setVisibility(View.VISIBLE);
            downAltitudeButton.setVisibility(View.VISIBLE);
        } else {
            upAltitudeButton.setVisibility(View.GONE);
            downAltitudeButton.setVisibility(View.GONE);
        }
    }

    public void onBtnSetAltitudeTap(View view) {
        TextView altitudeValue = findViewById(R.id.btnTakeOffAltitude);

        switch (view.getId()) {
            case R.id.btnUpAltitude:
                if (mDroneAltitude < 9.51) {
                    mDroneAltitude += 0.5;
                    altitudeValue.setText(String.format("%2.1fm\n이륙고도", mDroneAltitude));
                    alertUser(String.format("이륙 고도 변경 : %2.1fm", mDroneAltitude));
                } else if (mDroneAltitude >= 10.0) {
                    alertUser("고도 10m이상 설정 불가.");
                }
                break;
            case R.id.btnDownAltitude:
                if (mDroneAltitude >= 3.5) {
                    mDroneAltitude -= 0.5;
                    altitudeValue.setText(String.format("%2.1fm\n이륙고도", mDroneAltitude));
                    alertUser(String.format("이륙 고도 변경 : %2.1fm", mDroneAltitude));
                } else if (mDroneAltitude <= 3.49) {
                    alertUser("고도 3m이하 설정 불가");
                }
                break;
        }
    }

    public void onBtnSetOrderTap(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();
        final Geocoder geocoder = new Geocoder(this);

        TextView title = dialogView.findViewById(R.id.title);
        TextView message = dialogView.findViewById(R.id.message);
        LinearLayout orderDetailLayout = dialogView.findViewById(R.id.orderDetail);

        message.setVisibility(View.GONE);
        orderDetailLayout.setVisibility(View.GONE);
        title.setText("주소를 입력해 주세요");

        final EditText addressText = dialogView.findViewById(R.id.addressBox);
        final EditText detailAddressText = dialogView.findViewById(R.id.detailAddressBox);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Address> list = null;

                String address = addressText.getText().toString();
                String detailAddress = detailAddressText.getText().toString();
                try {
                    list = geocoder.getFromLocationName(address,10);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("test","입출력 오류 - 서버에서 주소변환시 에러발생.");
                }

                if (list != null) {
                    if (list.size() == 0) {
                        alertUserError("해당주소 없음.");
                    } else {
                        LatLng mar = new LatLng(list.get(0).getLatitude(), list.get(0).getLongitude());
                        mAddress.add(mar);
                        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(mar).animate(CameraAnimation.Linear);
                        Marker marker = new Marker();

                        marker.setPosition(mar);
                        marker.setMap(mNaverMap);
                        marker.setTag(address);
                        orderMarker.add(marker);
                        mNaverMap.moveCamera(cameraUpdate);
                        orderListLog("배달주소 : " + address + " " + detailAddress);
                        ttsPrint("새로운 주문이 접수되었습니다.");
                    }
                }
                alertDialog.dismiss();
            }
        });
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    // UI Updating //

    public void updateDroneLocation() {
        mGps = this.drone.getAttribute(AttributeType.GPS);

        LatLng droneLocation = new LatLng(mGps.getPosition().getLatitude(), mGps.getPosition().getLongitude());
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(droneLocation).animate(CameraAnimation.Linear);

        if (mMapLock == true) {
            droneMarker.setPosition(droneLocation);
            droneMarker.setIcon(OverlayImage.fromResource(R.drawable.location_overlay_icon));
            droneMarker.setFlat(true);
            droneMarker.setWidth(100);
            droneMarker.setHeight(400);
            droneMarker.setMap(mNaverMap);
            droneMarker.setAnchor(new PointF(0.5f, 0.85f));
            droneMarker.setAngle(mYaw);
            mNaverMap.moveCamera(cameraUpdate);

            poly.add(0, droneLocation);
            polylineOverlay.setCoords(poly);
            poly.set(0, droneLocation);
            polylineOverlay.setCoords(poly);
            polylineOverlay.setWidth(4);
            polylineOverlay.setCapType(PolylineOverlay.LineCap.Round);
            polylineOverlay.setJoinType(PolylineOverlay.LineJoin.Round);
            polylineOverlay.setColor(Color.RED);
            polylineOverlay.setMap(mNaverMap);
        } else {
            droneMarker.setPosition(droneLocation);
            droneMarker.setIcon(OverlayImage.fromResource(R.drawable.location_overlay_icon));
            droneMarker.setFlat(true);
            droneMarker.setWidth(100);
            droneMarker.setHeight(400);
            droneMarker.setMap(mNaverMap);
            droneMarker.setAnchor(new PointF(0.5f, 0.85f));
            droneMarker.setAngle(mYaw);

            poly.add(0, droneLocation);
            polylineOverlay.setCoords(poly);
            poly.set(0, droneLocation);
            polylineOverlay.setCoords(poly);
            polylineOverlay.setWidth(4);
            polylineOverlay.setCapType(PolylineOverlay.LineCap.Round);
            polylineOverlay.setJoinType(PolylineOverlay.LineJoin.Round);
            polylineOverlay.setColor(Color.RED);
            polylineOverlay.setMap(mNaverMap);
        }
    }

    protected void updateUI(Boolean isConnected) {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        Button connectButton = findViewById(R.id.btnConnect);
        Button armButton = findViewById(R.id.btnArmTakeOff);

        LinearLayout layoutARM = findViewById(R.id.layoutARM);
        LinearLayout layoutDroneAttribute = findViewById(R.id.layoutDroneAttribute);

        TextView altitudeValue = findViewById(R.id.btnTakeOffAltitude);

        if (isConnected) {
            connectButton.setText("Disconnect");
            layoutARM.setVisibility(View.VISIBLE);
            layoutDroneAttribute.setVisibility(View.VISIBLE);
            altitudeValue.setText(String.format("%2.1fm\n이륙고도", mDroneAltitude));
        } else {
            connectButton.setText("Connect");
            layoutARM.setVisibility(View.INVISIBLE);
            layoutDroneAttribute.setVisibility(View.INVISIBLE);
        }

        if (vehicleState.isFlying()) {
            // Land
            armButton.setText("LAND");
        } else if (vehicleState.isArmed()) {
            // Take off
            armButton.setText("TAKE OFF");
        } else if (vehicleState.isConnected()) {
            // Connected but not Armed
            armButton.setText("ARM");
        }
    }

    protected void clearValue() {
        TextView voltageTextView = findViewById(R.id.voltageValueTextView);
        voltageTextView.setText(String.format("0V")); // Clear voltage

        TextView altitudeTextView = findViewById(R.id.altitudeValueTextView);
        altitudeTextView.setText(String.format("0m")); // Clear altitude

        TextView speedTextView = findViewById(R.id.speedValueTextView);
        speedTextView.setText(String.format("0m/s")); // Clear speed

        TextView yawTextView = findViewById(R.id.YAWValueTextView);
        yawTextView.setText(String.format("0deg")); // Clear yaw

        TextView gpsTextView = findViewById(R.id.gpsValueTextView);
        gpsTextView.setText(String.format("0")); // Clear GPS count

        droneMarker.setMap(null); // Clear drone marker
        polylineOverlay.setMap(null); // Clear path
    }

    protected void updateVoltage() { // Drone battery value
        TextView voltageTextView = findViewById(R.id.voltageValueTextView);
        Battery droneBattery = this.drone.getAttribute(AttributeType.BATTERY);
        voltageTextView.setText(String.format("%3.1f", droneBattery.getBatteryVoltage()) + "V");

        if (droneBattery.getBatteryVoltage() < 11) {
            voltageTextView.setTextColor(Color.RED);
        }
    }

    protected void updateAltitude() { // Drone altitude value
        TextView altitudeTextView = findViewById(R.id.altitudeValueTextView);
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        altitudeTextView.setText(String.format("%3.1f", droneAltitude.getAltitude()) + "m");

        if (droneAltitude.getAltitude() < 0) {
            altitudeTextView.setTextColor(Color.RED);
        } else {
            altitudeTextView.setTextColor(Color.WHITE);
        }
    }

    protected void updateSpeed() { // Drone speed value
        TextView speedTextView = findViewById(R.id.speedValueTextView);
        Speed droneSpeed = this.drone.getAttribute(AttributeType.SPEED);
        speedTextView.setText(String.format("%3.1f", droneSpeed.getGroundSpeed()) + "m/s");
    }

    protected void updateYaw() { // Yaw value
        TextView yawTextView = findViewById(R.id.YAWValueTextView);
        mDroneYaw = this.drone.getAttribute(AttributeType.ATTITUDE);
        mYaw = (float) mDroneYaw.getYaw();
        if (mYaw < 0) {
            mYaw = mYaw + 360;
        } else {
            mYaw = (float) mDroneYaw.getYaw();
        }
        yawTextView.setText(String.format("%3.0f", mYaw) + "deg");
    }

    protected void updateSatellitesCount() { // Satellite Count
        TextView gpsTextView = findViewById(R.id.gpsValueTextView);
        Gps droneGpsCount = this.drone.getAttribute(AttributeType.GPS);
        gpsTextView.setText(String.format("%d", droneGpsCount.getSatellitesCount()));

        if (droneGpsCount.getSatellitesCount() < 10) {
            gpsTextView.setTextColor(Color.RED);
        } else {
            gpsTextView.setTextColor(Color.WHITE);
        }
    }

    protected void updateVehicleModesForType(int droneType) { // Drone Mode
        List<VehicleMode> vehicleModes = VehicleMode.getVehicleModePerDroneType(droneType);
        ArrayAdapter<VehicleMode> vehicleModeArrayAdapter = new ArrayAdapter<VehicleMode>(this, android.R.layout.simple_spinner_item, vehicleModes);
        vehicleModeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.modeSelector.setAdapter(vehicleModeArrayAdapter);
    }

    protected void updateVehicleMode() { // Drone Mode
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        VehicleMode vehicleMode = vehicleState.getVehicleMode();
        ArrayAdapter arrayAdapter = (ArrayAdapter) this.modeSelector.getAdapter();
        this.modeSelector.setSelection(arrayAdapter.getPosition(vehicleMode));
    }

    // Delivery //

    public void checkOrder() {
        mOrderLog.setOnItemClickListener(new OrderLog.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(mAddress.get(pos)).animate(CameraAnimation.Linear);
                mNaverMap.moveCamera(cameraUpdate);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
                builder.setView(dialogView);

                final AlertDialog alertDialog = builder.create();

                LinearLayout addressLayout = dialogView.findViewById(R.id.addressLayout);
                TextView title = dialogView.findViewById(R.id.title);
                TextView message = dialogView.findViewById(R.id.message);

                addressLayout.setVisibility(View.GONE);
                title.setText("주문 상세 정보");
                message.setVisibility(View.GONE);

                Button btnPositive = dialogView.findViewById(R.id.btnPositive);
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                Button btnNegative = dialogView.findViewById(R.id.btnNegative);
                btnNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
            }
        });
    }

    public void delOrder() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
                builder.setView(dialogView);

                final AlertDialog alertDialog = builder.create();

                LinearLayout addressLayout = dialogView.findViewById(R.id.addressLayout);
                LinearLayout orderDetailLayout = dialogView.findViewById(R.id.orderDetail);
                TextView title = dialogView.findViewById(R.id.title);
                TextView message = dialogView.findViewById(R.id.message);

                addressLayout.setVisibility(View.GONE);
                orderDetailLayout.setVisibility(View.GONE);
                title.setText("삭제하시겠습니까?");
                message.setText("확인을 누르시면 삭제됩니다.");

                Button btnPositive = dialogView.findViewById(R.id.btnPositive);
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOrderDataLog.remove(position);
                        mOrderLog.notifyItemRemoved(position);
                        mAddress.remove(position);
                        orderMarker.get(position).setMap(null);
                        alertDialog.dismiss();
                    }
                });
                Button btnNegative = dialogView.findViewById(R.id.btnNegative);
                btnNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOrderLog.notifyDataSetChanged();
                        alertDialog.dismiss();
                    }
                });
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mOrderRecyclerView);
    }

    private void runGuideMode(LatLng guideLatLng) {
        State vehicleState = drone.getAttribute(AttributeType.STATE);
        final LatLong target = new LatLong(guideLatLng.latitude, guideLatLng.longitude);

        if (vehicleState.isConnected()) {
            if (vehicleState.isArmed()) {
                if (vehicleState.isFlying()) {
                    if (vehicleState.getVehicleMode() == vehicleState.getVehicleMode().COPTER_GUIDED) {
                        mGuideMode.mGuidedPoint = guideLatLng;
                        mGuideMode.mMarkerGuide.setPosition(guideLatLng);
                        mGuideMode.mMarkerGuide.setMap(mNaverMap);
                        ControlApi.getApi(drone).goTo(target, true, new AbstractCommandListener() {
                            @Override
                            public void onSuccess() {
                                alertUser("현재고도를 유지하며 이동합니다.");
                            }

                            @Override
                            public void onError(int executionError) {
                                alertUserError("이동할 수 없습니다.");
                            }

                            @Override
                            public void onTimeout() {
                                alertUserError("시간초과.");
                            }
                        });

                    } else if (vehicleState.getVehicleMode() != vehicleState.getVehicleMode().COPTER_GUIDED) {
                        mGuideMode.mGuidedPoint = guideLatLng;
                        mGuideMode.mMarkerGuide.setPosition(guideLatLng);
                        mGuideMode.mMarkerGuide.setMap(mNaverMap);
                        mGuideMode.DialogSimple(drone, target);
                    }
                } else {
                    alertUserError("비행중이 아닙니다.");
                }
            } else {
                alertUserError("시동을 걸어주세요.");
            }
        } else {
            alertUserError("드론을 연결해주세요.");
        }
    }

    @Override
    public void onLinkStateUpdated(@NonNull LinkConnectionStatus connectionStatus) {
        switch(connectionStatus.getStatusCode()){
            case LinkConnectionStatus.FAILED:
                Bundle extras = connectionStatus.getExtras();
                String msg = null;
                if (extras != null) {
                    msg = extras.getString(LinkConnectionStatus.EXTRA_ERROR_MSG);
                }
                alertUserError("Connection Failed:" + msg);
                break;
        }
    }
}