<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&color=0:ffd59e,50:ffb38a,100:ff8a80&animation=fadeIn&height=120&section=header"/>


# 🍳 Gastro Mind

> ERP modular para hostelería: **productos, lotes, stock y escandallos**.
> Responde a tres preguntas de cualquier cocina: qué hay en cámara, qué hay
> que gastar primero y cuánto cuesta de verdad cada plato.

Todas las cocinas hacen escandallos, pero casi siempre con un precio medio
apuntado en un Excel que se queda viejo con la siguiente factura del proveedor.
Gastro Mind calcula el coste con lo que costó de verdad cada lote que se gasta,
y gasta primero lo que caduca antes, como en una cámara bien llevada.

Está en desarrollo: el dominio, que es donde viven las reglas del negocio, está
hecho con TDD. La API REST y la persistencia son lo siguiente.

## Cómo calcula

Cada entrada de género es un **lote** con su caducidad y su precio de compra.
Un ejemplo con arroz bomba:

| Lote | Caduca | Compra | Coste por kg |
|---|---|---|---|
| **LOT-001** | En 5 meses | 2 kg por 20 € | 10,00 € |
| **LOT-002** | En 6 meses | 5 kg por 10 € | 2,00 € |

Una receta que necesita **4 kg** gasta primero LOT-001, que caduca antes
(2 kg × 10 € = 20 €), y completa con LOT-002 (2 kg × 2 € = 4 €). El escandallo
de ese arroz es **24 €**, no un precio medio.

- **FEFO** (*first expired, first out*): se gasta primero el lote que caduca
  antes, no el que entró antes.
- Si no hay stock suficiente, **no se toca ningún lote**: el consumo es todo o
  nada.
- Un lote **caducado no se acepta** al darlo de entrada.
- El dinero va en `BigDecimal` con redondeo bancario, y las cantidades nunca
  son negativas.

## Qué hace

- **🥕 Productos** — ficha con categoría (frescos, despensa, bebidas y no
  comestibles), unidad de medida (kg, g, L, ml, ud, manojo y ración) y los
  **14 alérgenos** de declaración obligatoria.
- **📦 Lotes** — cada entrada de género con su SKU, su caducidad, su precio de
  compra y su coste unitario.
- **🧊 Stock** — cantidad disponible por producto, sumando sus lotes, y
  consumo FEFO.
- **📖 Recetas** — ingredientes sin productos repetidos, pasos, raciones,
  tiempo y dificultad.
- **💶 Escandallo** — coste de un ingrediente según los lotes que consume y
  coste total de una receta.

## Cómo está hecho

- **Java 21 + Maven multimódulo** → arquitectura hexagonal en tres módulos:
  `domain`, `application` e `infrastructure`.
- **Dominio sin frameworks**: entidades (`Product`, `Batch`, `Recipe`), value
  objects inmutables como `record` (`Money`, `Quantity`, `RecipeIngredient`,
  `RecipeStep`) y servicios de dominio (`InventoryService`, `CostingService`).
- **TDD de principio a fin**: cada regla nace de un test en rojo, y el
  historial de commits lo cuenta.
- **82 tests** (JUnit 5 + AssertJ) con nombres en español que se leen como
  reglas del negocio: *«CostingService debería calcular el coste con múltiples
  lotes»*.
- **Una rama y un pull request por funcionalidad.**
- **Spring Boot 3** reservado para la capa de infraestructura.

## Correrlo en local

Requisitos: Java 21 y Maven.

```sh
mvn test              # todos los tests
mvn -pl domain test   # solo el dominio
```

## Lo que viene

- [ ] **Compras**: proveedores, pedidos y albaranes de entrada que generan lotes.
- [ ] Casos de uso en `application`.
- [ ] **API REST** y persistencia.
- [ ] Mermas e inventario.
- [ ] Coste unitario sin redondeo intermedio.

## Notas

- Las categorías ya separan bebidas y alcohol pensando en un futuro TPV.
- `consumeProduct` es `synchronized`: dos consumos a la vez sobre el mismo
  inventario no se pisan.

<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&color=0:ffd59e,50:ffb38a,100:ff8a80&height=80&section=footer"/>
