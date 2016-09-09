package com.esri.arcgisruntime.sample.featurelayergeodatabase;

import java.io.File;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.esri.arcgisruntime.datasource.arcgis.Geodatabase;
import com.esri.arcgisruntime.datasource.arcgis.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;

public class MainActivity extends AppCompatActivity {

    private static File extStorDir;
    private static String extSDCardDirName;
    private static String vtpkFilename;
    private static String geodbFilename;
    private static String mVtpk;
    private static String mGeoDb;

    private MapView mMapView;
    private Geodatabase mGeodatabase;
    private ArcGISMap mArcGISMap;

    // define permission to request
    private final String[] reqPermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create the path to local data
        extStorDir = Environment.getExternalStorageDirectory();
        extSDCardDirName = this.getResources().getString(R.string.config_data_sdcard_offline_dir);
        vtpkFilename = this.getResources().getString(R.string.config_vtpk_name);
        geodbFilename = this.getResources().getString(R.string.config_geodb_name);

        // full path to data
        mVtpk = createvtpkFilePath();
        mGeoDb = createGeoDbFilePath();

        // create MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // For API level 23+ request permission at runtime
        if (ContextCompat.checkSelfPermission(MainActivity.this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            addData(mVtpk, mGeoDb);
        } else {
            // request permission
            int requestCode = 2;
            ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
        }
    }

    /**
     * Handle the permissions request response
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addData(mVtpk, mGeoDb);
        } else {
            // report to user that permission was denied
            Toast.makeText(MainActivity.this, getResources().getString(R.string.location_permission_denied),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create the vector tile layer file location and name structure
     */
    private static String createvtpkFilePath() {
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName + File.separator + vtpkFilename;
    }

    /**
     * Create the mobile geodatabase file location and name structure
     */
    private static String createGeoDbFilePath() {
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName + File.separator + geodbFilename;
    }

    /**
     * Load a vector tile file into a MapView
     *
     * @param vtpkFile  Full path to vector tile layer package file
     * @param geoDbFile Full path to geodatabase file
     */
    private void addData(String vtpkFile, final String geoDbFile) {
        // create a new ArcGISVectorTiledLayer from local path
        ArcGISVectorTiledLayer vectorTiledLayer = new ArcGISVectorTiledLayer(vtpkFile);
        // create a Basemap instance for use in creating an ArcGISMap instance
        Basemap basemap = new Basemap(vectorTiledLayer);
        mArcGISMap = new ArcGISMap(basemap);

        mMapView.setWrapAroundMode(WrapAroundMode.ENABLE_WHEN_SUPPORTED);
        // set the initial viewpoint
        mMapView.setViewpoint(new Viewpoint(33.902017, -118.218533, 2));

        // set the mArcGISMap to be displayed in this view
        mMapView.setMap(mArcGISMap);

        // create a new Geodatabase from local path
        mGeodatabase = new Geodatabase(geoDbFile);
        // load the geodatabase
        mGeodatabase.loadAsync();
        // add feature layer from geodatabase to the ArcGISMap
        mGeodatabase.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                GeodatabaseFeatureTable geoDbTable = mGeodatabase.getGeodatabaseFeatureTable("Trailheads");
                FeatureLayer fLayer = new FeatureLayer(geoDbTable);
                mArcGISMap.getOperationalLayers().add(fLayer);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }
}
