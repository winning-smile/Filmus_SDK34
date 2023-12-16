package com.example.filmus

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.Socket


object SocketHandler{
    private lateinit var socket: Socket
    lateinit var reader: BufferedReader
    lateinit var writer: DataOutputStream

    fun connectSocket() {
        socket = Socket("192.168.0.12", 50000)
        reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        writer = DataOutputStream(socket.getOutputStream())
    }

    fun closeSocket() {
        writer.close()
        reader.close()
        socket.close()
    }
}