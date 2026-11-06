package ph.edu.asiatech.goananas.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Small JPEG copies of photos stored inside the Firestore documents, so photos and profile pictures
 * reach the web dashboard and other phones even when Cloud Storage is not enabled (free plan).
 */
object Thumbs {
    private val cache = LruCache<String, Bitmap>(80)

    /** "data:image/jpeg;base64,…" text of a reduced copy of the file (longest side [maxSide] pixels) */
    fun fromFile(file: File, maxSide: Int = 360, quality: Int = 70): String? {
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
        val bmp = BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return null
        val scale = maxSide.toFloat() / maxOf(bmp.width, bmp.height)
        val small = if (scale < 1f) Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true) else bmp
        val out = ByteArrayOutputStream()
        small.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return "data:image/jpeg;base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    /** turns a data URI back into a bitmap (kept in a small memory cache) */
    fun decode(dataUri: String): Bitmap? {
        val key = dataUri.length.toString() + ":" + dataUri.hashCode()
        cache.get(key)?.let { return it }
        return runCatching {
            val bytes = Base64.decode(dataUri.substringAfter(","), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()?.also { cache.put(key, it) }
    }
}
