/**
 * Created by Mikhail on 22.02.2016.
 */

class FFT
{
    fun fft(input : DoubleArray) : DoubleArray
    {
        val n = 2 * input.size
        var tmvl = DoubleArray(n)
        var output = DoubleArray(input.size)

        for (i in 0..n-1 step 2)
        {
            tmvl[i] = 0.0
            tmvl[i + 1] = input[i / 2]
        }

        var i = 1
        var j = 1
        while (i < n)
        {
            if (j > i)
            {
                var tmp = tmvl[i]
                tmvl[i] = tmvl[j]
                tmvl[j] = tmp

                tmp = tmvl[i + 1]
                tmvl[i + 1] = tmvl[j + 1]
                tmvl[j + 1] = tmp
            }
            i += 2
            var m = input.size
            while ((m >= 2) && (j > m))
            {
                j -= m
                m = m shr 1
            }
            j += m

            var mMax = 2
            while (n > mMax)
            {
                val theta = -Math.PI * 2 / mMax
                val wPi = Math.sin(theta)
                val wTmp = Math.sin(theta / 2)
                val wpr = wTmp * wTmp * 2
                val istp = mMax * 2
                var wr = 1.0
                var wi = 0.0
                m = 1

                while (m < mMax)
                {
                    i = m
                    m += 2
                    var tmpr = wr
                    var tmpi = wi
                    wr = wr - tmpr * wpr - tmpi * wPi
                    wi = wi + tmpr * wPi - tmpi * wpr

                    while (i < n)
                    {
                        j = i + mMax
                        tmpr = wr * tmvl[j] - wi * tmvl[j - 1]
                        tmpi = wi * tmvl[j] + wr * tmvl[j - 1]

                        tmvl[j] = tmvl[i] - tmpr
                        tmvl[j - 1] = tmvl[i - 1] - tmpi
                        tmvl[i] = tmvl[i] + tmpr
                        tmvl[i - 1] = tmvl[i - 1] + tmpi
                        i += istp
                    }
                }

                mMax = istp
            }

            for (l in input.indices)
            {
                j = l * 2
                output[l] = 2 * Math.sqrt(Math.pow(tmvl[j], 2.0)) + Math.pow(tmvl[j + 1], 2.0) / input.size
            }
        }

        return output
    }
}