#!/usr/bin/env sh
# Carga datos de ejemplo en la API: productos, proveedores y albaranes. Uso: ./scripts/datos-ejemplo.sh [URL de la API]
set -e
API="${1:-http://localhost:8080}"

# Crea un recurso y devuelve su id
crear() {
  curl -sf -X POST "$API/$1" -H 'Content-Type: application/json' -d "$2" | sed -n 's/^{"id":"\([^"]*\)".*/\1/p'
}

# Fecha dentro de N días (date de GNU, la de Linux)
dentro_de() {
  date -d "+$1 days" +%F
}

ARROZ=$(crear products '{"name": "Arroz bomba", "description": "Especial paella", "category": "GRAIN", "unit": "KILOGRAM", "allergens": []}')
PAN=$(crear products '{"name": "Pan de semillas", "description": "Masa madre con sésamo", "category": "BAKERY", "unit": "UNIT", "allergens": ["GLUTEN", "SESAME"]}')
GAMBA=$(crear products '{"name": "Gamba roja", "description": "De Huelva", "category": "SEAFOOD", "unit": "KILOGRAM", "allergens": ["CRUSTACEANS"]}')
MERLUZA=$(crear products '{"name": "Merluza de pincho", "description": "Del Cantábrico", "category": "FISH", "unit": "KILOGRAM", "allergens": ["FISH"]}')
MEJILLON=$(crear products '{"name": "Mejillón", "description": "Gallego, de batea", "category": "SEAFOOD", "unit": "KILOGRAM", "allergens": ["MOLLUSCS"]}')
NATA=$(crear products '{"name": "Nata para cocinar", "description": "35 % de materia grasa", "category": "DAIRY", "unit": "LITER", "allergens": ["DAIRY"]}')
SOJA=$(crear products '{"name": "Salsa de soja", "description": "Fermentación natural", "category": "SAUCE", "unit": "LITER", "allergens": ["SOYBEANS", "GLUTEN"]}')
MOSTAZA=$(crear products '{"name": "Mostaza de Dijon", "description": null, "category": "SAUCE", "unit": "UNIT", "allergens": ["MUSTARD"]}')
crear products '{"name": "Vino blanco", "description": "Albariño para cocinar", "category": "ALCOHOL", "unit": "LITER", "allergens": ["SULFITES"]}' > /dev/null
TOMATE=$(crear products '{"name": "Tomate pera", "description": null, "category": "VEGETABLE", "unit": "KILOGRAM", "allergens": []}')
crear products '{"name": "Pesto genovés", "description": "Albahaca, piñones y parmesano", "category": "SAUCE", "unit": "GRAM", "allergens": ["NUTS", "DAIRY"]}' > /dev/null
NORA=$(crear products '{"name": "Ñora", "description": "Seca, para sofritos", "category": "SPICE", "unit": "UNIT", "allergens": []}')

PESCADOS=$(crear suppliers '{"name": "Pescados Cimadevilla", "taxId": "B33123456", "phone": "985 123 456", "email": "pedidos@cimadevilla.es"}')
HUERTO=$(crear suppliers '{"name": "Frutas y Verduras El Huerto", "phone": "984 222 333"}')
DISTRIBUCIONES=$(crear suppliers '{"name": "Distribuciones Asturianas", "taxId": "B33987654", "email": "comercial@distrias.es"}')

crear goods-receipts "{\"supplierId\": \"$PESCADOS\", \"deliveryNoteNumber\": \"ALB-1234\", \"lines\": [
  {\"productId\": \"$GAMBA\", \"quantity\": 2, \"unit\": \"KILOGRAM\", \"amount\": 60.00, \"expirationDate\": \"$(dentro_de 2)\", \"lotCode\": \"GR-778\"},
  {\"productId\": \"$MERLUZA\", \"quantity\": 3000, \"unit\": \"GRAM\", \"amount\": 45.00, \"expirationDate\": \"$(dentro_de 4)\", \"lotCode\": \"ME-12\"},
  {\"productId\": \"$MEJILLON\", \"quantity\": 5, \"unit\": \"KILOGRAM\", \"amount\": 17.50, \"expirationDate\": \"$(dentro_de 1)\"}
]}" > /dev/null

crear goods-receipts "{\"supplierId\": \"$HUERTO\", \"deliveryNoteNumber\": \"F-88\", \"lines\": [
  {\"productId\": \"$TOMATE\", \"quantity\": 10, \"unit\": \"KILOGRAM\", \"amount\": 18.00, \"expirationDate\": \"$(dentro_de 6)\"},
  {\"productId\": \"$NORA\", \"quantity\": 20, \"unit\": \"UNIT\", \"amount\": 6.00, \"expirationDate\": \"$(dentro_de 90)\"}
]}" > /dev/null

crear goods-receipts "{\"supplierId\": \"$DISTRIBUCIONES\", \"deliveryNoteNumber\": \"D-311\", \"lines\": [
  {\"productId\": \"$ARROZ\", \"quantity\": 5, \"unit\": \"KILOGRAM\", \"amount\": 22.50, \"expirationDate\": \"$(dentro_de 300)\", \"lotCode\": \"AB-2026-09\"},
  {\"productId\": \"$NATA\", \"quantity\": 6, \"unit\": \"LITER\", \"amount\": 21.60, \"expirationDate\": \"$(dentro_de 12)\"},
  {\"productId\": \"$PAN\", \"quantity\": 10, \"unit\": \"UNIT\", \"amount\": 18.00, \"expirationDate\": \"$(dentro_de 0)\"},
  {\"productId\": \"$MOSTAZA\", \"quantity\": 3, \"unit\": \"UNIT\", \"amount\": 9.90, \"expirationDate\": \"$(dentro_de 200)\"},
  {\"productId\": \"$SOJA\", \"quantity\": 1, \"unit\": \"LITER\", \"amount\": 4.20, \"expirationDate\": \"$(dentro_de 365)\"}
]}" > /dev/null

echo "Datos de ejemplo cargados en $API: 12 productos, 3 proveedores y 3 albaranes"
