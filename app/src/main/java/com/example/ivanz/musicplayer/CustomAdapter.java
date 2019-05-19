package com.example.ivanz.musicplayer;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<PlaylistItem> {

    private int resourceLayout;
    private Context mContext;
    private List<PlaylistItem> songList;

    public CustomAdapter(Context context, int resource, List<PlaylistItem> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
        this.songList = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null)
        {
            LayoutInflater viewInflater = LayoutInflater.from(mContext);
            viewInflater.inflate(resourceLayout, null);
        }

        PlaylistItem song = getItem(position);

        if(song != null)
        {
            TextView songName = (TextView) convertView.findViewById(R.id.textViewSongName);
            ImageButton playButton = (ImageButton) convertView.findViewById(R.id.likeButton);
            ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.moreButton);

            songName.setText(song.getSongName());
        }

        return convertView;
    }
}