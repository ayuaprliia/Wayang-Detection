package com.example.wayang_detection.detector

import com.example.wayang_detection.data.model.BoundingBox
import com.example.wayang_detection.data.model.DetectionResult
import com.example.wayang_detection.data.repository.WayangRepository

/**
 * Temporal smoothing for real-time detection — reduces flickering,
 * stabilizes bounding boxes, and provides consistent class labels.
 *
 * Implements:
 * 1. IoU-based frame-to-frame track matching
 * 2. Exponential Moving Average (EMA) for bounding box positions
 * 3. Class voting for stable character identification
 * 4. Detection persistence (survives brief detection drops)
 * 5. Confidence decay for stale tracks
 */
class DetectionSmoother(
    private val maxAge: Int = 5,            // Frames to keep a detection alive without match
    private val iouMatchThreshold: Float = 0.25f, // IoU threshold for matching across frames
    private val emaAlpha: Float = 0.45f      // EMA weight for new bounding box (higher = more responsive)
) {
    private val tracks = mutableListOf<Track>()

    private data class Track(
        var characterId: String,
        var characterName: String,
        var confidence: Float,
        var box: BoundingBox,
        var age: Int = 0,          // Frames since last matched
        var hits: Int = 1,         // Total frames this track was detected
        val classVotes: MutableMap<String, Int> = mutableMapOf()  // characterId → vote count
    )

    /**
     * Process a new frame's detections and return smoothed results.
     * Thread-safe (synchronized).
     */
    @Synchronized
    fun smooth(detections: List<DetectionResult>): List<DetectionResult> {
        // 1. Age all existing tracks
        tracks.forEach { it.age++ }

        // 2. Match new detections to existing tracks (greedy, highest IoU first)
        val usedDet = mutableSetOf<Int>()
        val usedTrack = mutableSetOf<Int>()

        val candidates = mutableListOf<Triple<Int, Int, Float>>() // trackIdx, detIdx, IoU
        for (ti in tracks.indices) {
            for (di in detections.indices) {
                val iou = boxIoU(tracks[ti].box, detections[di].boundingBox)
                if (iou > iouMatchThreshold) {
                    candidates.add(Triple(ti, di, iou))
                }
            }
        }
        candidates.sortByDescending { it.third }

        for ((ti, di, _) in candidates) {
            if (ti in usedTrack || di in usedDet) continue

            val track = tracks[ti]
            val det = detections[di]

            // EMA smooth bounding box
            track.box = emaBox(track.box, det.boundingBox)

            // EMA smooth confidence
            track.confidence = track.confidence * (1 - emaAlpha) + det.confidence * emaAlpha

            // Class voting — accumulate votes for stable classification
            track.classVotes[det.characterId] = (track.classVotes[det.characterId] ?: 0) + 1
            val bestEntry = track.classVotes.maxByOrNull { it.value }
            if (bestEntry != null) {
                val bestChar = WayangRepository.getById(bestEntry.key)
                if (bestChar != null) {
                    track.characterId = bestChar.id
                    track.characterName = bestChar.name
                }
            }

            track.age = 0
            track.hits++
            usedDet.add(di)
            usedTrack.add(ti)
        }

        // 3. Create new tracks for unmatched detections
        for (di in detections.indices) {
            if (di !in usedDet) {
                val det = detections[di]
                tracks.add(Track(
                    characterId = det.characterId,
                    characterName = det.characterName,
                    confidence = det.confidence,
                    box = det.boundingBox,
                    classVotes = mutableMapOf(det.characterId to 1)
                ))
            }
        }

        // 4. Remove expired tracks
        tracks.removeAll { it.age > maxAge }

        // 5. Return active tracks with confidence decay for stale ones
        return tracks.mapNotNull { track ->
            val decayFactor = if (track.age == 0) 1f
            else (1f - track.age.toFloat() / (maxAge + 1).toFloat())
            val displayConf = track.confidence * decayFactor

            // Don't return very low confidence stale tracks
            if (displayConf < 0.2f) return@mapNotNull null

            DetectionResult(
                characterId = track.characterId,
                characterName = track.characterName,
                confidence = displayConf,
                boundingBox = track.box
            )
        }
    }

    /** Clear all tracks. Call when leaving detection screen. */
    @Synchronized
    fun reset() {
        tracks.clear()
    }

    private fun emaBox(old: BoundingBox, new: BoundingBox): BoundingBox {
        return BoundingBox(
            left   = old.left   + emaAlpha * (new.left   - old.left),
            top    = old.top    + emaAlpha * (new.top    - old.top),
            right  = old.right  + emaAlpha * (new.right  - old.right),
            bottom = old.bottom + emaAlpha * (new.bottom - old.bottom)
        )
    }

    private fun boxIoU(a: BoundingBox, b: BoundingBox): Float {
        val interL = maxOf(a.left, b.left)
        val interT = maxOf(a.top, b.top)
        val interR = minOf(a.right, b.right)
        val interB = minOf(a.bottom, b.bottom)
        val interArea = maxOf(0f, interR - interL) * maxOf(0f, interB - interT)
        val aArea = (a.right - a.left) * (a.bottom - a.top)
        val bArea = (b.right - b.left) * (b.bottom - b.top)
        val union = aArea + bArea - interArea
        return if (union > 0f) interArea / union else 0f
    }
}
