### SETUP ENV

```shell

unzip /opt/opencv/4.7.0.zip

sudo apt install default-jdk
sudo apt install cmake ant zip unzip ffmpeg screen

sudo apt install  cmake ant zip unzip ffmpeg screen build-essential pkg-config \
  libjpeg-dev libtiff5-dev  libpng-dev \
  libavcodec-dev libavformat-dev libswscale-dev libv4l-dev \
  libxvidcore-dev libx264-dev \
  libfontconfig1-dev libcairo2-dev \
  libgdk-pixbuf2.0-dev libpango1.0-dev \
  libgtk2.0-dev libgtk-3-dev \
  libatlas-base-dev gfortran \
  libhdf5-dev libhdf5-serial-dev libhdf5-103 \
  libqt5gui5 libqt5webkit5
  
nano ./.bashrc
JAVA_HOME=/usr/lib/jvm/default-java
ANT_HOME=/usr/share/ant
PATH="$PATH:$JAVA_HOME/bin:$ANT_HOME/bin"
source ./.bashrc

wget https://github.com/opencv/opencv/archive/refs/tags/4.7.0.zip
unzip opencv-4.7.0.zip
rm  Desktop/opencv-4.7.0.zip

cd opencv-4.7.0 &&\
  mkdir build && \
  cd build
cd .. && \
  rm -r build && \
  mkdir build && \
  cd build 
  
cmake -D CMAKE_BUILD_TYPE=RELEASE \
  -D BUILD_SHARED_LIBS=ON \
  -D CMAKE_INSTALL_PREFIX=/usr/local \
  -D ENABLE_NEON=OFF \
  -D ENABLE_VFPV3=OFF \
  -D WITH_OPENMP=OFF \
  -D WITH_OPENCL=OFF \
  -D BUILD_ZLIB=ON \
  -D BUILD_TIFF=ON \
  -D WITH_FFMPEG=ON \
  -D WITH_TBB=ON \
  -D BUILD_TBB=ON \
  -D BUILD_TESTS=OFF \
  -D WITH_EIGEN=ON \
  -D WITH_GSTREAMER=ON \
  -D WITH_V4L=ON \
  -D WITH_LIBV4L=ON \
  -D WITH_VTK=OFF \
  -D WITH_QT=OFF \
  -D OPENCV_ENABLE_NONFREE=ON \
  -D INSTALL_C_EXAMPLES=OFF \
  -D INSTALL_PYTHON_EXAMPLES=OFF \
  -D OPENCV_GENERATE_PKGCONFIG=OFF \
  -D BUILD_EXAMPLES=ON ..

make -j2
```
