package com.dedztbh.kuantum.ejml.operator

import com.dedztbh.kuantum.common.Config
import com.dedztbh.kuantum.common.readInt
import com.dedztbh.kuantum.common.upperBound
import com.dedztbh.kuantum.ejml.matrix.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Created by DEDZTBH on 2020/09/22.
 * Project KuantumCircuitSim
 */

fun getJointState(initState: String, jointStateSize: Int) =
    if (initState.isBlank())
        CMatrix(jointStateSize, 1).apply {
            set(0, 0, 1.0, 0.0)
        }
    else loadCsv(initState)

class Tester(config: Config, scope: CoroutineScope) : TFinder(config, scope) {
    /** 2^N by 1 column vector */
    @JvmField
    var jointState = getJointState(config.init_state, jointStateSize)

    @JvmField
    var hasMeasGate = false

    override suspend fun runCmd(cmd: String) = when (cmd) {
        "MEASURE" -> {
            val i = readInt()
            val results = opMatrix * jointState
            val probs = mutableListOf(0.0)
            val labels = mutableListOf<Int>()
            val a = CNum()
            for (j in 0 until jointStateSize) {
                results.get(j, 0, a)
                if (a.magnitude2 > 0) {
                    labels.add(j)
                    probs.add(a.magnitude2 + probs.last())
                }
            }
            println("Measurement(s):")
            repeat(i) {
                val r = Random.nextDouble()
                val index = probs.upperBound(r)
                println(allssket[labels[index]])
            }
            println()
            0
        }
        "MEASALL" -> {
            jointState = opMatrix * jointState
            opMatrix = IN2
            val probs = mutableListOf(0.0)
            val labels = mutableListOf<Int>()
            val a = CNum()
            for (j in 0 until jointStateSize) {
                jointState.get(j, 0, a)
                if (a.magnitude2 > 0) {
                    labels.add(j)
                    probs.add(a.magnitude2 + probs.last())
                }
            }
            val index = probs.upperBound(Random.nextDouble())
            val collapseState = labels[index]
            println("MeasAll: ${allssket[collapseState]}")
            jointState = CMatrix(jointStateSize, 1).apply {
                set(collapseState, 0, 1.0, 0.0)
            }
            hasMeasGate = true
            0
        }
        "MEASONE" -> {
            val i = readInt()
            jointState = opMatrix * jointState
            opMatrix = IN2
            var prob = 0.0
            val a = CNum()
            for (j in 0 until jointStateSize) {
                if (allss[j][i] == '0') {
                    jointState.get(j, 0, a)
                    prob += a.magnitude2
                }
            }
            val c = if (Random.nextDouble() < prob) '0' else {
                prob = 1.0 - prob
                '1'
            }
            println("MeasOne: Qubit #$i is |$c>")
            for (j in 0 until jointStateSize) {
                if (allss[j][i] != c) {
                    jointState.set(j, 0, 0.0, 0.0)
                }
            }
            COps.scale(1.0 / sqrt(prob), 0.0, jointState)
            hasMeasGate = true
            0
        }
        else -> super.runCmd(cmd)
    }

    override suspend fun done() {
        if (!hasMeasGate) super.done()
    }

    override suspend fun printResult() {
        if (hasMeasGate) {
            println("Circuit matrix unavailable due to MeasAll/MeasOne command(s).")
        } else {
            super.printResult()
        }
        println("\nFinal state: ")
        println("Init ${allssket[0]}")
        (opMatrix * jointState).printFancy2(allssket = allssket)
    }
}

class PTester(config: Config, scope: CoroutineScope) : PTFinder(config, scope) {
    /** 2^N by 1 column vector */
    @JvmField
    var jointState = getJointState(config.init_state, jointStateSize)

    @JvmField
    var hasMeasGate = false

    override suspend fun runCmd(cmd: String) = when (cmd) {
        "MEASURE" -> {
            val i = readInt()
            opMatrix = reduceOps()
            reversedNewOps = mutableListOf(GlobalScope.async { opMatrix })
            val results = opMatrix * jointState
            val probs = mutableListOf(0.0)
            val labels = mutableListOf<Int>()
            val a = CNum()
            for (j in 0 until jointStateSize) {
                results.get(j, 0, a)
                if (a.magnitude2 > 0) {
                    labels.add(j)
                    probs.add(a.magnitude2 + probs.last())
                }
            }
            println("Measurement(s):")
            repeat(i) {
                val r = Random.nextDouble()
                val index = probs.upperBound(r)
                println(allssket[labels[index]])
            }
            println()
            0
        }
        "MEASALL" -> {
            jointState = reduceOps() * jointState
            reversedNewOps = mutableListOf(GlobalScope.async { IN2 })
            val probs = mutableListOf(0.0)
            val labels = mutableListOf<Int>()
            val a = CNum()
            for (j in 0 until jointStateSize) {
                jointState.get(j, 0, a)
                if (a.magnitude2 > 0) {
                    labels.add(j)
                    probs.add(a.magnitude2 + probs.last())
                }
            }
            val r = Random.nextDouble()
            val index = probs.upperBound(r)
            val collapseState = labels[index]
            println("MeasAll: ${allssket[collapseState]}")
            jointState = CMatrix(jointStateSize, 1).apply {
                set(collapseState, 0, 1.0, 0.0)
            }
            hasMeasGate = true
            0
        }
        "MEASONE" -> {
            val i = readInt()
            jointState = reduceOps() * jointState
            reversedNewOps = mutableListOf(GlobalScope.async { IN2 })
            var prob = 0.0
            val a = CNum()
            for (j in 0 until jointStateSize) {
                if (allss[j][i] == '0') {
                    jointState.get(j, 0, a)
                    prob += a.magnitude2
                }
            }
            val c = if (Random.nextDouble() < prob) '0' else {
                prob = 1.0 - prob
                '1'
            }
            println("MeasOne: Qubit #$i is |$c>")
            for (j in 0 until jointStateSize) {
                if (allss[j][i] != c) {
                    jointState.set(j, 0, 0.0, 0.0)
                }
            }
            COps.scale(1.0 / sqrt(prob), 0.0, jointState)
            hasMeasGate = true
            0
        }
        else -> super.runCmd(cmd)
    }

    override suspend fun done() {
        if (!hasMeasGate) super.done()
        else {
            opMatrix = reduceOps()
        }
    }

    override suspend fun printResult() {
        if (hasMeasGate) {
            println("Circuit matrix unavailable due to MeasAll/MeasOne command(s).")
        } else {
            super.printResult()
        }
        println("\nFinal state: ")
        println("Init ${allssket[0]}")
        (opMatrix * jointState).printFancy2(allssket = allssket)
    }
}