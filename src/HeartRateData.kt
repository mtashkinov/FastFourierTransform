import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Mikhail on 26.02.2016.
 */
class HeartRateData(file : File)
{
    val MEASUREMENT_DURATION = 15
    val TIME_TO_CALCULATE_SIZE = 5
    private val PART_SHIFT = 0.5
    private val STRONG_FILTER_WINDOW = 15

    var times : MutableList<Long> = ArrayList()
    var data : MutableList<Double> = ArrayList()
        private set
    private var pikes : MutableList<MutableList<Int>> = ArrayList()
    var partSize = 0
    var isBadData = true
    var estimatedPulse = 0
    private var totalFFT : DoubleArray = DoubleArray(0)
    var freq = 0.0
    private val parts = ArrayList<IntRange>()
    var size : Int = -1
        private set
    var knowSize = false
    private var lastInterpTime = 0.0
    var interpStep = 0.0
    private var filter : Filter? = null
    var firstMeasurement = true
        private set
    var pulse = 0

    val pulseHistory = ArrayList<Int>()
    val estimatedPulseHistory = ArrayList<Int>()
    val isBadDataHistory = ArrayList<Boolean>()
    val filteredDataHistory = ArrayList<DoubleArray>()
    val interpDataHistory = ArrayList<DoubleArray>()
    val totalFFTHistory = ArrayList<DoubleArray>()
    var interpData = ArrayList<Double>()
    var strongData = doubleArrayOf()
    val strongDataHistory = ArrayList<DoubleArray>()
    val interpStepHistory = ArrayList<Double>()


    init
    {
        var lines = file.readLines()

        if (lines[1].equals("true") || lines[1].equals("false"))
        {
            lines = lines.drop(3)
        }

        val pulseString = lines.find { x -> x.equals("Pulse history") }
        if (pulseString != null)
        {
            val index = lines.indexOf(pulseString)
            lines = lines.dropLast(lines.size - index)
        }

        for (i in lines.indices)
        {
            val line = lines[i].replace(",", " ").replace(";", " ")
            val lineScanner = Scanner(line)

            addData(lineScanner.nextLong(), lineScanner.next().toDouble())
            lineScanner.close()
        }

    }

    fun isReady() : Boolean
    {
        return times.size == size
    }

    fun addData(time : Long, data : Double)
    {
        if ((!knowSize) and (time >= TIME_TO_CALCULATE_SIZE * 1000))
        {
            calculateSize()
            calculateParts()
        }

        times.add(time)
        synchronized(this.data)
        {
            this.data.add(data)
        }

        if ((times.size > size) && (knowSize))
        {
            times.removeAt(0)
            this.data.removeAt(0)
        }

        if (firstMeasurement)
        {
            if (isReady())
            {
                countPulse()
            }
        } else
        {
            tryToAddNewFilteredPoint()
            if (isReady() && isFreqChanged())
            {
                countPulse()
            } else if ((filter!!.isReady()) && (!isFreqChanged()))
            {
                updatePulse()
            }
        }
    }

    fun countPulse()
    {
        freq = countFreq()
        filter = Filter(size, freq)

        interpData = ArrayList<Double>(countInterpolatedData(0..data.lastIndex, freq).asList())
        for (value in interpData)
        {
            filter!!.addData(value)
        }
        val filteredData = filter!!.getData()
        interpDataHistory.add(interpData.toDoubleArray())
        filteredDataHistory.add(filteredData)
        countPikes(filteredData)
        getPulse(filteredData, interpData.toDoubleArray())
        firstMeasurement = false
    }

    private fun updatePulse()
    {
        pikes.removeAt(0)
        val fftCounter = FFT(partSize)
        val filteredData = filter!!.getData()
        interpDataHistory.add(interpData.toDoubleArray())
        filteredDataHistory.add(filteredData)
        val fft = fftCounter.fft(filteredData.slice(parts.last()).toDoubleArray())
        val pikeDetector = FFTPikeDetector(fft, freq, partSize)
        pikes.add(pikeDetector.pikes)
        getPulse(filteredData, interpData.toDoubleArray())
    }

    private fun getPulse(filteredData: DoubleArray, interpolatedData: DoubleArray)
    {
        val estimatedPulse = getEstimatedPulse(filteredData)
        getPrecisePulse(estimatedPulse, interpolatedData)
        dropOldData()
    }

