cmake_minimum_required(VERSION 3.18.1)
PROJECT("PyNiDE")

add_library(pynide SHARED pynide.cpp)
target_link_libraries(pynide)
