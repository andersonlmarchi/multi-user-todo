package br.com.jtech.tasklist.adapters.input.protocols;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskCreateRequest(
        @NotBlank @Size(max = 500) String title
) {
}
