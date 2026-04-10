package br.com.jtech.tasklist.adapters.input.protocols;

import jakarta.validation.constraints.Size;

public record TaskUpdateRequest(
        @Size(max = 500) String title,
        Boolean done
) {
}
