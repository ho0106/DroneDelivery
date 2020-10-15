package com.example.dronedelivery;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.Marker;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;

public class GuideMode extends AppCompatActivity {
    LatLng mGuidedPoint; // 가이드모드 목적지 저장.
    Marker mMarkerGuide = new com.naver.maps.map.overlay.Marker(); // 가이드모드 마커 생성
    private MainActivity mMainActivity;

    public GuideMode(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
    }

    public void DialogSimple(final Drone drone, final LatLong point) {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(mMainActivity);
        alt_bld.setMessage("확인하시면 가이드모드로 전환후 기체가 이동합니다.").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_GUIDED, new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        ControlApi.getApi(drone).goTo(point, true, null);
                        mMainActivity.alertUser("현재고도를 유지하며 이동합니다.");
                    }
                    @Override
                    public void onError(int i) {
                        mMainActivity.alertUser("기체를 이동할 수 없습니다.");
                    }
                    @Override
                    public void onTimeout() {
                        mMainActivity.alertUser("가이드모드 시간초과");
                    }
                });
            }
            // Action for 'No' Button
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    // Custom Dialog
    /*public void DialogDimpleCustom(final Drone drone, final LatLong point) {
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        TextView title = dialogView.findViewById(R.id.title);
        title.setText("현재 고도를 유지하며");
        TextView message = dialogView.findViewById(R.id.message);
        message.setText("목표지점까지 기체가 이동합니다.");
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_GUIDED, new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        ControlApi.getApi(drone).goTo(point, true, null);
                    }
                    @Override
                    public void onError(int i) {
                        mMainActivity.alertUser("Error");
                    }
                    @Override
                    public void onTimeout() {
                        mMainActivity.alertUser("Time out");
                    }
                });
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
        alertDialog.show();
    }*/
}
