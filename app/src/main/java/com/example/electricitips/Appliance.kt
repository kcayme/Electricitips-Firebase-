package com.example.electricitips

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Appliance(var imgId: Int,
                var name: String,
                var modelCode: String,
                var type: String,
                var rating: Double,
                var duration: Double,
                var frequency: String) {

    @Exclude
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "imgID" to imgId,
            "name" to name,
            "code" to modelCode,
            "type" to type,
            "rating" to rating,
            "duration" to duration,
            "frequency" to frequency
        )
    }

}