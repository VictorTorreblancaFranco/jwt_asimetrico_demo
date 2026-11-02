# JWT asimétrico: demo con frontend y backend

Aplicación para demostrar comunicación segura entre `Pedidos` y `Facturación` usando RS256, JWE y validación de claims.

## Ejecutar todo con Docker

Requisito: Docker Desktop.

```bash
docker compose up --build
```

Abrir el frontend en [http://localhost:5500](http://localhost:5500).

- `frontend/`: interfaz para generar y probar tokens.
- `backend/`: API Spring WebFlux con Nimbus JOSE + JWT.
- `docker-compose.yml`: levanta ambos servicios.

## Ejecutar sin Docker

Terminal 1:

```bash
cd backend
mvn spring-boot:run
```

Terminal 2:

```bash
npx --yes serve frontend -l 5500
```

Abrir `http://localhost:5500`.
