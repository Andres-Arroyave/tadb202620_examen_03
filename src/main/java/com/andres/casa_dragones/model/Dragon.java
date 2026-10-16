package com.andres.casa_dragones.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public record Dragon(
        String id,
        String nombre,
        String color,
        @JsonAlias("tamaño")
        String tamano
) {
}