    private fun getEstimatedPulse(filteredData: DoubleArray) : Int
    {
        val totalFFTCounter = FFT(size)
        totalFFT = totalFFTCounter.fft(filteredData)
        totalFFTHistory.add(totalFFT)
        val prevPulse = if ((estimatedPulseHistory.size > 0) && (!isBadData)) estimatedPulseHistory.last() else 0
        val pulseDetector = PulseDetector(totalFFT, size, pikes, partSize, freq, prevPulse)
        isBadData = pulseDetector.isBadData
        estimatedPulse = pulseDetector.pulse
        isBadDataHistory.add(isBadData)
        estimatedPulseHistory.add(estimatedPulse)

        return estimatedPulse
    }

    private fun getPrecisePulse(estimatedPulse: Int, interpolatedData: DoubleArray)
    {
        val strongData = applyStrongFilter(estimatedPulse, interpolatedData)
        pulse = PulsePikeDetector().getPulse(strongData, interpStep)
        pulseHistory.add(pulse)
    }

    private fun applyStrongFilter(estimatedPulse: Int, interpolatedData: DoubleArray) : DoubleArray
    {
        val min = Math.round(Math.max(PulseDetector.MIN_HEART_RATE, estimatedPulse - STRONG_FILTER_WINDOW.toDouble())).toInt()
        val max = Math.round(Math.min(PulseDetector.MAX_HEART_RATE, estimatedPulse + STRONG_FILTER_WINDOW.toDouble())).toInt()
        val strongFilter = Filter(size, freq, min, max)
        for (value in interpolatedData.slice(0..size-1))
        {
            strongFilter.addData(value)
        }
        val result = strongFilter.getData()
        strongData = result
        strongDataHistory.add(result)
        interpStepHistory.add(interpStep)

        return result
    }

    private fun dropOldData()
    {
        val dataToDrop = (partSize * PART_SHIFT).toInt()

        filter!!.removeData(dataToDrop)
        for (i in 1..dataToDrop)
        {
            interpData.removeAt(0)
        }
    }

    private fun tryToAddNewFilteredPoint()
    {
        while (canInterpPoint())
        {
            val point = interpolatePoint(data.lastIndex, lastInterpTime + interpStep)
            interpData.add(point)
            filter!!.addData(point)
            lastInterpTime += interpStep
        }
    }

    private fun isFreqChanged() : Boolean
    {
        return Math.abs(countFreq() - freq) >= 1
    }

    private fun canInterpPoint() : Boolean
    {
        return (times.last() - lastInterpTime) > interpStep
    }

    private fun countPikes(filteredData : DoubleArray)
    {
        pikes.clear()
        for (part in parts)
        {
            val fftCounter = FFT(partSize)
            val fft = fftCounter.fft(filteredData.slice(part).toDoubleArray())
            val pikeDetector = FFTPikeDetector(fft, freq, partSize)
            pikes.add(pikeDetector.pikes)
        }
    }

    private fun calculateSize()
    {
        val freq = countFreq()
        val expectedSize = MEASUREMENT_DURATION * freq
        size = findClosest2Power(expectedSize)
        partSize =  size / 4
        knowSize = true
    }

    private fun calculateParts()
    {
        var start = 0
        while (start + partSize <= size)
        {
            val end = start + partSize - 1
            parts.add(start..end)
            start += (partSize * PART_SHIFT).toInt()
        }
    }

    fun countInterpolatedData(range : IntRange, freq: Double) : DoubleArray
    {
        return convertLineInterp(range, freq)
    }

    private fun countFreq() : Double
    {
        return 1000 / ((times.last() - times.first()).toDouble() / (times.size))
    }

    private fun convertLineInterp(range : IntRange, freq : Double) : DoubleArray
    {
        lastInterpTime = times.last().toDouble()
        interpStep = 1000 / freq
        return DoubleArray(range.last - range.first + 1, { i -> interpolatePoint(range.start, times[range.start] + i * interpStep) })
    }

    private fun interpolatePoint(start : Int, time : Double) : Double
    {
        var i = start
        while ((i < times.size) && (times[i] < time))
        {
            ++i
        }
        if (i == times.size)
        {
            --i
        }
        if (times[i] == time.toLong())
        {
            return data[i]
        } else
        {
            return data[i - 1] + (data[i] - data[i-1]) * (time - times[i-1]) / (times[i] - times[i-1])
        }
    }

    private fun findClosest2Power(n : Double) : Int
    {
        var pow = 1
        while (pow < n)
        {
            pow *= 2
        }
        if (pow - n > n - pow / 2)
        {
            return pow / 2
        } else
        {
            return pow
        }
    }
}