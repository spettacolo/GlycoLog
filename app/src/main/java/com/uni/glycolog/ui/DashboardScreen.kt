package com.uni.glycolog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    onNavigateToAddEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "GlycoLog",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            border = BorderStroke(1.dp, Color(0xFF2A2A2A))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Grafico 7 giorni", color = Color.Gray)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            border = BorderStroke(1.dp, Color(0xFF2A2A2A))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "125 mg/dL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2ECC71), shape = CircleShape)
                            .padding(8.dp)
                    ) {
                        Text("✓", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "3 ore fa · Pre-cena",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Button(
            onClick = { onNavigateToAddEntry() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2ECC71)
            )
        ) {
            Text("+ Nuova misurazione", color = Color.Black)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Text("Storico")
            }

            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Text("PDF")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            border = BorderStroke(1.dp, Color(0xFF2A2A2A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text("Media 7 giorni", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        "132 mg/dL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color.LightGray.copy(alpha = 0.2f))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Tempo in range", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        "82%",
                        color = Color(0xFF2ECC71),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
