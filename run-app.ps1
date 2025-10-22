# =====================================================
# Script de Inicio - Economía Circular
# Facilita ejecutar la aplicación en diferentes modos
# =====================================================

param(
    [Parameter(Position=0)]
    [ValidateSet('dev', 'prod', 'test', 'help')]
    [string]$Profile = 'help'
)

function Show-Help {
    Write-Host "`n==================================================" -ForegroundColor Cyan
    Write-Host "  Economía Circular - Script de Inicio" -ForegroundColor Cyan
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Uso:" -ForegroundColor Yellow
    Write-Host "  .\run-app.ps1 [perfil]" -ForegroundColor White
    Write-Host ""
    Write-Host "Perfiles disponibles:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  dev" -ForegroundColor Green
    Write-Host "    - Base de datos: MySQL local (localhost:3306)" -ForegroundColor Gray
    Write-Host "    - Datos: Mínimos (4 usuarios, 5 artículos)" -ForegroundColor Gray
    Write-Host "    - Usuario: admin@test.com / Test123!" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  prod" -ForegroundColor Green
    Write-Host "    - Base de datos: Google Cloud SQL" -ForegroundColor Gray
    Write-Host "    - Datos: Completos (11 usuarios, 36 artículos)" -ForegroundColor Gray
    Write-Host "    - Usuario: admin@economiacircular.com / Test123!" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  test" -ForegroundColor Green
    Write-Host "    - Ejecuta los tests unitarios" -ForegroundColor Gray
    Write-Host "    - Base de datos: H2 en memoria" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Ejemplos:" -ForegroundColor Yellow
    Write-Host "  .\run-app.ps1 dev      # Inicia en modo desarrollo" -ForegroundColor White
    Write-Host "  .\run-app.ps1 prod     # Inicia en modo producción" -ForegroundColor White
    Write-Host "  .\run-app.ps1 test     # Ejecuta tests" -ForegroundColor White
    Write-Host ""
    Write-Host "Notas importantes:" -ForegroundColor Yellow
    Write-Host "  - Para desarrollo, asegúrate de tener MySQL corriendo" -ForegroundColor Gray
    Write-Host "  - Crea la BD: CREATE DATABASE economia_circular;" -ForegroundColor Gray
    Write-Host "  - Liquibase creará las tablas automáticamente" -ForegroundColor Gray
    Write-Host ""
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Start-Dev {
    Write-Host "`n==================================================" -ForegroundColor Green
    Write-Host "  INICIANDO EN MODO DESARROLLO" -ForegroundColor Green
    Write-Host "==================================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Verificando MySQL..." -ForegroundColor Yellow
    
    # Verificar si MySQL está corriendo
    $mysqlProcess = Get-Process mysqld -ErrorAction SilentlyContinue
    if ($null -eq $mysqlProcess) {
        Write-Host "⚠️  ADVERTENCIA: MySQL no parece estar corriendo" -ForegroundColor Red
        Write-Host "   Asegúrate de iniciar MySQL antes de continuar" -ForegroundColor Yellow
        Write-Host ""
        $continue = Read-Host "¿Continuar de todos modos? (s/n)"
        if ($continue -ne 's') {
            Write-Host "Abortado." -ForegroundColor Red
            return
        }
    } else {
        Write-Host "✅ MySQL está corriendo" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "Configuración:" -ForegroundColor Yellow
    Write-Host "  - Perfil: dev" -ForegroundColor Gray
    Write-Host "  - Base de datos: localhost:3306/economia_circular" -ForegroundColor Gray
    Write-Host "  - Datos: Mínimos para desarrollo" -ForegroundColor Gray
    Write-Host "  - Puerto: 8080" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Usuarios de prueba:" -ForegroundColor Yellow
    Write-Host "  - admin@test.com / Test123!" -ForegroundColor Cyan
    Write-Host "  - test@test.com / Test123!" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Iniciando aplicación..." -ForegroundColor Green
    Write-Host "==================================================" -ForegroundColor Green
    Write-Host ""
    
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
}

function Start-Prod {
    Write-Host "`n==================================================" -ForegroundColor Magenta
    Write-Host "  INICIANDO EN MODO PRODUCCIÓN" -ForegroundColor Magenta
    Write-Host "==================================================" -ForegroundColor Magenta
    Write-Host ""
    Write-Host "Configuración:" -ForegroundColor Yellow
    Write-Host "  - Perfil: prod" -ForegroundColor Gray
    Write-Host "  - Base de datos: Google Cloud SQL" -ForegroundColor Gray
    Write-Host "  - Datos: Completos para producción" -ForegroundColor Gray
    Write-Host "  - Puerto: 8080" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Usuarios de prueba:" -ForegroundColor Yellow
    Write-Host "  - admin@economiacircular.com / Test123!" -ForegroundColor Cyan
    Write-Host "  - maria.gonzalez@email.com / Test123!" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Iniciando aplicación..." -ForegroundColor Green
    Write-Host "==================================================" -ForegroundColor Magenta
    Write-Host ""
    
    mvn spring-boot:run -Dspring-boot.run.profiles=prod
}

function Start-Test {
    Write-Host "`n==================================================" -ForegroundColor Blue
    Write-Host "  EJECUTANDO TESTS" -ForegroundColor Blue
    Write-Host "==================================================" -ForegroundColor Blue
    Write-Host ""
    Write-Host "Ejecutando tests unitarios..." -ForegroundColor Yellow
    Write-Host ""
    
    mvn test
    
    Write-Host ""
    Write-Host "==================================================" -ForegroundColor Blue
    Write-Host "Tests completados" -ForegroundColor Blue
    Write-Host "==================================================" -ForegroundColor Blue
    Write-Host ""
}

# Main
switch ($Profile) {
    'dev' { Start-Dev }
    'prod' { Start-Prod }
    'test' { Start-Test }
    'help' { Show-Help }
    default { Show-Help }
}

