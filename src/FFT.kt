import PulseDetector.Companion.MAX_HEART_RATE
import PulseDetector.Companion.MIN_HEART_RATE

/**
 * Created by Mikhail on 26.02.2016.
 */
class FFT(private var size: Int)
{
    private var m : Int = 0

    private var cos : DoubleArray
    private var sin : DoubleArray

    init
    {
        this.m = (Math.log(size.toDouble()) / Math.log(2.0)).toInt()

        cos = DoubleArray(size / 2)
        sin = DoubleArray(size / 2)

        for (i in 0..size / 2 - 1)
        {
            cos[i] = Math.cos(-2.0 * Math.PI * i.toDouble() / size)
            sin[i] = Math.sin(-2.0 * Math.PI * i.toDouble() / size)
        }

    }

    fun fft(data: DoubleArray) : DoubleArray
    {
        var re = data.clone()
        var im = DoubleArray(data.size)

        var i: Int
        var j: Int
        var k: Int
        var n1: Int
        var n2: Int
        var a: Int
        var c: Double
        var s: Double
        var t1: Double
        var t2: Double

        j = 0
        n2 = size / 2
        i = 1
        while (i < size - 1)
        {
            n1 = n2
            while (j >= n1)
            {
                j = j - n1
                n1 = n1 / 2
            }
            j = j + n1

            if (i < j)
            {
                t1 = re[i]
                re[i] = re[j]
                re[j] = t1
                t1 = im[i]
                im[i] = im[j]
                im[j] = t1
            }
            i++
        }

        n2 = 1

        i = 0
        while (i < m)
        {
            n1 = n2
            n2 = n2 + n2
            a = 0

            j = 0
            while (j < n1)
            {
                c = cos[a]
                s = sin[a]
                a += 1 shl m - i - 1

                k = j
                while (k < size)
                {
                    t1 = c * re[k + n1] - s * im[k + n1]
                    t2 = s * re[k + n1] + c * im[k + n1]
                    re[k + n1] = re[k] - t1
                    im[k + n1] = im[k] - t2
                    re[k] = re[k] + t1
                    im[k] = im[k] + t2
                    k = k + n2
                }
                j++
            }
            i++
        }

        return DoubleArray(data.size, {i -> Math.sqrt(re[i] * re[i] + im[i] * im[i])})
    }

    companion object
    {
        fun fromValueToIndex(value: Double, size: Int, freq: Double): Int
        {
            val index = value * size / freq
            val res = index.toInt()
            if (fromIndexToValue(res, size, freq) < MIN_HEART_RATE)
            {
                return res + 1
            } else if (fromIndexToValue(res, size, freq) > MAX_HEART_RATE)
            {
                return res - 1
            } else
            {
                return res
            }
            //return index.toInt()
        }

        fun fromIndexToValue(index: Int, size: Int, freq: Double): Double
        {
            return index * freq / size
        }

        fun getMaxIndex(array: DoubleArray, start: Int, end: Int): Int
        {
            var max = array[start]
            var index = start
            for (i in start + 1..end)
            {
                if (array[i] > max)
                {
                    max = array[i]
                    index = i
                }
            }
            return index
        }
    }
}
