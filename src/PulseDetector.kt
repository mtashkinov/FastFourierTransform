import java.util.*

/**
 * Created by Mikhail on 23.03.2016.
 */
class PulseDetector(private val fft : DoubleArray, val size : Int, val pikes : MutableList<MutableList<Int>>, val partSize : Int, val freq: Double)
{
    companion object
    {
        val MIN_HEART_RATE = 40.0
        val MAX_HEART_RATE = 220.0
    }

    private val ACCEPT_COEF = 0.4

    private var discretization = 0.0
    var pulse = 0
    private set
    var jointPikes = ArrayList<Double>()
    var votes = ArrayList<Int>()
    var filteredPikes : MutableList<Double> = ArrayList()
    var isBadData = false

    init
    {
        getDiscretization()
        countPikeEntries()
        filterByVotes(1.0)
        addDoubledVotes(findDoubledPikes())
        filterByVotes(pikes.size * ACCEPT_COEF)
        filterByMaxDoubledVotes()
        //filterDoubledPulsePikes()

        if (filteredPikes.size == 0)
        {
            isBadData = true
        } else
        {
            findPulse()
        }
    }

    fun countPikeEntries()
    {
        for (partNum in pikes.indices)
        {
            for (pike in pikes[partNum])
            {
                val pikePulse = FFT.fromIndexToValue(pike, partSize, freq) * 60
                val existedPikeIndex = jointPikes.indices.find { x -> Math.abs(pikePulse - jointPikes[x]) <= discretization }
                if (existedPikeIndex == null)
                {
                    jointPikes.add(pikePulse)
                    votes.add(1)
                }
                else
                {
                    ++votes[existedPikeIndex]
                }
            }
        }
    }

    fun findPulse()
    {
        val firstIndex = FFT.fromValueToIndex(PulseDetector.MIN_HEART_RATE / 60, size, freq)
        val lastIndex = FFT.fromValueToIndex(PulseDetector.MAX_HEART_RATE / 60, size, freq)
        var index = FFT.getMaxIndex(fft, firstIndex, lastIndex)
        var fftClearedPike = fft.clone()
        while ((fftClearedPike[index] != 0.0) && (filteredPikes.find { x -> isPikeCloseToIndex(index, x)} == null))
        {
            fftClearedPike = PikeDetector.clearPike(fftClearedPike, index)
            index = FFT.getMaxIndex(fftClearedPike, firstIndex, lastIndex)
        }

        if (fftClearedPike[index] == 0.0)
        {
            isBadData = true
        }

        pulse = Math.round(FFT.fromIndexToValue(index, size, freq) * 60).toInt()
    }

    fun isPikeCloseToIndex(index : Int, pike : Double) : Boolean
    {
        return Math.abs(FFT.fromIndexToValue(index, size, freq) * 60 - pike) < discretization + 0.1

    }

    fun findDoubledPikes() : ArrayList<Double>
    {
        val doubledPikes = ArrayList<Double>()
        for (pike in jointPikes)
        {
            if ((pike > 100) && (filteredPikes.find { x -> Math.abs(pike / 2 - x) < discretization * 2 } != null))
            {
                doubledPikes.add(pike)
            }
        }

        return doubledPikes
    }

    fun filterByVotes(threshold : Double)
    {
        val resultPikes = ArrayList<Double>()
        val filteredIndices = votes.indices.filter { index -> (votes[index] > threshold)}
        filteredIndices.forEach { x -> resultPikes.add(jointPikes[x]) }

        filteredPikes = resultPikes
    }

    fun addDoubledVotes(doubledPikes : ArrayList<Double>)
    {
        for (doublePike in doubledPikes)
        {
            val votesForDoubled = votes[jointPikes.indexOf(doublePike)]
            val foundPikes = filteredPikes.filter { x -> Math.abs(doublePike / 2 - x) < discretization * 2 }
            for (pike in foundPikes)
            {
                val index = jointPikes.indexOf(pike)
                votes[index] += votesForDoubled
            }
        }
    }

    fun filterByMaxDoubledVotes()
    {
        val resultPikes = ArrayList<Double>()
        val doubledPikes = findDoubledPikes()
        val doubledVotes = IntArray(filteredPikes.size)

        for (doublePike in doubledPikes)
        {
            val votesForDoubled = votes[jointPikes.indexOf(doublePike)]
            val foundPikes = filteredPikes.filter { x -> Math.abs(doublePike / 2 - x) < discretization * 2 }
            for (pike in foundPikes)
            {
                val index = filteredPikes.indexOf(pike)
                doubledVotes[index] += votesForDoubled
            }
        }

        val max = doubledVotes.max()
        val filteredIndices = doubledVotes.indices.filter { index -> (doubledVotes[index] == max)}
        filteredIndices.forEach { x -> resultPikes.add(filteredPikes[x]) }

        filteredPikes = resultPikes
    }

    fun getDiscretization()
    {
        discretization = (FFT.fromIndexToValue(1, partSize, freq) * 60 +
                         FFT.fromIndexToValue(1, size, freq) * 60) / 2
    }
}