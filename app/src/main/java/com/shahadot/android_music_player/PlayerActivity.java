package com.shahadot.android_music_player;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.frolo.waveformseekbar.WaveformSeekBar;
import com.shahadot.android_music_player.databinding.ActivityPlayerBinding;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import jp.wasabeef.glide.transformations.BlurTransformation;

// Media3 imports
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

public class PlayerActivity extends AppCompatActivity {
    private ActivityPlayerBinding binding;
    private ExoPlayer player;
    private final Handler handler = new Handler();
    private List<Song> songList = new ArrayList<>();
    private List<Song> shuffledList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                long currentPosition = player.getCurrentPosition();
                long duration = player.getDuration();
                if (duration > 0) {
                    float progress = ((float) currentPosition / duration);
                    binding.waveformSeekBar.setProgressInPercentage(progress);
                    binding.textElapsed.setText(formatTime((int) (currentPosition / 1000)));
                    binding.textDuration.setText(formatTime((int) (duration / 1000)));
                }
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        songList = getIntent().getParcelableArrayListExtra("songList");
        currentIndex = getIntent().getIntExtra("position", 0);

        if (songList == null || songList.isEmpty()) {
            Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        shuffledList = new ArrayList<>(songList);
        Collections.shuffle(shuffledList);

        binding.waveformSeekBar.setWaveform(createWaveForm(), true);
        initPlayerWithSong(currentIndex);
        setupControls();
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void setupControls() {
        binding.buttonPlayPause.setOnClickListener(v -> togglePlayPause());
        binding.buttonNext.setOnClickListener(v -> playNext());
        binding.buttonPrev.setOnClickListener(v -> playPrev());
        binding.buttonShuffle.setOnClickListener(v -> toggleShuffle());
        binding.buttonRepeat.setOnClickListener(v -> toggleRepeat());

        binding.waveformSeekBar.setCallback(new WaveformSeekBar.Callback() {
            @Override
            public void onProgressChanged(WaveformSeekBar seekBar, float percent, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    long seekPosition = (long) (percent * duration);
                    player.seekTo(seekPosition);
                    binding.textElapsed.setText(formatTime((int) (seekPosition / 1000)));
                }
            }

            @Override
            public void onStartTrackingTouch(WaveformSeekBar seekBar) {
                handler.removeCallbacks(updateRunnable);
            }

            @Override
            public void onStopTrackingTouch(WaveformSeekBar seekBar) {
                handler.postDelayed(updateRunnable, 0);
            }
        });
    }

    private void toggleRepeat() {
        isRepeat = !isRepeat;
        player.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        binding.buttonRepeat.setColorFilter(isRepeat ? getResources().getColor(R.color.green) : getResources().getColor(R.color.white));
    }

    private void toggleShuffle() {
        isShuffle = !isShuffle;
        if (isShuffle) {
            Collections.shuffle(shuffledList);
            binding.buttonShuffle.setColorFilter(getResources().getColor(R.color.purple));
        } else {
            shuffledList = new ArrayList<>(songList);
            binding.buttonShuffle.clearColorFilter();
        }
        initPlayerWithSong(currentIndex);
    }

    private void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
            handler.removeCallbacks(updateRunnable);
        } else {
            player.play();
            handler.postDelayed(updateRunnable, 0);
        }
        updatePlayPauseIcon();
    }

    private void initPlayerWithSong(int index) {
        Song song = isShuffle ? shuffledList.get(index) : songList.get(index);

        if (player != null) {
            player.release();
        }

        player = new ExoPlayer.Builder(this).build();
        player.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Toast.makeText(PlayerActivity.this, "Error Playing Media: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                updatePlayPauseIcon();
                if (playbackState == Player.STATE_READY) {
                    binding.textDuration.setText(formatTime((int) (player.getDuration() / 1000)));
                    handler.postDelayed(updateRunnable, 0);
                } else if (playbackState == Player.STATE_ENDED) {
                    playNext();
                }
            }
        });

        player.setMediaItem(MediaItem.fromUri(song.data));
        player.prepare();
        player.play();

        updatePlayPauseIcon();
        updateUI(song);
    }

    private void updateUI(Song song) {
        binding.textTitle.setText(song.title != null ? song.title : "No Title");
        binding.textArtist.setText(song.artist != null ? song.artist : "No Artist");
        setTitle(song.title);

        Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), song.albumId);

        if (hasAlbumArt(albumArtUri)) {
            Glide.with(this)
                    .asBitmap()
                    .load(albumArtUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_music_note_24)
                    .error(R.drawable.ic_music_note_24)
                    .into(binding.imageAlbumArtPlayer);

            Glide.with(this)
                    .asBitmap()
                    .load(albumArtUri)
                    .apply(bitmapTransform(new BlurTransformation(25, 3)))
                    .placeholder(R.drawable.ic_music_note_24)
                    .error(R.drawable.ic_music_note_24)
                    .into(binding.bgAlbumArt);
        } else {
            binding.imageAlbumArtPlayer.setImageResource(R.drawable.ic_music_note_24);
            binding.bgAlbumArt.setImageResource(R.drawable.ic_music_note_24);
        }
    }

    private boolean hasAlbumArt(Uri albumArtUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(albumArtUri)) {
            return inputStream != null;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private int[] createWaveForm() {
        Random random = new Random(System.currentTimeMillis());
        int[] values = new int[50];
        for (int i = 0; i < values.length; i++) {
            values[i] = 5 + random.nextInt(50);
        }
        return values;
    }

    private void updatePlayPauseIcon() {
        binding.buttonPlayPause.setImageResource(
                player != null && player.isPlaying()
                        ? R.drawable.ic_pause_24
                        : R.drawable.ic_play_arrow_24
        );
    }

    private void playNext() {
        currentIndex = (currentIndex + 1) % (isShuffle ? shuffledList.size() : songList.size());
        initPlayerWithSong(currentIndex);
    }

    private void playPrev() {
        int listSize = isShuffle ? shuffledList.size() : songList.size();
        currentIndex = (currentIndex - 1 + listSize) % listSize;
        initPlayerWithSong(currentIndex);
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            handler.removeCallbacks(updateRunnable);
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
