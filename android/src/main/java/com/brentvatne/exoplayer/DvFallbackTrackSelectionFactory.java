package com.brentvatne.exoplayer;

import androidx.annotation.Nullable;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Timeline;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.ExoTrackSelection;
import androidx.media3.exoplayer.upstream.BandwidthMeter;

/**
 * Track selection factory that explicitly rejects Dolby Vision tracks so Media3 selects the
 * HDR10 base layer (HEVC) instead. Use this to play DV P7 files on DV-unsupported devices.
 * <p>
 * For each video track group with MIME type {@link MimeTypes#VIDEO_DOLBY_VISION}, this factory
 * returns null (no selection), so the selector falls back to the HEVC BL track.
 */
public final class DvFallbackTrackSelectionFactory implements ExoTrackSelection.Factory {

    private final AdaptiveTrackSelection.Factory delegate;

    public DvFallbackTrackSelectionFactory() {
        this.delegate = new AdaptiveTrackSelection.Factory();
    }

    @Override
    @Nullable
    public ExoTrackSelection[] createTrackSelections(
            @Nullable ExoTrackSelection.Definition[] definitions,
            BandwidthMeter bandwidthMeter,
            MediaSource.MediaPeriodId mediaPeriodId,
            Timeline timeline) {
        ExoTrackSelection[] selections = delegate.createTrackSelections(
                definitions, bandwidthMeter, mediaPeriodId, timeline);
        if (selections == null || definitions == null) {
            return selections;
        }
        // Explicitly reject Dolby Vision: no selection for DV groups so HEVC BL is chosen
        for (int i = 0; i < definitions.length; i++) {
            ExoTrackSelection.Definition def = definitions[i];
            if (def != null && isDolbyVision(def)) {
                selections[i] = null;
            }
        }
        return selections;
    }

    private static boolean isDolbyVision(ExoTrackSelection.Definition definition) {
        if (definition.group == null || definition.group.length == 0) {
            return false;
        }
        String mimeType = definition.group.getFormat(0).sampleMimeType;
        return MimeTypes.VIDEO_DOLBY_VISION.equals(mimeType);
    }
}
