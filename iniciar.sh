#!/bin/bash

# Capturar Ctrl+C para detener la app
trap 'echo ""; echo "Deteniendo la aplicación..."; kill $APP_PID; wait $APP_PID 2>/dev/null; echo "La app se detuvo."; exit' INT

echo "=== Iniciando script de FitnessTracker ==="

# Verificar si la DB Docker está corriendo
DB_NAME="fitness_mysql"
DB_STATUS=$(docker ps --filter "name=$DB_NAME" --format "{{.Names}}")

if [ "$DB_STATUS" == "$DB_NAME" ]; then
    echo "La base de datos '$DB_NAME' ya está corriendo en Docker."
else
    echo "La base de datos '$DB_NAME' no está corriendo. Iniciando..."
    docker start $DB_NAME
    echo "Base de datos iniciada."
fi

# Esperar un par de segundos para asegurar que la DB esté lista
sleep 3

# Iniciar la aplicación
echo "Iniciando la aplicación FitnessTracker en Maven..."
mvn spring-boot:run &
APP_PID=$!
echo "Aplicación iniciada con PID $APP_PID. Esperando que arranque..."

# Esperar unos segundos y verificar puerto 8080
sleep 5

if lsof -i:8080 -sTCP:LISTEN >/dev/null ; then
    echo "✅ La aplicación está corriendo en http://localhost:8080"
else
    echo "❌ No se pudo detectar la aplicación en el puerto 8080. Verificar logs."
fi

# Mantener script en primer plano hasta que se detenga la app
wait $APP_PID

echo "=== Script finalizado ==="
