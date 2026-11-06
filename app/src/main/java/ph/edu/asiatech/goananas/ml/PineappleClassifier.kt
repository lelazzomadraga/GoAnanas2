package ph.edu.asiatech.goananas.ml

import android.content.Context
import android.graphics.Bitmap
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import ph.edu.asiatech.goananas.data.PineappleClass
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/** why a photograph was not accepted */
enum class Rejection { NOT_PINEAPPLE, UNCLEAR }

/** result of one diagnosis */
data class Diagnosis(
    /** probabilities of the six conditions in PineappleClass.modelOrder (they add up to 1) */
    val probabilities: List<Float>,
    val top: PineappleClass,
    val confidence: Float,
    val inferenceMs: Long,
    val qualityWarnings: List<String>,
    val enhancedPreview: Bitmap,
    /** probability that the photo does not show a pineapple */
    val notPineapple: Float,
    val rejection: Rejection?,
) {
    val lowConfidence: Boolean get() = confidence < LOW_CONFIDENCE
    val ranked: List<Pair<PineappleClass, Float>> get() = PineappleClass.modelOrder.zip(probabilities).sortedByDescending { it.second }

    companion object {
        const val LOW_CONFIDENCE = 0.60f
        /** below this, the result is too uncertain to be shown */
        const val MIN_CONFIDENCE = 0.40f
        /** minimum share of colored (plant-like) pixels; blank, gray, or document photos have much less */
        const val MIN_COLOR_RATIO = 0.06f
    }
}

data class ModelInfo(val version: String, val trained: Boolean, val testAccuracy: String, val classes: List<String>)

/**
 * Runs the MobileNetV2 model (assets/go_ananas_model.tflite) on the phone, without internet.
 * The model has seven outputs: the six conditions and "not_pineapple".
 */
class PineappleClassifier(private val context: Context) {
    private val interpreter: Interpreter by lazy { Interpreter(loadModel(), Interpreter.Options().apply { setNumThreads(4) }) }

    val labels: List<String> by lazy {
        context.assets.open(LABELS_FILE).bufferedReader().readLines().map { it.trim() }.filter { it.isNotEmpty() }
    }

    val info: ModelInfo by lazy {
        runCatching {
            val j = JSONObject(context.assets.open(INFO_FILE).bufferedReader().readText())
            ModelInfo(j.optString("version", "unknown"), j.optBoolean("trained", false), j.optString("test_accuracy", "—"), labels)
        }.getOrElse { ModelInfo("unknown", false, "—", labels) }
    }

    @Synchronized
    fun classify(bitmap: Bitmap): Diagnosis {
        val warnings = ImagePreprocessor.qualityWarnings(bitmap)
        val colorRatio = ImagePreprocessor.colorRatio(bitmap)
        val prepared = ImagePreprocessor.prepare(bitmap)
        val output = Array(1) { FloatArray(labels.size) }
        val start = System.nanoTime()
        interpreter.run(prepared.input, output)
        val ms = (System.nanoTime() - start) / 1_000_000

        val byLabel = labels.zip(output[0].toList()).toMap()
        val notPine = byLabel[PineappleClass.NOT_PINEAPPLE] ?: 0f
        val raw = PineappleClass.modelOrder.map { byLabel[it.label] ?: 0f }
        val sum = raw.sum().takeIf { it > 0f } ?: 1f
        val probs = raw.map { it / sum }                       // share among the six conditions
        val k = probs.indices.maxBy { probs[it] }
        val topRaw = raw[k]

        val rejection = when {
            colorRatio < Diagnosis.MIN_COLOR_RATIO -> Rejection.NOT_PINEAPPLE
            notPine > topRaw -> Rejection.NOT_PINEAPPLE
            info.trained && topRaw < Diagnosis.MIN_CONFIDENCE -> Rejection.UNCLEAR
            else -> null
        }
        return Diagnosis(probs, PineappleClass.modelOrder[k], topRaw.coerceIn(0f, 1f), ms, warnings, prepared.enhanced, notPine, rejection)
    }

    private fun loadModel(): MappedByteBuffer {
        val fd = context.assets.openFd(MODEL_FILE)
        FileInputStream(fd.fileDescriptor).use { stream ->
            return stream.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
        }
    }

    companion object {
        const val MODEL_FILE = "go_ananas_model.tflite"
        const val LABELS_FILE = "labels.txt"
        const val INFO_FILE = "model_info.json"
    }
}
