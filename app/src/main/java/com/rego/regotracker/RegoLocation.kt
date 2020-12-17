package com.rego.regotracker

import com.google.gson.Gson

data class RegoLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double,
    val speed: Float,
    val time: Long
) {

    fun toJsonString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

}