#!/usr/bin/env sh
# Carga productos de ejemplo en la API. Uso: ./scripts/datos-ejemplo.sh [URL de la API]
set -e
API="${1:-http://localhost:8080}"

crear() {
  curl -sf -o /dev/null -X POST "$API/products" -H 'Content-Type: application/json' -d "$1"
}

crear '{"name": "Arroz bomba", "description": "Especial paella", "category": "GRAIN", "unit": "KILOGRAM", "allergens": []}'
crear '{"name": "Pan de semillas", "description": "Masa madre con sésamo", "category": "BAKERY", "unit": "UNIT", "allergens": ["GLUTEN", "SESAME"]}'
crear '{"name": "Gamba roja", "description": "De Huelva", "category": "SEAFOOD", "unit": "KILOGRAM", "allergens": ["CRUSTACEANS"]}'
crear '{"name": "Merluza de pincho", "description": "Del Cantábrico", "category": "FISH", "unit": "KILOGRAM", "allergens": ["FISH"]}'
crear '{"name": "Mejillón", "description": "Gallego, de batea", "category": "SEAFOOD", "unit": "KILOGRAM", "allergens": ["MOLLUSCS"]}'
crear '{"name": "Nata para cocinar", "description": "35 % de materia grasa", "category": "DAIRY", "unit": "LITER", "allergens": ["DAIRY"]}'
crear '{"name": "Salsa de soja", "description": "Fermentación natural", "category": "SAUCE", "unit": "LITER", "allergens": ["SOYBEANS", "GLUTEN"]}'
crear '{"name": "Mostaza de Dijon", "description": null, "category": "SAUCE", "unit": "UNIT", "allergens": ["MUSTARD"]}'
crear '{"name": "Vino blanco", "description": "Albariño para cocinar", "category": "ALCOHOL", "unit": "LITER", "allergens": ["SULFITES"]}'
crear '{"name": "Tomate pera", "description": null, "category": "VEGETABLE", "unit": "KILOGRAM", "allergens": []}'
crear '{"name": "Pesto genovés", "description": "Albahaca, piñones y parmesano", "category": "SAUCE", "unit": "GRAM", "allergens": ["NUTS", "DAIRY"]}'
crear '{"name": "Ñora", "description": "Seca, para sofritos", "category": "SPICE", "unit": "UNIT", "allergens": []}'

echo "Productos de ejemplo cargados en $API"
