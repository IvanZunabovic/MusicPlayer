package com.example.ivanz.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.EOFException;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MusicManager {

    private static MediaPlayer player;
    private static List<PlaylistItem> songList;
    private static int songPosition;
    private static String songTitle;
    private static boolean shuffle;
    private static boolean repeat;
    private Context mContext;
    private static Random rand;

    public MusicManager(Context context) {
        mContext = context;
        rand = new Random();
    }

    public MediaPlayer getPlayer() {
        if(player == null)
        {
            synchronized (MusicManager.class) {
                player = new MediaPlayer();
            }
        }

        return player;
    }

    public static List<PlaylistItem> getSongList() {
        return songList;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public int getSongPosition() {
        return songPosition;
    }

    public static boolean getShuffleState() {
        return shuffle;
    }

    public static boolean getRepeatState() {
        return repeat;
    }

    public void setSongList(List<PlaylistItem> newSongList) {
        if(songList == null)
            songList = newSongList;
    }

    public void setSongTitle(String newSongTitle) {
        songTitle = newSongTitle;
    }

    public  void setShuffle() {
        if(shuffle)
            shuffle = false;

        else
            shuffle = true;
    }

    public void setRepeat() {
        if(repeat)
            repeat = false;

        else
            repeat = true;
    }

    public void setSongPosition(int position) {
        songPosition = position;
    }

    public void playSong() {
        player.reset();

        PlaylistItem currentSong = songList.get(songPosition);
        songTitle = currentSong.getSongName();

        File currentSongFile = new File(currentSong.getPath());
        currentSongFile.setReadable(true, false);

        try
        {
            player.stop();
            player.reset();
            player.setDataSource(mContext, Uri.fromFile(currentSongFile));
            player.prepare();
            player.start();
        }

        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        catch (Exception e) {
            System.out.println("Exception of type : " + e.toString());
            e.printStackTrace();
        }
    }

    public void pause() {
        player.pause();
    }

    public  void continuePlaying() {
        player.start();
    }

    public void seek(int position) {
        player.seekTo(position);
    }

    public void playPrevious() {
        if (songPosition > 0 && !shuffle) {
            songPosition--;
            playSong();
        }

        else if(shuffle)
        {
            songPosition = rand.nextInt(songList.size());
            playSong();
        }
    }

    public void playNext() {
        if(songPosition < songList.size() - 1 && !shuffle)
        {
            songPosition++;
            playSong();
        }

        else if(repeat && !shuffle)
        {
            songPosition = 0;
            playSong();
        }

        else if(!repeat && shuffle)
        {
            int newPosition = songPosition;

            while(newPosition == songPosition)
                newPosition = rand.nextInt(songList.size());

            songPosition = newPosition;
            playSong();
        }
    }

    public boolean isMediaPlaying() {
        return player.isPlaying();
    }

    public String getDuration() {
        int duration = player.getDuration();
        String time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );

        return time;
    }

    public int getMediaPosition() {
        return player.getCurrentPosition();
    }
}
