package tk.mallumo.ext

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfInt
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.image.DataBufferByte
import javax.imageio.ImageIO


fun ByteArray.toMat():Mat{
    val rgb = inputStream().use {
        ImageIO.read(it)
    }
    val pixels: ByteArray = (rgb.raster.dataBuffer as DataBufferByte).data
    return Mat(rgb.height, rgb.width, CvType.CV_8UC3).apply {
        put(0, 0, pixels)
    }
}

fun Mat.toBytesOfWebP(): ByteArray = MatOfByte().let {
    val dst = Mat()
    Imgproc.cvtColor(this, dst, Imgproc.COLOR_BGR2GRAY);
    Imgcodecs.imencode(".webp", dst, it, MatOfInt(Imgcodecs.IMWRITE_WEBP_QUALITY, 65,Imgcodecs.IMWRITE_PAM_FORMAT_GRAYSCALE_ALPHA))
        it.toArray().apply {
            it.release()
        }

}