/**
 * Created by Mikhail on 25.02.2016.
 */

val size = 1024
fun main(args : Array<String>)
{

    val input = Data("input.txt", size)
    val fft = FFT(size)

    val res = fft.fft(input.data.toDoubleArray())

    val index = getMaxIndex(res, fromValueToIndex((60 / 30.0), size, input.freq), fromValueToIndex((60 / 220.0), size, input.freq))
    print(fromIndexToValue(index, size, input.freq) * 60)
}


fun fromValueToIndex(value : Double, size : Int, freq : Double) : Int
{
    val index = size / (value * freq)
    return index.toInt()
}

fun fromIndexToValue(index : Int, size : Int, freq : Double) : Double
{
    return index * freq / size
}

fun getMaxIndex(array : DoubleArray, start : Int, end : Int) : Int
{
    var max = array[start]
    var index = start
    for (i in start + 1..end)
    {
        if (array[i] > max)
        {
            max = array[i]
            index = i
        }
    }
    return index
}

fun generate() : DoubleArray
{
    return DoubleArray(size, {x -> Math.cos(x * 0.1)})
}