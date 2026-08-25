package dev.filippotaiuti.temperature.entity;

public enum TemperatureUnit {
    CELSIUS("C"),
    FAHRENHEIT("F");

    private final String symbol;

    TemperatureUnit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

}