@echo off
REM Levanta Postgres (Docker), el backend y la web, y abre el navegador.
REM Colocar un acceso directo a este archivo en el Escritorio para arrancar todo con un doble clic.

cd /d "%~dp0"

echo Levantando PostgreSQL con Docker Compose...
docker compose up -d postgres

echo Iniciando backend (Spring Boot) en una nueva ventana...
start "Backend - Sistema Contable" cmd /k "cd backend && gradlew.bat bootRun --args=--spring.profiles.active=dev"

echo Iniciando web (Vite) en una nueva ventana...
start "Web - Sistema Contable" cmd /k "cd web && npm install && npm run dev"

echo Esperando a que la web arranque...
timeout /t 12 /nobreak > nul

start "" "http://localhost:5173"

echo.
echo Listo. Cierra las ventanas de Backend y Web para detener el sistema.
pause
