package com.example.length_converter_app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private val unitToMeterMap = mapOf(
        "Gigametre" to 1e9,
        "Megametre" to 1e6,
        "Kilometre" to 1e3,
        "Hectometre" to 1e2,
        "Decametre" to 10.0,
        "Meter" to 1.0,
        "Decimetre" to 0.1,
        "Centimetre" to 0.01,
        "Millimetre" to 0.001,
        "Micrometre" to 1e-6,
        "Nanometre" to 1e-9,
        "Nautical mile" to 1852.0,
        "Fathom" to 1.8288,
        "Inch" to 0.0254,
        "Mile" to 1609.34,
        "Foot" to 0.3048
    )

    private lateinit var inputValue: TextInputEditText
    private lateinit var resultValue: TextInputEditText
    private lateinit var fromUnitDropdown: MaterialAutoCompleteTextView
    private lateinit var toUnitDropdown: MaterialAutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        setupDropdowns()
        setupTextChangeListener()
    }

    private fun setupViews() {
        inputValue = findViewById(R.id.inputValue)
        resultValue = findViewById(R.id.resultValue)
        fromUnitDropdown = findViewById(R.id.fromUnitDropdown)
        toUnitDropdown = findViewById(R.id.toUnitDropdown)
    }

    private fun setupDropdowns() {
        val units = unitToMeterMap.keys.toList()
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, units)

        fromUnitDropdown.setAdapter(adapter)
        toUnitDropdown.setAdapter(adapter)

        // Set default values
        fromUnitDropdown.setText("Meter", false)
        toUnitDropdown.setText("Mile", false)

        fromUnitDropdown.setOnItemClickListener { _, _, _, _ -> updateConversion() }
        toUnitDropdown.setOnItemClickListener { _, _, _, _ -> updateConversion() }
    }

    private fun setupTextChangeListener() {
        inputValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateConversion()
            }
        })
    }

    private fun updateConversion() {
        val value = inputValue.text.toString().toDoubleOrNull()
        val fromUnit = fromUnitDropdown.text.toString()
        val toUnit = toUnitDropdown.text.toString()

        if (value != null) {
            val result = convertLength(value, fromUnit, toUnit)
            if (result != null) {
                val formatter = DecimalFormat("#.########")
                resultValue.setText(formatter.format(result))
            }
        } else {
            resultValue.setText("")
        }
    }

    private fun convertLength(value: Double, fromUnit: String, toUnit: String): Double? {
        val fromRate = unitToMeterMap[fromUnit]
        val toRate = unitToMeterMap[toUnit]

        if (fromRate != null && toRate != null) {
            val meters = value * fromRate
            return meters / toRate
        }
        return null
    }
}