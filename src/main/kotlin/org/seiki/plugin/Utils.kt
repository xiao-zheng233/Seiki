@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package org.seiki.plugin

import com.huaban.analysis.jieba.JiebaSegmenter
import com.huaban.analysis.jieba.WordDictionary
import kotlinx.coroutines.TimeoutCancellationException
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.jetbrains.skia.*
import org.laolittle.plugin.getBytes
import org.laolittle.plugin.gif.GifImage
import org.laolittle.plugin.gif.GifSetting
import org.laolittle.plugin.gif.buildGifImage
import org.seiki.SweetBoy
import org.seiki.plugin.SkikoUtil.bar
import org.seiki.plugin.SkikoUtil.makeFromResource
import org.seiki.plugin.UnvcodeUtil.MathUtil.minIndex
import org.seiki.plugin.UnvcodeUtil.MathUtil.variance
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import java.text.Normalizer
import kotlin.math.pow
import org.jetbrains.skia.Image as SkiaImage
import org.jetbrains.skia.Canvas as SkiaCanvas
import org.jetbrains.skia.Color as SkiaColor
import org.jetbrains.skia.Paint as SkiaPaint
import org.jetbrains.skia.Point as SkiaPoint
import java.awt.Color as AwtColor
import java.awt.Font as AwtFont

val ownerList = arrayListOf(2630557998L, 1812691029L)

val biliVideoRegex =
    """[\s\S]*?(?:(?:https?://)?(?:www\.)?bilibili\.com/video/)?([aA][vV]\d+|[bB][vV][a-zA-Z\d]+)[\s\S]*?""".toRegex()
val bili23tvRegex = """[\s\S]*?((?:https?://)?(?:www\.)?b23\.tv/[a-zA-Z\d]+)[\s\S]*?""".toRegex()
val biliUserRegex = """[\s\S]*?(?:https?://)?(?:space\.bilibili\.com|bilibili\.com/space)/(\d+)[\s\S]*?""".toRegex()

val MemberPermission.levelName: String
    get() = when (this) {
        MemberPermission.MEMBER -> "??????"
        MemberPermission.ADMINISTRATOR -> "?????????"
        MemberPermission.OWNER -> "??????"
    }

val User.name: String get() = "${this.nameCardOrNick}(${this.id})"

suspend fun Contact.uploadAsImage(url: String) =
    SweetBoy.getStream(url).use { it.uploadAsImage(this@uploadAsImage) }

suspend fun Contact.uploadAsImage(file: File) =
    file.uploadAsImage(this@uploadAsImage)

suspend fun Contact.uploadAsAudio(url: String) =
    SweetBoy.getStream(url).toExternalResource().use { (this@uploadAsAudio as AudioSupported).uploadAudio(it) }

suspend fun Contact.uploadAsAudio(file: File) =
    file.toExternalResource().use { (this@uploadAsAudio as AudioSupported).uploadAudio(it) }

fun String.convert(max: Int = 200): ArrayList<String> {
    var num = 0
    var len = 0
    var str = ""
    val list: ArrayList<String> = arrayListOf()
    this.split("\n").forEach {
        num += it.length
        len += it.length
        if (num < max) str += it + "\n" else {
            list.add(str)
            str = ""
            num = 0
        }
    }
    if (len < max) list.add(str)
    return list
}

fun Throwable.buildMessage() = buildMessageChain {
    +PlainText("Warning! ${this@buildMessage}\n")
    if (this@buildMessage.cause != null) +PlainText("Caused by: ${this@buildMessage.cause}")
    +Image("{D3A4F304-847D-BB7B-1534-8ABFDC7575B4}.png")
}
suspend fun <T : Contact, R> T.runCatching(block: suspend T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        e.buildMessage().sendTo(this)
        Result.failure<R>(e).also { throw e }
    }
}


/**
 * @author LaoLittle?????????
 */
suspend fun MessageEvent.getOrWaitImage(): Image? =
    (message.takeIf { m -> m.contains(Image) } ?: runCatching {
        subject.sendMessage("??????30??????????????????...")
        nextMessage(30_000) { event -> event.message.contains(Image) }
    }.getOrElse { e ->
        when (e) {
            is TimeoutCancellationException -> {
                messageChainOf(PlainText("???????????????!"), message.quote()).sendTo(subject)
                return null
            }
            else -> throw e
        }
    }).firstIsInstanceOrNull<Image>()

suspend fun MessageEvent.getOrWait(): MessageChain? =
    runCatching {
        this@getOrWait.nextMessage(30_000)
    }.getOrElse {
        when (it) {
            is TimeoutCancellationException -> {
                messageChainOf(PlainText("???????????????!"), message.quote()).sendTo(subject)
                return null
            }
            else -> throw it
        }
    }

