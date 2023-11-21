package com.reptimate.iot_teamnova.Cage

data class CageItem(val idx: String,
                    val cageName: String,
                    val boardTempname: String,
                    val currentUvbLight: String,
                    val currentHeatingLight: String,
                    val autoChkLight: String,
                    val autoChkTemp: String,
                    val autoChkHumid: String,
                    val currentTemp: String,
                    val currentTemp2: String,
                    val maxTemp: String,
                    val minTemp: String,
                    val currentHumid: String,
                    val currentHumid2: String,
                    val maxHumid: String,
                    val minHumid: String,
                    val autoLightUtctimeOn: String,
                    val autoLightUtctimeOff: String
                    )
