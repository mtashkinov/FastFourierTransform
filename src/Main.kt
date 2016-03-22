import java.io.File

/**
 * Created by Mikhail on 25.02.2016.
 */

val size = 512
val AREA = 10
fun main(args : Array<String>)
{
    val inputDir = File("Input")
    val files = inputDir.listFiles()
    val outputDir = File("Output")
    outputDir.mkdir()
    clearDir(outputDir)

    for (file in files)
    {
        val input = Data(file, size)
        val fft = FFT(size)

        val res = fft.fft(input.data.toDoubleArray())

        val start = fromValueToIndex((60 / 30.0), size, input.freq)
        val end = fromValueToIndex((60 / 220.0), size, input.freq)
        //val index = getMaxIndex(res, start, end)
        val output = File(outputDir, "sp-" + file.name)
        output.createNewFile()

        printFFT(output, res, size, input.freq)
    }
}

fun clearDir(dir : File)
{
    val files = dir.listFiles()
    files.forEach { file -> file.delete() }
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

fun printFFT(file : File, fft : DoubleArray, size : Int, freq : Double)
{
    for (i in fft.indices)
    {
        file.appendText("${fromIndexToValue(i, size, freq) * 60};${fft[i]}\n")
    }
}

fun findPikeEnd(curPos : Int, fft: DoubleArray)
{
    var index = curPos
    while (index < fft.size && !isMin(index, fft))
    {
        ++index
    }
}

fun isMax(index : Int, fft: DoubleArray) : Boolean
{
    var start = 0
    if (index - AREA > 0) start = index - AREA
    var end = fft.size - 1
    if (index + AREA < fft.size - 1) end = index + AREA
    return fft.slice(start..end).max() == fft[index]
}

fun isMin(index : Int, fft: DoubleArray) : Boolean
{
    var start = 0
    if (index - AREA > 0) start = index - AREA
    var end = fft.size - 1
    if (index + AREA < fft.size - 1) end = index + AREA
    return fft.slice(start..end).min() == fft[index]
}