package com.example.scannercardnumber.utils

object Utils {

     fun basicCard(detections: String): String? {
        val regex = "\\d{4}\\D\\d{4}\\D\\d{4}\\D\\d{4}".toRegex()
        val matcher = regex.find(detections)
        return if (matcher != null) {
            val cardNumber = detections.substring(matcher.range.first, matcher.range.last + 1)
            if (cardNumber.startsWith("8600") ||
                cardNumber.startsWith("9860") ||
                cardNumber.startsWith("6262")
            ) cardNumber else null
        } else null
    }

     fun uzCard(detections: String): String? {
        val regex = "^8[0-9]{12}(?:[0-9]{3})?".toRegex()
        val matcher = regex.find(detections)
        return if (matcher != null) {
            val cardNumber = detections.substring(matcher.range.first, matcher.range.last + 1)
            if (cardNumber.startsWith("8600")) cardNumber else null
        } else null
    }

     fun humoCard(detections: String): String? {
        val regex = "^9[0-9]{12}(?:[0-9]{3})?".toRegex()
        val matcher = regex.find(detections)
        return if (matcher != null) {
            val cardNumber = detections.substring(matcher.range.first, matcher.range.last + 1)
            if (cardNumber.startsWith("9860")) cardNumber else null
        } else null
    }

     fun unionCard(detections: String): String? {
        val regex = "^6[0-9]{12}(?:[0-9]{3})?".toRegex()
        val matcher = regex.find(detections)
        return if (matcher != null) {
            val cardNumber = detections.substring(matcher.range.first, matcher.range.last + 1)
            if (cardNumber.startsWith("6262")) cardNumber else null
        } else null
    }

     fun attoCard(detections: String): String? {
        val regex = "^9[0-9]{12}(?:[0-9]{3})?".toRegex()
        val matcher = regex.find(detections)
        return if (matcher != null) {
            val cardNumber = detections.substring(matcher.range.first, matcher.range.last + 1)
            if (cardNumber.startsWith("9987")) cardNumber else null
        } else null
    }

     fun expireDate(detections: String): String? {
        val regex = "\\d{2}/\\d{2}".toRegex()
        val matcher = regex.find(detections)
        return if (matcher != null) {
            val cardExpire = detections.substring(matcher.range.first, matcher.range.last + 1)
            cardExpire
        } else null
    }
}

