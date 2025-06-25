package com.shahadot.android_music_player;

import android.content.ContentUris;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shahadot.android_music_player.databinding.ItemSongBinding;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final List<Song> songs;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public SongAdapter(List<Song> songs, OnItemClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSongBinding binding = ItemSongBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SongViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.binding.textTitle.setText(song.title);
        holder.binding.textArtist.setText(song.artist);

        Uri artUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                song.albumId
        );

        Glide.with(holder.binding.getRoot().getContext())
                .load(artUri)
                .circleCrop()
                .placeholder(R.drawable.ic_music_note_24)
                .error(R.drawable.ic_music_note_24)
                .into(holder.binding.imageAlbumArt);

        // Correct usage of getBindingAdapterPosition()
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onItemClick(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        final ItemSongBinding binding;

        public SongViewHolder(ItemSongBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
