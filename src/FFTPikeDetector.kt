import java.util.*

/**
 * Created by Mikhail on 29.05.2016.
 */
class FFTPikeDetector(val fft : DoubleArray, val freq : Double, val size : Int)
{
    private var firstIndex = 0
    private var lastIndex = 0
    var pikes = ArrayList<Int>()

    init
    {
        firstIndex = FFT.fromValueToIndex(PulseDetector.MIN_HEART_RATE / 60, size, freq)
        lastIndex = FFT.fromValueToIndex(PulseDetector.MAX_HEART_RATE / 60, size, freq)
        findPikes()
    }
    private fun findPikes()
    {
        var oldPike = FFT.getMaxIndex(fft, firstIndex, lastIndex)
        var clearedPike = fft
        var newPike = checkBorderPike(fft, oldPike)
        while ((clearedPike[newPike] != 0.0) && (fft[newPike] / fft[oldPike] >= 0.75))
        {
            pikes.add(newPike)
            clearedPike = clearPike(clearedPike, newPike)
            oldPike = newPike
            newPike = FFT.getMaxIndex(clearedPike, firstIndex, lastIndex)
            newPike = checkBorderPike(clearedPike, newPike)
        }
    }

    private fun checkBorderPike(data : DoubleArray, startPos : Int) : Int
    {
        var index = startPos
        var fftClearedPike = data.clone()
        while (isBorder(index) && !isMax(data, index) && (fftClearedPike[index] != 0.0))
        {
            fftClearedPike = clearPike(fftClearedPike, index)
            index = FFT.getMaxIndex(fftClearedPike, firstIndex, lastIndex)
        }

        return index
    }

    private fun isBorder(index: Int) : Boolean
    {
        return (index <= firstIndex + AREA) ||  (index >= lastIndex - AREA)
    }

    companion object
    {
        private val AREA = 2

        fun clearPike(data: DoubleArray, index: Int) : DoubleArray
        {
            val pike = findPike(data, index)

            val clearedFFT = data.clone()
            for (i in pike)
            {
                clearedFFT[i] = 0.0
            }

            return clearedFFT
        }

        private fun findPike(data: DoubleArray, curPos: Int): IntRange
        {
            var start = curPos - 1
            var end = curPos + 1
            while (end < data.size - 1 && (!isMin(data, end) && !isMax(data, end)))
            {
                ++end
            }
            if (isMax(data, end))
            {
                --end
            }

            while (start > 0 && !isMin(data, start))
            {
                --start
            }

            if (start < 0)
            {
                start = 0
            }

            if (end >= data.size)
            {
                end = data.lastIndex
            }

            return start..end
        }

        private fun isMax(data: DoubleArray, index: Int): Boolean
        {
            var start = 0
            if (index - AREA > 0) start = index - AREA
            var end = data.size - 1
            if (index + AREA < data.size - 1) end = index + AREA
            return data.slice(start..end).max() == data[index]
        }

        private fun isMin(data: DoubleArray, index: Int): Boolean
        {
            var start = 0
            if (index - AREA > 0) start = index - AREA
            var end = data.size - 1
            if (index + AREA < data.size - 1) end = index + AREA
            return data.slice(start..end).min() == data[index]
        }
    }
}