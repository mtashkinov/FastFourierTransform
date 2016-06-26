import java.io.File
import java.text.DateFormat
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

        var result = ""
        if (!input.isBadData)
        {
            result = "${file.name};${input.pulse}"
        }
        else
        {
            result = "${file.name};-"
        }
        println(result.replace(';', ' '))
        resultFile.appendText(result + "\n")

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

fun printPartsFFT(file: File, data : HeartRateData)
{
    var size = FFT.fromValueToIndex(PulseDetector.MAX_HEART_RATE / 60, data.partSize, data.freq[0]) -
               FFT.fromValueToIndex(PulseDetector.MIN_HEART_RATE / 60, data.partSize, data.freq[0]) + 1
    val lines = ArrayList<String>(size)
    for (i in 0..size-1)
    {
        lines.add("")
    }

    for (i in data.partsFFT.indices)
    {
        val start = FFT.fromValueToIndex(PulseDetector.MIN_HEART_RATE / 60, data.partSize, data.freq[i])
        val end = FFT.fromValueToIndex(PulseDetector.MAX_HEART_RATE / 60, data.partSize, data.freq[i])
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
            lines[j - start] += "${FFT.fromIndexToValue(j, data.partSize, data.freq[i]) * 60};${data.partsFFT[i][j]};"
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
    for (i in data.filteredData.indices)
    {
        file.appendText("${data.interpTimes[i]};${data.pureData[i]};${data.freqData[i]};${data.filteredData[i]}\n")
    }
}

/*fun test(data : HeartRateData) : List<Int>
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
}*/