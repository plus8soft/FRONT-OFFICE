/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.entity.ce;

/**
 * Type of external currency rate source.
 * 
 * EXTERNAL - External rate provider (stub implementation, can be replaced with ECB, Fixer.io, etc.)
 * MARKET - Market rate provider (stub implementation, can be replaced with market data APIs)
 */
public enum RateType {
    /**
     * External rate provider.
     * Currently implemented as stub, can be replaced with:
     * - European Central Bank (ECB)
     * - Fixer.io
     * - ExchangeRate-API
     * - Open Exchange Rates
     * - CurrencyLayer
     */
    EXTERNAL,
    /**
     * Market rate provider.
     * Currently implemented as stub with hardcoded values.
     * Can be replaced with market data API integration.
     */
    MARKET
}
