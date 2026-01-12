package com.gastromind.domain.valueobject;

public enum Category {
    // Frescos
    MEAT,           // Carnes
    FISH,           // Pescados
    SEAFOOD,        // Mariscos (distinto de pescado por alérgenos/precio)
    VEGETABLE,      // Verduras y Hortalizas
    FRUIT,          // Frutas
    DAIRY,          // Lácteos y Huevos

    // Despensa
    GRAIN,          // Arroces, Pastas, Legumbres
    BAKERY,         // Pan y Bollería
    SPICE,          // Especias y Condimentos
    SAUCE,          // Salsas y Aceites
    DESSERT,        // Repostería

    // Bebidas -> Interesante si se amplia a TPV
    SOFT_DRINK,     // Agua, Refrescos
    ALCOHOL,        // Vinos, Cervezas, Licores (Importante separarlo)

    // No Comestibles
    CLEANING,       // Productos de limpieza
    PACKAGING,      // Envases, Servilletas, Take-away

    OTHER
}
