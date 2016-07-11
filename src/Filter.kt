/**
 * Created by Mikhail on 10.06.2016.
 */
class Filter(size : Int, freq: Double, pulseRange: IntRange)
{
    private var firstDataSent = false
    private var firstData = 0.0
    private val highPassFilter = FreqFilter(pulseRange.first.toDouble() / 60, freq, FreqFilter.PassType.Highpass, 1.0)
    private val lowPassFilter = FreqFilter(pulseRange.last.toDouble() / 60, freq, FreqFilter.PassType.Lowpass, 1.0)
    private val amplitudeEqualizer = AmplitudeEqualizer(size, freq)

    fun addData(value : Double)
    {
        if (!firstDataSent)
        {
            firstData = value
            firstDataSent = true
        }

        lowPassFilter.Update(value-firstData)
        highPassFilter.Update(lowPassFilter.getValue())
        val filtered = highPassFilter.getValue()
        amplitudeEqualizer.addData(filtered)
    }

    fun getData() : DoubleArray
    {
        return amplitudeEqualizer.getData()
    }

    fun getShiftedData() : DoubleArray
    {
        return amplitudeEqualizer.getShiftedData()
    }

    fun removeData(dataToRemove : Int)
    {
        amplitudeEqualizer.removeData(dataToRemove)
    }

    fun isReady() : Boolean
    {
        return amplitudeEqualizer.isReady()
    }
}