package br.com.jtech.tasklist.adapters.input.protocols;

import java.util.UUID;

public record TaskListResponse(UUID id, String name, boolean archived) {
}
