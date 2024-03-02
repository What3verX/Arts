package com.osmanlioglu.arts;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.osmanlioglu.arts.databinding.ArtItemBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtViewHolder> {


    ArrayList<Arts> artsArrayList;
    public ArtAdapter(ArrayList<Arts> artsArrayList){
        this.artsArrayList = artsArrayList;

    }

    @NonNull
    @Override
    public ArtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ArtItemBinding artItemBinding = ArtItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new ArtViewHolder(artItemBinding);



    }

    @Override
    public void onBindViewHolder(@NonNull ArtViewHolder holder, int position) {

        holder.binding.artItem.setText(artsArrayList.get(position).name);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),ArtActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("itemId",artsArrayList.get(holder.getAdapterPosition()).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return artsArrayList.size();
    }

    public static class ArtViewHolder extends RecyclerView.ViewHolder {
        private ArtItemBinding binding;
        public ArtViewHolder(ArtItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

}
