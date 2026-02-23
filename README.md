# 🇷🇴 TourScan România

**Descoperă istoria din jurul tău prin lentila camerei tale.**

TourScan este o aplicație modernă pentru Android, creată pentru a te ajuta să identifici și să descoperi informații despre atracții turistice, monumente și locuri istorice din România. Pur și simplu fă o fotografie sau alege una din galerie, iar aplicația va analiza imaginea și îți va spune povestea din spatele locației.

---

## ✨ Funcționalități Principale

* **📸 Recunoaștere Inteligentă a Obiectivelor:** Fă o poză sau selectează una existentă pentru a analiza și identifica instantaneu obiective turistice din România.
* **✨ Interfață Modernă (Glassmorphism):** Un design premium și curat, construit integral cu Jetpack Compose, folosind gradienți personalizați, efecte de sticlă mată (blur) și animații fluide.
* **🌓 Suport Dinamic pentru Teme (Dark/Light):** Adaptare automată la tema sistemului, folosind negru pur (OLED) pe timp de noapte pentru economisirea bateriei și un aspect elegant.
* **📚 Galerie Personală (Istoric):** Păstrează evidența tuturor locațiilor scanate într-o grilă foto modernă, organizată pe 3 coloane.
* **📖 Informații Detaliate:** Accesează detalii complete despre locurile pe care le-ai scanat, inclusiv descrieri și data exactă a capturii.

---

## 🛠️ Tehnologii Folosite

Acest proiect folosește cele mai noi standarde și biblioteci pentru dezvoltarea nativă pe Android:

* **Limbaj:** [Kotlin](https://kotlinlang.org/)
* **Interfață (UI):** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material Design 3)
* **Arhitectură:** MVVM (Model-View-ViewModel) 
* **Dependency Injection:** [Koin](https://insert-koin.io/)
* **Încărcare Imagini:** [Coil](https://coil-kt.github.io/coil/)
* **Navigație:** Jetpack Navigation Compose
* **Asincronism:** Kotlin Coroutines & StateFlow

---

## 🏗️ Structura Proiectului

* `ui/screens/home/`: Conține ecranul principal de scanare, lansatoarele pentru cameră/galerie și bara de navigație inferioară în stil glassmorphism.
* `ui/screens/photolist/`: Gestionează istoricul locațiilor scanate, afișate într-o grilă dinamică pe 3 coloane.
* `ui/screens/details/`: Ecranul care afișează imaginea mărită și informațiile detaliate despre obiectivul identificat.
* `ui/components/`: Componente vizuale reutilizabile în întreaga aplicație.
