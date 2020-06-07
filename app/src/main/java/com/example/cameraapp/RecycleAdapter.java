package com.example.cameraapp;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.MyHolder> {
    List<Bitmap> image_list;
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);

        return new MyHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Bitmap bitmap = image_list.get(position);
        holder.image.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return image_list.size();
    }
    public RecycleAdapter(List<Bitmap> image_list){
        this.image_list = image_list;
    }
    public class MyHolder extends RecyclerView.ViewHolder{
        ImageView image;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }
    }
}
