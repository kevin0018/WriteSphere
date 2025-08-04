# WriteSphere

Una aplicación web moderna construida con Angular (frontend) y Spring Boot (backend).

## Requisitos del Sistema

### Requisitos Generales
- **Sistema Operativo**: Linux o Windows
- **Git**: Para clonar el repositorio

### Para el Frontend (Angular)
- **Node.js**: versión 18.13.0 o superior
- **npm**: versión 8.19.3 o superior
- **Angular CLI**: versión 20.x.x o superior

### Para el Backend (Spring Boot)
- **Java JDK**: versión 17 o superior
- **Maven**: versión 3.6.0 o superior (incluido en el proyecto con Maven Wrapper)

## Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/kevin0018/WriteSphere.git
cd WriteSphere
```

### 2. Configurar el Frontend (Angular)

#### Instalar Node.js y npm

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install nodejs npm
```

**Windows:**
Descargar desde [nodejs.org](https://nodejs.org/)

#### Instalar Angular CLI

```bash
npm install -g @angular/cli
```

#### Instalar dependencias del proyecto

```bash
cd frontend
npm install
```

#### Ejecutar el servidor de desarrollo

```bash
ng serve
```

La aplicación estará disponible en `http://localhost:4200`

### 3. Configurar el Backend (Spring Boot)

#### Instalar Java JDK

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

**Windows:**
Descargar desde [Oracle](https://www.oracle.com/java/technologies/javase-downloads.html) o [OpenJDK](https://openjdk.org/)

#### Verificar la instalación de Java

```bash
java -version
javac -version
```

#### Ejecutar la aplicación Spring Boot

El proyecto incluye Maven Wrapper, por lo que no necesitas instalar Maven por separado:

```bash
cd backend

# Linux/macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

La API estará disponible en `http://localhost:8080`

## Comandos Útiles

### Frontend
```bash
# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm start

# Compilar para producción
npm run build

# Ejecutar tests
npm test

# Servidor SSR
npm run serve:ssr:frontend
```

### Backend
```bash
# Compilar el proyecto
./mvnw compile

# Ejecutar tests
./mvnw test

# Empaquetar la aplicación
./mvnw package

# Limpiar y compilar
./mvnw clean install
```

