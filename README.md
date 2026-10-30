LexMovil
Aplicación móvil desarrollada como Producto Mínimo Viable (MVP) para la asignatura DSY1105 – Desarrollo de Aplicaciones Móviles de Duoc UC. El proyecto responde al caso “Estudio y consulta móvil de legislación actualizada” de la organización Código CH.

LexMovil busca reunir en una sola aplicación móvil funciones básicas para consultar material jurídico de ejemplo y apoyar su estudio.
Objetivo del proyecto
Facilitar la consulta y organización de contenido jurídico desde un dispositivo Android, reduciendo la necesidad de utilizar distintas herramientas para buscar y revisar material de estudio. La aplicación es un prototipo académico y no reemplaza las fuentes oficiales ni entrega asesoría jurídica.
Funcionalidades implementadas
- Registro de usuario: formulario para crear una cuenta con nombre, RUT, correo, tipo de usuario y contraseña, con validaciones.
- Inicio de sesión: permite ingresar con una cuenta registrada y mantener la sesión guardada en el dispositivo.
- Consulta de leyes y códigos: listado de contenido jurídico de ejemplo organizado por categoría.
- Búsqueda: permite buscar por nombre, código, categoría o descripción.
- Detalle de una norma: muestra información general y artículos asociados.
- Favoritos: permite marcar normas para acceder a ellas desde una lista de favoritos durante el uso de la aplicación.
- Descarga en PDF: genera un documento PDF con el contenido de la norma seleccionada.
- Captura de documentos: permite tomar una fotografía con la cámara, revisar la imagen y guardarla.
- Pantalla “Acerca de”: entrega información sobre la aplicación.
Tecnologías utilizadas
- Kotlin como lenguaje de programación.
- Android Studio como entorno recomendado de desarrollo.
- Jetpack Compose para construir la interfaz de usuario.
- Material 3 para componentes visuales.
- Navigation Compose para la navegación entre pantallas.
- ViewModel y StateFlow para administrar y observar el estado de la aplicación.
- SharedPreferences para guardar localmente los datos de usuarios y la sesión.
- Android FileProvider para compartir de forma segura la ubicación temporal de las fotografías.
Estructura general del proyecto
El código fuente principal se encuentra en Ev2_DesarrolloAPL/app/src/main/java/duoc/example/EV2_Desarrollo/.
duoc.example.EV2_Desarrollo/
├── MainActivity.kt
├── model/
│   ├── ErroresFormulario.kt
│   ├── Ley.kt
│   └── UsuarioMemory.kt
├── repository/
│   └── LeyRepository.kt
├── ui/
│   ├── Camara.kt
│   ├── auth/
│   ├── componentes/
│   ├── navegacion/
│   ├── pantallas/
│   └── theme/
└── viewmodel/
    ├── AuthViewModel.kt
    └── LeyViewModel.kt
- model: define las estructuras de datos de las leyes, artículos, usuarios y errores de formulario.
- repository: contiene y entrega el catálogo de leyes de ejemplo.
- ui: reúne las pantallas, componentes visuales, navegación y tema.
- viewmodel: administra el estado, las búsquedas, los favoritos y las operaciones de autenticación.
Requisitos
- Android Studio con soporte para proyectos Kotlin y Jetpack Compose.
- JDK compatible con la configuración de Gradle del proyecto.
- Emulador Android o dispositivo Android para ejecutar y probar la aplicación.
- Para probar la captura de fotografías, se requiere un dispositivo o emulador con cámara y conceder el permiso correspondiente.
Cómo ejecutar el proyecto
1. Descarga o clona este repositorio.
2. Abre Android Studio.
3. Selecciona Open y abre la carpeta Ev2_DesarrolloAPL (la carpeta que contiene settings.gradle.kts).
4. Espera a que termine la sincronización de Gradle y se descarguen las dependencias.
5. Selecciona un emulador o conecta un dispositivo Android.
6. Ejecuta la configuración app con el botón Run.
Si Android Studio solicita instalar una versión del SDK o herramientas de compilación, instala los componentes indicados y vuelve a sincronizar el proyecto.
Datos y alcance del MVP
El catálogo jurídico está definido dentro del código en LeyRepository.kt y utiliza contenido de ejemplo. La aplicación no consulta automáticamente una API oficial ni garantiza que los textos legales estén actualizados. La integración con una fuente oficial, el historial real de modificaciones normativas y funciones adicionales de estudio quedan fuera de lo que está conectado en esta versión.

Los usuarios se almacenan localmente mediante SharedPreferences; no hay una base de datos remota ni un sistema de autenticación de servidor. Por ello, esta versión está pensada para pruebas y demostraciones académicas, no para almacenar información sensible o utilizarse como sistema productivo.
Pruebas sugeridas
1. Registrar un usuario con datos de prueba y comprobar las validaciones del formulario.
2. Iniciar sesión con la cuenta creada y verificar que se abra la pantalla principal.
3. Abrir el listado de leyes y entrar al detalle de una norma.
4. Buscar por código, título o categoría.
5. Marcar una norma como favorita y revisar la sección Mis Favoritos.
6. Generar el PDF de una norma y comprobar que se guarde correctamente.
7. Probar la captura de una fotografía y aceptar o denegar el permiso de cámara.
Consideraciones
LexMovil es un proyecto académico de alcance MVP. El contenido incluido es demostrativo, no constituye asesoría jurídica y debe contrastarse con fuentes oficiales antes de utilizarse con fines legales.
