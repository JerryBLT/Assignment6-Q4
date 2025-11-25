package com.example.ballgame

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.ballgame.ui.theme.BallGameTheme

// used AI to help me figure out how to incorporate tilt input to velocity, velocity damping, and position update using velocity


class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager // SensorManager to access device sensors
    private var gyro: Sensor? = null // Gyroscope sensor instance

    private var gyroX by mutableFloatStateOf(0f)// Gyroscope X-axis angular velocity
    private var gyroY by mutableFloatStateOf(0f) // Gyroscope Y-axis angular velocity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SensorManager and get the gyroscope sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Set UI content using Jetpack Compose
        setContent {
            BallGameTheme {
                BallGameScreen(
                    gyroX = gyroX,
                    gyroY = gyroY
                )
            }
        }
    }

    // Register gyroscope sensor listener when activity resumes
    override fun onResume() {
        super.onResume()
        gyro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    // Unregister sensor listener when activity pauses
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // Update gyroX and gyroY with latest sensor angular velocity values
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        // Gyroscope returns angular velocity (rad/s)
        gyroX = event.values[0]
        gyroY = event.values[1]
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun BallGameScreen(gyroX: Float, gyroY: Float) {

    // Debug Mode (required for emulator)
    var debugMode by remember { mutableStateOf(true) }
    var simGyroX by remember { mutableFloatStateOf(0f) }
    var simGyroY by remember { mutableFloatStateOf(0f) }

    // Use real gyro values or simulated values based on debug mode
    val tiltX = if (debugMode) simGyroX else (-gyroX)   // forward/back
    val tiltY = if (debugMode) simGyroY else gyroY      // left/right


    // Ball state
    var ballX by remember { mutableFloatStateOf(300f) }
    var ballY by remember { mutableFloatStateOf(300f) }
    val ballRadius = 30f

    // Velocity
    var vx by remember { mutableFloatStateOf(0f) }
    var vy by remember { mutableFloatStateOf(0f) }

    // Ball velocity components
    val sensitivity = 22f
    val damping = 0.90f

    // Sensitivity for tilt effect and velocity damping to simulate friction
    val walls = remember {
        mutableStateListOf<
                androidx.compose.ui.geometry.Rect
                >()
    }

    LaunchedEffect(Unit) {
        // List of maze walls as rectangles for collision detection
        walls.add(androidx.compose.ui.geometry.Rect(0f, 0f, 1000f, 40f))   // top
        walls.add(androidx.compose.ui.geometry.Rect(0f, 1900f, 1100f, 2000f))  // bottom
        walls.add(androidx.compose.ui.geometry.Rect(0f, 0f, 40f, 2000f))   // left
        walls.add(androidx.compose.ui.geometry.Rect(1000f, 0f, 1100f, 2000f)) // right

        // Middle obstacle
        walls.add(androidx.compose.ui.geometry.Rect(200f, 900f, 900f, 1000f))
    }

    // Update the ball's velocity and position based on tilt input
    LaunchedEffect(tiltX, tiltY) {
        vx += (tiltY * sensitivity) // Tilt right/left affects horizontal velocity
        vy += (tiltX * sensitivity) // Tilt forward/back affects vertical velocity

        // Apply damping to simulate friction and slow ball gradually
        vx *= damping
        vy *= damping

        ballX += vx
        ballY += vy
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Debug mode toggle UI
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Debug Mode (Emulator)", color = Color.White)
            Spacer(Modifier.width(8.dp))
            Switch(checked = debugMode, onCheckedChange = { debugMode = it })
        }

        if (debugMode) {
            Text("Simulated Tilt X", color = Color.White)
            Slider(
                value = simGyroX,
                onValueChange = { simGyroX = it },
                valueRange = -3f..3f,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Text("Simulated Tilt Y", color = Color.White)
            Slider(
                value = simGyroY,
                onValueChange = { simGyroY = it },
                valueRange = -3f..3f,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // Game canvas showing the maze and the ball
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {

                val width = size.width
                val height = size.height

                // Constrain ball position within screen bounds
                ballX = ballX.coerceIn(ballRadius, width - ballRadius)
                ballY = ballY.coerceIn(ballRadius, height - ballRadius)

                // Draw each wall
                walls.forEach { rect ->
                    drawIntoCanvas {
                        it.nativeCanvas.drawRect(
                            rect.left, rect.top,
                            rect.right, rect.bottom,
                            android.graphics.Paint().apply { color = android.graphics.Color.BLACK }
                        )
                    }
                }

                // Collision with maze walls
                walls.forEach { rect ->
                    // Horizontal bounce when ball overlaps wall horizontally at vertical range
                    if (ballY in rect.top..rect.bottom &&
                        (ballX + ballRadius > rect.left && ballX - ballRadius < rect.right)
                    ) {
                        vx = -vx * 0.6f
                    }
                    // Vertical bounce when ball overlaps wall vertically at horizontal range
                    if (ballX in rect.left..rect.right &&
                        (ballY + ballRadius > rect.top && ballY - ballRadius < rect.bottom)
                    ) {
                        vy = -vy * 0.6f // Reverse and reduce vertical velocity
                    }
                }

                // Draw the ball as an yellow circle at its current position
                drawCircle(
                    color = Color.Yellow,
                    radius = ballRadius,
                    center = Offset(ballX, ballY)
                )
            }
        }
    }
}