@Deprecated("?????????", level = DeprecationLevel.WARNING)
val String.consolas: String
    get() {
        val hash: HashMap<String, String> = hashMapOf(
            Pair("0", "????"),
            Pair("1", "????"),
            Pair("2", "????"),
            Pair("3", "????"),
            Pair("4", "????"),
            Pair("5", "????"),
            Pair("6", "????"),
            Pair("7", "????"),
            Pair("8", "????"),
            Pair("9", "????"),
            Pair("a", "????"),
            Pair("b", "????"),
            Pair("c", "????"),
            Pair("d", "????"),
            Pair("e", "????"),
            Pair("f", "????"),
            Pair("g", "????"),
            Pair("h", "????"),
            Pair("i", "????"),
            Pair("j", "????"),
            Pair("k", "????"),
            Pair("l", "????"),
            Pair("m", "????"),
            Pair("n", "????"),
            Pair("o", "????"),
            Pair("p", "????"),
            Pair("q", "????"),
            Pair("r", "????"),
            Pair("s", "????"),
            Pair("t", "????"),
            Pair("u", "????"),
            Pair("v", "????"),
            Pair("w", "????"),
            Pair("x", "????"),
            Pair("y", "????"),
            Pair("z", "????"),
            Pair("A", "????"),
            Pair("B", "????"),
            Pair("C", "????"),
            Pair("D", "????"),
            Pair("E", "????"),
            Pair("F", "????"),
            Pair("G", "????"),
            Pair("H", "????"),
            Pair("I", "????"),
            Pair("J", "????"),
            Pair("K", "????"),
            Pair("L", "????"),
            Pair("M", "????"),
            Pair("N", "????"),
            Pair("O", "????"),
            Pair("P", "????"),
            Pair("Q", "????"),
            Pair("R", "????"),
            Pair("S", "????"),
            Pair("T", "????"),
            Pair("U", "????"),
            Pair("V", "????"),
            Pair("W", "????"),
            Pair("X", "????"),
            Pair("Y", "????"),
            Pair("Z", "????")
        )
        var str = ""
        this.forEach {
            str += if (it.toString() in hash.keys) hash[it.toString()] else it.toString()
        }
        return str
    }

/**
 * @author LaoLittle?????????
 */
object YinglishUtil {
    val String.yinglish get() = this.chs2yin(100)
    fun String.chs2yin(yingLevel: Int): String {
        val b = JiebaSegmenter().process(this, JiebaSegmenter.SegMode.SEARCH)
        var yinglish = ""
        b.forEach { keyWord ->
            val part = WordDictionary.getInstance().parts[keyWord.word]
            val chars = keyWord.word.toCharArray()
            yinglish += getYinglishNode(chars, part, yingLevel)
        }
        return yinglish
    }

    private fun getYinglishNode(chars: CharArray, part: String?, yingLevel: Int): String {
        val randomOneTen = { (1..100).random() }
        var pon = ""
        if (randomOneTen() > yingLevel)
            return String(chars)
        when (chars[0].toString()) {
            in arrayOf("!", "???", "???", "?", "???") -> return "???"
            in arrayOf(",", "???", "???") -> return "..."
        }
        if (chars.size > 1 && randomOneTen() > 50)
            return "${chars[0]}???...${String(chars)}"
        else if (part == "n" && randomOneTen() > 50) {
            repeat(chars.count()) { pon += "???" }
            return pon
        }
        return "...${String(chars)}"
    }
}

/**
 * @author LaoLittle?????????
 */
object SkikoUtil {
    fun SkiaCanvas.bar(block: SkiaCanvas.() -> Unit) {
        save()
        block()
        restore()
    }

    fun Rect.copy(
        left: Float = this.left,
        top: Float = this.top,
        right: Float = this.right,
        bottom: Float = this.bottom
    ) = Rect(left, top, right, bottom)

    internal fun SkiaImage.Companion.makeFromResource(name: String) = makeFromEncoded(
        SeikiMain::class.java.getResourceAsStream(name)?.readBytes() ?: throw IllegalStateException("????????????????????????: $name")
    )
}

/**
 * @author LaoLittle?????????
 */
object PatPatUtil {
    private const val width = 320
    private const val height = 320

    suspend fun patpat(image: SkiaImage, delay: Double = .05): GifImage {
        return buildGifImage(GifSetting(width, height, 100, true, GifSetting.Repeat.Infinite)) {
            addFrame(pat(Rect(40f, 40f, 300f, 300f), SkiaPoint(0f, 0f), image, 0).getBytes(), delay)
            addFrame(pat(Rect(40f, 70f, 300f, 300f), SkiaPoint(0f, 0f), image, 1).getBytes(), delay)
            addFrame(pat(Rect(33f, 105f, 300f, 300f), SkiaPoint(0f, 0f), image, 2).getBytes(), delay)
            addFrame(pat(Rect(37f, 90f, 300f, 300f), SkiaPoint(0f, 0f), image, 3).getBytes(), delay)
            addFrame(pat(Rect(40f, 65f, 300f, 300f), SkiaPoint(0f, 0f), image, 4).getBytes(), delay)
        }
    }

