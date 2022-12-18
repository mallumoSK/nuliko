@file:Repository("https://jitpack.io")
@file:Repository("https://maven.google.com")
@file:Repository("https://jetbrains.bintray.com/trove4j")
@file:Repository("https://repo1.maven.org/maven2/")

@file:DependsOn("com.github.DevSrSouza:svg-to-compose:-SNAPSHOT")
@file:DependsOn("com.google.guava:guava:23.0")
@file:DependsOn("com.android.tools:sdk-common:27.2.0-alpha16")
@file:DependsOn("com.android.tools:common:27.2.0-alpha16")
@file:DependsOn("com.squareup:kotlinpoet:1.7.2")
@file:DependsOn("org.ogce:xpp3:1.1.6")

import br.com.devsrsouza.svg2compose.Svg2Compose
import br.com.devsrsouza.svg2compose.VectorType
import java.io.File

private val projectRoot = File(File("").absolutePath).parentFile.absolutePath

ComposeAssets(
    inputLocationDir = File("$projectRoot/sampledata/svg"),
    outputLocationDir = File(projectRoot),
).svgConverter()

class ComposeAssets(
    var inputLocationDir: File,
    var outputLocationDir: File,
    var outputPackage: String = "compose.assets",
    var outputSvgAccessorName: String = "Svg",
    var outputStringsAccessorName: String = "Strings",
    var allAssetsPropertyName: String = "All",
    var svgNameTransformFunction: ((name: String, group: String) -> String) = defaultSvgNameTransformFunction)  {
    
    fun svgConverter() {
        val outputFile = outputLocationDir
            .takeIf { it.exists() }
            ?.let {
                File("${it.absolutePath}/src/main/kotlin")
            } ?: throw NullPointerException("property of outputLocationDir must be defined")

        val inputDir = inputLocationDir
            .takeIf { it.exists() }
            ?: throw NullPointerException("property of compose_assets.svgLocationDir must be defined")

        Svg2Compose.parse(
            applicationIconPackage = outputPackage,
            accessorName = outputSvgAccessorName,
            outputSourceDirectory = outputFile,
            vectorsDirectory = inputDir,
            type = VectorType.SVG,
            allAssetsPropertyName = allAssetsPropertyName,
            iconNameTransformer = svgNameTransformFunction
        )
        println("Svg input:\n ${inputDir.absolutePath}")
        println("Svg output:")
        println(File(outputFile, "${outputPackage.replace(".", "/")}/${outputSvgAccessorName.toLowerCase()}").absolutePath)
        File(outputFile, "${outputPackage.replace(".", "/")}/${outputSvgAccessorName.toLowerCase()}")
            .listFiles()
            ?.filter { it.name.endsWith(".kt") }
            ?.forEach {
                val text = it.readText()
                    .replace("import androidx.compose.ui.graphics.PathFillType.NonZero", "import androidx.compose.ui.graphics.PathFillType.Companion.NonZero")
                    .replace("import androidx.compose.ui.graphics.PathFillType.EvenOdd", "import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd")
                    .replace("import androidx.compose.ui.graphics.StrokeCap.Butt", "import androidx.compose.ui.graphics.StrokeCap.Companion.Butt")
                    .replace("import androidx.compose.ui.graphics.StrokeCap.Round", "import androidx.compose.ui.graphics.StrokeCap.Companion.Round")
                    .replace("import androidx.compose.ui.graphics.StrokeCap.Square", "import androidx.compose.ui.graphics.StrokeCap.Companion.Square")
                    .replace("import androidx.compose.ui.graphics.StrokeJoin.Miter", "import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter")
                    .replace("import androidx.compose.ui.graphics.StrokeJoin.Round", "import androidx.compose.ui.graphics.StrokeJoin.Companion.Round")
                    .replace("import androidx.compose.ui.graphics.StrokeJoin.Bevel", "import androidx.compose.ui.graphics.StrokeJoin.Companion.Bevel")
                it.writeText(text)

            }
    }

    companion object {

        private val defaultSvgNameTransformFunction: ((String, String) -> String) = { name, group ->
            name.removeSuffix(group, ignoreCase = true)
        }

        fun String.removeSuffix(suffix: String, ignoreCase: Boolean): String {
            if (ignoreCase) {
                val index = lastIndexOf(suffix, ignoreCase = true)

                return with(if (index > -1) substring(0, index) else this) {
                    replace("-", "_")
                        .replace("Black24dp", "")
                        .replace("24dp", "")
                        .replace("24px", "")
                        .replace("_.", ".")
                        .dropWhile { it == '_' }
                        .dropLastWhile { it == '_' }

                }
            } else {
                return removeSuffix(suffix)
            }
        }
    }
}
