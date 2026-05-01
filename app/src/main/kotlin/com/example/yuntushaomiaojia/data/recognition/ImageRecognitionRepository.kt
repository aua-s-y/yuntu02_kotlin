package com.example.yuntushaomiaojia.data.recognition

import android.graphics.Bitmap
import android.graphics.Color
import com.example.yuntushaomiaojia.data.tool.ToolRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.Locale

class ImageRecognitionRepository {

    fun recognizeImage(
        bitmap: Bitmap,
        action: String,
        onResult: (String) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            .process(image)
            .addOnSuccessListener { labels ->
                onResult(buildRecognitionResult(labels, action, bitmap))
            }
            .addOnFailureListener {
                onResult(buildRecognitionResult(emptyList(), action, bitmap))
            }
    }

    fun buildRecognitionResult(labels: List<ImageLabel>, action: String, bitmap: Bitmap): String {
        val candidates = inferRecognitionCandidates(bitmap, action).toMutableList()
        labels.forEach { label -> addLabelCandidates(candidates, label, action) }
        candidates.sortByDescending { candidate -> candidate.confidence }

        val best = candidates.firstOrNull() ?: RecognitionCandidate("目标物体", 30, "兜底判断")
        val builder = StringBuilder()
        builder.append("可能是“").append(best.answer).append("”")
        builder.append("\n参考可信度：").append(best.confidence).append("%")
        builder.append("\n判断依据：").append(best.reason)

        appendAlternativeCandidates(builder, candidates, best)
        appendLabelClues(builder, labels)
        builder.append("\n\n拍摄建议：让主体位于画面中央，并保持背景简洁。")
        return builder.toString()
    }

    private fun appendAlternativeCandidates(
        builder: StringBuilder,
        candidates: List<RecognitionCandidate>,
        best: RecognitionCandidate
    ) {
        if (candidates.size <= 1) {
            return
        }
        builder.append("\n\n备选结果：")
        var shown = 0
        for (index in 1 until candidates.size) {
            if (shown >= MAX_ALTERNATIVE_COUNT) {
                break
            }
            val candidate = candidates[index]
            if (candidate.answer == best.answer) {
                continue
            }
            builder.append("\n可能是“")
                .append(candidate.answer)
                .append("” ")
                .append(candidate.confidence)
                .append("%")
            shown++
        }
    }

    private fun appendLabelClues(builder: StringBuilder, labels: List<ImageLabel>) {
        if (labels.isEmpty()) {
            builder.append("\n\n已根据图片颜色和当前工具类型给出参考判断。")
            return
        }
        builder.append("\n\n识别线索：")
        labels.take(MAX_LABEL_CLUE_COUNT).forEach { label ->
            builder.append("\n")
                .append(label.text)
                .append(" ")
                .append(Math.round(label.confidence * 100))
                .append("%")
        }
    }

    private fun addLabelCandidates(
        candidates: MutableList<RecognitionCandidate>,
        imageLabel: ImageLabel,
        action: String
    ) {
        val label = imageLabel.text.lowercase(Locale.ROOT)
        val confidence = Math.round(imageLabel.confidence * 100)

        if (action == ToolRepository.PLANT_RECOGNITION) {
            addPlantLabelCandidate(candidates, label, imageLabel.text, confidence)
        }
        if (action == ToolRepository.FRUIT_RECOGNITION) {
            addFoodLabelCandidate(candidates, label, imageLabel.text, confidence)
        }
        if (action == ToolRepository.ANIMAL_RECOGNITION) {
            addAnimalLabelCandidate(candidates, label, imageLabel.text, confidence)
        }
    }

    private fun addPlantLabelCandidate(
        candidates: MutableList<RecognitionCandidate>,
        label: String,
        originalLabel: String,
        confidence: Int
    ) {
        if (label.containsAny("rose")) addCandidate(candidates, "玫瑰", confidence + 10, "画面特征：$originalLabel")
        if (label.containsAny("sunflower")) addCandidate(candidates, "向日葵", confidence + 10, "画面特征：$originalLabel")
        if (label.containsAny("orchid")) addCandidate(candidates, "兰花", confidence + 10, "画面特征：$originalLabel")
        if (label.containsAny("daisy", "tulip", "lotus", "flower", "petal")) {
            addCandidate(candidates, "花卉", confidence + 4, "画面特征：$originalLabel")
        }
        if (label.containsAny("cactus")) addCandidate(candidates, "仙人掌", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("succulent")) addCandidate(candidates, "多肉植物", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("tree", "trunk", "forest", "wood")) {
            addCandidate(candidates, "树木", confidence + 2, "画面特征：$originalLabel")
        }
        if (label.containsAny("leaf", "plant", "vegetation", "grass", "shrub", "houseplant", "plant stem", "botany")) {
            addCandidate(candidates, "绿植", confidence, "画面特征：$originalLabel")
        }
    }

