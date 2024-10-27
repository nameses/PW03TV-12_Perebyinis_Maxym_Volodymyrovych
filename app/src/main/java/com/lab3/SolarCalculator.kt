package com.lab3

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt


@SuppressLint("DefaultLocale")
@Composable
fun SolarCalculator() {
    var power by remember { mutableStateOf("5.0") }
    var deviationCurrent by remember { mutableStateOf("1.0") }
    var deviationFinal by remember { mutableStateOf("0.25") }
    var price by remember { mutableStateOf("7.0") }
    var result by remember { mutableStateOf<Results?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Розрахунок прибутку від сонячних електростанцій",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = power,
            onValueChange = { power = it },
            label = { Text("Середньодобова потужність") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = deviationCurrent,
            onValueChange = { deviationCurrent = it },
            label = { Text("Середньоквадратичне відхилення поточне") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = deviationFinal,
            onValueChange = { deviationFinal = it },
            label = { Text("Середньоквадратичне відхилення після вдосконалення") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Вартість електроенергії") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                result = calculateResults(
                    power.toDoubleOrNull() ?: 0.0,
                    deviationCurrent.toDoubleOrNull() ?: 0.0,
                    deviationFinal.toDoubleOrNull() ?: 0.0,
                    price.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Розрахувати")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if(result != null){
            Column(modifier = Modifier.align(Alignment.Start))
            {
                Text(String.format("Прибуток поточний: %.2f", result?.incomeCurrent))
                Text(String.format("Штраф поточний: %.2f", result?.penaltyCurrent))
                Text(String.format("Прибуток після вдосконалення: %.2f", result?.incomeAfter))
                Text(String.format("Штраф після вдосконалення: %.2f", result?.penaltyAfter))
                Text(String.format("Кінцевий прибуток: %.2f", result?.incomeFinal))
            }
        }

    }
}

fun calculateResults(averagePower: Double, deviationCurrent: Double, deviationFinal: Double, price: Double): Results {
    val deltaWCurrent = integrate(averagePower, deviationCurrent)
    val incomeCurrent = (averagePower * 24 * deltaWCurrent) * price
    val fineCurrent = (averagePower * 24 * (1 - deltaWCurrent)) * price

    val deltaWAfter = integrate(averagePower, deviationFinal)
    val incomeAfter = (averagePower * 24 * deltaWAfter) * price
    val fineAfter = (averagePower * 24 * (1 - deltaWAfter)) * price
    val incomeFinale = incomeAfter - fineAfter

    return Results(incomeCurrent, fineCurrent, incomeAfter, fineAfter, incomeFinale)
}

fun integrate(
    averagePower: Double,
    deviation: Double
): Double {
    val lowerLimit = 4.75
    val upperLimit  = 5.25
    val steps = 10000
    val stepSize = (upperLimit - lowerLimit) / steps
    var sum = 0.0

    for (i in 0 until steps) {
        val x1 = lowerLimit + i * stepSize
        val x2 = x1 + stepSize
        sum += 0.5 * (calculatePd(x1, averagePower, deviation) + calculatePd(x2, averagePower, deviation)) * stepSize
    }

    return sum
}

fun calculatePd(p: Double, averagePower: Double, deviation: Double): Double {
    return (1 / (deviation * sqrt(2 * PI))) * exp(-(p - averagePower).pow(2) / (2 * deviation.pow(2)))
}

data class Results(
    val incomeCurrent: Double,
    val penaltyCurrent: Double,
    val incomeAfter: Double,
    val penaltyAfter: Double,
    val incomeFinal: Double
)