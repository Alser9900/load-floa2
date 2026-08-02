package com.alser.loadflow

import kotlin.math.atan2
import kotlin.math.hypot

/**
 * عدد مركب بسيط (الجزء الحقيقي + الجزء التخيلي)
 * يُستخدم لتمثيل عناصر مصفوفة Ybus
 */
data class Complex(val re: Double, val im: Double) {

    fun magnitude(): Double = hypot(re, im)

    fun angleRad(): Double = atan2(im, re)

    operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)

    operator fun times(other: Complex) = Complex(
        re * other.re - im * other.im,
        re * other.im + im * other.re
    )

    companion object {
        val ZERO = Complex(0.0, 0.0)
    }
}
