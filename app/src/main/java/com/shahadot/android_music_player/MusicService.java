package com.shahadot.android_music_player;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

public class MusicService extends MediaSessionService {

    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "music_channel";

    private ExoPlayer player;
    private MediaSession mediaSession;

    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        player = new ExoPlayer.Builder(this).build();

        mediaSession = new MediaSession.Builder(this, player)
                .setSessionActivity(createSessionActivityPendingIntent())
                .build();
    }

    private PendingIntent createSessionActivityPendingIntent() {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String songTitle = null;
        if ("PLAY_SONG".equals(intent.getAction())) {
            String songUri = intent.getStringExtra("song_uri");
            songTitle = intent.getStringExtra("song_title");
            player.setMediaItem(MediaItem.fromUri(songUri));
            player.prepare();
            player.play();
        }

        Notification notification = createNotification(songTitle);
        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }


    @OptIn(markerClass = UnstableApi.class)
    private Notification createNotification(String songTitle) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note_24)
                .setContentTitle("Playing Music")
                .setContentText(songTitle != null ? songTitle : "Unknown Title")
                .setContentIntent(createSessionActivityPendingIntent())
                .setStyle(new androidx.media3.session.MediaStyleNotificationHelper.MediaStyle(mediaSession))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .build();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
        player.release();
    }
}
