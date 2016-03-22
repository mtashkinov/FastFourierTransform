import java.io.File
import java.util.*

/**
 * Created by Mikhail on 26.02.2016.
 */
class Data(val file : File, val size : Int)
{
    var times : MutableList<Long> = ArrayList()
        private set
    var data : MutableList<Double> = ArrayList()
        private set
    var freq : Double = 0.0
        private set


    init
    {
        val scanner = Scanner(file)
        while (scanner.hasNextLine())
        {
            var line = scanner.nextLine()
            line = line.replace(";", " ")
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
        scanner.close()

        countFreq()
    }

    private fun countFreq()
    {
        freq = 1000 / ((times[size - 1] - times[0]).toDouble() / size)
    }


}