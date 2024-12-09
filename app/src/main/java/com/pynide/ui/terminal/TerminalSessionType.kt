package com.pynide.ui.terminal

import android.os.Parcel
import android.os.Parcelable

import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
enum class TerminalSessionType(val id: Int) : Parcelable {
    TERMINAL(0), INTERPRETER(1), CONSOLE(2);

    private companion object : Parceler<TerminalSessionType> {
        override fun TerminalSessionType.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
        }

        override fun create(parcel: Parcel): TerminalSessionType {
            return fromId(parcel.readInt())
        }

        private fun fromId(id: Int): TerminalSessionType {
            return entries.find { it.id == id } ?: TERMINAL
        }
    }
}