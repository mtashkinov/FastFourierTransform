/**
 * Created by Mikhail on 11.07.2016.
 */
class RRPikesCounter
{
    companion object
    {
        fun countPikes(data : DoubleArray) : Int
        {
            var firstEntry = -1
            var lastEntry = -1
            var entries = -1

            for (i in 1..data.lastIndex)
            {
                if (data[i] * data[i-1] <= 0 && data[i-1] < 0)
                {
                    if (firstEntry == -1)
                    {
                        firstEntry = i - 1
                    }
                    entries++
                    lastEntry = i - 1
                }
            }

            if (entries > 0)
            {
                if ((lastEntry - firstEntry + 1) / entries < data.size - lastEntry + firstEntry + 1)
                {
                    entries++
                }
            }

            return entries
        }
    }
}