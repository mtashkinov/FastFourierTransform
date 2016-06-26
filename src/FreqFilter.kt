/**
 * Created by Mikhail on 10.06.2016.
 */
class FreqFilter(private val frequency: Double, private val sampleRate: Double, private val passType: FreqFilter.PassType, /// <summary>
        /// rez amount, from sqrt(2) to ~ 0.1
        /// </summary>
                 private val resonance: Double)
{
    private var c = 0.0
    private var a1 = 0.0
    private var a2 = 0.0
    private var a3 = 0.0
    private var b1 = 0.0
    private var b2 = 0.0

    /// <summary>
    /// Array of input values, latest are in front
    /// </summary>
    private val inputHistory = DoubleArray(2)

    /// <summary>
    /// Array of output values, latest are in front
    /// </summary>
    private val outputHistory = DoubleArray(3)

    init
    {

        when (passType)
        {
            FreqFilter.PassType.Lowpass  ->
            {
                c = 1.0f / Math.tan(Math.PI * frequency / sampleRate)
                a1 = 1.0f / (1.0f + resonance * c + c * c)
                a2 = 2f * a1
                a3 = a1
                b1 = 2.0f * (1.0f - c * c) * a1
                b2 = (1.0f - resonance * c + c * c) * a1
            }
            FreqFilter.PassType.Highpass ->
            {
                c = Math.tan(Math.PI * frequency / sampleRate)
                a1 = 1.0f / (1.0f + resonance * c + c * c)
                a2 = -2f * a1
                a3 = a1
                b1 = 2.0f * (c * c - 1.0f) * a1
                b2 = (1.0f - resonance * c + c * c) * a1
            }
        }
    }

    enum class PassType
    {
        Highpass,
        Lowpass
    }

    fun Update(newInput: Double)
    {
        val newOutput = a1 * newInput + a2 * this.inputHistory[0] + a3 * this.inputHistory[1] - b1 * this.outputHistory[0] - b2 * this.outputHistory[1]

        this.inputHistory[1] = this.inputHistory[0]
        this.inputHistory[0] = newInput

        this.outputHistory[2] = this.outputHistory[1]
        this.outputHistory[1] = this.outputHistory[0]
        this.outputHistory[0] = newOutput
    }


    fun getValue(): Double
    {
        return this.outputHistory[0]
    }


}
