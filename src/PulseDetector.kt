/**
 * Created by Mikhail on 23.03.2016.
 */
class PulseDetector(private val data: HeartRateData)
{
    private val AREA = 3
    val MIN_HEART_RATE = 40.0
    val MAX_HEART_RATE = 220.0
    private val USUAL_HEART_RATE = 80.0

    lateinit var fft : DoubleArray
    private set
    var pulse = 0
    private set
    var isBadData = false
    lateinit var interpolatedData : DoubleArray

    private var firstIndex = 0
    private var lastIndex = 0

    init
    {
        val fftDetector = FFT(data.size)
        firstIndex = FFT.fromValueToIndex(MIN_HEART_RATE / 60, data.size, data.freq)
        lastIndex = FFT.fromValueToIndex(MAX_HEART_RATE / 60, data.size, data.freq)
        synchronized(data.data)
        {
            data.countInterpolatedData()
            interpolatedData = data.interpData
            fft = fftDetector.fft(interpolatedData)
            val index = findMaxPike()
            pulse = (FFT.fromIndexToValue(index, data.size, data.freq) * 60).toInt()
        }
    }

    private fun findMaxPike() : Int
    {
        val index = FFT.getMaxIndex(fft, firstIndex, lastIndex)
        val oldMax = fft[index]
        var result = checkBorderPike(fft, index)

        // Check for bad data
        if (fft[result].toDouble() / oldMax < 0.75)
        {
            isBadData = true
        } else
        {
            val clearedPike = clearPike(fft, result)
            var sndResult = FFT.getMaxIndex(clearedPike, firstIndex, lastIndex)
            checkBorderPike(clearedPike, sndResult)
            val usualHRIndex = FFT.fromValueToIndex(USUAL_HEART_RATE / 60, data.size, data.freq)
            if ((fft[sndResult] / fft[result] > 0.75) && (Math.abs(sndResult - usualHRIndex) < Math.abs(result - usualHRIndex)))
            {
                result = sndResult
            }
        }

        return result
    }

    private fun checkBorderPike(data : DoubleArray, startPos : Int) : Int
    {
        var index = startPos
        var fftClearedPike = data.clone()
        while (isBorder(index) && !isMax(index))
        {
            fftClearedPike = clearPike(fftClearedPike, index)
            index = FFT.getMaxIndex(fftClearedPike, firstIndex, lastIndex)
        }

        return index
    }

    private fun clearPike(data: DoubleArray, index: Int) : DoubleArray
    {
        val pikeEnds = findPikeEnds(index)

        var indices : List<Int>

        indices = data.indices.filter { i -> i >= pikeEnds[0] && i <= pikeEnds[1] }

        val clearedFFT = data.clone()
        for (i in indices)
        {
            clearedFFT[i] = 0.0
        }

        return clearedFFT
    }

    private fun findPikeEnds(curPos : Int) : IntArray
    {
        var start = curPos
        var end = curPos
        while (end < fft.size - 1 && (!isMin(end) && !isMax(end)))
        {
            ++end
        }

        while (start > 0 && !isMin(start))
        {
            --start
        }


        return intArrayOf(start, end)
    }

    private fun isBorder(index: Int) : Boolean
    {
        return (index <= firstIndex + AREA) ||  (index >= lastIndex - AREA)
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