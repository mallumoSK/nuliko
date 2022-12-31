package tk.mallumo

import org.opencv.core.Core
import tk.mallumo.io.Repository
import java.io.File
import kotlin.time.ExperimentalTime


val isDebug get() = File("/home/one/apps/").exists()
/*

https://github.com/03/ONVIF-Java

*/

/*
 java -jar ./nuliko-rpi-1.0.0-all.jar \
--backup-dir "/tmp/backup" \
--backup-days 1 \
--cam-auth-pass "Oscadnica993"



cmake -D CMAKE_BUILD_TYPE=RELEASE -DBUILD_SHARED_LIBS=ON -D CMAKE_INSTALL_PREFIX=/usr/local -D ENABLE_NEON=OFF -D ENABLE_VFPV3=OFF -D WITH_OPENMP=OFF -D WITH_OPENCL=OFF -D BUILD_ZLIB=ON -D BUILD_TIFF=ON -D WITH_FFMPEG=ON -D WITH_TBB=ON -D BUILD_TBB=ON -D BUILD_TESTS=OFF -D WITH_EIGEN=ON -D WITH_GSTREAMER=ON -D WITH_V4L=ON -D WITH_LIBV4L=ON -D WITH_VTK=OFF -D WITH_QT=OFF -D OPENCV_ENABLE_NONFREE=ON -D INSTALL_C_EXAMPLES=OFF -D INSTALL_PYTHON_EXAMPLES=OFF -D OPENCV_GENERATE_PKGCONFIG=OFF -D BUILD_EXAMPLES=ON ..

 */
@ExperimentalTime
fun main(args: Array<String>) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    GlobalParams.init(args)

    if (!isDebug) Repository.onvif.runCam1()
    Repository.onvif.runCam2()
    Repository.direct.run()

    stopUntilExit()

    Repository.close()
}

private fun stopUntilExit() {
    System.`in`.bufferedReader().use {
        while (true) {
            if (it.readLine()?.trim() == "x") break
        }
    }
}
