# Kata 3 - Manejo de JSON y Arquitectura Hexagonal

## Objetivo de la Entrega
Construir una aplicación capaz de consumir un dataset en formato JSON desde una URL externa, transformar y validar los datos hacia un modelo de dominio fuertemente tipado, y generar un nuevo reporte estructurado en formato JSON que incluya metadatos calculados. Todo el desarrollo se rige bajo los principios de la Arquitectura Hexagonal, logrando un desacoplamiento total entre la lógica de negocio y las herramientas de entrada/salida (I/O).

## Vídeo Explicativo
- **Enlace al vídeo (YouTube):** https://youtu.be/PWiLVXCVHrQ
- **Duración aproximada:** 4:24

## Entorno y Requisitos
- **JDK:** Oracle OpenJDK 27 | **Gestor:** Maven | **IDE:** IntelliJ IDEA
- **Librerías Nativas:** Uso de `java.net.http.HttpClient` y `java.net.http.HttpRequest` para las peticiones de red.
- **Dependencias Externas:**
    - `Gson` (Google) para la serialización y deserialización de archivos JSON, implementando `GsonBuilder` con `setPrettyPrinting` para un formateo de salida legible.

## Estructura del Proyecto (Arquitectura Hexagonal)
El código se organiza bajo el paquete `software.ulpgc.katas`, aplicando una separación estricta de responsabilidades:

- **Paquete `domain.model` (El Núcleo):** Entidades aisladas de cualquier tecnología externa. Se utiliza un `record` inmutable (`VideoGame`) y enumerados (`Genre`, `Platform`) para blindar y tipar los datos.
- **Paquete `ports` (Los Contratos):** Interfaces genéricas (`GameReader`, `GameWriter`) que definen qué operaciones de entrada y salida necesita el dominio, sin especificar cómo se hacen.
- **Paquete `adapters` (La Infraestructura):** Implementaciones concretas de los puertos.
    - `JsonUrlGameReader`: Extrae el JSON remoto, lo mapea a un DTO intermedio (`GameDto`) y lo transforma al modelo de dominio.
    - `JsonFileGameWriter`: Toma los datos del dominio, genera metadatos dinámicamente (`MetadataDto`, `ReportOutputDto`) y escribe el archivo `games.json` resultante en el disco.
- **Clase Principal (`Main`):** Punto de ensamblaje que actúa como inyector de dependencias, orquestando la instanciación de los adaptadores y la ejecución del flujo.

## Práctica Deliberada (Iteraciones)
Para asimilar el uso de JSON y el patrón de puertos y adaptadores, la arquitectura se ha replicado desde cero en distintos dominios aislados en ramas independientes:
- **Rama `master`:** Iteración 1 - Dataset de Videojuegos (Principal, demostrada en el vídeo).
- **Rama `iteration-2`:** Iteración 2 - [Especificar dataset si lo hay, ej: Dataset de Películas].
- **Rama `iteration-3`:** Iteración 3 - [Especificar dataset si lo hay, ej: Dataset de Libros].

## Flujo Git y Proceso de Desarrollo
- **Ramas base:** Uso de rama principal `master` y rama de desarrollo `develop`.
- **Desarrollo iterativo:** El código se construyó de adentro hacia afuera mediante commits atómicos y descriptivos. Primero se implementó el Dominio y el Puerto de lectura; luego el Adaptador de lectura y su ensamblaje; y finalmente las clases de escritura junto con el formateo JSON.
- **Integración:** Las funcionalidades se unificaron mediante merges limpios asegurando la estabilidad del sistema en cada paso.

## Ejecución Rápida y Verificación
1. Clonar el repositorio y abrir en IntelliJ IDEA.
2. Esperar a que el IDE indexe y Maven descargue la dependencia de `Gson` especificada en el `pom.xml`.
3. Navegar a la clase `Main`.
4. Ejecutar la aplicación (Shift + F10).
5. Verificar la creación del archivo `games.json` en el directorio raíz del proyecto, el cual contendrá los metadatos generados en la cabecera y la lista formateada de videojuegos.