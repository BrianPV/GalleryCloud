package com.example.gallery;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class AlbumActivity extends AppCompatActivity {

    GridView galleryGridView;
    ArrayList<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>();
    String album_name = "";
    LoadAlbumImages loadAlbumTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Como es una clase que  creamos solo, le tenemos que pasar el layout con el cual va relacionado
        setContentView(R.layout.activity_album);

        //Obtenemos un intent de la Main Activity
        Intent intent = getIntent();

        //al album le pasamos el nombre de la carpeta
        album_name = intent.getStringExtra("name");

        // y seteamos el titulo de la activity con el nombre del album que recibimos
        setTitle(album_name);

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


        loadAlbumTask = new LoadAlbumImages();
        loadAlbumTask.execute();

    }

    //Creamos un hilo donde cargara las imagenes del album
    class LoadAlbumImages extends AsyncTask<String, Void, String>
    {

        @Override
        protected void onPreExecute()
        {
            //Antes de ejecutar el hilo, limpiamos el ImageList
            super.onPreExecute();
            imageList.clear();
        }

        @Override
        protected String doInBackground(String... strings)
        {
            //Variables de string
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;


            //Este variable traera el contenido que se encuentra en el almacenamiento externo. PSD: Regularmente es el princpial
            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            //Este variable traera el contenido que se encuentra en el almacenamiento interno
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            //Hacemos un arreglo en donde traera la
            // DATA se refiere a la ruta absoluta del "filesystem"
            // BUCKET_DISPLAY_NAME se refiere al folder en donde esta alojada la imagen
            //DATE_MODIFIED se refiere al tiempo de la ultima vez que ha sido modificado
            String[] projection = { MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };


            //El cursor externo que va a obtener el contexto y con la ayuda de la query
            // va a pasarle uriExternal () mas aparte le estamos diciendo que al momento
            // de hacer la seleccion, este nos mostrara el nombre de la carpeta con el album name
            Cursor cursorExternal = getContentResolver()
                    .query(uriExternal,
                            projection,
                            "bucket_display_name = \""+album_name+"\"",
                            null,
                            null);


            //Lo mismo de arriba
            Cursor cursorInternal = getContentResolver().
                    query(uriInternal,
                            projection,
                            "bucket_display_name = \""+album_name+"\"",
                            null,
                            null);

            //Hacemos un merge
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});

            //Creamos un ciclo while en donde continuara hasta que ya no haya un siguiente
            while (cursor.moveToNext())
            {

                //La variable path le asignamos la ruta que consiga de la busqueda del cursor
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));

                //La variable timestamp le asignamos el tiempo de la ultima vez que fue modificado
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

                //La variable timestamp le asignamos el tiempo de la ultima vez que fue modificado
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                //Finalmente agregamos al Arraylist el HashMap que habiamos creado en el helper "Function"
                //en donde le pasamos los parametros necesarios para que asi los guarde y les demos uso
                imageList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null));
            }

            //Cerramos el cursor
            cursor.close();

            //Hacemos una collecion en donde las vamos a ordernar por tiempo en desencediente
            Collections.sort(imageList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending

            return xml;
        }

        @Override
        protected void onPostExecute(String s)
        {
            //Iniciamos una instancia del imageView
            SingleAlbumAdapter adapter = new SingleAlbumAdapter(AlbumActivity.this, imageList);

            //Le asignamos el adaptador
            galleryGridView.setAdapter(adapter);

            //Cada vez que den click en una imagen
            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    //Se hara un intent en donde se pasara al activity GalleryPreview
                    Intent intent = new Intent(AlbumActivity.this, GalleryPreview.class);

                    //En donde con la ayuda del helper "Function" se pasara la ruta del archivo y de igual forma pasamos la posicion de este
                    intent.putExtra("path", imageList.get(+position).get(Function.KEY_PATH));

                    //Finalmente se inicial el intent
                    startActivity(intent);
                }
            });
        }
    }


}

class SingleAlbumAdapter extends BaseAdapter
{
    private Activity activity;
    private ArrayList<HashMap< String, String >> data;

    public SingleAlbumAdapter(Activity a, ArrayList < HashMap < String, String >> d)
    {
        activity = a;
        data = d;
    }
    public int getCount()
    {
        return data.size();
    }

    public Object getItem(int position)
    {
        return position;
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {

        //Inicializamos un variable de la clase de abajo
        SingleAlbumViewHolder holder = null;

        //Si el convertView es null
        if (convertView == null)
        {
            //Le decimos que el holder(Que es ImageView) sea igual a una nueva instancia de la clase
            holder = new SingleAlbumViewHolder();

            //Aqui le asignamos el Layout "single_album_row"
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.single_album_row, parent, false);

            //En donde decimos que el imageView de la clase sea relacionado con el id galleryImage
            holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);

            convertView.setTag(holder);
        } else { //Si no es nulo
            holder = (SingleAlbumViewHolder) convertView.getTag();
        }


        //Al imageView le seteamos la posicion actual
        holder.galleryImage.setId(position);

        //Con data obtenemos la posicion
        HashMap < String, String > picturePath = new HashMap < String, String > ();
        picturePath = data.get(position);


        //Con un try catch y la ayuda de Glide mostramos la imagen
        try {

            //Le pasamos el contexto a glide
            Glide.with(activity)
                    .load(new File(picturePath.get(Function.KEY_PATH))) // Uri of the picture, Convirtiendo la foto en un archivo le pasamos la ruta donde se encuentra
                    .into(holder.galleryImage);  //Finalmente la posicionamos en el ImageView

        } catch (Exception e)
        {
            //Mostramos si hay una excepcion
            Log.e("Excepcion: ", "SingleAlbumAdapter Fallo en : " + e.toString() );
        }

        return convertView;
    }
}


class SingleAlbumViewHolder {
    ImageView galleryImage;
}
