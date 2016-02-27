/**
 * Created by Mikhail on 26.02.2016.
 */
class FFT(internal var n : Int)
{
    internal var m : Int = 0

    internal var cos : DoubleArray
    internal var sin : DoubleArray

    init
    {
        this.m = (Math.log(n.toDouble()) / Math.log(2.0)).toInt()

        cos = DoubleArray(n / 2)
        sin = DoubleArray(n / 2)

        for (i in 0..n / 2 - 1)
        {
            cos[i] = Math.cos(-2.0 * Math.PI * i.toDouble() / n)
            sin[i] = Math.sin(-2.0 * Math.PI * i.toDouble() / n)
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
        n2 = n / 2
        i = 1
        while (i < n - 1)
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
                while (k < n)
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
}
