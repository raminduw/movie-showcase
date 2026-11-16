# 🎬 CineNote

A movie companion app built to showcase **modern Android architecture**: Clean Architecture layers,
SOLID, MVI, Jetpack Compose, Hilt, type-safe Navigation, Paging 3, Room and thorough unit tests.

Browse and search the whole [TMDB](https://www.themoviedb.org) catalogue, save favourites, and keep a
private note on each one, all stored on the device.

## Features

| | |
|---|---|
| **Splash** | Android 12+ SplashScreen API, an animated film-frame/heart icon, and a zoom-out exit |
| **Movie list** | Popular / Trending / Top rated / In cinemas / Upcoming, infinite scroll (Paging 3), shimmer skeleton loading |
| **Favourites** | ♥ toggle on every card, a Favourites tab with swipe to remove and **Undo** |
| **Details** | Backdrop hero, a **shared-element poster transition**, genres, runtime, cast, trailer, similar movies |
| **Search** | Searches the whole TMDB catalogue (debounced, cancels stale requests). Favourites search covers titles *and* notes |
| **Notes** | A short personal note per favourite (bottom-sheet editor, 500-character limit), saved in Room |
| **States** | Loading, empty, error with retry, offline, and a missing/invalid token, each with a clear message |

## Setup

1. Get a free **API Read Access Token** at <https://www.themoviedb.org/settings/api>.
2. Add it to `local.properties` (never committed):
   ```properties
   TMDB_TOKEN=eyJhbGciOi...
   ```
3. Run `./gradlew :app:installDebug` or press ▶ in Android Studio.

## Architecture

```
                 ┌──────────────── :app (composition root: Hilt, NavHost, splash) ────────────────┐
                 │                                                                                 │
   :feature:movies        :feature:detail        :feature:favorites      ← UI + ViewModels (MVI)
         │                       │                       │                  never depend on each other
         └──────────┬────────────┴───────────┬───────────┘
                    ▼                        ▼
             :core:ui (MVI base,      :core:designsystem (theme,
              formatters, errors)      reusable composables)
                    │
                    ▼
             :core:domain   ← pure Kotlin: models, repository *interfaces*, use cases
                    ▲
                    │ implements
             :core:data     ← repositories, mappers, paging, error mapping
               │        │
               ▼        ▼
     :core:network   :core:database
     (Retrofit, DTOs) (Room, entities)
```

**Data flow (one direction):**

```
TMDB JSON → MovieDto ─(DTO mapper)→ Movie (domain) ─(UI mapper)→ MovieUi → Compose
Room row  → FavoriteEntity ─(entity mapper)→ FavoriteMovie (domain) → FavoriteUi → Compose

UI ──Action──▶ ViewModel ──▶ UseCase ──▶ Repository (interface) ──▶ API / DAO
UI ◀──State (StateFlow)── ViewModel        Effects (Channel) → navigation, snackbars
```

### Modules

| Module | Responsibility | Depends on |
|---|---|---|
| `core:domain` | Models, repository contracts, use cases. **No Android.** | paging-common, coroutines |
| `core:data` | `TmdbMovieRepository`, `OfflineFavoritesRepository`, mappers, `TmdbPagingSource`, error mapping | domain, network, database |
| `core:network` | `TmdbApi` (Retrofit), DTOs, `AuthInterceptor`, `TimeoutDns` | — |
| `core:database` | Room `FavoriteEntity`, `FavoriteDao` | — |
| `core:designsystem` | Theme, `MoviePoster`, `FavoriteButton`, shimmer, `NoteEditorSheet`… | — |
| `core:ui` | `MviViewModel`, `ObserveEffects`, `Formatters`, error → message | domain |
| `core:testing` | Fakes (`FakeMovieRepository`, `FakeFavoritesRepository`), `MainDispatcherExtension`, `TestData` | domain |
| `feature:*` | One screen each: Contract (State/Action/Effect), ViewModel, UI mapper, Screen, typed route | domain, ui, designsystem |
| `build-logic` | Gradle convention plugins (`cinenote.android.feature`, `cinenote.hilt`, …) | — |

## SOLID in this codebase

| Principle | Where to look |
|---|---|
| **S**ingle responsibility | One use case per action (`ToggleFavoriteUseCase`, `UpdateFavoriteNoteUseCase`…). Mappers only map, DAOs only query, composables only render state. |
| **O**pen/closed | `TmdbPagingSource` takes a `fetchPage` lambda: adding a new list needs no change to the paging code. Adding a `MovieCategory` is one enum entry plus one path. |
| **L**iskov substitution | `FakeFavoritesRepository` and `OfflineFavoritesRepository` are interchangeable: ViewModel tests run real use cases against the fake. |
| **I**nterface segregation | `FavoritesReader` and `FavoritesWriter` are separate interfaces; `ObserveFavoritesUseCase` can't write, and `RestoreFavoriteUseCase` can't read. |
| **D**ependency inversion | The domain owns `MovieRepository` and friends; `:core:data` implements them; `DataModule` binds them with Hilt `@Binds`. Features never see Retrofit or Room. |

## Testing

```bash
./gradlew test            # all unit tests, every layer
```

| Layer | What's tested | How |
|---|---|---|
| Domain | Every use case (validation, trimming, toggle, undo, note rules) | JUnit 5 + fakes, Paging `asSnapshot` |
| Network | Request paths, query params, auth header, JSON parsing, HTTP errors, DNS timeout | **MockWebServer** + JSON fixtures |
| Database | Real SQL: upsert, ordering, title/note search, note update, reactive `Flow`s | **Robolectric** + in-memory Room |
| Data | DTO/entity mappers, error mapping, paging (keys, page limit, de-duplication), repositories | MockK |
| Presentation | ViewModels (state, effects, debounce, undo, notes), UI mappers, formatters | Turbine + `MainDispatcherExtension` |

Stack: JUnit 5 (parameterised and nested tests), Truth, MockK, Turbine, kotlinx-coroutines-test.

## Tech stack

Kotlin 2.2 · Jetpack Compose (Material 3) · Hilt (KSP) · Navigation Compose (type-safe routes) ·
Paging 3 · Room · Retrofit + OkHttp + kotlinx.serialization · Coil 3 · Coroutines/Flow ·
Core SplashScreen · Gradle version catalog + convention plugins.

## Ideas for next steps

- Compose UI tests and screenshot tests (Roborazzi/Paparazzi)
- detekt/ktlint in CI
- An offline cache for the movie lists (`RemoteMediator`)
- Settings in DataStore (language, region, theme)
- Baseline Profiles

---
This product uses the TMDB API but is not endorsed or certified by TMDB.
