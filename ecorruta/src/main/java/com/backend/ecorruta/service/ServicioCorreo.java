package com.backend.ecorruta.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioCorreo {

        private final RestClient cliente;
        private final String remitente;

        public ServicioCorreo(
                        @Value("${resend.api-key}") String apiKey,
                        @Value("${resend.from}") String remitente) {
                this.remitente = remitente;
                this.cliente = RestClient.builder()
                                .baseUrl("https://api.resend.com")
                                .defaultHeader("Authorization", "Bearer " + apiKey)
                                .build();
        }

        public void enviarCodigo(String destinatario, String codigo) {

                String texto = """
                                Ecorruta - Verifica tu correo

                                Tu código de verificación es: %s

                                Este código vence en 10 minutos.

                                Si no solicitaste este registro,
                                puedes ignorar este mensaje.
                                """.formatted(codigo);

                String html = """
                                <!DOCTYPE html>
                                <html lang="es">
                                <head>
                                    <meta charset="UTF-8">
                                    <meta name="viewport"
                                          content="width=device-width, initial-scale=1.0">
                                    <title>Verificación Ecorruta</title>
                                </head>

                                <body style="
                                    margin: 0;
                                    padding: 0;
                                    background-color: #f3f4f6;
                                    font-family: Arial, Helvetica, sans-serif;
                                ">

                                    <table role="presentation"
                                           width="100%"
                                           cellpadding="0"
                                           cellspacing="0"
                                           style="background-color: #f3f4f6;">

                                        <tr>
                                            <td align="center"
                                                style="padding: 40px 15px;">

                                                <table role="presentation"
                                                       width="100%"
                                                       cellpadding="0"
                                                       cellspacing="0"
                                                       style="
                                                           max-width: 550px;
                                                           background-color: #ffffff;
                                                           border-radius: 12px;
                                                           overflow: hidden;
                                                       ">

                                                    <!-- ENCABEZADO -->
                                                    <tr>
                                                        <td align="center"
                                                            style="
                                                                background-color: #9d2449;
                                                                padding: 35px 20px;
                                                            ">

                                                            <h1 style="
                                                                color: #ffffff;
                                                                margin: 0;
                                                                font-size: 32px;
                                                                font-weight: bold;
                                                                letter-spacing: 1px;
                                                            ">
                                                                Ecorruta
                                                            </h1>

                                                            <p style="
                                                                color: #ffffff;
                                                                font-size: 11px;
                                                                letter-spacing: 2px;
                                                                margin: 10px 0 0;
                                                            ">
                                                                GOBIERNO DEL ESTADO
                                                            </p>

                                                        </td>
                                                    </tr>

                                                    <!-- LÍNEA DORADA -->
                                                    <tr>
                                                        <td style="
                                                            height: 5px;
                                                            background-color: #b38e5d;
                                                            font-size: 0;
                                                        ">
                                                            &nbsp;
                                                        </td>
                                                    </tr>

                                                    <!-- CONTENIDO -->
                                                    <tr>
                                                        <td align="center"
                                                            style="padding: 38px 30px;">

                                                            <h2 style="
                                                                color: #242424;
                                                                font-size: 23px;
                                                                margin: 0 0 20px;
                                                            ">
                                                                Verifica tu correo electrónico
                                                            </h2>

                                                            <p style="
                                                                color: #666666;
                                                                font-size: 15px;
                                                                line-height: 1.8;
                                                                margin: 0 0 25px;
                                                            ">
                                                                Gracias por registrarte
                                                                en <strong>Ecorruta</strong>.

                                                                <br>

                                                                Ingresa el siguiente código
                                                                para verificar tu correo
                                                                electrónico y activar tu cuenta.
                                                            </p>

                                                            <!-- TARJETA DEL CÓDIGO -->
                                                            <table role="presentation"
                                                                   width="100%"
                                                                   cellpadding="0"
                                                                   cellspacing="0"
                                                                   style="
                                                                       background-color: #f9f0f3;
                                                                       border: 1px solid #e9ccd5;
                                                                       border-radius: 10px;
                                                                   ">

                                                                <tr>
                                                                    <td align="center"
                                                                        style="padding: 28px 15px;">

                                                                        <p style="
                                                                            color: #777777;
                                                                            font-size: 12px;
                                                                            font-weight: bold;
                                                                            letter-spacing: 2px;
                                                                            margin: 0 0 15px;
                                                                        ">
                                                                            TU CÓDIGO DE VERIFICACIÓN
                                                                        </p>

                                                                        <p style="
                                                                            color: #9d2449;
                                                                            font-size: 40px;
                                                                            font-weight: bold;
                                                                            letter-spacing: 8px;
                                                                            margin: 0;
                                                                            line-height: 1.3;
                                                                        ">
                                                                            {{CODIGO}}
                                                                        </p>

                                                                        <p style="
                                                                            color: #777777;
                                                                            font-size: 13px;
                                                                            margin: 15px 0 0;
                                                                        ">
                                                                            Válido durante
                                                                            <strong>10 minutos</strong>
                                                                        </p>

                                                                    </td>
                                                                </tr>
                                                            </table>

                                                            <!-- AVISO -->
                                                            <p style="
                                                                color: #888888;
                                                                font-size: 13px;
                                                                line-height: 1.7;
                                                                margin: 28px 0 0;
                                                            ">
                                                                Si no solicitaste este registro,
                                                                puedes ignorar este mensaje.
                                                                Tu cuenta no será creada
                                                                sin verificar el código.
                                                            </p>

                                                        </td>
                                                    </tr>

                                                    <!-- PIE DE PÁGINA -->
                                                    <tr>
                                                        <td align="center"
                                                            style="
                                                                background-color: #f8f8f8;
                                                                padding: 25px 20px;
                                                                border-top: 1px solid #eeeeee;
                                                            ">

                                                            <p style="
                                                                color: #9d2449;
                                                                font-size: 15px;
                                                                font-weight: bold;
                                                                margin: 0 0 8px;
                                                            ">
                                                                Ecorruta
                                                            </p>

                                                            <p style="
                                                                color: #999999;
                                                                font-size: 12px;
                                                                margin: 0 0 8px;
                                                            ">
                                                                Oaxaca, México
                                                            </p>

                                                            <p style="
                                                                color: #aaaaaa;
                                                                font-size: 11px;
                                                                margin: 0;
                                                            ">
                                                                Este correo fue generado
                                                                automáticamente.
                                                                Por favor, no respondas.
                                                            </p>

                                                        </td>
                                                    </tr>

                                                </table>

                                            </td>
                                        </tr>
                                    </table>

                                </body>
                                </html>
                                """.replace("{{CODIGO}}", codigo);

                try {
                        cliente.post()
                                        .uri("/emails")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(Map.of(
                                                        "from", remitente,
                                                        "to", List.of(destinatario),
                                                        "subject", "Ecorruta - Verifica tu correo",
                                                        "html", html,
                                                        "text", texto))
                                        .retrieve()
                                        .toBodilessEntity();

                } catch (RestClientException ex) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "No se pudo enviar el correo. "
                                                        + "Revisa la configuración de Resend.");
                }
        }

}
