package matheusjullien.photomap;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_LOCATION = 0;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    private static final int REQUEST_WRITE_STORAGE = 1;
    private static String[] PERMISSIONS_WRITE_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private LinearLayout root;
    private Button photo, video;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Uri fileUri;
    private PageDatabase mPageDatabase;
    private String timeStamp;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(new Fabric.Builder(this)
                .kits(new Crashlytics(), new Answers())
                .debuggable(true)
                .build());
        setContentView(R.layout.activity_maps);

        mPageDatabase = new PageDatabase(this);
        mPageDatabase.open();

        root = (LinearLayout) findViewById(R.id.root);
        photo = (Button) findViewById(R.id.photo);
        video = (Button) findViewById(R.id.video);
        Button all = (Button) findViewById(R.id.all);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildGoogleApiClient();

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        type = MEDIA_TYPE_IMAGE;

                        requestWriteExternalPermission();
                    } else {
                        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                        if (fileUri != null) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                        } else {
                            // Create file failed, advise user
                        }
                    }
                } else {
                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                    if (fileUri != null) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                    } else {
                        // Create file failed, advise user
                    }
                }
            }
        });

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        type = MEDIA_TYPE_VIDEO;

                        requestWriteExternalPermission();
                    } else {
                        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

                        if (fileUri != null) {
                            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                            startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
                        } else {
                            // Create file failed, advise user
                        }
                    }
                } else {
                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

                    if (fileUri != null) {
                        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                        startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
                    } else {
                        // Create file failed, advise user
                    }
                }
            }
        });

        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Page> pageArrayList = mPageDatabase.getAllPages();

                if (!pageArrayList.isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), ScreenSlidePagerActivity.class);

                    startActivity(intent);
                } else {
                    // No files saved, advise user
                }
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();

        if (mMap != null) {
            mMap.clear();

            ArrayList<Page> pageArrayList = mPageDatabase.getAllPages();
            ArrayList<LatLng> latLngArrayList = new ArrayList<>();

            for (Page p : pageArrayList) {
                LatLng mLatLng = new LatLng(p.getLatitude(),p.getLongitude());

                if (!latLngArrayList.contains(mLatLng)) {
                    latLngArrayList.add(mLatLng);

                    mMap.addMarker(new MarkerOptions().position(mLatLng).title("Click here").snippet("See pictures and videos at this location"));
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private Uri getOutputMediaFileUri(int type){
        if (getOutputMediaFile(type) != null) {
            return Uri.fromFile(getOutputMediaFile(type));
        } else {
            return null;
        }
    }

    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PhotoMap");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && fileUri != null) {
                galleryAdd(fileUri);

                Page mPage = new Page();
                mPage.setPath(fileUri.toString());
                mPage.setDate(timeStamp);
                mPage.setType("image");

                if (mLastLocation != null) {
                    mPage.setLatitude(mLastLocation.getLatitude());
                    mPage.setLongitude(mLastLocation.getLongitude());
                }

                mPageDatabase.insertPage(mPage);

                onResume();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }

        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && fileUri != null) {
                galleryAdd(fileUri);

                Page mPage = new Page();
                mPage.setPath(fileUri.toString());
                mPage.setDate(timeStamp);
                mPage.setType("video");

                if (mLastLocation != null) {
                    mPage.setLatitude(mLastLocation.getLatitude());
                    mPage.setLongitude(mLastLocation.getLongitude());
                }

                mPageDatabase.insertPage(mPage);

                onResume();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
            } else {
                // Video capture failed, advise user
            }
        }
    }

    private void galleryAdd(Uri mPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(mPath);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("pageLatLng",marker.getPosition());

                Intent intent = new Intent(getApplicationContext(),ScreenSlidePagerActivity.class);
                intent.putExtra("bundle",bundle);

                startActivity(intent);
            }
        });

        mMap.clear();

        ArrayList<Page> pageArrayList = mPageDatabase.getAllPages();
        ArrayList<LatLng> latLngArrayList = new ArrayList<>();

        for (Page p : pageArrayList) {
            LatLng mLatLng = new LatLng(p.getLatitude(),p.getLongitude());

            if (!latLngArrayList.contains(mLatLng)) {
                latLngArrayList.add(mLatLng);

                mMap.addMarker(new MarkerOptions().position(mLatLng).title("Click here").snippet("See all pictures and videos"));
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                requestLocationPermissions();
            } else {
                if (!isLocationEnabled()) {
                    createAlertBuilder();
                } else {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    mMap.setMyLocationEnabled(true);

                    if (mLastLocation != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 12));
                    }
                }
            }
        } else {
            if (!isLocationEnabled()) {
                createAlertBuilder();
            } else {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mMap.setMyLocationEnabled(true);

                if (mLastLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 12));
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    private void requestLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            Snackbar.make(root, R.string.permission_location_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MapsActivity.this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
        }
    }

    private void requestWriteExternalPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Snackbar.make(root, R.string.permission_write_storage_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MapsActivity.this, PERMISSIONS_WRITE_STORAGE, REQUEST_WRITE_STORAGE);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_WRITE_STORAGE, REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (verifyPermissions(grantResults)) {
                Snackbar.make(root, R.string.permission_available_location, Snackbar.LENGTH_SHORT).show();

                mGoogleApiClient.connect();
            } else {
                Snackbar.make(root, R.string.permissions_not_granted, Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_WRITE_STORAGE) {
            if (verifyPermissions(grantResults)) {
                Snackbar.make(root, R.string.permission_available_write_storage, Snackbar.LENGTH_SHORT).show();

                if (type == MEDIA_TYPE_IMAGE) {
                    photo.performClick();
                } else if (type == MEDIA_TYPE_VIDEO) {
                    video.performClick();
                }
            } else {
                Snackbar.make(root, R.string.permissions_not_granted, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static boolean verifyPermissions(int[] grantResults) {
        if (grantResults.length < 1){
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                int locationMode = Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);

                if (locationMode != Settings.Secure.LOCATION_MODE_OFF && locationMode != Settings.Secure.LOCATION_MODE_SENSORS_ONLY) {
                    return true;
                } else {
                    return false;
                }
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();

                return false;
            }
        } else {
            @SuppressWarnings("deprecation")
            String locationProviders = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private void createAlertBuilder() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setCancelable(false);
        mBuilder.setTitle("Location services disabled");
        mBuilder.setMessage("PhotoMap needs access to location services to get user location");
        mBuilder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        mBuilder.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog mAlert = mBuilder.create();
        mAlert.show();
    }
}