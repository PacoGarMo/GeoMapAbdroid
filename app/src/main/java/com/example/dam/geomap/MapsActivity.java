package com.example.dam.geomap;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.GregorianCalendar;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;

    private ObjectContainer objectContainer; // lo pongo aqui para ahorarme la llamada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //tratan de cargar de forma asincrona el mapa
        //una vez cargado llama a si mismo y ejecuta onMapReady

        Bundle b = getIntent().getExtras();

        GregorianCalendar dateLocation = (GregorianCalendar) b.get("date");
        objectContainer = openDB(getApplicationContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    public ObjectContainer openDB(Context context) { //abrimos la conexion --- PREGUNTAR A CARMELO SI ES POSIBLE HACERLO SIN NECESIDAD DE LA CALSE TOOLS
        ObjectContainer objectContainer = null;
        try {
            String path = Bd4oTools.getPath(context);
            Log.v("xyzyx", path);
            objectContainer = Db4oEmbedded.openFile(Bd4oTools.getDb4oConfig(), path);
        } catch (IOException ex){
            Log.v("xyzyx", ex.toString());
        }
        return objectContainer;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        //ahora obtendremos la localizacion de la DB
        ObjectSet<Localizacion> locations = Bd4oTools.getLocations(objectContainer);
        PolylineOptions line = new PolylineOptions();
        //inicializamos latitud
        LatLng start = null;
        LatLng end = null;

        //recorremos las localizaciones
        for(int i = 0; i<locations.size(); i++){
            Localizacion localizacion = locations.get(i);
            Log.v("xyzyx", "Localizacion: " + localizacion.getLocalizacion().toString());
            LatLng newLatLng = new LatLng(localizacion.getLocalizacion().getLatitude(),
                    localizacion.getLocalizacion().getLongitude());
            line.add(newLatLng);

            if(i==0){
                start = newLatLng;
            }else if(i == locations.size() - 1){
                end = newLatLng;
            }
        }

        Bd4oTools.closeDB(objectContainer); // cierro la base de datos

        if(start == null || end == null){
            Log.v("xyzyx", "No latlng");
            return;
        }

        //LatLng granada = new LatLng(37.1608,-3.5911);
        this.map.addMarker(new MarkerOptions().position(start).title("Start"));
        this.map.moveCamera(CameraUpdateFactory.newLatLng(start));
        //ZOOM
        this.map.moveCamera(CameraUpdateFactory.zoomTo(17));
        //pintamos la linea inicial en le mapa
        this.map.addPolyline(line);
        //pintamos la linea final en le mapa
        map.addMarker(new MarkerOptions().position(end).title("End"));
        map.moveCamera(CameraUpdateFactory.newLatLng(end));
    }


}
