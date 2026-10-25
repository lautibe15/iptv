# TV Personal

Aplicación Android simple para reproducir streams HLS `.m3u8`.

## Incluye
- Telefe, El Trece y TyC Sports precargados.
- Agregar, editar y eliminar canales desde la app.
- Soporte HTTP y HTTPS.
- Reproducción HLS con AndroidX Media3 / ExoPlayer.
- Pantalla completa y controles nativos.
- Canales guardados localmente en el dispositivo.

## Generar APK con GitHub Actions
El workflow `.github/workflows/build-apk.yml` genera un APK de depuración.

1. Subir el proyecto a un repositorio GitHub.
2. Abrir **Actions** > **Build APK**.
3. Ejecutar **Run workflow** (o hacer un commit en `main`).
4. Al terminar, descargar el artifact **TVPersonal-APK**.
5. Dentro del ZIP está `app-debug.apk`.

El APK de debug se puede instalar manualmente en un TV Box Android habilitando la instalación desde fuentes desconocidas para el navegador/gestor de archivos utilizado.

## Nota
La app reproduce la URL que el usuario configure. Si el servidor cambia, expira, requiere autenticación/DRM o deja de emitir, la app no puede recuperar automáticamente una nueva URL.
