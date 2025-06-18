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

import androidx.compose.foundation.layout.Arrangement


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
    val customFont = FontFamily(Font(R.font.inter_extrabold))
    val customCyan = Color(0xFFC1CFD5)

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

    val celleColpiteGiocatore = remember { mutableStateListOf<Pair<Int, Int>>() }
    var interazioneAbilitata by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val naviDistrutteGiocatore = remember { mutableStateListOf<Int>() }
    var messaggioNaveDistrutta by remember { mutableStateOf<String?>(null) }
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

    Spacer(Modifier.height(50.dp)) //Provvisorio

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

    Spacer(modifier = Modifier.height(16.dp))
    //Etichetta che consente di visualizzare il turno
    Box(
        modifier = Modifier
            .width(150.dp)
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

    Spacer(Modifier.height(200.dp))
    /*Valore MOMENTANEO. Da modificare in seguito*/

    Box(
        modifier = Modifier
            .clickable { navController.popBackStack() }
            .size(150.dp, 60.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.black_button),
            contentDescription = "Sfondo Return Button",
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







