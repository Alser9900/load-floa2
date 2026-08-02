package com.alser.loadflow

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * حل مسائل تدفق الأحمال (Load Flow) بطريقة نيوتن-رافسون
 * منقول مباشرة من منطق كود الماتلاب NR_LoadFlow.m ليعمل بشكل أصلي (native) داخل التطبيق
 * بدون الحاجة لماتلاب على الجهاز.
 */
object LoadFlowSolver {

    data class Result(
        val converged: Boolean,
        val iterations: Int,
        val Vmag: DoubleArray,
        val deltaDeg: DoubleArray,
        val Pfinal: DoubleArray,   // القدرة الفعالة النهائية لكل بص (تُحسب من الحل)
        val Qfinal: DoubleArray    // القدرة غير الفعالة النهائية لكل بص
    )

    private const val TOL = 1e-6
    private const val MAX_ITER = 50

    fun solve(
        n: Int,
        busType: IntArray,      // 1=Slack, 2=PV, 3=PQ
        Psp: DoubleArray,
        Qsp: DoubleArray,
        VmagInit: DoubleArray,
        deltaInit: DoubleArray,
        Ybus: Array<Array<Complex>>
    ): Result {

        val Vmag = VmagInit.copyOf()
        val delta = deltaInit.copyOf()

        val Ymag = Array(n) { i -> DoubleArray(n) { k -> Ybus[i][k].magnitude() } }
        val Yang = Array(n) { i -> DoubleArray(n) { k -> Ybus[i][k].angleRad() } }

        val pqIdx = (0 until n).filter { busType[it] == 3 }
        val nonSlackIdx = (0 until n).filter { busType[it] != 1 }

        val npq = pqIdx.size
        val nDelta = nonSlackIdx.size

        var converged = false
        var lastIter = 0

        for (iter in 1..MAX_ITER) {
            lastIter = iter

            val Pcalc = DoubleArray(n)
            val Qcalc = DoubleArray(n)
            for (i in 0 until n) {
                var p = 0.0
                var q = 0.0
                for (k in 0 until n) {
                    val ang = Yang[i][k] - delta[i] + delta[k]
                    p += Vmag[i] * Vmag[k] * Ymag[i][k] * cos(ang)
                    q -= Vmag[i] * Vmag[k] * Ymag[i][k] * sin(ang)
                }
                Pcalc[i] = p
                Qcalc[i] = q
            }

            val mismatch = DoubleArray(nDelta + npq)
            for (a in nonSlackIdx.indices) {
                val i = nonSlackIdx[a]
                mismatch[a] = Psp[i] - Pcalc[i]
            }
            for (a in pqIdx.indices) {
                val i = pqIdx[a]
                mismatch[nDelta + a] = Qsp[i] - Qcalc[i]
            }

            val maxMismatch = mismatch.maxOfOrNull { abs(it) } ?: 0.0
            if (maxMismatch < TOL) {
                converged = true
                break
            }

            val size = nDelta + npq
            val J = Array(size) { DoubleArray(size) }

            // J1 = dP/d(delta) , J2 = dP/d|V|
            for (a in nonSlackIdx.indices) {
                val i = nonSlackIdx[a]
                for (b in nonSlackIdx.indices) {
                    val j = nonSlackIdx[b]
                    J[a][b] = if (i == j) {
                        var s = 0.0
                        for (k in 0 until n) {
                            if (k != i) {
                                s += Vmag[i] * Vmag[k] * Ymag[i][k] *
                                    sin(Yang[i][k] - delta[i] + delta[k])
                            }
                        }
                        s
                    } else {
                        -Vmag[i] * Vmag[j] * Ymag[i][j] *
                            sin(Yang[i][j] - delta[i] + delta[j])
                    }
                }
                for (b in pqIdx.indices) {
                    val j = pqIdx[b]
                    J[a][nDelta + b] = if (i == j) {
                        var s = 2 * Vmag[i] * Ymag[i][i] * cos(Yang[i][i])
                        for (k in 0 until n) {
                            if (k != i) {
                                s += Vmag[k] * Ymag[i][k] * cos(Yang[i][k] - delta[i] + delta[k])
                            }
                        }
                        s
                    } else {
                        Vmag[i] * Ymag[i][j] * cos(Yang[i][j] - delta[i] + delta[j])
                    }
                }
            }

            // J3 = dQ/d(delta) , J4 = dQ/d|V|
            for (a in pqIdx.indices) {
                val i = pqIdx[a]
                for (b in nonSlackIdx.indices) {
                    val j = nonSlackIdx[b]
                    J[nDelta + a][b] = if (i == j) {
                        var s = 0.0
                        for (k in 0 until n) {
                            if (k != i) {
                                s += Vmag[i] * Vmag[k] * Ymag[i][k] *
                                    cos(Yang[i][k] - delta[i] + delta[k])
                            }
                        }
                        s
                    } else {
                        -Vmag[i] * Vmag[j] * Ymag[i][j] *
                            cos(Yang[i][j] - delta[i] + delta[j])
                    }
                }
                for (b in pqIdx.indices) {
                    val j = pqIdx[b]
                    J[nDelta + a][nDelta + b] = if (i == j) {
                        var s = -2 * Vmag[i] * Ymag[i][i] * sin(Yang[i][i])
                        for (k in 0 until n) {
                            if (k != i) {
                                s -= Vmag[k] * Ymag[i][k] * sin(Yang[i][k] - delta[i] + delta[k])
                            }
                        }
                        s
                    } else {
                        -Vmag[i] * Ymag[i][j] * sin(Yang[i][j] - delta[i] + delta[j])
                    }
                }
            }

            val dx = solveLinear(J, mismatch) ?: break // مصفوفة شبه منفردة (singular) -> توقف

            for (a in nonSlackIdx.indices) {
                delta[nonSlackIdx[a]] += dx[a]
            }
            for (a in pqIdx.indices) {
                Vmag[pqIdx[a]] += dx[nDelta + a]
            }
        }

        // حساب P, Q النهائية لكل البصات (تشمل Slack و PV) من الحل النهائي
        val Pfinal = DoubleArray(n)
        val Qfinal = DoubleArray(n)
        for (i in 0 until n) {
            var p = 0.0
            var q = 0.0
            for (k in 0 until n) {
                val ang = Yang[i][k] - delta[i] + delta[k]
                p += Vmag[i] * Vmag[k] * Ymag[i][k] * cos(ang)
                q -= Vmag[i] * Vmag[k] * Ymag[i][k] * sin(ang)
            }
            Pfinal[i] = p
            Qfinal[i] = q
        }

        val deltaDeg = DoubleArray(n) { delta[it] * 180.0 / Math.PI }

        return Result(converged, lastIter, Vmag, deltaDeg, Pfinal, Qfinal)
    }

