cmake_minimum_required(VERSION 3.4.1)

set(libs_DIR jniLibs/${ANDROID_ABI})
link_directories(${libs_DIR})
include_directories(public)

add_library( opdfium

             SHARED

             main.cpp
             PdfRendererCompat.cpp)


target_link_libraries(
        opdfium

        log
        jnigraphics
        android
        c++.cr
        chrome_zlib.cr
        icui18n.cr
        icuuc.cr
        pdfium.cr)
