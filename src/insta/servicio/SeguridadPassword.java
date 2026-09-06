/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package insta.servicio;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
/**
 *
 * @author diego
 */
public final class SeguridadPassword {

    private static final String ALGORITMO =
            "PBKDF2WithHmacSHA256";

    private static final String IDENTIFICADOR =
            "PBKDF2-SHA256";

    private static final int ITERACIONES = 600_000;

    private static final int TAMANO_SAL_BYTES = 16;
    private static final int TAMANO_HASH_BITS = 256;

    private static final SecureRandom ALEATORIO =
            new SecureRandom();

    private SeguridadPassword() {
        
    }

 
    public static final class Credenciales {

        private final String passwordHash;
        private final String passwordSalt;

        private Credenciales(
                String passwordHash,
                String passwordSalt
        ) {
            this.passwordHash = passwordHash;
            this.passwordSalt = passwordSalt;
        }

        public String getPasswordHash() {
            return passwordHash;
        }

        public String getPasswordSalt() {
            return passwordSalt;
        }
    }

    public static Credenciales generarCredenciales(
            String password
    ) {
        Objects.requireNonNull(
                password,
                "La contraseña no puede ser null."
        );

        if (password.isEmpty()) {
            throw new IllegalArgumentException(
                    "La contraseña no puede estar vacía."
            );
        }

        byte[] sal = new byte[TAMANO_SAL_BYTES];
        ALEATORIO.nextBytes(sal);

        byte[] hash = calcularHash(
                password,
                sal,
                ITERACIONES
        );

        String salCodificada = Base64.getEncoder()
                .encodeToString(sal);

        String hashCodificado = Base64.getEncoder()
                .encodeToString(hash);


        String hashCompleto =
                IDENTIFICADOR
                + "$"
                + ITERACIONES
                + "$"
                + hashCodificado;

        return new Credenciales(
                hashCompleto,
                salCodificada
        );
    }

    public static boolean verificar(
            String passwordIngresada,
            String hashGuardado,
            String salGuardada
    ) {
        if (passwordIngresada == null
                || hashGuardado == null
                || salGuardada == null) {

            return false;
        }

        String[] partes = hashGuardado.split("\\$", -1);

        if (partes.length != 3) {
            return false;
        }

        if (!IDENTIFICADOR.equals(partes[0])) {
            return false;
        }

        int iteraciones;
        byte[] hashEsperado;
        byte[] sal;

        try {
            iteraciones = Integer.parseInt(partes[1]);

            /*
             * Evita aceptar una cantidad absurda
             * de trabajo desde un registro inválido.
             */
            if (iteraciones < 10_000
                    || iteraciones > 2_000_000) {

                return false;
            }

            hashEsperado = Base64.getDecoder()
                    .decode(partes[2]);

            sal = Base64.getDecoder()
                    .decode(salGuardada);

        } catch (IllegalArgumentException e) {
            return false;
        }

        if (hashEsperado.length != TAMANO_HASH_BITS / 8) {
            return false;
        }

        if (sal.length != TAMANO_SAL_BYTES) {
            return false;
        }

        byte[] hashCalculado = calcularHash(
                passwordIngresada,
                sal,
                iteraciones
        );

        return MessageDigest.isEqual(
                hashEsperado,
                hashCalculado
        );
    }

    private static byte[] calcularHash(
            String password,
            byte[] sal,
            int iteraciones
    ) {
        char[] caracteres = password.toCharArray();

        PBEKeySpec especificacion = new PBEKeySpec(
                caracteres,
                sal,
                iteraciones,
                TAMANO_HASH_BITS
        );

        try {
            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance(ALGORITMO);

            return fabrica.generateSecret(especificacion)
                    .getEncoded();

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "No se pudo procesar la contraseña.",
                    e
            );

        } finally {
            especificacion.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }
}
