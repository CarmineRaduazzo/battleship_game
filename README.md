**BattleShip Game – Android Application**

BattleShip is an Android application developed as an educational project, with the aim of reimagining the classic Battleship game in a modern and interactive form. The app allows the user to challenge the computer in a strategic duel, characterized by an intuitive interface, advanced features, and simple yet engaging gameplay logic.

**Application Overview**

The application brings back the Battleship game with a modern graphical design, built entirely with Jetpack Compose. Players can place their fleet manually or use an automatic placement feature, and then engage in a challenge against the in-game AI.

The gameplay experience is enhanced by dynamic turn management, a scoring system, real-time visual notifications, and end-of-game messages. The design was conceived to be minimal and accessible while preserving the visual identity of the original game.

**Key Features**

- Dynamic 8x8 grid for ship placement.
- Manual or automatic fleet positioning.
- Match against a computer opponent with automated turn logic.
- Scoring system based on the size of ships destroyed.
- Visual feedback when ships are hit or sunk.
- Modern and responsive UI developed entirely with Jetpack Compose.
- Simple screen navigation with state preservation between stages.

**Installation**

To install and run the application on an Android device or emulator, follow these steps:
**1. Clone the repository from GitHub:**
  - Open Android Studio
  - Select: _File_ → _New_ → _Project from Version Control_
  - Choose Git as the Version Control type
  - Paste the following URL into the URL field: https://github.com/CarmineRaduazzo/battleship_game
2. Open the project and wait for the Gradle synchronization to complete.
3. Ensure the development environment is correctly configured:
  - Android SDK **version 35** installed (API Level 35)
  - Minimum supported version: Android 7.0 (API Level 24)
  - A working Android emulator or a physical device connected via USB
4. Run the application:
  - Select the target device or emulator
  - Click the _Run_ button in Android Studio to build and launch the app

**Project Structure**

The app is structured into three main screens:
- **MainActivity**: the entry point and navigation menu.
- **GiocoScreen**: fleet placement and game setup screen.
- **GiocoAttivoScreen**: active gameplay screen where the user challenges the AI.

The project is developed entirely in Kotlin, using Jetpack Compose for the user interface. All gameplay logic is self-contained within the app and does not require external connections. The project was developed by a three-member team, with responsibilities distributed according to each member’s area of expertise.

Some implementation details have been intentionally omitted to focus this documentation on the user experience and key functionalities.
