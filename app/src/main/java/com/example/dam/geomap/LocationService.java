package com.example.dam.geomap;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;

import java.io.IOException;

//el que obtiene las geolocalizaciones y las manda a las bases de datos

public class LocationService extends Service {

    public LocationService() {
    }

    public ObjectContainer openDB(Context context) { //abrimos la conexion --- PREGUNTAR A CARMELO SI ES POSIBLE HACERLO SIN NECESIDAD DE LA CALSE TOOLS
        ObjectContainer objectContainer = null;
        try {
            String path = Bd4oTools.getPath(context);
            objectContainer = Db4oEmbedded.openFile(Bd4oTools.getDb4oConfig(), path);
        } catch (IOException ex){
            Log.v("xyzyx", ex.toString());
        }
        return objectContainer;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent i=new Intent(this, LocationService.class);
        //forzamos que el servicio se comvierta en servicio foreground
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Notification.Builder constructorNotificacion = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Localizando posicion")
                .setContentText("Guardando registro")
                .setContentIntent(PendingIntent.getActivity(this, 0, i, 0));
        startForeground(1, constructorNotificacion.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle b = intent.getExtras();

        Location location = (Location) b.get("ruta");
        Localizacion l1 = new Localizacion(location);
        Log.v("xyzyx", "Location: " + location.toString());
        ObjectContainer objectContainer = openDB(getApplicationContext());
        Bd4oTools.saveDataLocation(objectContainer, l1);
        Bd4oTools.closeDB(objectContainer);


        return START_STICKY;
    }

}
