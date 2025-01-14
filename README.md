# AgroTech 2.0 🌱

Sistema inteligente de monitoreo y gestión de riego que integra sensores Arduino para optimizar el uso del agua en cultivos.

## 🔍 Descripción

AgroTech 2.0 es una evolución significativa de nuestro sistema original, diseñado para proporcionar una solución integral en el monitoreo y gestión de sistemas de riego agrícola. Esta versión introduce una interfaz gráfica moderna y capacidades avanzadas de procesamiento de datos, permitiendo a los agricultores optimizar el uso del agua y mejorar la eficiencia de sus sistemas de riego.

### Novedades en 2.0
- Interfaz gráfica completamente renovada
- Dashboard interactivo para visualización de datos
- Sistema mejorado de procesamiento de datos
- Nuevas opciones de exportación (SQL, NoSQL, JSON)
- Integración optimizada con sensores Arduino

## ✨ Características

### Monitoreo en Tiempo Real
- Lectura continua de sensores de humedad
- Medición de temperatura ambiental
- Control de estados de irrigación
- Visualización en tiempo real de datos

### Dashboard Interactivo
- Visualización clara de métricas clave
- Gráficos interactivos de tendencias
- Alertas y notificaciones configurables
- Panel de control personalizable

### Gestión de Datos
- Importación de datos desde CSV con formato específico:
   - timestamp
   - soil_humidity
   - air_temperature
   - air_humidity
   - irrigation_status

### Tipos de Mediciones
- Por Contenedores (Baldes, Tanques, Bidones)
- Por Bomba (diferentes capacidades)
- Por Manguera (diferentes diámetros)
- Por Surco (dimensiones personalizables)
- Manual

### Formatos de Exportación
- SQL: Genera scripts para bases de datos relacionales
- NoSQL: Genera documentos para MongoDB
- JSON: Formato de intercambio de datos universal

### Estructura del Proyecto
```
src/
        |---main
|   |---java
|       |---com.agrotech
|           |---config/      // Configuraciones del sistema
        |           |---controller/  // Controladores JavaFX
        |           |---exception/   // Excepciones personalizadas
        |           |---handler/     // Manejadores de eventos
        |           |---model/       // Modelos de datos
        |           |---service/     // Servicios de negocio
        |   |---resources           // Recursos de la aplicación
|---test                    // Pruebas unitarias
```
## 🛠️ Tecnologías

- Java 21
- JavaFX
- Arduino (Hardware y Sensores)
- Maven
- JSerialComm
- CSS3
- FXML

## 📋 Requisitos Previos

### Software
- JDK 21
- Maven 3.8+
- Arduino IDE 2.0+
- IntelliJ IDEA (recomendado)

### Hardware
- Arduino Uno/Mega
- Sensor DHT11/22
- Sensor de Humedad del Suelo
- Módulo Relay (para control de riego)

## 🚀 Instalación

1. **Clonar el Repositorio**
```bash
git clone git@github.com:SebastianBC09/AgroTech2.0-App.git
cd agrotech-2.0
```

2. **Compilar el Proyecto**
```bash
mvn clean install
```

3. **Configurar Arduino**
- Abrir `arduino/AgroTechMainSketch/` en Arduino IDE
- Cargar el sketch al Arduino
- Conectar los sensores según el diagrama proporcionado

4. **Ejecutar la Aplicación**
```bash
mvn javafx:run
```

## 💻 Uso

1. **Inicio**
   - Ejecutar la aplicación
   - Conectar el dispositivo Arduino
   - Verificar la conexión de sensores

2. **Importación de Datos**
   - Seleccionar archivo CSV
   - Verificar formato de datos
   - Confirmar importación

3. **Dashboard**
   - Monitorear lecturas en tiempo real
   - Analizar tendencias
   - Configurar alertas

4. **Exportación**
   - Seleccionar formato de salida
   - Configurar parámetros
   - Generar archivo de exportación

## 📖 Documentación

La documentación detallada del proyecto está en desarrollo. Próximamente incluirá:
- Guía de Usuario
- Documentación Técnica
- Configuración de Hardware
- Preguntas Frecuentes

## 🤝 Contribuir

1. Fork del repositorio
2. Crear rama feature (`git checkout -b feature/NuevaCaracteristica`)
3. Commit de cambios (`git commit -am 'Agrega nueva característica'`)
4. Push a la rama (`git push origin feature/NuevaCaracteristica`)
5. Crear Pull Request

### Guías de Contribución
- Seguir estándares de código Java
- Documentar nuevas funcionalidades
- Incluir tests unitarios
- Actualizar documentación relevante

## 📝 Licencia

Este proyecto está bajo la Licencia MIT.

## ✍️ Autores

### Equipo Principal
- **Juan Sebastian Ballen Castañeda** - [GitHub](https://github.com/SebastianBC09)
- **Jean Francois Nizo Segura** - [GitHub](https://github.com/)
- **William Stiven Álvarez Molano** - [GitHub](https://github.com/)

### Contribuidores
- Ver lista de [contribuidores](https://github.com/tu-usuario/agrotech-2.0/contributors)

## 🙏 Agradecimientos

- Comunidad Arduino por sus librerías y soporte
- Contribuidores de JavaFX por el framework
- Todos los agricultores que probaron y proporcionaron feedback

## 🚧 Estado del Proyecto

- Versión Actual: 2.0
- Estado: En desarrollo activo
- Última Actualización: Noviembre 2024

---
Desarrollado con ❤️ para la comunidad agrícola
