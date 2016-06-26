import java.util.*

/**
 * Created by Mikhail on 10.06.2016.
 */
class Filter
{
    fun apply(data : DoubleArray, freq : Double) : DoubleArray
    {
        val filtered = DoubleArray(data.size)
        val highPassFilter = FreqFilter((PulseDetector.MIN_HEART_RATE - 0)/ 60, freq, FreqFilter.PassType.Highpass, 1.0)
        val lowPassFilter = FreqFilter((PulseDetector.MAX_HEART_RATE + 0) / 60, freq, FreqFilter.PassType.Lowpass, 1.0)
        for (i in data.indices)
        {
            lowPassFilter.Update(data[i]-data[0])
            highPassFilter.Update(lowPassFilter.getValue())
            filtered[i] = highPassFilter.getValue()
        }
        val amplitudeFilter = AmplitudeFilter()

        return amplitudeFilter.apply(filtered, freq)
    }

    fun apply1(data : DoubleArray, freq : Double) : DoubleArray
    {
        val filtered = DoubleArray(data.size)
        val highPassFilter = FreqFilter(PulseDetector.MIN_HEART_RATE / 60, freq, FreqFilter.PassType.Highpass, 1.0)
        val lowPassFilter = FreqFilter(PulseDetector.MAX_HEART_RATE / 60, freq, FreqFilter.PassType.Lowpass, 1.0)
        for (i in data.indices)
        {
            lowPassFilter.Update(data[i] - data[0])
            highPassFilter.Update(lowPassFilter.getValue())
            filtered[i] = highPassFilter.getValue()
        }

        return filtered
    }

    fun apply2(data : DoubleArray, freq : Double) : DoubleArray
    {
        val amplitudeFilter = AmplitudeFilter()

        return amplitudeFilter.apply(data, freq)
    }
}