package com.alser.loadflow

/**
 * مستودع بيانات مشترك (Singleton) يحمل بيانات النظام بين الشاشات الثلاث:
 * إدخال البصات -> إدخال Ybus -> النتائج
 *
 * busType : 1 = Slack , 2 = PV , 3 = PQ
 */
object AppData {

    var n: Int = 0

    var busType: IntArray = IntArray(0)
    var Psp: DoubleArray = DoubleArray(0)      // القدرة الفعالة المحددة (pu)
    var Qsp: DoubleArray = DoubleArray(0)      // القدرة غير الفعالة المحددة (pu)
    var Vmag: DoubleArray = DoubleArray(0)     // مقدار الفولت (محدد لـ Slack/PV، ابتدائي لـ PQ)
    var delta: DoubleArray = DoubleArray(0)    // الزاوية الابتدائية (rad) - عادة صفر

    var Ybus: Array<Array<Complex>> = arrayOf()

    var result: LoadFlowSolver.Result? = null

    fun initBuses(size: Int) {
        n = size
        busType = IntArray(size) { 3 }
        Psp = DoubleArray(size)
        Qsp = DoubleArray(size)
        Vmag = DoubleArray(size) { 1.0 }
        delta = DoubleArray(size)
        Ybus = Array(size) { Array(size) { Complex.ZERO } }
    }
}
