package dev.filippotaiuti.temperature.dto;

public record ValidationError(
    String field, 
    String message)
{
}
