package com.example.battleship

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.annotation.SuppressLint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController



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
            val stripeThickness = 18.dp.toPx() //Linea più bassa

            if (horizontal) { //Linea centrale nera
                val yCenter = h / 2f - stripeThickness / 2f
                drawRect(color = Color.Black, topLeft = Offset(0f, yCenter), size = Size(w, stripeThickness))

                if (index == 0) { //Punte avanti e dietro
                    drawPath(path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w / 4f, 0f)
                        quadraticTo(0f, h / 2f, w / 4f, h)
                        lineTo(0f, h)
                        close()
                    }, color = Color.Black)
                } else if (index == total - 1) {
                    drawPath(path = Path().apply {
                        moveTo(w, 0f)
                        lineTo(w * 3 / 4f, 0f)
                        quadraticTo(w, h / 2f, w * 3 / 4f, h)
                        lineTo(w, h)
                        close()
                    }, color = Color.Black)
                }
            } else {
                val xCenter = w / 2f - stripeThickness / 2f
                drawRect(color = Color.Black, topLeft = Offset(xCenter, 0f), size = Size(stripeThickness, h))

                if (index == 0) { //Punte sopra e sotto
                    drawPath(path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(0f, h / 4f)
                        quadraticTo(w / 2f, 0f, w, h / 4f)
                        lineTo(w, 0f)
                        close()
                    }, color = Color.Black)
                } else if (index == total - 1) {
                    drawPath(path = Path().apply {
                        moveTo(0f, h)
                        lineTo(0f, h * 3 / 4f)
                        quadraticTo(w / 2f, h, w, h * 3 / 4f)
                        lineTo(w, h)
                        close()
                    }, color = Color.Black)
                }
            }
        }
    }
}

//Selezione Navi per orientamento dinamico
@Composable
fun Legenda(ships: List<Ship>, orientation: String, onShipSelected: (Ship) -> Unit){
    val gridCellSpacing= 4.dp //valore per la distanza delle celle della griglia

    Column(horizontalAlignment = Alignment.CenterHorizontally){
        Spacer(Modifier.height(10.dp))

        val availableShips =  ships.filter { it.quantity > 0 }
        val group1 = listOfNotNull(
            availableShips.find { it.size == 5},
            availableShips.find { it.size == 2}
        )

        val group2 = listOfNotNull(
            availableShips.find { it.size == 4 },
            availableShips.find { it.size == 3 }
        )

        if (orientation == "right"){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp), //distanza tra le navi
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    group1.forEach { ship ->
                        Box(modifier = Modifier.clickable { onShipSelected(ship) }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(gridCellSpacing)
                            ) {  //distanza tra i blocchi
                                repeat(ship.size) { index ->
                                    BlockLegenda(32.dp, index, ship.size, true)
                                }
                            }
                        }
                    }
                } //continuo...

                Spacer(Modifier.height(24.dp)) //distanza tra i gruppi
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp), //distanza tra le navi
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    group2.forEach { ship ->
                        Box(modifier = Modifier.clickable { onShipSelected(ship) }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(gridCellSpacing)
                            ) {//distanza tra i blocchi
                                repeat(ship.size) { index ->
                                    BlockLegenda(32.dp, index, ship.size, true)
                                }
                            }
                        }
                    }
                }
            }

        } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp), //distanza tra i gruppi di navi verticaI
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp), //distanza tra le navi
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                group1.forEach { ship ->
                    Box(modifier = Modifier.clickable { onShipSelected(ship) }) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(gridCellSpacing)
                        ) { // distanza tra i blocchi
                            repeat(ship.size) { index ->
                                BlockLegenda(28.dp, index, ship.size, false)
                            }
                        }
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp), //distanza tra le navi
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                group2.forEach{ ship ->
                    Box(modifier = Modifier.clickable { onShipSelected(ship) }){
                        Column(
                            verticalArrangement = Arrangement.spacedBy(gridCellSpacing)
                        ){//distanza tra i blocchi
                            repeat(ship.size){ index ->
                                BlockLegenda(28.dp,index,ship.size,false)
                            }
                        }
                    }
                }
            }
        }
        }
    }
}
//Funzione per rimuovere le navi
fun removeShip(
    placedShips: MutableList<List<Pair<Int, Int>>>,
    row: Int,
    col: Int,
    shipsAvailable: MutableList<Ship>
) {
    val shipToRemove = placedShips.find { it.contains(row to col)}
    if(shipToRemove != null) {
        placedShips.remove(shipToRemove)
        val size = shipToRemove.size
        val idx = shipsAvailable.indexOfFirst { it.size == size }

        if(idx != -1) {
            shipsAvailable[idx] = shipsAvailable[idx].copy(quantity = shipsAvailable[idx].quantity + 1)
        }
    }
}

