/**
 * Created by Mikhail on 11.03.2017.
 */
class PulsePikeDetector
{
    fun getPulse(data: DoubleArray, timeStep: Double) : Int
    {
        var i = 1
        while ((i < data.size-1) and (data[i] * data[i+1] <= 0.0))
        {
            ++i
        }

        val start = i
        var end = start
        var count = 0

        for (i in start..data.lastIndex-1)
        {
            if (data[i] * data[i+1] <= 0.0)
            {
                ++count
                end = i + 1
            }
        }
        val pikes = (count - 1) / 2

        return Math.round(pikes * 1000 * 60 / (end - start) / timeStep).toInt()
    }
}