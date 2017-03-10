/**
 * Created by Mikhail on 10.06.2016.
 */
class Filter(size : Int, freq: Double)
{
    private var firstDataSent = false
    private var firstData = 0.0
    private var highPassFilter = FreqFilter(PulseDetector.MIN_HEART_RATE / 60, freq, FreqFilter.PassType.Highpass, 1.0)
    private var lowPassFilter = FreqFilter(PulseDetector.MAX_HEART_RATE / 60, freq, FreqFilter.PassType.Lowpass, 1.0)
    private val amplitudeEqualizer = AmplitudeEqualizer(size, freq)

    constructor(size: Int, freq: Double, minHeartRate: Int, maxHeartRate: Int) : this(size, freq)
    {
        highPassFilter = FreqFilter(minHeartRate.toDouble() / 60, freq, FreqFilter.PassType.Highpass, 1.0)
        lowPassFilter = FreqFilter(maxHeartRate.toDouble() / 60, freq, FreqFilter.PassType.Lowpass, 1.0)
    }

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

    fun removeData(dataToRemove : Int)
    {
        amplitudeEqualizer.removeData(dataToRemove)
    }

    fun isReady() : Boolean
    {
        return amplitudeEqualizer.isReady()
    }
}