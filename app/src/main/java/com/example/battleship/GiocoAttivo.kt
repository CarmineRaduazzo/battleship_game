package com.example.battleship

import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement

enum class Turno { GIOCATORE, PC }

@Composable
fun GiocoAttivoScreen(navController: NavController) {
    val customFont = FontFamily(Font(R.font.inter_extrabold))
    val customCyan = Color(0xFFC1CFD5)

    val giocatoreShips = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<List<List<Pair<Int, Int>>>>("giocatoreShips")
    val pcShips = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<List<List<Pair<Int, Int>>>>("pcShips")

    var messaggioNaveDistrutta by remember { mutableStateOf<String?>(null) }
    var interazioneAbilitata by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    val celleColpiteGiocatore = remember { mutableStateListOf<Pair<Int, Int>>() }
    val celleColpitePC = remember { mutableStateListOf<Pair<Int, Int>>() }
    val naviDistruttePC = remember { mutableStateListOf<Int>() }
    val naviDistrutteGiocatore = remember { mutableStateListOf<Int>() }

    var punteggioGiocatore by remember { mutableStateOf(0) }
    var punteggioPC by remember { mutableStateOf(0) }

    var partitaFinita by remember { mutableStateOf(false) }
    var vincitore by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var turno by remember { mutableStateOf(if ((0..1).random() == 0) Turno.GIOCATORE else Turno.PC) }



    if (giocatoreShips == null || pcShips == null) {
        Text("Data not available", color = Color.Red)
        return
    }
    LaunchedEffect(turno) {
        if (turno == Turno.PC && !partitaFinita) {
            interazioneAbilitata = false

            attaccoPC(
                celleColpiteGiocatore,
                giocatoreShips,
                naviDistrutteGiocatore,
                onNaveColpita = { m, p ->
                    m?.let { messaggioNaveDistrutta = it }
                    p?.let { punteggioPC += it }
                },
                onVittoriaPC = {
                    vincitore = "The PC has won!"
                    partitaFinita = true
                }
            )

            if (!partitaFinita) {
                turno = Turno.GIOCATORE
                interazioneAbilitata = true
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(24.dp))

            messaggioNaveDistrutta?.let {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Snackbar(containerColor = Color(0xFF323232), contentColor = Color.White) {
                        Text(it.visuals.message)
                    }
                }

                LaunchedEffect(it) {
                    snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                    messaggioNaveDistrutta = null
                }
            }

            Spacer(Modifier.height(40.dp)) //Provvisorio

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //Prima Etichetta: Player Score
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.player_turn_label),
                            contentDescription = "Player Score",
                            contentScale = ContentScale.Inside,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "Player: $punteggioGiocatore",
                            color = Color.Black,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = customFont
                        )
                    }
                    //Seconda etichetta: PC score
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.player_turn_label),
                            contentDescription = "PC Score",
                            contentScale = ContentScale.Inside,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "PC: $punteggioPC",
                            color = Color.Black,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = customFont
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .width(150.dp) //Leggermente più lungo delle 2 etichette precedenti
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.player_turn_label),
                        contentDescription = "Turn",
                        contentScale = ContentScale.Inside,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "Turn: ${if (turno == Turno.GIOCATORE) "Player" else "PC"}",
                        color = Color.Black,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = customFont
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            Box(modifier = Modifier.width(420.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val titolo = if (turno == Turno.PC) "Attack the user" else "Attack the PC (tap to attack)"
                    Text(titolo, fontSize = 20.sp, fontFamily = customFont)

                    Spacer(Modifier.height(20.dp))

                    Grid8x8(
                        placedShips = if (turno == Turno.PC) giocatoreShips else pcShips,
                        mostraNavi = turno == Turno.PC,
                        celleColpite = if (turno == Turno.PC) celleColpiteGiocatore else celleColpitePC,
                        onCellClick = { row, col ->
                            if (turno == Turno.GIOCATORE && interazioneAbilitata && !partitaFinita) {
                                val cella = row to col
                                if (cella !in celleColpitePC) {
                                    celleColpitePC.add(cella)

                                    val (msg, punti) = verificaNaviDistrutteConStato(pcShips, celleColpitePC, naviDistruttePC)
                                    msg?.let { messaggioNaveDistrutta = it }
                                    punti?.let { punteggioGiocatore += it }

                                    if (naviDistruttePC.size == pcShips.size) {
                                        // Calcolo vincitore in base ai punteggi
                                        vincitore = when {
                                            punteggioGiocatore > punteggioPC -> "You won!"
                                            punteggioGiocatore < punteggioPC -> "The PC has won!"
                                            else -> "It's a draw!"
                                        }
                                        partitaFinita = true
                                        return@Grid8x8
                                    }

                                    coroutineScope.launch {
                                        interazioneAbilitata = false

                                        val haColpito = èColpo(pcShips, cella)

                                        if (!haColpito) {
                                            delay(3000)
                                            turno = Turno.PC
                                            messaggioNaveDistrutta = null

                                            attaccoPC(
                                                celleColpiteGiocatore,
                                                giocatoreShips,
                                                naviDistrutteGiocatore,
                                                onNaveColpita = { m, p ->
                                                    m?.let { messaggioNaveDistrutta = it }
                                                    p?.let { punteggioPC += it }
                                                },
                                                onVittoriaPC = {
                                                    vincitore = "The PC has won!"
                                                    partitaFinita = true
                                                }
                                            )

                                            turno = Turno.GIOCATORE
                                        }

                                        interazioneAbilitata = true
                                    }
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(72.dp))

            Box(
                modifier = Modifier
                    .clickable { navController.popBackStack() }
                    .size(150.dp, 60.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.black_button),
                    contentDescription = "Sfondo Pulsante",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Inside
                )
                Text(
                    text = "Return",
                    modifier = Modifier.align(Alignment.Center),
                    color = customCyan,
                    fontSize = 20.sp,
                    fontFamily = customFont
                )
            }
        }

        // Overlay fine partita
        if (partitaFinita) {
            val messaggioVincitore = vincitore ?: run {
                when {
                    punteggioGiocatore > punteggioPC -> "You won!"
                    punteggioGiocatore < punteggioPC -> "The PC has won!"
                    else -> "It's a draw!"
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Game Over!",
                        fontSize = 28.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = messaggioVincitore,
                        fontSize = 22.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    (navController.context as? android.app.Activity)?.finish()
                                }
                                .size(width = 150.dp, height = 50.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.black_button),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                "Quit",
                                color = customCyan,
                                fontFamily = customFont,
                                fontSize = 20.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("preparazione") {
                                        popUpTo("gioco") { inclusive = true }
                                    }
                                }
                                .size(width = 150.dp, height = 50.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cyan_button),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                "Rematch",
                                color = Color.Black,
                                fontFamily = customFont,
                                fontSize = 20.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

suspend fun attaccoPC(
    celleColpite: MutableList<Pair<Int, Int>>,
    naviGiocatore: List<List<Pair<Int, Int>>>,
    naviDistrutteGiocatore: MutableList<Int>,
    onNaveColpita: (String?, Int?) -> Unit,
    onVittoriaPC: () -> Unit
) {
    val tutteLeCelle = (0 until 8).flatMap { riga -> (0 until 8).map { col -> riga to col } }
    val celleNonAncoraColpite = tutteLeCelle.toMutableSet().apply { removeAll(celleColpite) }

    while (true) {
        delay(1500)
        if (celleNonAncoraColpite.isEmpty()) break

        val cella = celleNonAncoraColpite.random()
        celleNonAncoraColpite.remove(cella)
        celleColpite.add(cella)

        val haColpito = èColpo(naviGiocatore, cella)

        val (messaggio, punti) = verificaNaviDistrutteConStato(naviGiocatore, celleColpite, naviDistrutteGiocatore)
        if (messaggio != null) {
            onNaveColpita(messaggio, punti)
        }

        if (naviDistrutteGiocatore.size == naviGiocatore.size) {
            onVittoriaPC()
            break
        }

        delay(3000)

        if (!haColpito) break
    }
}

@Composable
fun Grid8x8(
    placedShips: List<List<Pair<Int, Int>>>,
    mostraNavi: Boolean,
    celleColpite: List<Pair<Int, Int>>,
    onCellClick: (Int, Int) -> Unit = { _, _ -> }
) {
    val blockSize = 42.dp

    fun isShipSunk(ship: List<Pair<Int, Int>>, hits: List<Pair<Int, Int>>): Boolean {
        return ship.all { it in hits }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row { // Lettere della griglia
            Text(" ", modifier = Modifier.width(blockSize))
            for (i in 'A'..'H') {
                Text(i.toString(), modifier = Modifier.width(blockSize), textAlign = TextAlign.Center)
            }
        }

        for (row in 0 until 8) {
            Row {
                Text(
                    (row + 1).toString(),
                    modifier = Modifier.width(blockSize),
                    textAlign = TextAlign.Center
                )
                for (col in 0 until 8) {
                    val cell = row to col
                    val contieneNave = placedShips.flatten().contains(cell)
                    val isColpito = cell in celleColpite

                    val shape = if (contieneNave && placedShips.any { ship -> ship.contains(cell) && isShipSunk(ship, celleColpite) }) {
                        val nave = placedShips.find { it.contains(cell) }!!
                        val index = nave.indexOf(cell)
                        val isVertical = nave.size > 1 && (nave[0].first != nave[1].first)

                        when (index) {
                            0 -> if (isVertical)
                                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            else
                                RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)

                            nave.lastIndex -> if (isVertical)
                                RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                            else
                                RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)

                            else -> RoundedCornerShape(0.dp)
                        }
                    } else {
                        RoundedCornerShape(8.dp)
                    }

                    val backgroundModifier = when {
                        isColpito && contieneNave ->
                            Modifier.background(Color.Red, shape)

                        isColpito && !contieneNave ->
                            Modifier.background(Color.Cyan, shape)

                        mostraNavi && contieneNave ->
                            Modifier.background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF7A7A7A), Color(0xFF4D4D4D))
                                ),
                                shape = shape
                            )
                        else -> Modifier.background(
                            Color.White,
                            shape
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(blockSize)
                            .padding(4.dp)
                            .then(backgroundModifier)
                            .border(
                                2.dp, Color.Black,
                                shape
                            )
                            .clickable { onCellClick(row, col) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isColpito) {
                            Text(
                                // Se la nave è colpita mostra la 'X' altrimenti la '●'
                                text = if (contieneNave) "X" else "●",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}


fun verificaNaviDistrutteConStato(
    tutteLeNavi: List<List<Pair<Int, Int>>>,
    celleColpite: List<Pair<Int, Int>>,
    naviDistrutte: MutableList<Int>
): Pair<String?, Int?> {
    for ((index, nave) in tutteLeNavi.withIndex()) {
        if (nave.all { it in celleColpite } && index !in naviDistrutte) {
            naviDistrutte.add(index)
            val punti = when (nave.size) {
                2 -> 100
                3 -> 200
                4 -> 300
                5 -> 400
                else -> 0
            }
            return "A ${nave.size}-cell ship has been destroyed!" to punti
        }
    }
    return null to null
}

fun èColpo(
    ships: List<List<Pair<Int, Int>>>,
    cella: Pair<Int, Int>
): Boolean {
    return ships.flatten().contains(cella)
}
