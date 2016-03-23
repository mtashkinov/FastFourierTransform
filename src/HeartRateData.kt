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

    private fun countFreq()
    {
        freq = 1000 / ((times[size - 1] - times[0]).toDouble() / size)
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