// Griglia per posizionamento navi

@Composable
fun Grid8x8(
    placedShips: List<List<Pair<Int, Int>>>,
    onPlace:(Int,Int) -> Unit,
    onRemove: (Int,Int) -> Unit,
    selectedShip: Ship?
) {
    val blockSize = 42.dp

    Column(modifier = Modifier.fillMaxWidth()) { //Header per le lettere
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("", modifier = Modifier.width(blockSize))
            for (i in 'A'..'H') {
                Text(
                    i.toString(),
                    modifier = Modifier.width(blockSize),
                    textAlign = TextAlign.Center
                )
            }
        }
        //Griglia (8 * 8)
        for (row in 0 until 8) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text(
                    (row + 1).toString(),
                    modifier = Modifier.width(blockSize),
                    textAlign = TextAlign.Center
                )

                for (col in 0 until 8) {
                    val shipCells = placedShips.find {
                        it.contains(row to col)
                    }
                    val indexInShip = shipCells?.indexOf(row to col) ?: -1
                    val shipLength = shipCells?.size ?: -1
                    val isShipCell = shipCells != null

                    val shape = when {
                        isShipCell && indexInShip == 0 && shipLength > 1 -> RoundedCornerShape(
                            6.dp,
                            0.dp,
                            0.dp,
                            6.dp
                        )

                        isShipCell && indexInShip == shipLength - 1 -> RoundedCornerShape(
                            0.dp,
                            6.dp,
                            6.dp,
                            0.dp
                        )

                        isShipCell && shipLength == 1 -> RoundedCornerShape(6.dp)
                        else -> RoundedCornerShape(8.dp)
                    }

                    Box( //continuo.... 2 commit della parte
                        modifier = Modifier
                            .size(blockSize)
                            .padding(4.dp)
                            .background(Color.White, shape)
                            .border(2.dp, Color.Black, shape)
                            .clickable {
                                if (isShipCell) {
                                    onRemove(row, col)
                                } else if (selectedShip != null) {
                                    onPlace(row, col)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isShipCell) {
                            Canvas(modifier = Modifier.fillMaxWidth()) {
                                val w = size.width
                                val h = size.height
                                val borderThickness = 2.dp.toPx()
                                val stripeThickness =
                                    4.dp.toPx() // serve per lo sfondo bianco, non cancellatelo
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(borderThickness / 2, borderThickness / 2),
                                    size = Size(w - borderThickness, h - borderThickness)
                                )// calcola l'orientamento della nave
                                val orientation = if (shipLength > 1) {
                                    val first = shipCells!![0]
                                    val second = shipCells[1]
                                    if (second.first == first.first) "right" else "down"
                                } else {
                                    "right"
                                }//striscia nera centrale

                                if (orientation == "right") {
                                    val yCenter = h / 2f - stripeThickness / 2f
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(0f, yCenter),
                                        size = Size(w, stripeThickness)
                                    )
                                } else {
                                    val xCenter = w / 2f - stripeThickness / 2f
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(xCenter, 0f),
                                        size = Size(stripeThickness, h)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GiocoScreen(navController: NavController) {
    //Direzione iniziale di posizionamento delle navi (orizzontale)
    var orientation by remember { mutableStateOf("right") }

    val shipsAvailable = remember {
        mutableStateListOf(
            Ship(5, 1),
            Ship(4, 2),
            Ship(3, 3),
            Ship(2, 4)
        )
    }

    var draggingShip by remember { mutableStateOf<Ship?>(null) }
    val placedShips = remember { mutableStateListOf<List<Pair<Int, Int>>>() }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp, start = 8.dp, end = 16.dp, top = 180.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(32.dp))
//Da continuare