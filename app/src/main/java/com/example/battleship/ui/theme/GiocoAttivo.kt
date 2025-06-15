package com.example.battaglianavale

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.isPopupLayout


enum class Turno { GIOCATORE, PC }

@Composable

//Recupero delle navi del giocatore e del PC da Gioco.kt
fun GiocoAttivoScreen(navController: NavController) {
    val giocatoreShips = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<List<List<Pair<Int, Int>>>>("giocatoreShips")

    val pcShips = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<List<List<Pair<Int, Int>>>>("pcShips")

    val turno by remember { mutableStateOf(Turno.GIOCATORE) }

    //Breve check per la presenza di dati: se null mostra errore
    if (giocatoreShips == null || pcShips == null) {
        Text("Dati non disponibuli", color = MaterialTheme.colorScheme.error)
        return
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(30.dp)) //Da verificare e approvare

            val snackbarHostState = remember { SnackbarHostState() }

            var messaggioNaveDistrutta by remember { mutableStateOf<String?>(null) }

            if (messaggioNaveDistrutta != null) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(messaggioNaveDistrutta!!)
                    }
                }

                //Il messaggio "scompare" dopo un po' di tempo
                LaunchedEffect(messaggioNaveDistrutta) {
                    snackbarHostState.showSnackbar(
                        messaggioNaveDistrutta!!,
                        duration = SnackbarDuration.Short
                    )
                    messaggioNaveDistrutta = null
                }
            }
            Spacer(Modifier.height(40.dp))

            //Visualizzazione campo del giocatore
            val customFont = FontFamily(Font(R.font.inter_ectrabold))
            if (turno == Turno.PC) {
                Text("Player Field", fontSize = 20.sp, fontFamily = customFont)
                Grid8x8(
                    placedShips = giocatoreShips,
                    mostraNavi = true,
                    celleColpite = remember { mutableStateListOf() },
                    onCellClik = { _, _ -> }
                )
            }
        }
    }
}

