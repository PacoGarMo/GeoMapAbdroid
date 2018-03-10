package com.example.dam.geomap;

import android.content.Context;
import android.location.Location;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.AndroidSupport;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Query;

import java.io.IOException;

/**
 * Created by Francisco on 10/03/2018.
 */

public class Bd4oTools {

    public static EmbeddedConfiguration getDb4oConfig() throws IOException {
        EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
        configuration.common().add(new AndroidSupport());
        configuration.common().objectClass(Localizacion.class).
                objectField("fecha").indexed(true);
        return configuration;
    }

    public static void saveDataLocation(ObjectContainer db, Localizacion location){
        db.store(location);
        db.commit();
    }

    public static void closeDB(ObjectContainer db){
        db.close();
    }

    public static ObjectSet<Localizacion> getLocations(ObjectContainer db){
        Query query = db.query();
        query.constrain(Localizacion.class);
        ObjectSet<Localizacion> locations = query.execute();
        return  locations;
    }

    public static String getPath (Context ctx) {
        return ctx.getDir("DataBase", 0) + "/" + "ejemplo.db4o";
    }

}
