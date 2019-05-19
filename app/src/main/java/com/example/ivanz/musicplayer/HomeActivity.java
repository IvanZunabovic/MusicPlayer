package com.example.ivanz.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    List<PlaylistItem> songList;
    ListView allSongsPlaylist;
    PlaylistAdapter searchResultAdapter = null;
    PlaylistAdapter customHomeAdapter = null;
    PlaylistAdapter customFavoriteAdapter = null;
    MusicManager musicManager = new MusicManager(this);
    MediaPlayer player = musicManager.getPlayer();
    TextView currentSong = null;
    boolean isFavoriteListActive = false;
    EditText searchBar;
    Context activityContext = this;
    FloatingActionButton addPlaylistButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this ,R.color.myStatusBarColor));

        songList = findSongs();
        musicManager.setSongList(songList);
        addPlaylistButton = findViewById(R.id.floatingAddButton);

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                musicManager.playNext();
                changeCurrentPlayingInfo();
            }
        });

        searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<PlaylistItem> searchResult = new ArrayList<>();

                for (PlaylistItem song : songList) {
                    if(song.getSongName().contains(searchBar.getText()))
                        searchResult.add(song);
                }

                searchResultAdapter = new PlaylistAdapter(activityContext, R.layout.playlist_element, searchResult);
                allSongsPlaylist.setAdapter(searchResultAdapter);
            }
        });

        searchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);

                    searchBar.clearFocus();
                    if(searchBar.getText().toString().matches(""))
                        searchBar.setVisibility(View.INVISIBLE);
                    return true;
                }
                return false;
            }
        });

        allSongsPlaylist = findViewById(R.id.allSongsPlaylist);
        final PlaylistAdapter customAdapter = new PlaylistAdapter(this, R.layout.playlist_element,songList);
        allSongsPlaylist.setAdapter(customAdapter);
    }

    public ArrayList<PlaylistItem> findSongs() {
        ArrayList<PlaylistItem> songs = new ArrayList<>();

        String[] projection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA};
        Cursor audioCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

        if(audioCursor != null){
            if(audioCursor.moveToFirst()){
                do {
                    int audioIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                    String songName = audioCursor.getString(audioIndex);
                    PlaylistItem newSong = new PlaylistItem(songName.replace(".mp3","").replace("_", " "), audioCursor.getString(audioCursor
                            .getColumnIndex(MediaStore.Audio.Media.DATA)));

                    songs.add(newSong);
                }

                while(audioCursor.moveToNext());
            }
        }

        audioCursor.close();

        return songs;
    }

    public void playMusic(View v) {
        TextView songName = (TextView) v;
        musicManager.setSongTitle((String) songName.getText());
        LinearLayout playingStatusBar = findViewById(R.id.playingStatusBar);

        for(int i = 0; i < songList.size(); i++)
        {
            if(songList.get(i).getSongName() == songName.getText())
            {
                musicManager.setSongPosition(i);
                musicManager.playSong();

                break;
            }
        }

        if(musicManager.isMediaPlaying())
        {
            changeMarkedSong(v);

            if(!playingStatusBar.isShown())
                   playingStatusBar.setVisibility(View.VISIBLE);

            Intent intent = new Intent(this, PlayingActivity.class);
            startActivity(intent);
        }
    }

    public void changeMarkedSong(View v) {
        TextView songName = (TextView) v;
        songName.setTextColor(getResources().getColor(R.color.activeState));

        changeCurrentPlayingInfo();

        if(currentSong != null)
            currentSong.setTextColor(getResources().getColor(R.color.inactiveState));

        currentSong = songName;
    }

    public void changeCurrentPlayingInfo() {
        TextView songInfo = findViewById(R.id.songInfo);
        songInfo.setText(musicManager.getSongTitle());
        songInfo.setSelected(true);
    }

    public void pauseOrPlayMusic(View v) {
        ImageButton playOrPauseButton = (ImageButton) v;

        if(player.isPlaying())
        {
            musicManager.pause();
            playOrPauseButton.setImageResource(R.drawable.play_icon);
            playOrPauseButton.setPadding(41,41,41,41);
        }

        else {
            musicManager.continuePlaying();
            playOrPauseButton.setImageResource(R.drawable.pause_icon);
            playOrPauseButton.setPadding(38,38,38,38);
        }
    }

    public void displayFavoriteList(View v) {
        ImageButton favoriteButton = (ImageButton) v;
        addPlaylistButton.setVisibility(View.INVISIBLE);

        if (!isFavoriteListActive)
        {
            isFavoriteListActive = true;
            favoriteButton.setImageResource(R.drawable.filled_heart_icon);
            List<PlaylistItem> favoriteList = new ArrayList<>();

            for (PlaylistItem song : songList)
                if(song.isFavorite())
                    favoriteList.add(song);


            customFavoriteAdapter = new PlaylistAdapter(this, R.layout.playlist_element, favoriteList);
            allSongsPlaylist.setAdapter(customFavoriteAdapter);
        }
    }

    public void homeDisplay(View v) {
        addPlaylistButton.setVisibility(View.INVISIBLE);

        if (isFavoriteListActive)
        {
            isFavoriteListActive = false;
            ImageButton favoriteButton = findViewById(R.id.homeFavoriteButton);
            favoriteButton.setImageResource(R.drawable.heart_icon);

            customHomeAdapter = new PlaylistAdapter(this, R.layout.playlist_element, songList);
            allSongsPlaylist.setAdapter(customHomeAdapter);
        }
    }

    public void openPlayingActivity(View v) {
        Intent intent = new Intent(this, PlayingActivity.class);
        startActivity(intent);
    }

    public void focusSearchBar(View v) {
        searchBar.setVisibility(View.VISIBLE);

        searchBar.requestFocus();
        searchBar.setText("");

        if(addPlaylistButton.getVisibility() == View.VISIBLE)
            addPlaylistButton.setVisibility(View.INVISIBLE);

        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);
    }

    public void playlistView(View v) {
        addPlaylistButton.setVisibility(View.VISIBLE);
    }
}
