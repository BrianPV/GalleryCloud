package com.example.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

public class GalleryPreview extends AppCompatActivity {

    ImageView GalleryPreviewImg;
    String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        //Le asginas el l,ayour "gallery_preview"
        setContentView(R.layout.gallery_preview);

        //Recibes el intent
        Intent intent = getIntent();

        //A la variable path le asignas lo que recibiste
        path = intent.getStringExtra("path");

        //Relacionas la vista
        GalleryPreviewImg = (ImageView) findViewById(R.id.GalleryPreviewImg);

        //Con ayuda de Glide, vemos la imagen
        Glide.with(GalleryPreview.this)
                .load(new File(path)) // Uri of the picture
                .into(GalleryPreviewImg);
    }
}
