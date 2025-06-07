package com.example.battleship
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.battleship.ui.theme.BattleShipTheme

import androidx.compose.material3.Text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BattleShipTheme {
                val navController = rememberNavController()
                val customFont = FontFamily(Font(R.font.inter_extrabold))

                NavHost(navController = navController, startDestination = "menu") {
                    composable("menu") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFFFFFF))
                                .padding(top = 125.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.home_image),
                                    contentDescription = "Home Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(430.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.battleship_game),
                                    color = Color.Black,
                                    fontSize = 70.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = customFont,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 80.sp
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .fillMaxWidth()
                                        .padding(bottom = 80.dp, start = 35.dp, end = 35.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(150.dp)
                                            .height(60.dp)
                                            .clickable { finish() }
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.black_button),
                                            contentDescription = "Quit Button",
                                            contentScale = ContentScale.Inside,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Text(
                                            text = stringResource(id = R.string.quit),
                                            color = Color.Cyan,
                                            fontSize = 20.sp,
                                            fontFamily = customFont,
                                            modifier = Modifier.align(Alignment.Center)
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
}
