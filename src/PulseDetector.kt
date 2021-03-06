import java.util.*

/**
 * Created by Mikhail on 23.03.2016.
 */
class PulseDetector(private val fft : DoubleArray, val size : Int, val pikes : MutableList<MutableList<Int>>,
                    val partSize : Int, val freq: Double, val prevPulse : Int)
{
    companion object
    {
        val MIN_HEART_RATE = 45.0
        val MAX_HEART_RATE = 220.0
    }

    private val ACCEPT_COEF = 0.4
    private val TRIES = 5

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
        //getPulseExpectedInterval()
        if (prevPulse != 0)
        {
            correctDoubledPikes()
        }
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
        var curTries = 0
        val firstIndex = FFT.fromValueToIndex(PulseDetector.MIN_HEART_RATE / 60, size, freq)
        val lastIndex = FFT.fromValueToIndex(PulseDetector.MAX_HEART_RATE / 60, size, freq)
        var index = FFT.getMaxIndex(fft, firstIndex, lastIndex)
        var fftClearedPike = fft.clone()
        while ((curTries <= TRIES) and (fftClearedPike[index] != 0.0) and (filteredPikes.find { x -> isPikeCloseToIndex(index, x)} == null))
        {
            fftClearedPike = FFTPikeDetector.clearPike(fftClearedPike, index)
            index = FFT.getMaxIndex(fftClearedPike, firstIndex, lastIndex)
            ++curTries
        }

        if (filteredPikes.find { x -> isPikeCloseToIndex(index, x)} == null)
        {
            isBadData = true
        }

        pulse = Math.round(FFT.fromIndexToValue(index, size, freq) * 60).toInt()
    }

    fun isPikeCloseToIndex(index : Int, pike : Double) : Boolean
    {
        return Math.abs(FFT.fromIndexToValue(index, size, freq) * 60 - pike) < discretization + 0.1
    }

    fun correctDoubledPikes()
    {
        val doubledPikes = findDoubledPikes()
        val doubledPikesInFiltered = filteredPikes.filter { x -> doubledPikes.contains(x) }
        for (doublePike in doubledPikesInFiltered)
        {
            val foundPikes = filteredPikes.filter { x -> Math.abs(doublePike / 2 - x) < discretization * 2 }

            val diff = Math.abs(doublePike - prevPulse)
            val pikesToDrop = foundPikes.filter { Math.abs(it - prevPulse) > diff }

            for (pike in pikesToDrop)
            {
                filteredPikes.remove(pike)
            }
        }
    }

    fun findDoubledPikes() : ArrayList<Double>
    {
        val doubledPikes = jointPikes.filterTo(ArrayList<Double>()) { (it > 100) && (filteredPikes.find { x -> Math.abs(it / 2 - x) < discretization * 2 } != null) }

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
            val doubledPikeIndex = jointPikes.indexOf(doublePike)
            val votesForDoubled = votes[doubledPikeIndex]
            val foundPikes = filteredPikes.filter { x -> Math.abs(doublePike / 2 - x) < discretization * 2 }
            for (pike in foundPikes)
            {
                val index = jointPikes.indexOf(pike)
                votes[index] += votesForDoubled
                if (filteredPikes.contains(doublePike))
                {
                    votes[doubledPikeIndex] += votes[index]
                }
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
            foundPikes
                    .map { filteredPikes.indexOf(it) }
                    .forEach { doubledVotes[it] += votesForDoubled }
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