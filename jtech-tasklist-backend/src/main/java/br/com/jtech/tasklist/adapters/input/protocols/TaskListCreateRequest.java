package br.com.jtech.tasklist.adapters.input.protocols;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskListCreateRequest(
        @NotBlank @Size(max = 255) String name
) {
}
