package com.example.cameraapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class ShowImages extends AppCompatActivity {
    TextView nts;
    RecyclerView recyclerView;
    DatabaseHelper databaseHelper;
    SQLiteDatabase sqLiteDatabase;
    Cursor cursor;
    List<Bitmap> image_list;
    RecycleAdapter recycleAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);
        nts = findViewById(R.id.nothingtoshow);
        recyclerView = findViewById(R.id.image_recycler);
        setVisiblity();
        databaseHelper = new DatabaseHelper(this);
        image_list = new ArrayList<>();
        sqLiteDatabase = databaseHelper.getReadableDatabase();
        cursor = sqLiteDatabase.rawQuery("select * from Image_Table",null);
        cursor.moveToFirst();
        if(cursor.getCount()==0){
            nts.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else{
            do {
                System.out.println(cursor.getCount());
                byte[] blob = cursor.getBlob(cursor.getColumnIndex("image_data"));
                ByteArrayInputStream inputStream = new ByteArrayInputStream(blob);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                image_list.add(bitmap);
            }while(cursor.moveToNext());
            recycleAdapter = new RecycleAdapter(image_list);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(recycleAdapter);
        }

    }
    public void setVisiblity(){
        recyclerView.setVisibility(View.VISIBLE);
        nts.setVisibility(View.GONE);
    }
}
