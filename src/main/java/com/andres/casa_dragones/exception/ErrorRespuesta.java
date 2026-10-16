package com.andres.casa_dragones.exception;

import java.time.LocalDateTime;

public record ErrorRespuesta(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta
) {
}
