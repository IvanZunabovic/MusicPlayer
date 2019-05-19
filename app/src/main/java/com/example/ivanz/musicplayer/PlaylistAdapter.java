package com.example.ivanz.musicplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class PlaylistAdapter extends ArrayAdapter<PlaylistItem>{

    Context context;
    int resources;
    List<PlaylistItem> songList;


    public PlaylistAdapter(@NonNull Context context, int resource, @NonNull List<PlaylistItem> itemsList) {
        super(context, resource, itemsList);
        this.context = context;
        this.resources = resource;
        this.songList = itemsList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(resources, null);
        TextView songName = view.findViewById(R.id.textViewSongName);
        ImageButton playButton = (ImageButton) view.findViewById(R.id.likeButton);
        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.moreButton);

        PlaylistItem song = songList.get(position);
        songName.setText(song.getSongName());

        return view;
    }
}