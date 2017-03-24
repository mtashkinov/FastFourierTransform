import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Mikhail on 25.02.2016.
 */

fun main(args : Array<String>)
{
    val inputDir = File("Input")
    val files = inputDir.listFiles().toMutableList()
    val outputDir = File("Output")
    val filtDir = File("Filtered")
    val resultFile = File("result.csv")
    resultFile.delete()
    resultFile.createNewFile()
    outputDir.mkdir()
    clearDir(outputDir)
    filtDir.mkdir()
    clearDir(filtDir)

    Collections.sort(files, { f1 : File, f2 : File ->
        val df = SimpleDateFormat("dd.MM.yyyy-hhmmss")
        df.parse(f1.name).compareTo(df.parse(f2.name))
    })

    for (file in files)
    {
        val input = HeartRateData(file)

        var result = "${file.name},"
        for (i in input.estimatedPulseHistory.indices)
        {
            if (!input.isBadDataHistory[i])
            {
                result += "${input.pulseHistory[i]}(${input.estimatedPulseHistory[i]}),"
            }
            else
            {
                result += "-,"
            }
        }
        println(result.replace(',', ' '))
        resultFile.appendText(result + "\n")


        val strongDataFile = File(filtDir, "st-" + file.name)
        printStrongData(strongDataFile, input)
        /*val filteredDataFile = File(filtDir, "sp-" + file.name)
        printFilteredData(filteredDataFile, input)*/
        /*val filteredDataFile = File(filtDir, file.name)
        printData(filteredDataFile, input)*/
        /*val spFile = File(filtDir, "sp-" + file.name)
        printFFT(spFile, input.totalFFT, input.data.size, input.totalFreq)*/
        /*val spFile = File(filtDir, "psp-" + file.name)
        printPartsFFT(spFile, input)*/
    }
}

fun clearDir(dir : File)
{
    val files = dir.listFiles()
    files.forEach { file -> file.delete() }
}

/*fun printPartsFFT(file: File, data : HeartRateData)
{
    var size = FFT.fromValueToIndex(PulseDetector.MAX_HEART_RATE / 60, data.partSize, data.freq) -
               FFT.fromValueToIndex(PulseDetector.MIN_HEART_RATE / 60, data.partSize, data.freq) + 1
    val lines = ArrayList<String>(size)
    for (i in 0..size-1)
    {
        lines.add("")
    }

    for (i in data.partsFFT.indices)
    {
        val start = FFT.fromValueToIndex(PulseDetector.MIN_HEART_RATE / 60, data.partSize, data.freq)
        val end = FFT.fromValueToIndex(PulseDetector.MAX_HEART_RATE / 60, data.partSize, data.freq)
        if (end - start + 1 > size)
        {
            var str = ""
            for (l in 0..i-1)
            {
                str += ";;"
            }
            for (l in size..end-start)
            {
                lines.add(str)
            }
            size = end - start + 1
        }
        for (j in start..end)
        {
            lines[j - start] += "${FFT.fromIndexToValue(j, data.partSize, data.freq) * 60};${data.partsFFT[i][j]};"
        }
        for (j in end-start+1..size - 1)
        {
            lines[j] += ";;"
        }
    }

    for (line in lines)
    {
        file.appendText(line + "\n")
    }
}*/

fun printFFT(file : File, data: HeartRateData)
{
    val firstIndex = FFT.fromValueToIndex(PulseDetector.MIN_HEART_RATE / 60, data.size, data.freq)
    val lastIndex = FFT.fromValueToIndex(PulseDetector.MAX_HEART_RATE / 60, data.size, data.freq)
    for (i in firstIndex..lastIndex)
    {
        file.appendText("${FFT.fromIndexToValue(i, data.size, data.freq) * 60};")
        for (j in data.totalFFTHistory.indices)
        {
            file.appendText("${data.totalFFTHistory[j][i]};")
        }
        file.appendText("\n")
    }
}

/*fun printData(file : File, data : HeartRateData)
{
    for (i in data.filteredData.indices)
    {
        file.appendText("${data.interpTimes[i]};${data.pureData[i]};${data.freqData[i]};${data.filteredData[i]}\n")
    }
}*/

fun printStrongData(file : File, data: HeartRateData)
{
    val interpTimes = DoubleArray(data.size, { x -> data.times.last() - (data.size - x) * data.interpStep })
    file.printWriter().use { out ->
        for (i in data.strongData.indices)
        {
            out.println("${interpTimes[i]},${data.filteredDataHistory.last()[i]},${data.strongData[i]}")
        }
    }
}

fun printFilteredData(file : File, data : HeartRateData)
{
    file.printWriter().use { out ->
        for (i in 0..data.size - 1)
        {
            for (j in data.filteredDataHistory.indices)
            {
                out.print("${data.filteredDataHistory[j][i]},")
            }
            out.println()
        }
    }
}