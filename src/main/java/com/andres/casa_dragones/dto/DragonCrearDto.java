package com.andres.casa_dragones.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record DragonCrearDto(
        String nombre,
        String color,
        @JsonAlias("tamaño")
        String tamano
) {
}
