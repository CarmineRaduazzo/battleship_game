package com.example.battleship

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.annotation.SuppressLint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//Nave
data class Ship(val size: Int, var quantity: Int)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GiocoScreen(navController: NavController) {
    val customFont = FontFamily(Font(R.font.inter_extrabold))
    val customCyan = Color(0xFFC1CFD5)
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

    val scope = rememberCoroutineScope()
    var draggingShip by remember { mutableStateOf<Ship?>(null) }
    val placedShips = remember { mutableStateListOf<List<Pair<Int, Int>>>() }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.2f)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Posiziona le tue navi",
                fontFamily = customFont,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp, start = 8.dp, end = 16.dp, top = 80.dp),
                horizontalAlignment = Alignment.Start
            ) {

                Spacer(modifier = Modifier.height(32.dp))

                Grid8x8(
                    placedShips = placedShips,
                    onPlace = { row, col ->
                        draggingShip?.let { ship ->
                            if (ship.quantity > 0) {
                                val cells = if (orientation == "right") {
                                    (0 until ship.size).map { row to col + it }
                                } else {
                                    (0 until ship.size).map { row + it to col }
                                }

                                val outOfBounds =
                                    cells.any { it.first !in 0..7 || it.second !in 0..7 }
                                val overlap =
                                    placedShips.any { it.intersect(cells.toSet()).isNotEmpty() }

                                //La nave viene piazzato se tutto è OK
                                if (!outOfBounds && !overlap) {
                                    placedShips.add(cells)
                                    val idx = shipsAvailable.indexOfFirst {
                                        it.size == ship.size && it.quantity > 0
                                    }
                                    if (idx != -1) {
                                        shipsAvailable[idx] = shipsAvailable[idx].copy(
                                            quantity = shipsAvailable[idx].quantity - 1
                                        )
                                    }
                                    draggingShip = null
                                }
                            }
                        }
                    },
                    onRemove = { row, col ->
                        //Quando si clicca la nave questa viene rimossa
                        removeShip(placedShips, row, col, shipsAvailable)
                    },
                    selectedShip = draggingShip
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Image( //Pulsante per cambiare l'orientamento delle navi
                        painter = painterResource(R.drawable.rotate_button),
                        contentDescription = "Cambia Orientamento",
                        modifier = Modifier
                            .size(56.dp)
                            .clickable {
                                orientation = if (orientation == "right") "down" else "right"
                            },
                        contentScale = ContentScale.Inside
                    )

                    Image( //Pulsante per inserire le navi in maniera casuale
                        painter = painterResource(id = R.drawable.modify_button),
                        contentDescription = "Piazza Navi Casuali",
                        modifier = Modifier
                            .size(56.dp)
                            .clickable {
                                placedShips.clear()
                                val (pcShips, updatedShips) = generaNaviCasuali()
                                placedShips.addAll(pcShips)
                                shipsAvailable.clear()
                                shipsAvailable.addAll(updatedShips)
                            },
                        contentScale = ContentScale.Inside
                    )

                    Image( //Pulsante per confermare l'orientamento delle navi
                        painter = painterResource(id = R.drawable.confirm_button),
                        contentDescription = "Conferma",
                        modifier = Modifier
                            .size(56.dp)
                            .clickable {
                                val allPlaced = shipsAvailable.all { it.quantity == 0 }
                                if (allPlaced) {
                                    scope.launch {
                                        for (i in 3 downTo 1) {
                                            snackbarHostState.showSnackbar("La partita inizia tra $i...")
                                            delay(1000)
                                        }
                                        val (pcShips, updatedShips) = generaNaviCasuali()
                                        val giocatoreShipsCopy = placedShips.map { it.toList() }
                                        val pcShipsCopy = pcShips.map { it.toList() }

                                        navController.currentBackStackEntry?.savedStateHandle?.apply {
                                            set("giocatoreShips", giocatoreShipsCopy)
                                            set("pcShips", pcShipsCopy)
                                        }

                                        navController.navigate("giocoAttivo")

                                        shipsAvailable.clear()
                                        shipsAvailable.addAll(updatedShips)
                                    }
                                }
                            },
                        contentScale = ContentScale.Inside
                    )
                }
            }

            //Etichetta "Ships Cheatsheet"
            Box(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(
                            color = customCyan,
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                        .align(Alignment.BottomCenter)
                ) {

                    // Pulsante grafico del Ships CheatSheet
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 25.dp, y = (-20).dp)
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .wrapContentWidth()
                                .border(2.dp, customCyan, shape = RoundedCornerShape(50))
                        ) {
                            Text(
                                text = stringResource(R.string.ships_cheatsheet),
                                fontFamily = customFont,
                                color = Color.Black,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Legenda(shipsAvailable, orientation) { ship ->
                            if (ship.quantity > 0) draggingShip = ship
                        }
                    }
                }
            }
        }
    }
}


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
fun Legenda(ships: List<Ship>, orientation: String, onShipSelected: (Ship) -> Unit) {
    val gridCellSpacing = 4.dp //valore per la distanza delle celle della griglia
    val blockSize = 32.dp

    val availableShips = ships.filter { it.quantity > 0 }
    val group1 = listOfNotNull(
        availableShips.find { it.size == 5 },
        availableShips.find { it.size == 2 }
    )

    val group2 = listOfNotNull(
        availableShips.find { it.size == 4 },
        availableShips.find { it.size == 3 }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        if (orientation == "right") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp), //distanza tra le navi
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    group1.forEach { ship ->
                        Box(modifier = Modifier.clickable { onShipSelected(ship) }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(gridCellSpacing)
                            ) {  //distanza tra i blocchi
                                repeat(ship.size) { index ->
                                    BlockLegenda(size = blockSize, index, ship.size, true)
                                }
                            }
                        }
                    }
                } //continuo...

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp), //distanza tra le navi
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    group2.forEach { ship ->
                        Box(modifier = Modifier.clickable { onShipSelected(ship) }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(gridCellSpacing)
                            ) {//distanza tra i blocchi
                                repeat(ship.size) { index ->
                                    BlockLegenda(size = blockSize, index, ship.size, true)
                                }
                            }
                        }
                    }
                }
            }

        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(y = (-5).dp)
            ) {
                (group1 + group2).forEach { ship ->
                    Box(
                        modifier = Modifier.clickable { onShipSelected(ship) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(gridCellSpacing),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            repeat(ship.size) { index ->
                                BlockLegenda(
                                    size = blockSize,
                                    index = index,
                                    total = ship.size,
                                    horizontal = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlockLegenda(size: Dp, index: Int, total: Int, horizontal: Boolean) {
    val newSize = size * 1.1f
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(newSize)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF7A7A7A), Color(0xFF4D4D4D))
                ),
                shape = shape
            )
            .border(2.dp, Color.Black, shape),
        contentAlignment = Alignment.Center
    ) {
        //Nel caso in cui volessimo rimettere la linea nera centrale
    }
}

// Griglia per posizionamento navi

@Composable
fun Grid8x8(
    placedShips: List<List<Pair<Int, Int>>>,
    onPlace: (Int, Int) -> Unit,
    onRemove: (Int, Int) -> Unit,
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
                            .background(
                                brush = if (isShipCell) {
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF7A7A7A), Color(0xFF4D4D4D))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(Color.White, Color.White)
                                    )
                                },
                                shape = shape
                            )
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
                                //Nel caso in cui volessimo rimettere la striscia nera
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
    val shipToRemove = placedShips.find { it.contains(row to col) }
    if (shipToRemove != null) {
        placedShips.remove(shipToRemove)
        val size = shipToRemove.size
        val idx = shipsAvailable.indexOfFirst { it.size == size }

        if (idx != -1) {
            shipsAvailable[idx] =
                shipsAvailable[idx].copy(quantity = shipsAvailable[idx].quantity + 1)
        }
    }
}

