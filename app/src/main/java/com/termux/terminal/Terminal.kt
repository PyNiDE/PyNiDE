package com.termux.terminal

object Terminal {
    @JvmStatic
    external fun createSubprocess(
        cmd: String,
        cwd: String,
        args: Array<String?>,
        envVars: Array<String?>,
        pidArray: IntArray,
        rows: Int,
        cols: Int,
        cellWidth: Int,
        cellHeight: Int,
    ): Int

    @JvmStatic
    external fun setPtyWindowSize(
        fd: Int,
        rows: Int,
        cols: Int,
        cellWidth: Int,
        cellHeight: Int,
    )

    @JvmStatic
    external fun setPtyUTF8Mode(fd: Int)

    @JvmStatic
    external fun waitFor(pid: Int): Int

    @JvmStatic
    external fun close(fd: Int)
}