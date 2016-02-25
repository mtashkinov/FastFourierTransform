/**
 * Created by Mikhail on 25.02.2016.
 */
fun main(args : Array<String>)
{
    val fft = FFT()
    val res = fft.fft(generate())
    print(res.indexOf(res.max()!!))
}

fun generate() : DoubleArray
{
    return DoubleArray(1024, {x -> Math.sin(x * 0.1)})
}