package com.devmastercrack.finia.presentation.finia.util

import com.devmastercrack.finia.presentation.finia.model.CategoryConfidence
import com.devmastercrack.finia.presentation.finia.model.CategoryKeywords

data class CategoryDetection(val category: String, val confidence: CategoryConfidence)

/** Keyword-scored, live category autodetection — mirrors `detectCategory()` in Finia.dc.html. */
fun detectCategory(text: String): CategoryDetection? {
    val t = text.lowercase().trim()
    if (t.isEmpty()) return null
    val words = t.split(Regex("\\s+"))
    var best: String? = null
    var bestScore = 0
    for ((cat, keywords) in CategoryKeywords) {
        var score = 0
        for (kw in keywords) if (t.contains(kw)) score += if (kw.length >= 5) 2 else 1
        if (score > bestScore) {
            bestScore = score
            best = cat
        }
    }
    val category = best ?: return null
    val confidence = if (bestScore >= 2 || words.size <= 3) CategoryConfidence.HIGH else CategoryConfidence.LOW
    return CategoryDetection(category, confidence)
}
