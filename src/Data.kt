import java.io.File
import java.util.*

/**
 * Created by Mikhail on 26.02.2016.
 */
class Data(val fileName : String, val size : Int)
{
    var times : MutableList<Long> = ArrayList()
        private set
    var data : MutableList<Double> = ArrayList()
        private set
    var freq : Double = 0.0
        private set


    init
    {
        val file = File(fileName)
        val scanner = Scanner(file)
        while (scanner.hasNextLine())
        {
            val line = scanner.nextLine()
            val lineScanner = Scanner(line)

            times.add(lineScanner.nextLong())
            data.add(lineScanner.next().toDouble())
            if (times.size > size)
            {
                times.removeAt(0)
                data.removeAt(0)
            }
        }
        scanner.close()

        countFreq()
    }

    private fun countFreq()
    {
        freq = 1000 / ((times[size - 1] - times[0]).toDouble() / size)
    }


}