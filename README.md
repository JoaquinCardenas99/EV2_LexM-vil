# LexMóvil

Aplicación Android académica en Kotlin y Jetpack Compose para explorar un catálogo jurídico de ejemplo, organizar favoritos y notas, fotografiar documentos y exportar PDF. Los textos del catálogo son material de demostración, no legislación oficial actualizada.

## Equipo y planificación

- Integrantes: completar con los nombres reales del equipo.
- Trello: incorporar el enlace real y las evidencias del tablero. No se ha actualizado desde esta revisión.
- Repositorio: https://github.com/JoaquinCardenas99/EV2_LexM-vil

## Funcionalidades y arquitectura MVVM

- **View:** pantallas Compose, navegación, formularios y lanzadores de cámara/selector de archivos.
- **ViewModel:** estados observables, validaciones, carga, errores, favoritos y coordinación de operaciones. El trabajo de archivos se ejecuta en `Dispatchers.IO`.
- **Repository:** Retrofit, sesión cifrada con Android Keystore, catálogo, indicador externo y acceso a archivos. Las pantallas no realizan HTTP ni escriben PDF directamente.
- **Backend:** Spring Boot, Java 21, H2 persistente; validación, contraseñas BCrypt y tokens revocables almacenados como hash.
- Registro/login, restauración de sesión y cierre de sesión real. Cada cuenta ve solo sus favoritos.
- CRUD de favoritos: agregar, listar, editar nota y eliminar.
- Catálogo y detalle consumidos desde el servidor, con errores visibles y reintento.
- UF consultada por Retrofit a mindicador.cl, mostrando valor y fecha de la respuesta. Su fallo no bloquea el resto de la app.
- Recursos nativos: **cámara** y **archivos**, con exportación de foto y PDF mediante el selector del sistema. No se solicitan permisos amplios de almacenamiento.
- Ícono propio de balanza, en formato vectorial y adaptativo.

## Endpoints utilizados

La API propia comienza en `/api`; los endpoints de perfil y favoritos requieren `Authorization: Bearer <token>`.

| Método | Ruta | Acción desde Android |
|---|---|---|
| POST | `/auth/register` | Registrar cuenta |
| POST | `/auth/login` | Iniciar sesión |
| GET | `/auth/me` | Restaurar sesión |
| POST | `/auth/logout` | Cerrar sesión |
| GET | `/favorites` | Cargar favoritos |
| POST | `/favorites` | Agregar favorito |
| PUT | `/favorites/{id}` | Guardar nota |
| DELETE | `/favorites/{id}` | Eliminar favorito |
| GET | `/laws` | Cargar catálogo y búsqueda local sobre su respuesta |
| GET | `/laws/{id}` | Abrir detalle |
| GET | `https://mindicador.cl/api/uf` | Mostrar UF de referencia en inicio |

Son diez operaciones de la API propia sobre ocho patrones de ruta y una consulta externa HTTPS. La búsqueda filtra el catálogo descargado y no se contabiliza como otro endpoint.

## Ejecutar el backend

Requisitos: Java 21 y Maven. Desde la raíz:

```bash
cd backend
mvn test package
java -jar target/lexmovil-api-0.1.0.jar
```

En ejecuciones posteriores se puede usar `bash scripts/iniciar-backend.sh`. Mantén esa terminal abierta durante la demostración. El servidor escucha en 8080; no ejecutes dos instancias simultáneas.

H2 persiste en `backend/data/lexmovil.mv.db`. Reiniciar desde el mismo directorio conserva usuarios y favoritos. No borrar esa carpeta. Variables opcionales: `PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## Ejecutar Android y entregar APK

1. Abrir `Ev2_DesarrolloAPL` en Android Studio. El proyecto utiliza SDK 37, Gradle 9.6 y JDK 25 para el daemon de Gradle.
2. Iniciar el backend con Java 21.
3. Iniciar un emulador Android y ejecutar la app o instalar el APK firmado.
4. Crear una cuenta de prueba; las cuentas antiguas guardadas solo en memoria no se migran.

El APK local para emulador utiliza `http://10.0.2.2:8080/api/`. **El backend debe permanecer encendido en el computador anfitrión.** El APK no contiene el servidor ni funciona con esa dirección en un teléfono físico.

