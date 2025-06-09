package com.example.battleship

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

//Nave
data class Ship(val size: Int, var quantity: Int)

//Funzione per generare navi casuali
fun generaNaviCasuali(): Pair<List<List<Pair<Int, Int>>>, List<Ship>> {
    val random = java.util.Random()
    val shipsToPlace = listOf(
        Ship(5, 1),
        Ship(4, 2),
        Ship(3, 3),
        Ship(2, 4)
    )

    val placedShips = mutableListOf<List<Pair<Int, Int>>>()

    for (ship in shipsToPlace) {
        repeat(ship.quantity) {
            var placed = false
            while (!placed) {
                val orientation = if (random.nextBoolean()) "right" else "down"
                val startRow = random.nextInt(8)
                val startCol = random.nextInt(8)

                val cells = if (orientation == "right") {
                    (0 until ship.size).map { startRow to startCol + it }
                } else {
                    (0 until ship.size).map { startRow + it to startCol }
                }

                val outOfBounds = cells.any { it.first !in 0..7 || it.second !in 0..7 }
                val overlap = placedShips.any { it.intersect(cells.toSet()).isNotEmpty() }

                if (!outOfBounds && !overlap) {
                    placedShips.add(cells)
                    placed = true
                }
            }
        }
    }

    val updatedShips = shipsToPlace.map { it.copy(quantity = 0) }
    return Pair(placedShips, updatedShips)
}

@Composable
fun BlockLegenda(size: Dp, index: Int, total: Int, horizontal: Boolean) {
    val newSize = size * 1.1f
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(newSize)
            .background(Color.White, shape)
            .border(2.dp, Color.Black, shape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.toPx()
            val h = size.toPx()
            val stripeThickness = 18.dp.toPx()

            if (horizontal) {
                val yCenter = h / 2f - stripeThickness / 2f
                drawRect(color = Color.Black, topLeft = Offset(0f, yCenter), size = Size(w, stripeThickness))

                if (index == 0) {
                    drawPath(path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w / 4f, 0f)
                        quadraticBezierTo(0f, h / 2f, w / 4f, h)
                        lineTo(0f, h)
                        close()
                    }, color = Color.Black)
                } else if (index == total - 1) {
                    drawPath(path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w, 0f)
                        lineTo(w * 3 / 4f, 0f)
                        quadraticBezierTo(w, h / 2f, w * 3 / 4f, h)
                        lineTo(w, h)
                        close()
                    }, color = Color.Black)
                }
            } else {
                val xCenter = w / 2f - stripeThickness / 2f
                drawRect(color = Color.Black, topLeft = Offset(xCenter, 0f), size = Size(stripeThickness, h))

                if (index == 0) {
                    drawPath(path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, 0f)
                        lineTo(0f, h / 4f)
                        quadraticBezierTo(w / 2f, 0f, w, h / 4f)
                        lineTo(w, 0f)
                        close()
                    }, color = Color.Black)
                } else if (index == total - 1) {
                    drawPath(path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, h)
                        lineTo(0f, h * 3 / 4f)
                        quadraticBezierTo(w / 2f, h, w, h * 3 / 4f)
                        lineTo(w, h)
                        close()
                    }, color = Color.Black)
                }
            }
        }
    }
}
