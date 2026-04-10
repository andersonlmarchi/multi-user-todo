package br.com.jtech.tasklist.adapters.input.protocols;

import java.util.UUID;

public record TaskResponse(UUID id, String title, boolean done, boolean archived) {
}
