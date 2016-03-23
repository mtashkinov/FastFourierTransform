/**
 * Created by Mikhail on 23.03.2016.
 */
class PulseDetector(private val data: HeartRateData)
{
    val MIN_HEART_RATE = 40.0
    val MAX_HEART_RATE = 220.0

    lateinit var fft : DoubleArray
    private set
    var pulse = 0
    private set
    var isBadData = false

    private var firstIndex = 0
    private var lastIndex = 0

    init
    {
        val fftDetector = FFT(data.size)
        firstIndex = FFT.fromValueToIndex(60 / MIN_HEART_RATE, data.size, data.freq)
        lastIndex = FFT.fromValueToIndex(60 / MAX_HEART_RATE, data.size, data.freq)
        synchronized(data.data)
        {
            fft = fftDetector.fft(data.data.toDoubleArray())
            val index = findMaxPike()
            pulse = (FFT.fromIndexToValue(index, data.size, data.freq) * 60).toInt()
        }
    }

    private fun findMaxPike() : Int
    {
        var index = FFT.getMaxIndex(fft, firstIndex, lastIndex)
        var oldMax = fft[index]
        if (isBorder(index) && !isMax(index))
        {
            oldMax = fft[index]
            var fftClearedBorder = clearBorder(index)
            index = FFT.getMaxIndex(fftClearedBorder, firstIndex, lastIndex)
            if (isBorder(index) && !isMax(index))
            {
                fftClearedBorder = clearBorder(index)
                index = FFT.getMaxIndex(fftClearedBorder, firstIndex, lastIndex)
            }
        }

        if (fft[index].toDouble() / oldMax < 0.75)
        {
            isBadData = true
        }
        return index
    }

    private fun clearBorder(index: Int) : DoubleArray
    {
        val borderEnd = findBorderPikeEnd(index)

        var indices : List<Int>
        if (index < borderEnd)
        {
            indices = fft.indices.filter { i -> i >= index && i <= borderEnd }
        } else
        {
            indices = fft.indices.filter { i -> i <= index && i >= borderEnd }
        }

        val clearedFFT = fft.clone()
        for (i in indices)
        {
            clearedFFT[i] = 0.0
        }

        return clearedFFT
    }

    private fun findBorderPikeEnd(curPos : Int) : Int
    {
        var index = curPos
        if (index == firstIndex)
        {
            while (index < fft.size - 1 && !isMin(index))
            {
                ++index
            }
        } else if (index == lastIndex)
        {
            while (index > 0 && !isMin(index))
            {
                --index
            }
        }

        return index
    }

    private fun isBorder(index: Int) : Boolean
    {
        return (index == firstIndex) ||  (index == lastIndex)
    }

    private fun isMax(index : Int) : Boolean
    {
        var start = 0
        if (index - AREA > 0) start = index - AREA
        var end = fft.size - 1
        if (index + AREA < fft.size - 1) end = index + AREA
        return fft.slice(start..end).max() == fft[index]
    }

    private fun isMin(index : Int) : Boolean
    {
        var start = 0
        if (index - AREA > 0) start = index - AREA
        var end = fft.size - 1
        if (index + AREA < fft.size - 1) end = index + AREA
        return fft.slice(start..end).min() == fft[index]
    }
}