import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import java.io.File
import java.util.*

/**
 * Created by Mikhail on 26.02.2016.
 */
class HeartRateData(val file : File)
{
    var times : MutableList<Long> = ArrayList()
        private set
    var data : MutableList<Double> = ArrayList()
        private set
    lateinit var interpTimes : DoubleArray
        private set
    lateinit var interpData : DoubleArray
        private set
    var freq : Double = 0.0
        private set
    var size = 0
        private set


    init
    {
        val lines = file.readLines()
        size = findClosest2Power(lines.size)

        for (x in lines)
        {
            val line = x.replace(";", " ")
            val lineScanner = Scanner(line)

            times.add(lineScanner.nextLong())
            data.add(lineScanner.next().toDouble())
            if (times.size > size)
            {
                times.removeAt(0)
                data.removeAt(0)
            }
            lineScanner.close()
        }

        countFreq()
    }

    fun countInterpolatedData()
    {
        convert()
    }

    private fun countFreq()
    {
        freq = 1000 / ((times[size - 1] - times[0]).toDouble() / size)
    }

    fun split(partSize : Int) : List<DoubleArray>
    {
        var list = ArrayList<DoubleArray>()
        val shift = partSize
        synchronized(data)
        {
            var end = data.size
            while (end >= partSize)
            {
                list.add(DoubleArray(partSize, { i -> data[end - partSize + i] }))
                end -= shift
            }
        }

        return list
    }

    private fun convert()
    {
        val funct = interpolate()
        interpTimes = DoubleArray(size, { i -> times[0] + i * 1000 / freq })
        interpData = DoubleArray(size, { i -> funct.value(times[0] + i * 1000 / freq) })
    }

    private fun interpolate() : PolynomialSplineFunction
    {
        val interpolator = SplineInterpolator()
        return interpolator.interpolate(times.map { x -> x.toDouble() }.toDoubleArray(), data.toDoubleArray())
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