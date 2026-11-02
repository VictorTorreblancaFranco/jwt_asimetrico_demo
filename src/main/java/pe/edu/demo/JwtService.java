package pe.edu.demo;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.interfaces.*;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
  private final KeyPair signingKeys = rsaKeys();
  private final KeyPair encryptionKeys = rsaKeys();

  public String issue(String subject, long seconds, String audience) throws Exception {
    Instant now = Instant.now();
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .issuer("urn:servicio:pedidos").subject(subject).audience(audience)
        .issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(seconds)))
        .jwtID(UUID.randomUUID().toString()).claim("scope", "facturacion:write").build();
    SignedJWT signed = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
        .type(JOSEObjectType.JWT).keyID("a-sig-01").build(), claims);
    signed.sign(new RSASSASigner((RSAPrivateKey) signingKeys.getPrivate()));
    JWEObject jwe = new JWEObject(new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
        .contentType("JWT").keyID("b-enc-01").build(), new Payload(signed.serialize()));
    jwe.encrypt(new RSAEncrypter((RSAPublicKey) encryptionKeys.getPublic()));
    return jwe.serialize();
  }

  public JWTClaimsSet decryptAndVerify(String token) throws Exception {
    JWEObject jwe = JWEObject.parse(token);
    if (jwe.getHeader().getAlgorithm() != JWEAlgorithm.RSA_OAEP_256 ||
        jwe.getHeader().getEncryptionMethod() != EncryptionMethod.A256GCM) throw new JOSEException("algoritmo JWE no permitido");
    jwe.decrypt(new RSADecrypter((RSAPrivateKey) encryptionKeys.getPrivate()));
    SignedJWT signed = SignedJWT.parse(jwe.getPayload().toString());
    if (signed.getHeader().getAlgorithm() != JWSAlgorithm.RS256 || !"a-sig-01".equals(signed.getHeader().getKeyID()))
      throw new JOSEException("algoritmo o kid no permitido");
    if (!signed.verify(new RSASSAVerifier((RSAPublicKey) signingKeys.getPublic()))) throw new JOSEException("firma invalida");
    JWTClaimsSet c = signed.getJWTClaimsSet();
    if (!"urn:servicio:pedidos".equals(c.getIssuer()) || !c.getAudience().contains("urn:servicio:facturacion")) throw new JOSEException("iss/aud invalidos");
    if (c.getExpirationTime() == null || c.getExpirationTime().before(new Date())) throw new JOSEException("token expirado");
    return c;
  }

  private static KeyPair rsaKeys() { try { KeyPairGenerator g=KeyPairGenerator.getInstance("RSA"); g.initialize(2048); return g.generateKeyPair(); } catch (GeneralSecurityException e) { throw new IllegalStateException(e); } }
}