    private val whitePaint = SkiaPaint().apply { color = SkiaColor.WHITE }
    private val srcInPaint = SkiaPaint().apply { blendMode = BlendMode.SRC_IN }
    private val hands = Array(5) { SkiaImage.makeFromResource("/data/PatPat/img$it.png") }

    private const val imgW = width.toFloat()
    private const val imgH = height.toFloat()
    fun pat(imgDst: Rect, handPoint: SkiaPoint, image: SkiaImage, no: Int): SkiaImage {
        val hand = hands[no]
        return Surface.makeRasterN32Premul(width, height).apply {
            canvas.apply {
                bar {
                    val radius = (width shr 1).toFloat()
                    translate(imgDst.left, imgDst.top)
                    scale(imgDst.width / width, imgDst.height / height)
                    drawCircle(imgW * .5f, imgH * .5f, radius, whitePaint)
                    drawImageRect(
                        image,
                        Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
                        Rect.makeWH(imgW, imgH),
                        FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
                        srcInPaint,
                        true
                    )
                }
                drawImageRect(
                    hand,
                    Rect.makeWH(hand.width.toFloat(), hand.height.toFloat()),
                    Rect(handPoint.x, handPoint.y, handPoint.x + width, handPoint.y + height),
                    SamplingMode.CATMULL_ROM,
                    null,
                    true
                )
            }
        }.makeImageSnapshot()
    }
}

object UnvcodeUtil {
    object MathUtil {
        fun List<Int>.sum(): Int {
            var sum = 0
            this.forEach { sum += it }
            return sum
        }

        fun List<Int>.average(): Double = this.sum().toDouble() / this.size

        fun List<Int>.variance(): Double {
            val average = this.average()
            var variance = 0.0
            this.forEach {
                variance += ((it - average).pow(2.0))
            }
            return variance / this.size
        }

        infix fun List<Int>.minus(other: List<Int>): List<Int> {
            if (this.size != other.size) throw Throwable("?!")
            return mutableListOf<Int>().apply {
                for (i in 0..this@minus.lastIndex) {
                    add(this@minus[i] - other[i])
                }
            }
        }

        fun List<Double>.minIndex(): Int {
            var minIndex = 0
            for (i in 1..this.lastIndex) {
                if (this[i] < this[minIndex])minIndex=i
            }
            return minIndex
        }
    }

    private val d = mutableMapOf<Char, MutableList<Char>>()

    init {
        for (i in 0 until 65536) {
            val word = i.toChar()
            val key = Normalizer.normalize(word.toString(), Normalizer.Form.NFKC).toCharArray()[0]
            if (word != key) {
                d.putIfAbsent(key, mutableListOf())
                d[key]?.add(word)
            }
        }
    }

    private fun draw(key: Char): List<Int> {
        var img = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
        val fnt = AwtFont(null, AwtFont.PLAIN, 100)
        var g2d = img.createGraphics()
        g2d.font = fnt
        val fm = g2d.fontMetrics
        val width = fm.stringWidth(key.toString())
        val height = fm.height
        g2d.dispose()
        img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        g2d = img.createGraphics()
        g2d.color = AwtColor.BLACK
        g2d.fillRect(0, 0, img.width, img.height)
        g2d.color = AwtColor.WHITE
        g2d.font = fnt
        g2d.drawString(key.toString(), 0, fm.ascent)
        g2d.dispose()
        //ImageIO.write(img, "png", File("/Users/lz233/Desktop/1.png"))
        return mutableListOf<Int>().apply {
            (img.raster.dataBuffer as DataBufferInt).data.forEach {
                val color = AwtColor(it)
                add(color.red / 255)
                add(color.green / 255)
                add(color.blue / 255)
            }
        }
    }

    private fun compare(key1: Char, key2: Char) = (draw(key1) - draw(key2).toSet()).variance()

    private fun masquerade(key: Char, skipAscii: Boolean, mse: Double = 0.1): Pair<Double, Char> {
        if ((key.code < 128) and skipAscii) return (-1.0 to key)
        val candidateGroup = d[key] ?: return (-1.0 to key)
        val differenceGroup = mutableListOf<Double>().apply {
            candidateGroup.forEach { add(compare(key, it)) }
        }
        val difference = differenceGroup.minOrNull()!!
        val new = candidateGroup[differenceGroup.minIndex()]
        return if (difference > mse) (-1.0 to key) else (difference to new)
    }

    fun convert(s: String, skipAscii: Boolean = true, mse: Double = 0.1): Pair<String, List<Double>> {
        val differenceList = mutableListOf<Double>()
        val str = StringBuilder().apply {
            s.toCharArray().forEach {
                val result = masquerade(it, skipAscii, mse)
                differenceList.add(result.first)
                append(result.second)
            }
        }.toString()
        return (str to differenceList)
    }

    fun String.unvcode(skipAscii: Boolean = true, mse: Double = 0.1) = convert(this, skipAscii, mse)

    val String.unvcode get() = this.unvcode().first
}
