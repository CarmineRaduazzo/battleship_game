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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.navigation.NavController

fun èColpo(ships: List<List<Pair<Int, Int>>>, cella: Pair<Int, Int>): Boolean {
    return ships.flatten().contains(cella)
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
                2 -> 100; 3 -> 200; 4 -> 300; 5 -> 400; else -> 0
            }
            return "Nave di ${nave.size} celle distrutta!" to punti
        }
    }
    return null to null
}

suspend fun attaccoPC(
    celleColpite: MutableList<Pair<Int, Int>>,
    naviGiocatore: List<List<Pair<Int, Int>>>,
    naviDistrutteGiocatore: MutableList<Int>,
    onNaveColpita: (String?, Int?) -> Unit,
    onVittoriaPC: () -> Unit
) {
    val tutteLeCelle = (0 until 8).flatMap { riga -> (0 until 8).map { riga to it } }
    val celleNonAncoraColpite = tutteLeCelle.toMutableSet().apply { removeAll(celleColpite) }

    while (true) {
        delay(1500)
        if (celleNonAncoraColpite.isEmpty()) break

        val cella = celleNonAncoraColpite.random()
        celleNonAncoraColpite.remove(cella)
        celleColpite.add(cella)

        val haColpito = èColpo(naviGiocatore, cella)

        val (messaggio, punti) = verificaNaviDistrutteConStato(naviGiocatore, celleColpite,
            naviDistrutteGiocatore)
        if (messaggio != null) onNaveColpita(messaggio, punti)

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
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment =
            Alignment.Start
    ) {
        Row { //Lettere della griglia
            Text(" ", modifier = Modifier.width(blockSize))
            for (i in 'A'..'H') Text(
                i.toString(), modifier =
                    Modifier.width(blockSize), textAlign = TextAlign.Center
            )
        }
        for (row in 0 until 8) {
            Row {
                Text( //Numero delle righe
                    (row + 1).toString(),
                    modifier =
                        Modifier.width(blockSize),
                    textAlign = TextAlign.Center
                )
                for (col in 0 until 8) {
                    val cell = row to col
                    val contieneNave =
                        placedShips.flatten().contains(cell)
                    val isColpito = cell in celleColpite
                    //Va a determinare il colore dello sfondo della cella
                    val backgroundModifier = when {
                        isColpito && contieneNave ->
                            Modifier.background(Color.Red, RoundedCornerShape(4.dp))

                        isColpito && !contieneNave ->
                            Modifier.background(Color.LightGray, RoundedCornerShape(4.dp))

                        mostraNavi && contieneNave ->
                            Modifier.background(
                                brush = Brush.verticalGradient(
                                    colors =
                                        listOf(Color(0xFF7A7A7A), Color(0xFF4D4D4D))
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )

                        else -> Modifier.background(
                            Color.White,

                            RoundedCornerShape(4.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(blockSize)
                            .padding(4.dp)
                            .then(backgroundModifier)
                            .border(
                                2.dp, Color.Black, RoundedCornerShape(4.dp)
                            )
                            .clickable { onCellClick(row, col) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isColpito) {
                            Text(
                                //Se la nave è colpita mostra la 'X' altrimenti la 'O'
                                text = if (contieneNave) "X" else "O",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
// Inizio Gioco Attivo Screen
enum class Turno { GIOCATORE, PC }
@Composable
fun GiocoAttivoScreen(navController: NavController) {
    val giocatoreShips = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<List<List<Pair<Int, Int>>>>("giocatoreShips")
    val pcShips = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<List<List<Pair<Int, Int>>>>("pcShips")
    var turno by remember {
        mutableStateOf(
            if ((0..1).random() == 0)
                Turno.GIOCATORE else Turno.PC
        )
    }
    if (giocatoreShips == null || pcShips == null) {
        Text("Dati non disponibili", color = Color.Red)
        return
    }
    //Stato + LaunchedEffect
    val celleColpiteGiocatore = remember {
        mutableStateListOf<Pair<Int,
                Int>>()
    }
    var interazioneAbilitata by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val naviDistrutteGiocatore = remember { mutableStateListOf<Int>() }
    var messaggioNaveDistrutta by remember {
        mutableStateOf<String?>(null)
    }
    var vincitore by remember { mutableStateOf<String?>(null) }
    var partitaFinita by remember { mutableStateOf(false) }
    var punteggioGiocatore by remember { mutableStateOf(0) }
    var punteggioPC by remember { mutableStateOf(0) }


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
                    vincitore = "Ha vinto il PC!"
                    partitaFinita = true
                }
            )

            if (!partitaFinita) {
                turno = Turno.GIOCATORE
                interazioneAbilitata = true
            }
        }
    }
}








