import java.util.*

/**
 * Created by Mikhail on 12.06.2016.
 */
class AmplitudeFilter
{
    private val START_SHIFT_COEF = 0.8

    fun apply(data : DoubleArray, freq : Double) : DoubleArray
    {
        val partSize = freq * 60 / PulseDetector.MIN_HEART_RATE
        var end = -1
        val energies = ArrayList<Double>()
        val parts = ArrayList<IntRange>()

        while (end != data.size - 1)
        {
            val start = end + 1
            val searchStart = end + (partSize * START_SHIFT_COEF).toInt()
            end = findPartEnd(data, searchStart)
            energies.add(countEnergy(data, start..end))
            parts.add(start..end)
        }

        return correctSignal(data, energies, parts)
    }

    private fun findPartEnd(data: DoubleArray, searchStart : Int) : Int
    {
        var i = searchStart
        while ((i < data.size) && (data[i-1]*data[i] > 0))
        {
            ++i
        }
        --i

        if (i >= data.size)
        {
            i = data.lastIndex
        }
        return i
    }

    private fun countEnergy(data: DoubleArray, part : IntRange) : Double
    {
        var energy = 0.0
        for (i in part)
        {
            energy += data[i] * data[i]
        }

        return energy / (part.last - part.first + 1)
    }

    private fun correctSignal(data: DoubleArray, energies : MutableList<Double>, parts : ArrayList<IntRange>) : DoubleArray
    {
        val resultData = DoubleArray(data.size)
        val maxEnergy = energies.max()!!
        for (i in energies.indices)
        {
            val energyCoef = Math.sqrt(maxEnergy / energies[i])
            for (index in parts[i])
            {
                resultData[index] = data[index] * energyCoef
            }
        }

        return resultData
    }
}