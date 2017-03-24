/**
 * Created by Mikhail on 11.03.2017.
 */
class PulsePikeDetector
{
    private val PIKE_SIZE_THRESHOLD = 3
    private val PIKE_VALUE_THRESHOLD = 0.1

    fun getPulse(data: DoubleArray, timeStep: Double) : Int
    {
        var i = 1
        while ((i < data.size-1) and (data[i] * data[i+1] >= 0.0))
        {
            ++i
        }

        val start = i
        var end = start
        var prevPike = (start..end)
        var count = 0

        for (i in start..data.lastIndex-1)
        {
            if (data[i] * data[i+1] <= 0.0)
            {
                if (isPikeValid(data, (prevPike.last..i + 1), prevPike))
                {
                    ++count
                    end = i + 1
                    prevPike = (prevPike.last..end)
                }
            }
        }
        val pikes = (count - 1) / 2

        return Math.round(pikes * 1000 * 60 / (end - start) / timeStep).toInt()
    }

    private fun isPikeValid(data: DoubleArray, pike: IntRange, prevPike: IntRange) : Boolean
    {
        return !(((pike.last - pike.first + 1) <= PIKE_SIZE_THRESHOLD) &&
                (data.slice(pike).maxBy {Math.abs(it)}!! < data.slice(prevPike).maxBy {Math.abs(it)}!! * PIKE_VALUE_THRESHOLD))
    }
}