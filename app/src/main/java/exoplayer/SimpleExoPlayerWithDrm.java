/*
package exoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.math.BigInteger;
import java.util.UUID;


public class SimpleExoPlayerWithDrm {

    private final static String TAG = "SimpleExoPlayerWithDrm";

    public static SimpleExoPlayer getPlayer(final Context context, ContentTypes contentTypes,
                                            String mediaUrl, final String licenseUrl, String webVTTSubtitlesUrl,
                                            boolean setPlayWhenReady)
            throws UnsupportedDrmException {

        String highBytes = "EDEF8BA979D64ACE";
        String lowBytes = "A3C827DCD51D21ED";
        long hiBits = new BigInteger(highBytes, 16).longValue();
        long loBits = new BigInteger(lowBytes, 16).longValue();
        UUID drmSchemeUuid = new UUID(hiBits, loBits);

        Handler mainHandler = new Handler();

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        DefaultHttpDataSourceFactory dataSourceFactory =
                new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "simpleexoplayerapp"),
                        (TransferListener<? super DataSource>) bandwidthMeter);

        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, dataSourceFactory);
        drmCallback.setKeyRequestProperty("utoken-drm", "wv");


        DrmSessionManager<FrameworkMediaCrypto> drmSessionManager =
                new DefaultDrmSessionManager<>(drmSchemeUuid, FrameworkMediaDrm.newInstance(drmSchemeUuid),
                        drmCallback, null, mainHandler, new DefaultDrmSessionManager.EventListener() {
                    @Override
                    public void onDrmKeysLoaded() {
                        Log.i(TAG, "Drm keys Loaded");
                    }

                    @Override
                    public void onDrmSessionManagerError(Exception e) {
                        Log.e(TAG, "DrmSessionManagerError: " + e.getMessage());
                    }

                    @Override
                    public void onDrmKeysRestored() {
                        Log.i(TAG, "Drm keys restored");
                    }

                    @Override
                    public void onDrmKeysRemoved() {
                        Log.i(TAG, "Drm keys removed");
                    }
                });

        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context,
                trackSelector, new DefaultLoadControl(), drmSessionManager);

        MediaSource mediaSource = null;
        switch (contentTypes) {
            case HLS:
                mediaSource = new HlsMediaSource(Uri.parse(mediaUrl),
                        dataSourceFactory, null, null);
                break;
            case M4F:
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                mediaSource = new ExtractorMediaSource(Uri.parse(mediaUrl),
                        dataSourceFactory, extractorsFactory, null, null);
                break;
            case DASH:
                mediaSource = new DashMediaSource(Uri.parse(mediaUrl), dataSourceFactory,
                        new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
                break;
            default:
        }

        MergingMediaSource mergingMediaSource = null;
        if (webVTTSubtitlesUrl != null) {
            Format subtitleFormat = Format.createTextSampleFormat(
                    null, // An identifier for the track. May be null.
                    MimeTypes.TEXT_VTT, // The mime type. Must be set correctly.
                    C.SELECTION_FLAG_DEFAULT, // Selection flags for the track.
                    null); // The subtitle language. May be null.
            MediaSource subtitleSource = new SingleSampleMediaSource(
                    Uri.parse(webVTTSubtitlesUrl), dataSourceFactory, subtitleFormat, C.TIME_UNSET);
            mergingMediaSource = new MergingMediaSource(mediaSource, subtitleSource);
            player.prepare(mergingMediaSource);
        } else {
            player.prepare(mediaSource);
        }

        player.setPlayWhenReady(setPlayWhenReady);

        return player;
    }


}
*/
