package com.example.wayang_detection.detector

/**
 * Non-Maximum Suppression (NMS) for filtering overlapping detections.
 * Uses greedy per-class NMS with IoU (Intersection over Union) filtering.
 */
object NmsProcessor {

    /**
     * Apply Non-Maximum Suppression to filter overlapping detections.
     * @param detections Raw detections from model output.
     * @param iouThreshold IoU threshold — boxes with IoU above this are suppressed.
     * @return Filtered list of non-overlapping detections.
     */
    fun nms(
        detections: List<RawDetection>,
        iouThreshold: Float
    ): List<RawDetection> {
        if (detections.isEmpty()) return emptyList()

        // Sort by confidence descending
        val sorted = detections.sortedByDescending { it.confidence }
        val selected = mutableListOf<RawDetection>()
        val suppressed = BooleanArray(sorted.size)

        for (i in sorted.indices) {
            if (suppressed[i]) continue
            selected.add(sorted[i])

            for (j in i + 1 until sorted.size) {
                if (suppressed[j]) continue
                // Only suppress same-class overlapping boxes
                if (sorted[i].classIdx == sorted[j].classIdx) {
                    if (calculateIoU(sorted[i], sorted[j]) > iouThreshold) {
                        suppressed[j] = true
                    }
                }
            }
        }

        return selected
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
