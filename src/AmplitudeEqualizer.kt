import java.util.*

/**
 * Created by Mikhail on 12.06.2016.
 */
class AmplitudeEqualizer(private val size : Int, private val freq : Double)
{
    private val START_SHIFT_COEF = 0.8

    private val partSize = freq * 60 / PulseDetector.MIN_HEART_RATE
    private var sourceData = ArrayList<Double>(size)
    var equalizedData = ArrayList<Double>(size)
    private set
    private var maxEnergy = 0.0

    fun isReady() : Boolean
    {
        return sourceData.size == size
    }

    fun addData(newData: Double)
    {
        if (sourceData.size == size)
        {
            sourceData.removeAt(0)
            equalizedData.removeAt(0)
        }

        sourceData.add(newData)

        if (isPartEnd())
        {
            val part = equalizedData.size..sourceData.lastIndex
            val energy = countEnergy(part)
            if (energy > maxEnergy)
            {
                recorrectSignal(energy/maxEnergy)
                maxEnergy = energy
            }
            addCorrectedSignal(energy, part)
        }
    }

    fun removeData(dataToRemove : Int)
    {
        for (i in 1..dataToRemove)
        {
            sourceData.removeAt(0)
            equalizedData.removeAt(0)
        }
    }

    fun getData() : DoubleArray
    {
        val part = equalizedData.size..sourceData.lastIndex
        val energy = countEnergy(part)
        var output : MutableList<Double>
        if (energy > maxEnergy)
        {
            output = recorrectSignalForOutput(energy/maxEnergy)
        } else
        {
            output = equalizedData.clone() as ArrayList<Double>
        }
        addCorrectedSignalForOutput(output, energy, part)

        return output.toDoubleArray()
    }

    private fun isPartEnd() : Boolean
    {
        if (equalizedData.size + partSize * START_SHIFT_COEF <= sourceData.lastIndex)
        {
            return (sourceData.last() * sourceData[sourceData.lastIndex - 1] <= 0)
        }
        else
        {
            return false
        }
    }

    private fun countEnergy(part : IntRange) : Double
    {
        var energy = 0.0
        for (i in part)
        {
            energy += sourceData[i] * sourceData[i]
        }

        return energy / (part.last - part.first + 1)
    }

    private fun addCorrectedSignal(partEnergy : Double, part : IntRange)
    {
        addCorrectedSignalForOutput(equalizedData, partEnergy, part)
    }

    private fun recorrectSignal(correctCoef : Double)
    {
        for (i in equalizedData.indices)
        {
            equalizedData[i] = equalizedData[i] * correctCoef
        }
    }

    private fun recorrectSignalForOutput(correctCoef: Double) : ArrayList<Double>
    {
        val result = ArrayList<Double>()
        for (i in equalizedData.indices)
        {
            result.add(equalizedData[i] * correctCoef)
        }

        return result
    }

    private fun addCorrectedSignalForOutput(data: MutableList<Double>, partEnergy : Double, part : IntRange)
    {
        val energyCoef = Math.sqrt(maxEnergy / partEnergy)
        for (index in part)
        {
            data.add(sourceData[index] * energyCoef)
        }
    }

    private fun findShift(data : DoubleArray) : Double
    {
        val maxs = DoubleArray(3)
        val mins = DoubleArray(3)

        for (value in data)
        {
            when
            {
                value >= maxs[0] ->
                {
                    maxs[2] = maxs[1]
                    maxs[1] = maxs[0]
                    maxs[0] = value
                }
                value >= maxs[1] ->
                {
                    maxs[2] = maxs[1]
                    maxs[1] = value
                }
                value > maxs[2] -> maxs[2] = value
                value <= mins[0] ->
                {
                    mins[2] = mins[1]
                    mins[1] = mins[0]
                    mins[0] = value
                }
                value <= mins[1] ->
                {
                    mins[2] = mins[1]
                    mins[1] = value
                }
                value < mins[2] -> mins[2] = value
            }
        }

        return -(mins[2] + maxs[2]) / 2
    }

    fun getShiftedData() : DoubleArray
    {
        val data = getData()
        val shift = findShift(data)

        return DoubleArray(data.size, { i -> data[i] + shift })
    }
}