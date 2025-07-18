package com.gity.feliyaattendance.utils

import android.content.Context
import android.content.res.AssetManager
import com.gity.feliyaattendance.admin.data.model.WorkerPerformanceData
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TensorFlowLiteHelper(
    private val context: Context
) {
    private var interpreter: Interpreter? = null
    private val modelFileName = "worker_analysis_model.tflite"

    // Scaler parameters dari tflite_model_info.json
    private val scalerMean = floatArrayOf(
        8.612440191387558f,    // attendance_rate mean
        4.135973551302499f,    // avg_work_hours mean
        30.479405992988333f,   // punctuality_score mean
        38.42768497459864f     // consistency_score mean
    )

    private val scalerScale = floatArrayOf(
        14.599618646459406f,   // attendance_rate scale
        4.026754450407032f,    // avg_work_hours scale
        38.25509941089032f,    // punctuality_score scale
        42.326272047778204f    // consistency_score scale
    )

    // Performance mapping dari model info
    private val performanceMapping = mapOf(
        0 to "High Performer", 1 to "Low Performer", 2 to "Medium Performer"
    )

    init {
        initializeInterpreter()
    }

    private fun initializeInterpreter() {
        try {
            val model = loadModelFile()
            interpreter = Interpreter(model)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val assetManager: AssetManager = context.assets
        val fileDescriptor = assetManager.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predictPerformance(
        attendanceRate: Double,
        avgWorkHours: Double,
        punctualityScore: Double,
        consistencyScore: Double
    ): Pair<String, Float> {
        return try {
            val interpreter = this.interpreter ?: return Pair("Unknown", 0f)

            // Prepare input data
            val inputData = floatArrayOf(
                attendanceRate.toFloat(),
                avgWorkHours.toFloat(),
                punctualityScore.toFloat(),
                consistencyScore.toFloat()
            )

            // Standardize input using scaler parameters
            val standardizedInput = FloatArray(4)
            for (i in inputData.indices) {
                standardizedInput[i] = (inputData[i] - scalerMean[i]) / scalerScale[i]
            }

            // Create input buffer
            val inputBuffer = ByteBuffer.allocateDirect(4 * 4) // 4 features * 4 bytes per float
            inputBuffer.order(ByteOrder.nativeOrder())
            inputBuffer.rewind()

            for (value in standardizedInput) {
                inputBuffer.putFloat(value)
            }

            // Prepare output buffers
            val clusterOutput = Array(1) { FloatArray(1) }
            val distanceOutput = Array(1) { FloatArray(1) }

            val outputs = mapOf(
                0 to clusterOutput, 1 to distanceOutput
            )

            // Run inference
            interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)

            val predictedCluster = clusterOutput[0][0].toInt()
            val distance = distanceOutput[0][0]

            // Convert distance to confidence score (0-1 range)
            val confidence = kotlin.math.max(0.0f, 1.0f - (distance / 10.0f))

            val performanceLabel = performanceMapping[predictedCluster] ?: "Unknown"

            Pair(performanceLabel, confidence)

        } catch (e: Exception) {
            e.printStackTrace()
            Pair("Error", 0f)
        }
    }

    fun predictBatch(workers: List<WorkerPerformanceData>): List<WorkerPerformanceData> {
        return workers.map { worker ->
            val (performanceLabel, confidence) = predictPerformance(
                worker.attendanceRate,
                worker.avgWorkHours,
                worker.punctualityScore,
                worker.consistencyScore
            )

            worker.copy(
                performanceLabel = performanceLabel, confidence = confidence.toDouble()
            )
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}