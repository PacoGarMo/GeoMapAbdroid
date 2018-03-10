package com.example.dam.geomap;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.AndroidSupport;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
import com.db4o.query.Query;

import java.io.IOException;
import java.util.GregorianCalendar;

public class Bd4oActivity extends AppCompatActivity {

    private static final String TAG = Bd4oActivity.class.getSimpleName();

    private ObjectContainer objectContainer;

    public EmbeddedConfiguration getDb4oConfig() throws IOException {
        EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
        configuration.common().add(new AndroidSupport());
        configuration.common().objectClass(Localizacion.class).
                objectField("fecha").indexed(true);
        return configuration;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bd4o);

        startService(new Intent(this, LocationService.class));

        objectContainer = openDataBase("ejemplo.db4o");

        Localizacion loc = new Localizacion();
        objectContainer.store(loc);
        objectContainer.commit();

        loc = new Localizacion(new Location("provider"));
        objectContainer.store(loc);
        objectContainer.commit();

        loc = new Localizacion(new Location("proveedor"), new GregorianCalendar(2018,1,22).getTime());
        objectContainer.store(loc);
        objectContainer.commit();

        Query consulta = objectContainer.query();
        consulta.constrain(Localizacion.class); //buscamos todas las localizaciones
        ObjectSet<Localizacion> localizaciones = consulta.execute(); //las obtengo
        for(Localizacion localizacion: localizaciones){ //las veo
            Log.v(TAG, "1: " + localizacion.toString());
        }

        ObjectSet<Localizacion> locs = objectContainer.query(
                new Predicate<Localizacion>() {
                    @Override
                    public boolean match(Localizacion loc) { //el objeto que este en la base de datos, su fechas sea la que le paso
                        return loc.getFecha().equals(new GregorianCalendar(2018,1,22).getTime());
                    }
                });
        for(Localizacion localizacion: locs){
            Log.v(TAG, "2: " + localizacion.toString());
        }
        objectContainer.close();
    }

    private ObjectContainer openDataBase(String archivo) {
        ObjectContainer objectContainer = null;
        try {
            String name = getExternalFilesDir(null) + "/" + archivo;
            objectContainer = Db4oEmbedded.openFile(getDb4oConfig(), name);
        } catch (IOException e) {
            Log.v(TAG, e.toString());
        }
        return objectContainer;
    }
}
