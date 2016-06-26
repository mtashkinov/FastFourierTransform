import java.io.File
import java.util.*

/**
 * Created by Mikhail on 26.02.2016.
 */
class HeartRateData(val file : File)
{
    val PART_SHIFT = 0.5
    var times : MutableList<Long> = ArrayList()
        private set
    var data : MutableList<Double> = ArrayList()
        private set
    var pikes : MutableList<MutableList<Int>> = ArrayList()
    private var maxSize = 0
    var partSize = 0
    var isBadData = true
    var pulse = 0
    var freq : MutableList<Double> = ArrayList()
    var filteredData = DoubleArray(0)
    var pureData = DoubleArray(0)
    var freqData = DoubleArray(0)
    var totalFFT : DoubleArray = DoubleArray(0)
    var totalFreq = 0.0
    private val parts = ArrayList<IntRange>()
    val partsFFT = ArrayList<DoubleArray>()
    var interpTimes = DoubleArray(0)


    init
    {
        var lines = file.readLines()
        maxSize = findClosest2Power(lines.size)
        partSize = maxSize / 4

        if (lines[1].equals("true") || lines[1].equals("false"))
        {
            lines = lines.drop(3)
        }

        if (lines.size >= maxSize)
        {
            var indices = lines.indices.filter { x -> x >= lines.size - maxSize }
            for (i in indices)
            {
                val index = i - indices[0]
                val line = lines[i].replace(";", " ")
                val lineScanner = Scanner(line)

                times.add(lineScanner.nextLong())
                data.add(lineScanner.next().toDouble())
                lineScanner.close()

                if (data.size > maxSize)
                {
                    times.removeAt(0)
                    data.removeAt(0)
                }
                if (((index + 1) >= partSize) && ((index + 1) % (partSize * PART_SHIFT).toInt() == 0))
                {
                    parts.add(index - partSize + 1..index)
                }
            }

            val fftCounter = FFT(maxSize)
            totalFreq = countFreq(0..data.lastIndex)


            pureData = countInterpolatedData(0..data.lastIndex, totalFreq)
            freqData = Filter().apply1(pureData, totalFreq)


            filteredData = Filter().apply(countInterpolatedData(0..data.lastIndex, totalFreq), totalFreq)
            totalFFT = fftCounter.fft(filteredData)

            countPikes()
            val pulseDetector = PulseDetector(totalFFT, freq, maxSize, pikes, partSize, totalFreq)
            isBadData = pulseDetector.isBadData
            pulse = pulseDetector.pulse
        }
    }

    private fun countPikes()
    {
        for (part in parts)
        {
            freq.add(countFreq(part))
            val fftCounter = FFT(partSize)
            partsFFT.add(fftCounter.fft(filteredData.slice(part).toDoubleArray()))
            val pikeDetector = PikeDetector(partsFFT.last(), freq.last(), partSize)
            pikes.add(pikeDetector.pikes)
        }
    }

    fun countInterpolatedData(range : IntRange, freq: Double) : DoubleArray
    {
        return convertLineInterp(range, freq)
    }

    private fun countFreq(range : IntRange) : Double
    {
        return 1000 / ((times[range.last] - times[range.first]).toDouble() / (range.last - range.first)) // +1 or not +1
    }

    private fun convertLineInterp(range : IntRange, freq : Double) : DoubleArray
    {
        interpTimes = DoubleArray(range.last - range.first + 1, { i -> times[range.start] + i * 1000 / freq})
        return DoubleArray(range.last - range.first + 1, { i -> lineInterpolate(range.start, times[range.start] + i * 1000 / freq) })
    }

    private fun lineInterpolate(start : Int, time : Double) : Double
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
            return data[i] + (data[i] - data[i-1]) * (time - times[i-1]) / (times[i] - times[i-1])
        }
    }

    private fun findClosest2Power(n : Int) : Int
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