    private fun addFoodLabelCandidate(
        candidates: MutableList<RecognitionCandidate>,
        label: String,
        originalLabel: String,
        confidence: Int
    ) {
        if (label.containsAny("apple")) addCandidate(candidates, "苹果", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("banana")) addCandidate(candidates, "香蕉", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("orange", "citrus", "tangerine")) addCandidate(candidates, "橙子", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("lemon")) addCandidate(candidates, "柠檬", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("grape")) addCandidate(candidates, "葡萄", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("strawberry")) addCandidate(candidates, "草莓", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("tomato")) addCandidate(candidates, "番茄", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("pineapple")) addCandidate(candidates, "菠萝", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("watermelon")) addCandidate(candidates, "西瓜", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("pear")) addCandidate(candidates, "梨", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("peach")) addCandidate(candidates, "桃子", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("mango")) addCandidate(candidates, "芒果", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("kiwi")) addCandidate(candidates, "猕猴桃", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("carrot")) addCandidate(candidates, "胡萝卜", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("corn")) addCandidate(candidates, "玉米", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("cucumber")) addCandidate(candidates, "黄瓜", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("eggplant")) addCandidate(candidates, "茄子", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("broccoli")) addCandidate(candidates, "西兰花", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("cabbage", "lettuce")) addCandidate(candidates, "叶菜", confidence + 5, "画面特征：$originalLabel")
        if (label.containsAny("fruit", "produce", "natural foods")) addCandidate(candidates, "水果", confidence - 8, "画面特征：$originalLabel")
        if (label.containsAny("vegetable")) addCandidate(candidates, "蔬菜", confidence - 5, "画面特征：$originalLabel")
        if (label.containsAny("food", "cuisine", "dish")) addCandidate(candidates, "果蔬或食物", confidence - 15, "画面特征：$originalLabel")
    }

    private fun addAnimalLabelCandidate(
        candidates: MutableList<RecognitionCandidate>,
        label: String,
        originalLabel: String,
        confidence: Int
    ) {
        if (label.containsAny("cat", "kitten")) addCandidate(candidates, "猫", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("dog", "puppy")) addCandidate(candidates, "狗", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("bird", "beak", "feather")) addCandidate(candidates, "鸟", confidence + 5, "画面特征：$originalLabel")
        if (label.containsAny("fish")) addCandidate(candidates, "鱼", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("horse")) addCandidate(candidates, "马", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("cow", "cattle", "bull", "ox")) addCandidate(candidates, "牛", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("sheep", "goat")) addCandidate(candidates, "羊", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("rabbit", "hare")) addCandidate(candidates, "兔子", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("panda")) addCandidate(candidates, "熊猫", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("bear")) addCandidate(candidates, "熊", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("tiger")) addCandidate(candidates, "老虎", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("lion")) addCandidate(candidates, "狮子", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("elephant")) addCandidate(candidates, "大象", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("monkey", "ape", "primate")) addCandidate(candidates, "猴子", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("deer")) addCandidate(candidates, "鹿", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("duck", "goose", "swan")) addCandidate(candidates, "水鸟", confidence + 6, "画面特征：$originalLabel")
        if (label.containsAny("butterfly")) addCandidate(candidates, "蝴蝶", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("spider")) addCandidate(candidates, "蜘蛛", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("snake")) addCandidate(candidates, "蛇", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("turtle", "tortoise")) addCandidate(candidates, "龟", confidence + 8, "画面特征：$originalLabel")
        if (label.containsAny("insect", "arthropod")) addCandidate(candidates, "昆虫", confidence + 3, "画面特征：$originalLabel")
        if (label.containsAny("animal", "mammal", "pet", "wildlife")) addCandidate(candidates, "动物", confidence - 8, "画面特征：$originalLabel")
    }

