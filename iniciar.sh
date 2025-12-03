#!/bin/bash

# Capturar Ctrl+C para detener la app
trap 'echo ""; echo "Deteniendo la aplicación..."; kill $APP_PID; wait $APP_PID 2>/dev/null; echo "La app se detuvo."; exit' INT

echo "=== Iniciando script de FitnessTracker ==="

# Configuración de la base de datos
DB_NAME="fitness_mysql"
DB_ROOT_PASSWORD="rootpassword"
DB_DATABASE="fitness_tracker"
DB_USER="fitness_user"
DB_PASSWORD="fitness_password"
DB_PORT="3306"

# Verificar si el contenedor existe
CONTAINER_EXISTS=$(docker ps -a --filter "name=$DB_NAME" --format "{{.Names}}")

if [ -z "$CONTAINER_EXISTS" ]; then
    echo "El contenedor '$DB_NAME' no existe. Creándolo..."
    docker run -d \
        --name $DB_NAME \
        -e MYSQL_ROOT_PASSWORD=$DB_ROOT_PASSWORD \
        -e MYSQL_DATABASE=$DB_DATABASE \
        -e MYSQL_USER=$DB_USER \
        -e MYSQL_PASSWORD=$DB_PASSWORD \
        -p $DB_PORT:3306 \
        mysql:8.0

    if [ $? -eq 0 ]; then
        echo "✅ Contenedor '$DB_NAME' creado exitosamente."
        echo "⏳ Esperando a que MySQL esté listo (esto puede tomar 20-30 segundos)..."
        sleep 25
    else
        echo "❌ Error al crear el contenedor. Abortando."
        exit 1
    fi
else
    # Verificar si está corriendo
    DB_STATUS=$(docker ps --filter "name=$DB_NAME" --format "{{.Names}}")

    if [ "$DB_STATUS" == "$DB_NAME" ]; then
        echo "✅ La base de datos '$DB_NAME' ya está corriendo."
    else
        echo "⚠️  El contenedor '$DB_NAME' existe pero no está corriendo. Iniciando..."
        docker start $DB_NAME
        echo "✅ Base de datos iniciada."
        sleep 5
    fi
fi

# Verificar que MySQL esté respondiendo
echo "Verificando conexión a MySQL..."
for i in {1..10}; do
    if docker exec $DB_NAME mysqladmin ping -h localhost --silent 2>/dev/null; then
        echo "✅ MySQL está listo para aceptar conexiones."
        break
    else
        echo "⏳ Esperando que MySQL esté listo... (intento $i/10)"
        sleep 3
    fi

    if [ $i -eq 10 ]; then
        echo "❌ MySQL no respondió a tiempo. Verificar logs con: docker logs $DB_NAME"
        exit 1
    fi
done

# Iniciar la aplicación
echo ""
echo "=== Iniciando la aplicación FitnessTracker ==="
mvn spring-boot:run &
APP_PID=$!
echo "Aplicación iniciada con PID $APP_PID. Esperando que arranque..."

# Esperar y verificar puerto 8080
sleep 8

if lsof -i:8080 -sTCP:LISTEN >/dev/null 2>&1; then
    echo "✅ La aplicación está corriendo en http://localhost:8080"
    echo ""
    echo "=== Información de conexión a MySQL ==="
    echo "Host: localhost:$DB_PORT"
    echo "Base de datos: $DB_DATABASE"
    echo "Usuario: $DB_USER"
    echo "Contraseña: $DB_PASSWORD"
    echo ""
    echo "Presiona Ctrl+C para detener la aplicación."
else
    echo "❌ No se pudo detectar la aplicación en el puerto 8080. Verificar logs."
fi

# Mantener script en primer plano hasta que se detenga la app
wait $APP_PID

echo ""
echo "=== Script finalizado ==="