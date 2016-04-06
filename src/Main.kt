import java.io.File

/**
 * Created by Mikhail on 25.02.2016.
 */

fun main(args : Array<String>)
{
    val inputDir = File("Input")
    val files = inputDir.listFiles()
    val outputDir = File("Output")
    val interpDir = File("Interpolated")
    val resultFile = File("result.csv")
    resultFile.createNewFile()
    outputDir.mkdir()
    clearDir(outputDir)
    interpDir.mkdir()
    clearDir(interpDir)

    for (file in files)
    {
        val input = HeartRateData(file)

        // If data completed
        if (input.freq != 0.0)
        {
            /*print(file.name + " ")
        test(input).forEach { x -> print(x.toString()+ " ") }
        println()*/
            val pulseDetector = PulseDetector(input)
            val output = File(outputDir, "sp-" + file.name)
            val interpolatedOutput = File(interpDir, "d" + file.name)
            interpolatedOutput.createNewFile()
            output.createNewFile()

            var result = ""
            if (!pulseDetector.isBadData)
            {
                result = "${file.name} ${pulseDetector.pulse}"
            }
            else
            {
                result = "${file.name} Bad data"
            }
            println(result)
            resultFile.appendText(result + "\n")

            printFFT(output, pulseDetector.fft, input.size, input.freq)
            printData(interpolatedOutput, input)
        }
    }
}

fun clearDir(dir : File)
{
    val files = dir.listFiles()
    files.forEach { file -> file.delete() }
}

fun printFFT(file : File, fft : DoubleArray, size : Int, freq : Double)
{
    for (i in fft.indices)
    {
        file.appendText("${FFT.fromIndexToValue(i, size, freq) * 60};${fft[i]}\n")
    }
}

fun printData(file : File, data : HeartRateData)
{
    data.countInterpolatedData()
    for (i in 0..data.size-1)
    {
        file.appendText("${data.times[i]};${data.data[i]};;${data.interpTimes[i]};${data.interpData[i]}\n")
    }
}

fun test(data : HeartRateData) : List<Int>
{
    val size = data.size / 4
    val list = data.split(size)
    val firstIndex = FFT.fromValueToIndex(60 / 40.0, size, data.freq)
    val lastIndex = FFT.fromValueToIndex(60 / 220.0, size, data.freq)

    return list.map { x ->
        val fft = FFT(size)
        val res = fft.fft(x)
        (FFT.fromIndexToValue(FFT.getMaxIndex(res, firstIndex, lastIndex), size, data.freq) * 60).toInt()
    }
}