package com.example.battleship

import kotlinx.coroutines.delay

enum class Turno { GIOCATORE, PC }

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



































