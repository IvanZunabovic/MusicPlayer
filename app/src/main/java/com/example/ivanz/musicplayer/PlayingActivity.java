package com.example.ivanz.musicplayer;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.jmusixmatch.entity.track.Track;
import org.jmusixmatch.entity.track.TrackData;

import java.util.ArrayList;

public class PlayingActivity extends AppCompatActivity {

    MusicManager musicManager = new MusicManager(this);
    MediaPlayer player = musicManager.getPlayer();
    private Handler mHandler = new Handler();
    private Lyrics lyrics;
    private TrackData data;
    private Track track;
    TextView lyricsDisplay;
    TextView currentSongProgress;
    private SeekBar seekBar;
    Runnable moveSeekBarThread;

    public class LyricsService extends AsyncTask<Void, TextView, Void> {
        private MusixMatch musixMatch;
        private String artistName;
        private String songName;

        public LyricsService(String artistName, String songName) {
            this.artistName = artistName;
            this.songName = songName;
            this.musixMatch = new MusixMatch("YourApiKey");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                track = musixMatch.getMatchingTrack(songName, artistName);
            }

            catch (Exception e) {
                Log.d("Lyrics Exception", e.getLocalizedMessage());
            }

            if(track != null)
            {
                data = track.getTrack();

                try {
                    lyrics = musixMatch.getLyrics(data.getTrackId());
                }

                catch (Exception e) {
                    Log.d("String Exception", e.getLocalizedMessage());
                }
            }

            else
                lyrics = null;

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            lyricsDisplay = findViewById(R.id.lyricsDisplay);

            if(lyrics != null)
            {
                try{
                    lyricsDisplay.setText(lyrics.getLyricsBody());
                }

                catch (Exception e)
                {
                    Log.d("Setting Lyrics", e.getLocalizedMessage());
                }
            }

            else
                lyricsDisplay.setText("No Lyrics");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        getSupportActionBar().hide();

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this ,R.color.myStatusBarColor));

        lyricsDisplay = findViewById(R.id.lyricsDisplay);
        lyricsDisplay.setMovementMethod(new ScrollingMovementMethod());

        currentSongProgress = findViewById(R.id.currentSongProgress);
        seekBar = findViewById(R.id.seekBar);

        seekBar.setMax(player.getDuration() / 1000);
        seekBar.setProgress(player.getCurrentPosition() / 1000);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (player != null && fromUser) {
                    player.seekTo(progress);
                    int mediaPos_new = player.getCurrentPosition();
                    int mediaMax = player.getDuration();

                    int minutes = mediaPos_new / (60 * 1000);
                    int seconds = (mediaPos_new / 1000) % 60;
                    String mediaPosStr = String.format("%d:%02d", minutes, seconds);
                    currentSongProgress.setText(mediaPosStr);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        moveSeekBarThread = new Runnable() {
            public void run() {
                if(player.isPlaying()){

                    int mediaPos_new = player.getCurrentPosition();
                    int mediaMax_new = player.getDuration();
                    seekBar.setMax(mediaMax_new);
                    seekBar.setProgress(mediaPos_new);

                    int minutes = mediaPos_new / (60 * 1000);
                    int seconds = (mediaPos_new / 1000) % 60;
                    String mediaPosStr = String.format("%d:%02d", minutes, seconds);

                    currentSongProgress.setText(mediaPosStr);

                    seekBar.postDelayed(this, 1000);
                }
            }
        };

        mHandler = new Handler();
        mHandler.removeCallbacks(moveSeekBarThread);
        mHandler.postDelayed(moveSeekBarThread, 1000);

        getSongLyrics();

        TextView currentSongName = findViewById(R.id.playingSongName);
        currentSongName.setText(musicManager.getSongTitle());
        currentSongName.setSelected(true);

        ImageButton favoriteButton = findViewById(R.id.favoriteButton);

        if(musicManager.getSongList().get(musicManager.getSongPosition()).isFavorite())
            favoriteButton.setImageResource(R.drawable.filled_heart_icon);

        TextView duration = findViewById(R.id.durationText);
        duration.setText(musicManager.getDuration());
    }

    public void shuffle(View v) {
        ImageButton shuffleButton = (ImageButton) v;

        musicManager.setShuffle();

        if(musicManager.getShuffleState())
            shuffleButton.setImageResource(R.drawable.shuffle_active_icon);

        else
            shuffleButton.setImageResource(R.drawable.shuffle_icon);
    }

    public void repeat(View v) {
        ImageButton repeatButton = (ImageButton) v;

        musicManager.setRepeat();

        if(musicManager.getRepeatState())
            repeatButton.setImageResource(R.drawable.repeat_active_icon);

        else
            repeatButton.setImageResource(R.drawable.repeat_icon);
    }

    public void playOrPause(View v) {
        ImageButton playButton = (ImageButton) v;

        if(musicManager.isMediaPlaying())
        {
            musicManager.pause();
            playButton.setImageResource(R.drawable.main_play_icon);
        }

        else
        {
            musicManager.continuePlaying();
            moveSeekBarThread.run();
            playButton.setImageResource(R.drawable.main_pause_button);
        }
    }

    public void nextSong(View v) {
        musicManager.playNext();
        lyricsDisplay.setText("");

        seekBar.setProgress(player.getCurrentPosition() / 1000);
        seekBar.setMax(player.getDuration() / 1000);
        TextView currentSongName = findViewById(R.id.playingSongName);

        currentSongName.setText(musicManager.getSongTitle());

        getSongLyrics();

        changeHeartIcon();
    }

    public void previousSong(View v) {
        musicManager.playPrevious();
        lyricsDisplay.setText("");

        seekBar.setProgress(player.getCurrentPosition() / 1000);
        seekBar.setMax(player.getDuration() / 1000);
        TextView currentSongName = findViewById(R.id.playingSongName);
        currentSongName.setText(musicManager.getSongTitle());

        getSongLyrics();

        changeHeartIcon();
    }

    public void back(View v) {
        finish();
    }

    public void favourite(View v) {
        ImageButton favoriteButton = (ImageButton) v;
        PlaylistItem currentSong = musicManager.getSongList().get(musicManager.getSongPosition());

        if(currentSong.isFavorite())
        {
            currentSong.setFavorite(false);
            favoriteButton.setImageResource(R.drawable.heart_icon);
        }

        else
        {
            currentSong.setFavorite(true);
            favoriteButton.setImageResource(R.drawable.filled_heart_icon);
        }
    }

    public void changeHeartIcon() {
        ImageButton favoriteButton = findViewById(R.id.favoriteButton);

        if(musicManager.getSongList().get(musicManager.getSongPosition()).isFavorite())
            favoriteButton.setImageResource(R.drawable.filled_heart_icon);

        else
            favoriteButton.setImageResource(R.drawable.heart_icon);
    }

    public void getSongLyrics() {
        String songInfo = musicManager.getSongTitle();
        String[] data = songInfo.split("-", 2);

        if(data.length == 2)
            new LyricsService(data[0], data[1]).execute();


        else
            new LyricsService("", data[0]).execute();
    }

    public void songSettings(View v) {
        FrameLayout songSettings = findViewById(R.id.songSettings);
        if(songSettings.getVisibility() == View.INVISIBLE)
        {
            songSettings.setVisibility(View.VISIBLE);
        }
    }

    public void closeSongSettings(View v) {
        FrameLayout songSettings = (FrameLayout) v;
        songSettings.setVisibility(View.INVISIBLE);
    }

    public void renameSettings(View v) {

    }
}
