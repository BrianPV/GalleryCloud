package com.example.gallery;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{

    static final int REQUEST_PERMISSION_KEY = 1;
    LoadAlbum loadAlbumTask;
    GridView galleryGridView;
    ArrayList<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        galleryGridView = (GridView) findViewById(R.id.galleryGridView);

        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels ;
        Resources resources = getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (metrics.densityDpi / 160f);

        //Con este if, dependemos de cuantas columas se haran el gridview de las imagenes dado que todas las pantallas
        //son diferentes
        if(dp < 360)
        {
            dp = (dp - 17) / 2;
            float px = Function.converToDpToPixel(dp, getApplicationContext());
            galleryGridView.setColumnWidth(Math.round(px));
        }

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        if(!Function.hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }

        //Como esta es la primera activity, tenemos que pedir los permisos y para esos, tenemos que pedirlos y despues de que acepten con el onRequestPermission, ahi ejecutamos la accion que prosigue


    }


    //Al momento de iniciar la app se inicia el pedir los permisios
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Hacemos un while

        switch (requestCode)
        {
            case REQUEST_PERMISSION_KEY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //Si el usuario da permiso, se ejecuta el hilo
                    loadAlbumTask = new LoadAlbum();
                    loadAlbumTask.execute();
                }
                else{ //Si no, se repite hasta que den permisos
                    Toast.makeText(MainActivity.this, "You must accept permissions.", Toast.LENGTH_LONG).show();
                }

        }
    }

//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//
//        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
//
//        if(!Function.hasPermissions(this, PERMISSIONS)){
//            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
//        }else{
//            loadAlbumTask = new LoadAlbum();
//            loadAlbumTask.execute();
//        }
//    }

    //Creamos un hilo para cargar los albums
    private class LoadAlbum extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            albumList.clear();
        }



        @Override
        protected String doInBackground(String... strings)
        {
            //Aun no tengo entendido para que es el xml y porque lo regresamos :V
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            String countPhoto = null;

            //Este variable traera el contenido que se encuentra en el almacenamiento externo. PSD: Regularmente es el princpial
            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            //Este variable traera el contenido que se encuentra en el almacenamiento interno
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;


            //Hacemos un arreglo en donde traera la
            // DATA se refiere a la ruta absoluta del "filesystem"
            // BUCKET_DISPLAY_NAME se refiere al folder en donde esta alojada la imagen
            //DATE_MODIFIED se refiere al tiempo de la ultima vez que ha sido modificado
            String[] projection = {
                    MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.MediaColumns.DATE_MODIFIED
            };


            //Aqui hacemos una seleccion sobre lo que le pedimos en la projection y le agregamos
            //una condicion en donde, si no es nula, que los agrupe por albums
            Cursor cursorExternal = getContentResolver().
                    query(uriExternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                            null, null);

            //Lo mismo de arriba
            Cursor cursorInternal = getContentResolver().
                    query(uriInternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                    null, null);

            //Hace un merge

            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});

            while (cursor.moveToNext())
            {
                //La variable path le asignamos la ruta que consiga de la busqueda del cursor
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));

                //La variable album le asignamos el nombre dle album
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

                //La variable timestamp le asignamos el tiempo de la ultima vez que fue modificado
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                //Con la ayuda del helper le pasamos cuantos fotos existne el el abum
                countPhoto = Function.getCount(getApplicationContext(), album);


                //Finalmente agregamos al Arraylist el HashMap que habiamos creado en el helper "Function"
                //en donde le pasamos los parametros necesarios para que asi los guarde y les demos uso
                albumList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), countPhoto));
            }

            //Cerramos el cursor
            cursor.close();

            //Hacemos una collecion en donde las vamos a ordernar por tiempo en desencediente
            Collections.sort(albumList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending

            return xml;
        }

        @Override
        protected void onPostExecute(String s)
        {
            //Hacemos un nuevo adaptador
            AlbumAdapter adapter = new AlbumAdapter(MainActivity.this, albumList);

            //Lo seteamos
            galleryGridView.setAdapter(adapter);


            //Al momento de dar click en un album
            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {

                    //Hacemos un item que va a pasar datos al AlbumActivity
                    Intent intent = new Intent(MainActivity.this, AlbumActivity.class);

                    //Le pasamos mediante la key "name" la posicion y el nombre del album
                    intent.putExtra("name", albumList.get(+position).get(Function.KEY_ALBUM));
                    //Iniciamos el cambio de activity y pasamos los datos

                    startActivity(intent);
                }
            });
        }
    }


}

//Creamos un hilo para cargar los albums
class AlbumAdapter extends BaseAdapter
{

    private Activity activity;
    private ArrayList<HashMap< String, String >> data;

    public AlbumAdapter(Activity a, ArrayList < HashMap < String, String >> d)
    {
        activity = a;
        data = d;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position)
    {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //Inicializamos los objetos que utilizaremos
        AlbumViewHolder holder = null;

        //Si es null
        if (convertView == null)
        {

            //Instanciamos un nuevo AlbumViewHolder();
            holder = new AlbumViewHolder();

            //Utilizamos el convertView para declararle que vamos a utilizar el diseño de Album_row que es
            //el que designamos para el gridview del main activity
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.album_row, parent, false);

            //Le asignamos sus respectivos id
            holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);
            holder.gallery_count = (TextView) convertView.findViewById(R.id.gallery_count);
            holder.gallery_title = (TextView) convertView.findViewById(R.id.gallery_title);

            //Le indicamos que vamos a setear el nuestros objetos(ImageView y TextView) en el convertview
            convertView.setTag(holder);
        }
        else {
            //Si no es vacio que obtenga el tag
            holder = (AlbumViewHolder) convertView.getTag();
        }

        //Le seteamos dependiendo la posicion
        holder.galleryImage.setId(position);
        holder.gallery_count.setId(position);
        holder.gallery_title.setId(position);

        //Inicialimos un nuevo HashMap
        HashMap < String, String > folders = new HashMap < String, String > ();

        folders = data.get(position);

        //Con un try catch intentamos obtener el nombre del album y su conteo

        try {

            //Obtenemos nombre del album
            holder.gallery_title.setText(folders.get(Function.KEY_ALBUM));
            holder.gallery_count.setText(folders.get(Function.KEY_COUNT));

            Glide.with(activity)
                    .load(new File(folders.get(Function.KEY_PATH))) // Uri of the picture
                    .into(holder.galleryImage);


        } catch (Exception e) {
            //Si hay un error que nos lo muestre
            Log.e("Exception", "AlbumAdapter en getView: " + e.toString());
        }

        return convertView;

    }
}


//Como estamos haciendo el adaptador en las mismas activity, tenemos que declarar nosotros mismo
//las cosas que vamos a utilizar para poder utilizarlas en el adaptador interno de la clase
class AlbumViewHolder
{
    //Por lo cual declaramos nuestras vistas aqui
    ImageView galleryImage;
    TextView gallery_count, gallery_title;
}