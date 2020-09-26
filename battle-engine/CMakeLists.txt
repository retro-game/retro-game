cmake_minimum_required(VERSION 3.1)
project(BattleEngine C)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

add_library(BattleEngine SHARED BattleEngine.c)
set_property(TARGET BattleEngine PROPERTY C_STANDARD 99)

if(CMAKE_C_COMPILER_ID MATCHES Clang OR CMAKE_COMPILER_IS_GNUCC)
  target_compile_options(BattleEngine PRIVATE -Wall -Wextra -ffast-math)
endif()
