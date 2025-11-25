# Assignment6-Q4
This project implements a simple mobile game where the user controls a ball by tilting their Android device. Movement comes directly from the gyroscope sensor, and the ball must navigate around walls and obstacles drawn using a Canvas. The solution also includes a Debug Mode (slider-based tilt simulation) so the game can be tested on an Android emulator

## Features
**Gyroscope-Based Tilt Control**
- Uses SensorManager and SensorEventListener
- Reads angular velocity from Sensor.TYPE_GYROSCOPE
- Maps tilt direction to intuitive screen movement
- Tilt left → ball moves left
- Tilt right → ball moves right
- Tilt forward → ball moves up
- Tilt backward → ball moves down

**Real-Time Ball Physics**
- Velocity updated every frame
- Damping applied for smooth movement
- Boundaries prevent ball from leaving the screen

**Maze & Obstacles (Canvas Drawing)**
- Maze rendered using Canvas
- Internal wall obstacles

**Debug Mode**
- Because the Android emulator does not support gyroscope hardware, the project includes:
Debug toggle (on/off)
X & Y tilt sliders to simulate gyroscope movement

**Built with Jetpack Compose**
- Fully declarative UI
- Canvas rendering
- State-driven animations using MutableState + LaunchedEffect

## Technical Breakdown

**Sensors Used**:
- Gyroscope (TYPE_GYROSCOPE)
- Provides angular velocity
Lifecycle Handling:
- Sensor registered in onResume()
- Unregistered in onPause()

**Drawing**:
- Maze and ball rendered using Canvas


## How to Test
1. git clone the respository
2. Open in Android studeio kotlin
3. Enable Debug Mode inside the app
Toggle “Debug Mode (Emulator)” switch
Move sliders to simulate tilt:
Simulated Tilt X → Up/Down
Simulated Tilt Y → Left/Right
