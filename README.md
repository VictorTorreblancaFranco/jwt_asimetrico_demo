# Demo JWT asimétrico: Spring WebFlux + RS256 + JWE

Miniaplicación para la exposición. Simula dos backends en un mismo proceso:

- `pedidos` emite un JWT firmado con RS256 y lo cifra como JWE.
- `facturacion` descifra el JWE, verifica la firma y valida `iss`, `aud` y `exp`.

## Ejecutar

```bash
mvn spring-boot:run
```

En otra terminal:

```bash
TOKEN=$(curl -s -X POST 'http://localhost:8080/demo/pedidos/token?seconds=60' | sed -E 's/.*"token":"([^"]+)".*/\1/')
curl -i -X POST http://localhost:8080/demo/facturacion/procesar -H "Authorization: Bearer $TOKEN"
```

Debe responder `201 Created`. Para probar expiración, soliciten `seconds=1`, esperen dos segundos y envíen el token: responderá `401 Unauthorized`. Para probar alteración, cambien un carácter del token. El ejemplo usa claves RSA efímeras para la clase; en producción deben almacenarse en un gestor de secretos y rotarse.

## Qué explicar

RS256 prueba origen e integridad. JWE protege la confidencialidad. HTTPS sigue siendo obligatorio en un entorno real.
