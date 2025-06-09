package com.example.battleship

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