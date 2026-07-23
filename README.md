# GlycoLog — Diario Digitale per la Gestione del Diabete

Progetto per il corso di **Programmazione di Dispositivi Mobili** (Università dell'Insubria) sviluppato da Alex Fasolo (756529).

App Android in Kotlin per il monitoraggio della glicemia: registrazione multi-parametro
(glicemia, insulina rapida/lenta, carboidrati, contesto temporale, attività fisica, note),
dashboard con grafici temporali, storico filtrabile, report PDF e notifiche.

## Architettura (MVVM)

```
UI (Jetpack Compose)  →  ViewModel  →  Repository  →  Room DAO  →  SQLite
```

- **UI**: schermate Compose (`ui/`) che osservano lo stato con `collectAsStateWithLifecycle`
- **ViewModel** (`MeasurementViewModel`): stato della UI, validazione input clinici,
  statistiche derivate con `combine`/`map` su `StateFlow`; creato con factory manuale
  (`MeasurementViewModelFactory`), senza framework di dependency injection
- **Repository** (`MeasurementRepository`): livello intermedio tra ViewModel e DAO
- **Room** (`data/`): entità `MeasurementEntity`, DAO con `Flow` per le letture reattive e
  `suspend` per le scritture, database singleton (`AppDatabase`)

## Funzionalità principali

| Funzione | Implementazione |
|---|---|
| Registrazione multi-parametro | `AddEntryScreen` + validazione clinica nel ViewModel (glicemia 20–600, insulina 0–100 UI, CHO 0–300 g) |
| Dashboard con grafici 24h/7g/30g | `GlucoseChart` (Canvas Compose custom) + `ChartDataBuilder` (aggregazione per giorno/ora) |
| Indicatore colore valore | verde in range (70–139), giallo attenzione (140–180), rosso fuori range |
| Storico con filtri | `HistoryScreen`: filtri per data (tutte/7g/30g) e range glicemico (ipo/in range/iper), eliminazione con conferma |
| Statistiche | `GlucoseStats`: media, Tempo in Range (70–180), deviazione standard, distribuzione per fasce orarie |
| Report PDF | `PdfReportGenerator` con API native `PdfDocument`; periodi oggi/7g/30g/personalizzato (DateRangePicker); condivisione via `FileProvider` |
| Promemoria configurabili | `ReminderWorker` (WorkManager periodico) + `ReminderScreen` (intervallo 2–24 ore, SharedPreferences) |
| Avvisi fuori range | notifica immediata al salvataggio di un valore fuori 70–180 (`NotificationHelper`) |

## Tecniche del corso utilizzate

- **Kotlin**: data class, null safety (`?.`, `?:`), `when` con range, lambda e higher-order
  functions, collection operators (`filter`, `map`), string template
- **Room + KSP**: entity/DAO/database singleton, repository, Flow reattivi
- **MVVM + coroutines**: `viewModelScope`, `Dispatchers.IO` per la generazione PDF
- **Jetpack Compose + Navigation Compose**: `NavHost` con route testuali,
  schermate come funzioni `@Composable` con callback di navigazione
- **Risorse**: stringhe in `strings.xml`, array in `arrays.xml`, tema Material 3
  con palette personalizzata (dark del mockup + variante light)
- **Activity Result API**: richiesta runtime del permesso `POST_NOTIFICATIONS`
- **WorkManager**: promemoria periodici al posto dei Service in background
- **Notifiche**: canali (API 26+), permesso runtime (API 33+)

## Build

Il progetto si compila con Android Studio (AGP 8.7.3, Gradle 9.2.1, JDK 17+):

```
gradlew assembleDebug
```

minSdk 24 · targetSdk 35 · Kotlin 2.0.21 · Compose BOM 2024.10.01

## Possibili estensioni future

- Sincronizzazione cloud (Firestore) e modalità caregiver
- Localizzazione inglese (`values-en/strings.xml`)
- Modifica di una misurazione esistente
