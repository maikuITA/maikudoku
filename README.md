# Maikudoku

Base app Sudoku in Kotlin con architettura MVVM, UI in Jetpack Compose e navigazione con Navigation Compose.

## Struttura

- `ui/menu/MenuScreen.kt`: schermata iniziale con scelta difficolta (`Facile`, `Intermedio`, `Difficile`).
- `navigation/AppNavGraph.kt`: route `menu` e `game/{difficulty}` con passaggio parametro.
- `ui/game/GameScreen.kt`: schermata gioco con rendering griglia e refresh partita.
- `ui/game/SudokuViewModel.kt`: stato UI e avvio generazione griglia in base alla difficolta.
- `domain/sudoku/SudokuGenerator.kt`: generatore Sudoku 9x9 con celle nascoste secondo difficolta.
- `domain/model/Difficulty.kt`: enum difficolta e mapping da parametro route.

## Esecuzione

1. Assicurati che `JAVA_HOME` punti a un JDK installato.
2. Apri il progetto in Android Studio e lancia l'app.

## Test rapidi

Esegui i test unitari:

```bash
./gradlew test
```