    /**
     * حل نظام معادلات خطي A*x = b بطريقة الحذف الغاوسي (Gaussian Elimination)
     * مع Partial Pivoting. تُستخدم كبديل عن عملية J \ Mismatch في الماتلاب.
     * تُرجع null إذا كانت المصفوفة شبه منفردة (لا يوجد حل مستقر).
     */
    private fun solveLinear(Ain: Array<DoubleArray>, bin: DoubleArray): DoubleArray? {
        val nS = bin.size
        val A = Array(nS) { i -> Ain[i].copyOf() }
        val b = bin.copyOf()

        for (col in 0 until nS) {
            var pivotRow = col
            var maxVal = abs(A[col][col])
            for (r in col + 1 until nS) {
                if (abs(A[r][col]) > maxVal) {
                    maxVal = abs(A[r][col])
                    pivotRow = r
                }
            }
            if (maxVal < 1e-12) return null

            if (pivotRow != col) {
                val tmpRow = A[col]; A[col] = A[pivotRow]; A[pivotRow] = tmpRow
                val tmpB = b[col]; b[col] = b[pivotRow]; b[pivotRow] = tmpB
            }

            for (r in col + 1 until nS) {
                val factor = A[r][col] / A[col][col]
                for (c in col until nS) {
                    A[r][c] -= factor * A[col][c]
                }
                b[r] -= factor * b[col]
            }
        }

        val x = DoubleArray(nS)
        for (r in nS - 1 downTo 0) {
            var sum = b[r]
            for (c in r + 1 until nS) {
                sum -= A[r][c] * x[c]
            }
            x[r] = sum / A[r][r]
        }
        return x
    }
}
