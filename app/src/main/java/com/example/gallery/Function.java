package com.example.gallery;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class Function {


    static final String KEY_ALBUM = "album_name";
    static final String KEY_PATH = "path";
    static final String KEY_TIMESTAMP = "timestamp";
    static final String KEY_TIME = "date";
    static final String KEY_COUNT = "date";


    public static  boolean hasPermissions(Context context, String... permissions)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
    }


    //Utilizamos el HashMap para agrupar la informacion de las variables
    // y asi mediante la key del HashMap poder recuperarla o
    // reutilizarla despues
    public static HashMap<String, String> mappingInbox(String album, String path, String timestamp, String time, String count)
    {
        //Iniciamos el Hashmap
        HashMap<String, String> map = new HashMap<String, String>();

        //Agregamos los valores al map
        map.put(KEY_ALBUM, album);
        map.put(KEY_PATH, path);
        map.put(KEY_TIMESTAMP, timestamp);
        map.put(KEY_TIME, time);
        map.put(KEY_COUNT, count);

        //Retornamos
        return map;
    }



    //Con la ayuda de MediaStore y Cursors, vamos a contar que tantas imagenes hay en cada album.
    //Esta funcion se utiliza junto a otro proceso de query en el MainActivity
    public static String getCount(Context c, String album_name)
    {
        //Declaramos dos variables para obtener la media de la memoria interna e externa

        //Este variable traera el contenido que se encuentra en el almacenamiento externo. PSD: Regularmente es el princpial
        Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        //Este variable traera el contenido que se encuentra en el almacenamiento interno
        Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        Log.e("getCount", "uriExternal: " + uriExternal.toString() + " uriInternal: " + uriInternal.toString() );

        //RECORDAR QUE ESTAMOS EN EL ACTIVITY EN DONDE YA LE DISTE CLICK A UN FOLDER DE IMAGENES

        //Hacemos un arreglo en donde traera la
        // DATA se refiere a la ruta absoluta del "filesystem"
        // BUCKET_DISPLAY_NAME se refiere al folder en donde esta alojada la imagen
        //DATE_MODIFIED se refiere al tiempo de la ultima vez que ha sido modificado
        String[] projection = {

                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_MODIFIED

        };

        //El cursor externo que va a obtener el contexto y con la ayuda de la query
        // va a pasarle uriExternal (), aparte de obtener el nombre del album
        Cursor cursorExternal = c.getContentResolver().
                query(uriExternal,
                        projection, "bucket_display_name = \""+album_name+"\"",
                        null,
                        null);

        //Lo mismo de arriba
        Cursor cursorInternal = c.getContentResolver().
                query(uriInternal,
                        projection,
                        "bucket_display_name = \""+album_name+"\"",
                        null,
                        null);

        //hacemos mergue
        Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});


        return cursor.getCount()+" Photos";
    }

    //Como el nombre lo dice, convierte el tiempo que le pases a formato de fecha
    public static String converToTime(String timestamp)
    {
        //Se declara una variable de tipo long en donde
        //le vamos a aplicar un parseLong para convertir de string a ong
        long datetime = Long.parseLong(timestamp);

        //Se declara una variable de fecha en donde le pasamos la variable long
        Date date = new Date(datetime);

        //Declaramos otra variable en donde le vamos a dar formato a la fecha que le pasamos
        DateFormat formatter = new SimpleDateFormat("dd/MM HH:mm");

        //finalmente retornamos la fecha ya con el formato
        return formatter.format(date);

    }

    //Convierte el dpi del celular a pixeles para un mejor manejo
    public static float converToDpToPixel(float dp, Context context)
    {
        //Primer declaramos un Resources en donde le vamos a pasar el contexto en el que estemos
        Resources resource = context.getResources();

        //Declaramos una variable DisplayMetrics que nos va a devolver el dpi del contexto en el que estemos
        DisplayMetrics metrics = resource.getDisplayMetrics();

        //Aqui es donde transformamos de densidad DPI a pixeles
        float px = dp * (metrics.densityDpi / 160f);

        //retornamos pixeles
        return px;

    }

}