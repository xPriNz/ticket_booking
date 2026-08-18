/**
 * Cross-cutting error handling — globalny handler wyjątków, wspólna hierarchia
 * wyjątków aplikacyjnych oraz filtr traceId.
 * <p>
 * Moduł jest oznaczony jako {@code OPEN} — wszystkie typy z tego pakietu są
 * dostępne dla innych modułów bez potrzeby jawnych zależności. To standardowe
 * podejście dla infrastruktury współdzielonej.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Error Handling",
    type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package pl.kocmon.booking.error;
