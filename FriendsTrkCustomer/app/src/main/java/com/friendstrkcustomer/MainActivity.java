package com.friendstrkcustomer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.teliver.sdk.core.TLog;
import com.teliver.sdk.core.Teliver;
import com.teliver.sdk.core.TrackingListener;
import com.teliver.sdk.core.TripListener;
import com.teliver.sdk.models.MarkerOption;
import com.teliver.sdk.models.PushData;
import com.teliver.sdk.models.TLocation;
import com.teliver.sdk.models.TrackingBuilder;
import com.teliver.sdk.models.Trip;
import com.teliver.sdk.models.TripBuilder;
import com.teliver.sdk.models.UserBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private String trackingId = "tracking_id", USERNAME = "username";

    private Application application;

    private String[] mulipleUsers = new String[]{"user_1,user_2,user_3"};

    private ArrayList<String> listUsers = new ArrayList<>();

    private Button btnTrip, btnTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TLog.setVisible(true);
        application = (Application) getApplicationContext();
        if (application.checkPermission(this))
            checkGps();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("message"));
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Teliver.identifyUser(new UserBuilder(USERNAME).setUserType(UserBuilder.USER_TYPE.CONSUMER).registerPush().build());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Drawable drawable = toolbar.getNavigationIcon();
        drawable.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        fragment.getMapAsync(this);
        btnTrip = (Button) findViewById(R.id.btnTrip);
        btnTracking = (Button) findViewById(R.id.btnTracking);

        btnTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!application.getBoolean("IN_CURRENT_TRIP")) {
                    PushData pushData = new PushData(mulipleUsers);
                    pushData.setMessage("Track Me");
                    Teliver.sendEventPush(trackingId, pushData, "tag");

                    TripBuilder tripBuilder = new TripBuilder(trackingId).withUserPushObject(pushData);
                    Teliver.setTripListener(new TripListener() {
                        @Override
                        public void onTripStarted(Trip tripDetails) {
                            Log.d("TELIVER::", "onTripStarted: " + tripDetails.getTrackingId());
                            application.storeBoolean("IN_CURRENT_TRIP", true);
                            btnTrip.setText(getString(R.string.txtStopTrip));
                        }

                        @Override
                        public void onLocationUpdate(Location location) {
                            Log.d("TELIVER::", "onLocationUpdate: " + location.getLatitude() + location.getLongitude());
                        }

                        @Override
                        public void onTripEnded(String trackingID) {
                            Log.d("TELIVER::", "on Trip Ended: " + trackingID);
                            application.storeBoolean("IN_CURRENT_TRIP", false);
                            btnTrip.setText(getString(R.string.txtStartTrip));
                        }

                        @Override
                        public void onTripError(String reason) {

                        }
                    });
                    Teliver.startTrip(tripBuilder.build());
                } else {
                    Teliver.stopTrip(trackingId);
                }
            }
        });

        btnTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!application.getBoolean("TRACKING_STARTED")) {
                    List<MarkerOption> markerOptionList = new ArrayList<>();
                    String[] ids = listUsers.toArray(new String[listUsers.size()]);
                    for (String id : ids) {
                        MarkerOption option = new MarkerOption(id);
                        option.setMarkerTitle("Hi There");
                        markerOptionList.add(option);
                    }

                    TrackingBuilder trackingBuilder = new TrackingBuilder(markerOptionList).withYourMap(googleMap)
                            .withListener(new TrackingListener() {
                                @Override
                                public void onTrackingStarted(String trackingId) {
                                    application.storeBoolean("TRACKING_STARTED", true);
                                    btnTracking.setText(getString(R.string.txtStopTracking));
                                }

                                @Override
                                public void onLocationUpdate(String trackingId, TLocation location) {
                                    Log.d("TELIVER::", "on Location Update " + trackingId + location.getLatitude() + location.getLongitude());
                                }

                                @Override
                                public void onTrackingEnded(String trackingId) {
                                    Log.d("TELIVER::", "on Tracking Ended: " + trackingId);
                                    application.storeBoolean("TRACKING_STARTED", false);
                                    btnTracking.setText(getString(R.string.txtStartTracking));
                                }

                                @Override
                                public void onTrackingError(String reason) {
                                    Log.d("TELIVER::", "on Tracking error: " + reason);
                                }
                            });

                    Teliver.startTracking(trackingBuilder.build());
                } else {
                    Log.d("TELIVER::", "onClick: stop tracking button is clicked");
                    Teliver.stopTracking(trackingId);
                    application.storeBoolean("TRACKING_STARTED", false);
                    btnTracking.setText(getString(R.string.txtStartTracking));
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    protected void onResume() {
        if (application.getBoolean("TRACKING_STARTED"))
            btnTracking.setText(getString(R.string.txtStopTracking));
        if (application.getBoolean("IN_CURRENT_TRIP"))
            btnTrip.setText(getString(R.string.txtStopTrip));
        super.onResume();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String trackingId = intent.getStringExtra("trackingId");
            String msg = intent.getStringExtra("msg");

            listUsers.add(trackingId);
            startTracking();

        }
    };

    private void startTracking() {
        String[] users = listUsers.toArray(new String[listUsers.size()]);
        List<MarkerOption> markerOptionList = new ArrayList<>();

        for (String id : users) {
            MarkerOption option = new MarkerOption(id);
            option.setMarkerTitle("Track Me");
            markerOptionList.add(option);
        }
        TrackingBuilder trackingBuilder = new TrackingBuilder(markerOptionList).withListener(new TrackingListener() {
            @Override
            public void onTrackingStarted(String trackingId) {

            }

            @Override
            public void onLocationUpdate(String trackingId, TLocation location) {

            }

            @Override
            public void onTrackingEnded(String trackingId) {

            }

            @Override
            public void onTrackingError(String reason) {

            }
        });
        Teliver.startTracking(trackingBuilder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 4:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    finish();
                else checkGps();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 6 && resultCode == RESULT_OK)
            Toast.makeText(MainActivity.this, "Gps is turned on", Toast.LENGTH_SHORT).show();
        else if (requestCode == 6 && resultCode == RESULT_CANCELED)
            finish();
    }


    private void checkGps() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        status.startResolutionForResult(MainActivity.this, 6);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}






