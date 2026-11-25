package com.nbaencolores.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

/** Reproductor IPTV nativo con ExoPlayer (Media3). */
@OptIn(markerClass = UnstableApi.class)
public class PlayerActivity extends Activity {
    private ExoPlayer player;
    private PlayerView view;
    private TextView msg, title, resizeBtn;
    private String url;
    private boolean firstHls, retried;
    private int resizeIdx = 0;
    private static final int[] MODES = {
            AspectRatioFrameLayout.RESIZE_MODE_FIT,
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
            AspectRatioFrameLayout.RESIZE_MODE_FILL};
    private static final String[] MODE_NAMES = {"Ajustar", "Zoom", "Estirar"};

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideBars();

        url = getIntent().getStringExtra("url");
        String name = getIntent().getStringExtra("title");

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        view = new PlayerView(this);
        view.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        view.setKeepScreenOn(true);
        root.addView(view, new FrameLayout.LayoutParams(-1, -1));

        // Barra superior: título + botón de tamaño
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(16), dp(10), dp(16), dp(10));
        top.setBackgroundColor(0x88000000);
        TextView back = pill("✕");
        back.setOnClickListener(v -> finish());
        title = new TextView(this);
        title.setText(name == null ? "" : name);
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setSingleLine(true);
        title.setPadding(dp(12), 0, dp(12), 0);
        resizeBtn = pill(MODE_NAMES[0]);
        resizeBtn.setOnClickListener(v -> {
            resizeIdx = (resizeIdx + 1) % MODES.length;
            view.setResizeMode(MODES[resizeIdx]);
            resizeBtn.setText(MODE_NAMES[resizeIdx]);
        });
        top.addView(back);
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));
        top.addView(resizeBtn);
        root.addView(top, new FrameLayout.LayoutParams(-1, -2, Gravity.TOP));

        msg = new TextView(this);
        msg.setTextColor(Color.WHITE);
        msg.setTextSize(15);
        msg.setGravity(Gravity.CENTER);
        msg.setPadding(dp(24), 0, dp(24), 0);
        root.addView(msg, new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER));

        // La barra superior se muestra/oculta junto con los controles
        view.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility ->
                top.setVisibility(visibility));
        setContentView(root);

        DefaultHttpDataSource.Factory http = new DefaultHttpDataSource.Factory()
                .setUserAgent("VLC/3.0.20 LibVLC/3.0.20")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(20000);
        DefaultLoadControl load = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(15000, 50000, 2000, 4000)
                .build();
        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this)
                        .setDataSourceFactory(new DefaultDataSource.Factory(this, http)))
                .setLoadControl(load)
                .build();
        view.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) msg.setText("");
            }

            @Override
            public void onPlayerError(PlaybackException e) {
                if (e.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    player.seekToDefaultPosition();
                    player.prepare();
                    return;
                }
                if (!retried) {
                    // Segundo intento con el otro formato (HLS <-> directo)
                    retried = true;
                    msg.setText("Reintentando…");
                    start(!firstHls);
                    return;
                }
                msg.setText("⚠️ No se pudo reproducir este canal.\n" + e.getErrorCodeName()
                        + "\nPuede estar caído o requerir otra configuración.");
            }
        });

        if (url == null || url.isEmpty()) { finish(); return; }
        String low = url.toLowerCase();
        firstHls = low.contains(".m3u8") || low.contains("output=m3u8") || low.contains("/hls/");
        msg.setText("Cargando…");
        start(firstHls);
    }

    private void start(boolean hls) {
        MediaItem.Builder mb = new MediaItem.Builder().setUri(url);
        if (hls) mb.setMimeType(MimeTypes.APPLICATION_M3U8);
        player.setMediaItem(mb.build());
        player.prepare();
        player.setPlayWhenReady(true);
    }

    private TextView pill(String t) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextColor(Color.WHITE);
        v.setTextSize(14);
        v.setPadding(dp(14), dp(8), dp(14), dp(8));
        v.setBackgroundColor(0x44FFFFFF);
        return v;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private void hideBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (player != null) player.setPlayWhenReady(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) player.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) { player.release(); player = null; }
    }
}
