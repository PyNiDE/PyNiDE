package com.pynide.terminal

import android.os.Parcel
import android.os.Parcelable

import androidx.annotation.StyleRes

import com.pynide.R

import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
enum class TerminalType(val id: Int, @StyleRes val title: Int) : Parcelable {
    DEFAULT(0, R.string.terminal),
    INTERPRETER(1, R.string.interpreter),
    CONSOLE(2, R.string.console);

    private companion object : Parceler<TerminalType> {
        override fun TerminalType.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeInt(title)
        }

        override fun create(parcel: Parcel): TerminalType {
            val id = parcel.readInt()
            return entries.find { it.id == id }!!
        }
    }
}