    private fun inferRecognitionCandidates(bitmap: Bitmap, action: String): List<RecognitionCandidate> {
        val candidates = mutableListOf<RecognitionCandidate>()
        val profile = analyzeColorProfile(bitmap)

        if (action == ToolRepository.PLANT_RECOGNITION) {
            addCandidate(candidates, "植物", 36, "当前工具类型")
            if (profile.greenRatio > 0.28f) addCandidate(candidates, "绿植", 58, "画面绿色占比较高")
            if (profile.redRatio + profile.pinkRatio + profile.purpleRatio + profile.yellowRatio > 0.16f) {
                addCandidate(candidates, "花卉", 55, "画面有明显花朵常见颜色")
            }
            if (profile.brownRatio > 0.16f && profile.greenRatio > 0.12f) {
                addCandidate(candidates, "树木", 50, "画面同时有树干色和绿色")
            }
            return candidates
        }

        if (action == ToolRepository.FRUIT_RECOGNITION) {
            addCandidate(candidates, "水果", 36, "当前工具类型")
            if (profile.redRatio > 0.18f) {
                addCandidate(candidates, "苹果", 56, "画面红色占比较高")
                addCandidate(candidates, "番茄", 50, "画面红色占比较高")
            }
            if (profile.yellowRatio > 0.16f) {
                addCandidate(candidates, "香蕉", 56, "画面黄色占比较高")
                addCandidate(candidates, "柠檬", 50, "画面黄色占比较高")
            }
            if (profile.orangeRatio > 0.14f) {
                addCandidate(candidates, "橙子", 57, "画面橙色占比较高")
                addCandidate(candidates, "胡萝卜", 49, "画面橙色占比较高")
            }
            if (profile.greenRatio > 0.24f) {
                addCandidate(candidates, "青苹果", 51, "画面绿色占比较高")
                addCandidate(candidates, "黄瓜或叶菜", 47, "画面绿色占比较高")
            }
            if (profile.purpleRatio > 0.10f) {
                addCandidate(candidates, "葡萄", 55, "画面紫色占比较高")
                addCandidate(candidates, "茄子", 49, "画面紫色占比较高")
            }
            return candidates
        }

        if (action == ToolRepository.ANIMAL_RECOGNITION) {
            addCandidate(candidates, "动物", 38, "当前工具类型")
            if (profile.whiteRatio > 0.28f && profile.brownRatio > 0.08f) {
                addCandidate(candidates, "猫或兔子", 42, "画面浅色毛发占比较高")
            }
            if (profile.darkRatio > 0.30f || profile.brownRatio > 0.22f) {
                addCandidate(candidates, "狗或深色动物", 42, "画面深色/棕色毛发占比较高")
            }
        }
        return candidates
    }

    private fun analyzeColorProfile(bitmap: Bitmap): ColorProfile {
        val stepX = maxOf(1, bitmap.width / SAMPLE_SIZE)
        val stepY = maxOf(1, bitmap.height / SAMPLE_SIZE)
        var total = 0
        var redSum = 0
        var greenSum = 0
        var blueSum = 0
        var redCount = 0
        var orangeCount = 0
        var yellowCount = 0
        var greenCount = 0
        var purpleCount = 0
        var pinkCount = 0
        var brownCount = 0
        var whiteCount = 0
        var darkCount = 0

        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val color = bitmap.getPixel(x, y)
                val red = Color.red(color)
                val green = Color.green(color)
                val blue = Color.blue(color)
                redSum += red
                greenSum += green
                blueSum += blue
                total++

                if (red > 150 && green < 110 && blue < 110) redCount++
                if (red > 170 && green in 70..170 && blue < 90) orangeCount++
                if (red > 170 && green > 150 && blue < 110) yellowCount++
                if (green > red * 1.15f && green > blue * 1.15f && green > 70) greenCount++
                if (blue > 110 && red > 90 && green < 120) purpleCount++
                if (red > 180 && blue > 130 && green < 150) pinkCount++
                if (red in 75..170 && green in 45..130 && blue < 100 && red > blue) brownCount++
                if (red > 210 && green > 210 && blue > 210) whiteCount++
                if (red < 70 && green < 70 && blue < 70) darkCount++
                x += stepX
            }
            y += stepY
        }

        if (total == 0) {
            return ColorProfile()
        }
        return ColorProfile(
            redRatio = redCount.ratio(total),
            orangeRatio = orangeCount.ratio(total),
            yellowRatio = yellowCount.ratio(total),
            greenRatio = greenCount.ratio(total),
            purpleRatio = purpleCount.ratio(total),
            pinkRatio = pinkCount.ratio(total),
            brownRatio = brownCount.ratio(total),
            whiteRatio = whiteCount.ratio(total),
            darkRatio = darkCount.ratio(total)
        )
    }

    private fun addCandidate(
        candidates: MutableList<RecognitionCandidate>,
        answer: String,
        confidence: Int,
        reason: String
    ) {
        val safeConfidence = confidence.coerceIn(1, 99)
        val existing = candidates.firstOrNull { candidate -> candidate.answer == answer }
        if (existing != null) {
            if (safeConfidence > existing.confidence) {
                existing.confidence = safeConfidence
                existing.reason = reason
            }
            return
        }
        candidates.add(RecognitionCandidate(answer, safeConfidence, reason))
    }

    private fun String.containsAny(vararg keys: String): Boolean {
        return keys.any { key -> contains(key) }
    }

    private fun Int.ratio(total: Int): Float {
        return if (total <= 0) 0f else this / total.toFloat()
    }

    private data class RecognitionCandidate(
        val answer: String,
        var confidence: Int,
        var reason: String
    )

    private data class ColorProfile(
        val redRatio: Float = 0f,
        val orangeRatio: Float = 0f,
        val yellowRatio: Float = 0f,
        val greenRatio: Float = 0f,
        val purpleRatio: Float = 0f,
        val pinkRatio: Float = 0f,
        val brownRatio: Float = 0f,
        val whiteRatio: Float = 0f,
        val darkRatio: Float = 0f
    )

    companion object {
        private const val SAMPLE_SIZE = 96
        private const val MAX_ALTERNATIVE_COUNT = 3
        private const val MAX_LABEL_CLUE_COUNT = 5
    }
}
