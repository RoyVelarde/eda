# EDA 2026

**Sistema de Gestión de la Evaluación de Ascenso de Docentes 2026**

> Aplicación web desarrollada para la gestión y administración del proceso de **Evaluación de Ascenso de Docentes 2026**, correspondiente a la sede **Ayacucho – Huamanga**.

> El sistema permite gestionar usuarios, personal, aulas, capacitaciones, asignaciones de participantes y tutores, así como consultar y generar reportes.

## Requisitos previos

Antes de ejecutar el proyecto, asegúrate de tener instaladas las siguientes herramientas:

- Docker Desktop
- Java JDK 25
- Node.js y npm
- Maven
- Git

## Instalación y ejecución local

Sigue los pasos en el orden indicado para iniciar correctamente el sistema.

### 1. Iniciar Docker Desktop

Abre **Docker Desktop** y espera hasta que el motor de Docker esté funcionando.

Puedes verificarlo ejecutando:

```powershell
docker --version
docker compose version
```

### 2. Levantar los contenedores

Desde la carpeta del proyecto que contiene el archivo `docker-compose.yml`, ejecuta: 

```powershell
docker compose up -d
```

### 3. Configurar las variables de entorno

El proyecto utiliza archivos `.env` para almacenar las variables de configuración necesarias. En las carpetas donde exista un archivo `.env.example`, crea una copia con el nombre `.env`.

> Abre el archivo `.env` y configura las variables y credenciales correspondientes.

### 4. Iniciar el backend

Abre el proyecto backend en **IntelliJ IDEA**. Inicia los microservicios desde sus respectivas clases principales de Spring Boot.

> Cada microservicio debe ejecutarse con las variables de entorno correspondientes.

### 5. Iniciar el frontend

Abre una terminal en la carpeta `frontend`. Instala las dependencias del proyecto e inicia el servidor de desarrollo:

```powershell
npm install
npm run dev
```

### 6. Acceder al sistema

Abre el navegador e ingresa a:

> **http://localhost:5173**

**Usuario de desarrollo:**

```text
Usuario: cts
Contraseña: Admin2026!
```

---

**EDA 2026 — Evaluación de Ascenso de Docentes**  
**Sede Ayacucho – Huamanga**