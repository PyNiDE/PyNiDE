cmake_minimum_required(VERSION 3.18.1)
PROJECT("PyNiDE")

set(CMAKE_CXX_FLAGS "-std=c++14 -DANDROID -g")
set(CMAKE_C_FLAGS "-w -std=c11 -DANDROID -D_LARGEFILE_SOURCE=1 -g")
set(CMAKE_ASM_FLAGS "${CFLAGS} -x assembler-with-cpp")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -ffunction-sections -fdata-sections")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -ffunction-sections -fdata-sections")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--exclude-libs,librlottie.a")

if (${ANDROID_ABI} STREQUAL "armeabi-v7a" OR ${ANDROID_ABI} STREQUAL "arm64-v8a")
    enable_language(ASM)
else ()
    enable_language(ASM_NASM)
endif ()

# rlottie
add_library(rlottie STATIC
        rlottie/src/lottie/lottieanimation.cpp
        rlottie/src/lottie/lottieitem.cpp
        rlottie/src/lottie/lottiekeypath.cpp
        rlottie/src/lottie/lottieloader.cpp
        rlottie/src/lottie/lottiemodel.cpp
        rlottie/src/lottie/lottieparser.cpp
        rlottie/src/lottie/lottieproxymodel.cpp
        rlottie/src/vector/freetype/v_ft_math.cpp
        rlottie/src/vector/freetype/v_ft_raster.cpp
        rlottie/src/vector/freetype/v_ft_stroker.cpp
        rlottie/src/vector/pixman/vregion.cpp
        rlottie/src/vector/stb/stb_image.cpp
        rlottie/src/vector/vbezier.cpp
        rlottie/src/vector/vbitmap.cpp
        rlottie/src/vector/vbrush.cpp
        rlottie/src/vector/vcompositionfunctions.cpp
        rlottie/src/vector/vdasher.cpp
        rlottie/src/vector/vdebug.cpp
        rlottie/src/vector/vdrawable.cpp
        rlottie/src/vector/vdrawhelper.cpp
        rlottie/src/vector/vdrawhelper_neon.cpp
        rlottie/src/vector/velapsedtimer.cpp
        rlottie/src/vector/vimageloader.cpp
        rlottie/src/vector/vinterpolator.cpp
        rlottie/src/vector/vmatrix.cpp
        rlottie/src/vector/vpainter.cpp
        rlottie/src/vector/vpath.cpp
        rlottie/src/vector/vpathmesure.cpp
        rlottie/src/vector/vraster.cpp
        rlottie/src/vector/vrect.cpp
        rlottie/src/vector/vrle.cpp)
target_compile_options(rlottie PUBLIC
        -Wall
        -fno-rtti
        -finline-functions
        -ffast-math
        -Os
        -fno-unwind-tables
        -fno-asynchronous-unwind-tables
        -Wnon-virtual-dtor
        -Woverloaded-virtual
        -Wno-unused-parameter
        -fvisibility=hidden)
set_target_properties(rlottie PROPERTIES
        ANDROID_ARM_MODE arm)
target_compile_definitions(rlottie PUBLIC
        HAVE_PTHREAD NDEBUG)
target_include_directories(rlottie PUBLIC
        ./
        rlottie/inc
        rlottie/src/vector/
        rlottie/src/vector/pixman
        rlottie/src/vector/freetype
        rlottie/src/vector/stb)
if (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    target_compile_options(rlottie PUBLIC
            -fno-integrated-as)
    target_compile_definitions(rlottie PUBLIC
            USE_ARM_NEON)
    target_sources(rlottie PRIVATE
            rlottie/src/vector/pixman/pixman-arm-neon-asm.S)
elseif (${ANDROID_ABI} STREQUAL "arm64-v8a")
    target_compile_options(rlottie PUBLIC
            -fno-integrated-as)
    target_compile_definitions(rlottie PUBLIC
            USE_ARM_NEON __ARM64_NEON__)
    target_sources(rlottie PRIVATE
            rlottie/src/vector/pixman/pixman-arma64-neon-asm.S)
endif ()

# pynide
add_library(pynide SHARED
        pynide.cpp
        lottie.cpp
        image.cpp)
target_compile_options(pynide PUBLIC
        -ffast-math
        -Os
        -funroll-loops
        -ffast-math
        -fno-strict-aliasing
        -fno-math-errno)
target_link_libraries(pynide -Wl,--no-whole-archive
        rlottie
        jnigraphics
        log
        z)