Para compilar el APK firmado destinado al emulador:

```bash
bash scripts/compilar-release.sh
```

Salida: `Ev2_DesarrolloAPL/app/build/outputs/apk/release/app-release.apk`.

Para un teléfono físico en la misma Wi-Fi, reemplazar la IP por la del computador:

```bash
LEXMOVIL_API_URL=http://192.168.1.100:8080/api/ bash scripts/compilar-release.sh
```

El teléfono debe alcanzar ese servidor y puerto. Alternativa para un teléfono conectado por USB: compilar con `http://127.0.0.1:8080/api/` y ejecutar `adb reverse tcp:8080 tcp:8080` antes de abrir la app. Para uso fuera de la red local se necesita un backend HTTPS publicado y recompilar con esa URL.

HTTP se habilita explícitamente solo para la compilación de demostración local mediante `LEXMOVIL_ALLOW_HTTP=true`. Usar cuentas de prueba; el tráfico local HTTP no cifra las credenciales. El valor predeterminado de esa opción es `false`.

## Firma release

La configuración lee `Ev2_DesarrolloAPL/keystore.properties`, excluido de Git, y usa una clave propia en `Ev2_DesarrolloAPL/signing/lexmovil-release.jks`, también excluida. Esta clave debe respaldarse de forma privada para poder actualizar el APK. No subir claves ni contraseñas al repositorio.

En otra máquina, copiar ambos archivos privados de forma segura, o crear una clave nueva con Android Studio → Generate Signed App Bundle or APK. La estructura del archivo de propiedades es:

```properties
storeFile=signing/lexmovil-release.jks
storePassword=CONTRASENA_PRIVADA
keyAlias=lexmovil
keyPassword=CONTRASENA_PRIVADA
```

Si ya existe instalada una versión debug, Android no permite actualizarla con una firma distinta. En ese caso, desinstalar manualmente la versión debug antes de instalar release; esto elimina la sesión local, pero conserva los datos del backend.

## Pruebas y verificación

```bash
cd backend
mvn test
cd ../Ev2_DesarrolloAPL
bash gradlew testDebugUnitTest -Pkotlin.compiler.execution.strategy=in-process
```

Diez pruebas Android con JUnit/MockWebServer cubren login, persistencia del token, restauración, 401, error de servidor, logout, arranque sin token, catálogo/filtro, detalle y UF válida/vacía (varias comprobaciones en una misma prueba). El backend tiene pruebas unitarias de autenticación e integración HTTP de sesiones, aislamiento de favoritos, validaciones y catálogo.

No se acredita 80% de cobertura: el anuncio compartido requiere al menos dos pruebas, mientras el PDF encontrado solicita 80%. El estado exacto de ejecución se registra en `VERIFICACION.txt`.

## Guion de defensa

1. Mostrar backend activo y abrir la app.
2. Registrar e iniciar sesión; comprobar UF y su fecha.
3. Abrir catálogo, buscar Civil y consultar detalle.
4. Agregar favorito, escribir una nota, guardar y comprobar en Mis Favoritos; después eliminarlo.
5. Exportar PDF a una ubicación elegida y abrirlo con un visor.
6. Tomar una foto, cancelar si se desea y repetir; guardarla mediante el selector de archivos.
7. Cerrar y volver a abrir la app; comprobar restauración de sesión. Cerrar sesión y volver a ingresar.
8. Mostrar pruebas, contrato Retrofit, capas MVVM y configuración de firma sin exponer contraseñas.

## Pendientes fuera del código

Completar integrantes y Trello con información real. Confirmar el criterio definitivo del docente ante las diferencias entre anuncio y PDF. Ensayar en el dispositivo de defensa. No se inventan commits históricos ni participación de compañeros.
