package com.example.dam.geomap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.GregorianCalendar;

public class GPSActivity extends AppCompatActivity {

    //properties
    private EditText etDate;
    private Button btnSendDate;

    private ObjectContainer objectContainer;

    private static final int PERMISO_LOCATION = 1;
    private static final int RESOLVE_RESULT = 2;
    private static final String TAG = "xyzyx";

    private FusedLocationProviderClient clienteLocalizacion;
    private LocationCallback callbackLocalizacion;
    private LocationRequest peticionLocalizacion;
    private LocationSettingsRequest ajustesPeticionLocalizacion;
    private SettingsClient ajustesCliente;

    private boolean checkPermissions() {
        int estadoPermisos = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return estadoPermisos == PackageManager.PERMISSION_GRANTED;
    }

    private void init() {

        etDate = findViewById(R.id.etDate);
        btnSendDate = findViewById(R.id.btnSendDate);

        btnSendDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date = etDate.getText().toString();
                String[] arrayDate = date.split("/");

                int d = Integer.parseInt(arrayDate[0]);
                int m = Integer.parseInt(arrayDate[1]);
                int y = Integer.parseInt(arrayDate[2]);

                GregorianCalendar dateRegist = new GregorianCalendar(y, m - 1, d);

                Intent i = new Intent(GPSActivity.this, MapsActivity.class);
                i.putExtra("date", dateRegist);

                startActivity(i);
            }
        });

        if(checkPermissions()) {
            startLocations();
            //startLocations();
        } else {
            requestPermissions();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        init();
    }

    private void requestPermissions() {
        boolean solicitarPermiso = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        // Provide an additional rationale to the user.
        // This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (solicitarPermiso) {
            Log.v(TAG, "Explicación racional del permiso");
            showSnackbar(R.string.app_name, android.R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(GPSActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISO_LOCATION);
                }
            });
        } else {
            Log.v(TAG, "Solicitando permiso");
            ActivityCompat.requestPermissions(GPSActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISO_LOCATION);
        }
    }

    private void showSnackbar(final int idTexto, final int textoAccion,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(idTexto),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(textoAccion), listener).show();
    }

    @SuppressLint("MissingPermission")
    private void startLocations(){
        clienteLocalizacion = LocationServices.getFusedLocationProviderClient(this);
        ajustesCliente = LocationServices.getSettingsClient(this);
        //busca la ultima localizacion del registro GPS
        clienteLocalizacion.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    Log.v(TAG, "última localización: " + location.toString());
                } else {
                    Log.v(TAG, "no hay última localización");
                }
            }
        });

        callbackLocalizacion = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //aqui es donde obtienes las localizaciones
                Location localizacion = locationResult.getLastLocation();
                Intent serviceIntent  = new Intent(GPSActivity.this, LocationService.class);
                serviceIntent.putExtra("ruta", localizacion);
                startService(serviceIntent );
                Log.v(TAG, localizacion.toString());
            }
        };

        //preparamos el objeto con el que lanzamos las localizaciones
        peticionLocalizacion = new LocationRequest();
        peticionLocalizacion.setInterval(10000); //cada 10s una nueva
        peticionLocalizacion.setFastestInterval(5000); //el intervalo la mitad
        peticionLocalizacion.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //maxima precision posible en las localizaciones

        //construimos y lanzamos el cliente de peticiones
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(peticionLocalizacion);
        ajustesPeticionLocalizacion = builder.build();

        //lanzamos el metodo
        ajustesCliente.checkLocationSettings(ajustesPeticionLocalizacion)
                //si todo fue bien lanza el listener
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.v(TAG, "Se cumplen todos los requisitos");
                        //lanzo el cliente de geolocalizacion, cada vez que obtenga una geolocalizacon nueva llama al callbackLocation
                        clienteLocalizacion.requestLocationUpdates(peticionLocalizacion, callbackLocalizacion, null);
                    }
                })
                //si falla
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            //si es subsanable
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.v(TAG, "Falta algún requisito, intento de adquisición");
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    //se ejecuta este metodo, intenta subsanar el error a traves de la imntervencion del usuario
                                    rae.startResolutionForResult(GPSActivity.this, RESOLVE_RESULT);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.v(TAG, "No se puede adquirir.");
                                }
                                break;
                                //si no es subsanable por el usuario
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Log.v(TAG, "Falta algún requisito, que no se puede adquirir.");
                        }
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && requestCode == PERMISO_LOCATION) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocations();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESOLVE_RESULT:
                switch (resultCode) {
                    //si se ha subsanado
                    case Activity.RESULT_OK:
                        Log.v(TAG, "Permiso ajustes localización");
                        //volvemos a intentar realizar el startLocation
                        startLocations();
                        break;
                    //en caso contrario, cierra
                    case Activity.RESULT_CANCELED:
                        Log.v(TAG, "Sin permiso ajustes localización");
                        break;
                }
                break;
        }
    }
}
