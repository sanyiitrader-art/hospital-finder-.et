import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.awt.AlphaComposite
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Generates every required launcher-icon density from the root-level
 * icon.png automatically at build time — legacy raster mipmaps (square
 * + round-masked) for API levels that need them, plus a correctly
 * inset adaptive-icon foreground for API 26+. No manual per-density
 * asset creation is required; icon.png itself is never modified.
 *
 * Lives in buildSrc (rather than inline in app/build.gradle.kts) because
 * java.awt / javax.imageio do not reliably resolve inside a Gradle Kotlin
 * DSL script's sandboxed compilation — buildSrc compiles as ordinary
 * Kotlin source with the full JDK available.
 */
abstract class GenerateLauncherIconsTask : DefaultTask() {

    @get:InputFile
    abstract val sourceIcon: RegularFileProperty

    @get:OutputDirectory
    abstract val outputResDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val srcFile = sourceIcon.get().asFile
        if (!srcFile.exists()) {
            throw GradleException(
                "icon.png not found at project root (${srcFile.path}). " +
                "Place a single square PNG named icon.png in the repository root."
            )
        }
        val source = ImageIO.read(srcFile)
            ?: throw GradleException("Could not read icon.png — is it a valid PNG?")

        val outDir = outputResDir.get().asFile
        outDir.deleteRecursively()
        outDir.mkdirs()

        val legacySizes = mapOf(
            "mipmap-mdpi" to 48,
            "mipmap-hdpi" to 72,
            "mipmap-xhdpi" to 96,
            "mipmap-xxhdpi" to 144,
            "mipmap-xxxhdpi" to 192
        )
        legacySizes.forEach { (dir, size) ->
            val folder = File(outDir, dir).apply { mkdirs() }
            writeSquare(source, size, File(folder, "ic_launcher.png"))
            writeRound(source, size, File(folder, "ic_launcher_round.png"))
        }

        val adaptiveSizes = mapOf(
            "mipmap-mdpi" to 108,
            "mipmap-hdpi" to 162,
            "mipmap-xhdpi" to 216,
            "mipmap-xxhdpi" to 324,
            "mipmap-xxxhdpi" to 432
        )
        adaptiveSizes.forEach { (dir, size) ->
            val folder = File(outDir, dir).apply { mkdirs() }
            writeInsetForeground(source, size, File(folder, "ic_launcher_foreground.png"))
        }
    }

    private fun scaledSquareImage(source: BufferedImage, size: Int): BufferedImage {
        val out = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g: Graphics2D = out.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.drawImage(source, 0, 0, size, size, null)
        g.dispose()
        return out
    }

    private fun writeSquare(source: BufferedImage, size: Int, dest: File) {
        ImageIO.write(scaledSquareImage(source, size), "png", dest)
    }

    private fun writeRound(source: BufferedImage, size: Int, dest: File) {
        val square = scaledSquareImage(source, size)
        val round = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g: Graphics2D = round.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.clip = Ellipse2D.Float(0f, 0f, size.toFloat(), size.toFloat())
        g.drawImage(square, 0, 0, null)
        g.dispose()
        ImageIO.write(round, "png", dest)
    }

    private fun writeInsetForeground(source: BufferedImage, canvasSize: Int, dest: File) {
        val contentSize = (canvasSize * 0.66f).toInt()
        val offset = (canvasSize - contentSize) / 2
        val out = BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_ARGB)
        val g: Graphics2D = out.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.composite = AlphaComposite.SrcOver
        g.drawImage(source, offset, offset, contentSize, contentSize, null)
        g.dispose()
        ImageIO.write(out, "png", dest)
    }
}