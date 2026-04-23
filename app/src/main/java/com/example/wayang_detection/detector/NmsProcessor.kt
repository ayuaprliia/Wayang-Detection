package com.example.wayang_detection.detector

/**
 * Non-Maximum Suppression (NMS) for filtering overlapping detections.
 * Uses greedy per-class NMS with IoU (Intersection over Union) filtering.
 */
object NmsProcessor {

    /**
     * Apply Non-Maximum Suppression to filter overlapping detections.
     * Two-pass approach:
     *   1. Per-class NMS: suppress overlapping boxes of the same class (standard YOLO NMS)
     *   2. Cross-class NMS: suppress overlapping boxes of different classes that cover
     *      the same physical object (keeps only the highest-confidence prediction)
     *
     * @param detections Raw detections from model output.
     * @param iouThreshold IoU threshold for per-class NMS.
     * @param crossClassIouThreshold IoU threshold for cross-class NMS (default 0.7).
     * @return Filtered list of non-overlapping detections (1 box per physical object).
     */
    fun nms(
        detections: List<RawDetection>,
        iouThreshold: Float,
        crossClassIouThreshold: Float = 0.7f
    ): List<RawDetection> {
        if (detections.isEmpty()) return emptyList()

        // ── Pass 1: Per-class NMS (standard) ──
        val sorted = detections.sortedByDescending { it.confidence }
        val perClassSelected = mutableListOf<RawDetection>()
        val suppressed1 = BooleanArray(sorted.size)

        for (i in sorted.indices) {
            if (suppressed1[i]) continue
            perClassSelected.add(sorted[i])

            for (j in i + 1 until sorted.size) {
                if (suppressed1[j]) continue
                // Only suppress same-class overlapping boxes
                if (sorted[i].classIdx == sorted[j].classIdx) {
                    if (calculateIoU(sorted[i], sorted[j]) > iouThreshold) {
                        suppressed1[j] = true
                    }
                }
            }
        }

        // ── Pass 2: Cross-class NMS ──
        // If two boxes of DIFFERENT classes overlap significantly,
        // keep only the one with higher confidence. This prevents
        // multiple bounding boxes on a single physical wayang object.
        val crossSorted = perClassSelected.sortedByDescending { it.confidence }
        val finalSelected = mutableListOf<RawDetection>()
        val suppressed2 = BooleanArray(crossSorted.size)

        for (i in crossSorted.indices) {
            if (suppressed2[i]) continue
            finalSelected.add(crossSorted[i])

            for (j in i + 1 until crossSorted.size) {
                if (suppressed2[j]) continue
                if (calculateIoU(crossSorted[i], crossSorted[j]) > crossClassIouThreshold) {
                    suppressed2[j] = true
                }
            }
        }

        return finalSelected
    }

    /**
     * Calculate Intersection over Union between two bounding boxes.
     * Coordinates are normalized (0.0 to 1.0).
     */
    private fun calculateIoU(a: RawDetection, b: RawDetection): Float {
        val intersectLeft = maxOf(a.left, b.left)
        val intersectTop = maxOf(a.top, b.top)
        val intersectRight = minOf(a.right, b.right)
        val intersectBottom = minOf(a.bottom, b.bottom)

        val intersectWidth = maxOf(0f, intersectRight - intersectLeft)
        val intersectHeight = maxOf(0f, intersectBottom - intersectTop)
        val intersectArea = intersectWidth * intersectHeight

        val aArea = (a.right - a.left) * (a.bottom - a.top)
        val bArea = (b.right - b.left) * (b.bottom - b.top)
        val unionArea = aArea + bArea - intersectArea

        return if (unionArea > 0f) intersectArea / unionArea else 0f
    }
}
