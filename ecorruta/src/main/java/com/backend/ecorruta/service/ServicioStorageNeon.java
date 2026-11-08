
package com.backend.ecorruta.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
public class ServicioStorageNeon {

    private final S3Client s3Client;

    @Value("${neon.storage.bucket}")
    private String bucket;

    public String guardarImagen(MultipartFile imagen) {

        if (imagen == null || imagen.isEmpty()) {
            return null;
        }

        if (imagen.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fotografía supera los 5 MB");
        }

        try {
            byte[] contenido = imagen.getBytes();
            String tipo = detectarTipo(contenido);

            String extension = switch (tipo) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> throw new IllegalStateException();
            };

            String nombre = UUID.randomUUID() + extension;
            String clave = "reportes/" + nombre;

            PutObjectRequest solicitud = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(clave)
                    .contentType(tipo)
                    .build();

            s3Client.putObject(
                    solicitud,
                    RequestBody.fromBytes(contenido));

            return "/api/reportes/imagenes/" + nombre;

        } catch (IOException | SdkException error) {

            System.err.println(
                    "ERROR NEON STORAGE: "
                            + error.getClass().getName());

            System.err.println(
                    "CAUSA: " + error.getMessage());

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo subir la fotografía a Neon",
                    error);
        }

    }

    private String detectarTipo(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47
                && bytes[4] == 0x0D
                && bytes[5] == 0x0A
                && bytes[6] == 0x1A
                && bytes[7] == 0x0A) {
            return "image/png";
        }

        if (bytes.length >= 12
                && new String(bytes, 0, 4,
                        java.nio.charset.StandardCharsets.US_ASCII)
                        .equals("RIFF")
                && new String(bytes, 8, 4,
                        java.nio.charset.StandardCharsets.US_ASCII)
                        .equals("WEBP")) {
            return "image/webp";
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fotografía no tiene un formato válido");
    }

    public byte[] obtenerImagen(String nombre) {
        try {
            return s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key("reportes/" + nombre)
                            .build())
                    .asByteArray();

        } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException error) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La fotografía no existe");

        } catch (SdkException error) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo recuperar la fotografía",
                    error);
        }
    }

    public void eliminarImagen(String imagenUrl) {
        if (imagenUrl == null) {
            return;
        }

        String prefijo = "/api/reportes/imagenes/";

        if (!imagenUrl.startsWith(prefijo)) {
            return;
        }

        String nombre = imagenUrl.substring(prefijo.length());

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key("reportes/" + nombre)
                        .build());
    }
}
