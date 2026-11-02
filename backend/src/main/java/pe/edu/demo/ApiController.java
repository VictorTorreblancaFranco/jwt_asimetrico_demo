package pe.edu.demo;

import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/demo")
public class ApiController {
  private final JwtService jwt;
  public ApiController(JwtService jwt) { this.jwt = jwt; }

  @PostMapping("/pedidos/token")
  public Mono<Map<String,String>> token(@RequestParam(defaultValue="60") long seconds) {
    return Mono.fromCallable(() -> Map.of("token", jwt.issue("servicio-pedidos", seconds, "urn:servicio:facturacion")));
  }

  @PostMapping("/facturacion/procesar")
  public Mono<ResponseEntity<Map<String,Object>>> process(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
    return Mono.fromCallable(() -> {
      if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.status(401).body(Map.<String,Object>of("error","Bearer requerido"));
      JWTClaimsSet claims = jwt.decryptAndVerify(auth.substring(7));
      if (!"facturacion:write".equals(claims.getStringClaim("scope"))) return ResponseEntity.status(403).body(Map.<String,Object>of("error","permiso insuficiente"));
      return ResponseEntity.status(201).body(Map.<String,Object>of("status","factura procesada","subject",claims.getSubject(),"jti",claims.getJWTID()));
    }).onErrorReturn(ResponseEntity.status(401).body(Map.<String,Object>of("error","JWT invalido, alterado o expirado")));
  }
}
