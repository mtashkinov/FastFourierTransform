import java.io.File

/**
 * Created by Mikhail on 25.02.2016.
 */

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
        val input = HeartRateData(file)

        val pulseDetector = PulseDetector(input)
        val output = File(outputDir, "sp-" + file.name)
        output.createNewFile()

        if (!pulseDetector.isBadData)
        {
            println("${file.name} ${pulseDetector.pulse}")
        } else
        {
            println("${file.name} Bad data")
        }

        printFFT(output, pulseDetector.fft, input.size, input.freq)
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