# 🏀 NBA en Colores

App Android de la NBA con los colores de cada equipo + IPTV deportivo.

## Qué trae
- **Juegos**: marcadores en vivo (se actualizan solos), resultados y próximos juegos por fecha, líderes PTS/REB/AST.
- **Detalle del juego**: parciales por cuarto, box score completo por equipo, forma reciente y lesionados.
- **Posiciones**: Este y Oeste con zonas de Playoffs / Play-In.
- **Equipos**: los 30 equipos con insignias en sus colores, plantilla, últimos resultados, próximos juegos.
- **Lesiones** de toda la liga y **Noticias**.
- **Equipo favorito**: la app se pinta con sus colores y sus juegos salen primero.
- **IPTV con ExoPlayer**: agrega listas por URL M3U, Xtream (servidor/usuario/clave) o archivo. La app guarda **solo los canales de deportes** y los divide: NBA, Baloncesto, Béisbol, Fútbol, NFL, Hockey, Boxeo/UFC, Tenis, Golf, Motor y Deportes generales. Favoritos, buscador y botón "Buscar en IPTV" desde cada juego.

## Descargar la APK
Cada vez que se sube un cambio a `main`, GitHub Actions compila la APK.
Ve a **Releases** (a la derecha en la página del repo) y descarga `NBA-en-Colores.apk`.

## Notas
- App no oficial, sin relación con la NBA ni sus equipos. Insignias originales hechas solo con colores y abreviaturas.
- Datos de marcadores y noticias: API pública de ESPN.
- La app no incluye canales; usa solo listas a las que tengas derecho.

## Actualizar sin borrar
En ⚙️ Ajustes → **Buscar actualización**. La app también revisa sola cada 6 horas y muestra un aviso.
Todas las versiones se firman con la misma llave (`app/nba-release.jks`), por eso se instalan encima.
