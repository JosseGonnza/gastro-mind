<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&color=0:ffd59e,50:ffb38a,100:ff8a80&animation=fadeIn&height=120&section=header"/>


# 🍳 Gastro Mind

> ERP modular para hostelería: **productos, lotes, stock y escandallos**.
> Responde a tres preguntas de cualquier cocina: qué hay en cámara, qué hay
> que gastar primero y cuánto cuesta de verdad cada plato.

Todas las cocinas hacen escandallos, pero casi siempre con un precio medio
apuntado en un Excel que se queda viejo con la siguiente factura del proveedor.
Gastro Mind calcula el coste con lo que costó de verdad cada lote que se gasta,
y gasta primero lo que caduca antes, como en una cámara bien llevada.

Está en desarrollo y crece por funcionalidades completas: cada una atraviesa
dominio, casos de uso, API y base de datos antes de empezar la siguiente. La
primera, el **catálogo de productos**, ya funciona de punta a punta.

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
  **14 alérgenos** de declaración obligatoria. Se guardan en PostgreSQL y se
  gestionan por API.
- **📦 Lotes** — cada entrada de género con su SKU, su caducidad, su precio de
  compra y su coste unitario.
- **🧊 Stock** — cantidad disponible por producto, sumando sus lotes, y
  consumo FEFO.
- **📖 Recetas** — ingredientes sin productos repetidos, pasos, raciones,
  tiempo y dificultad.
- **💶 Escandallo** — coste de un ingrediente según los lotes que consume y
  coste total de una receta.

Lotes, stock, recetas y escandallo viven por ahora solo en el dominio: llegarán
a la API en las próximas funcionalidades.

## La API

| Método | Ruta | Qué hace | Respuesta |
|---|---|---|---|
| `POST` | `/products` | Crea un producto | **201** con su `Location`, o **400** si no es válido |
| `GET` | `/products` | Lista los productos por nombre | **200** |
| `GET` | `/products/{id}` | Consulta un producto | **200**, o **404** si no existe |

- Los errores siguen el estándar **Problem Details** (RFC 9457):
  `application/problem+json` con `status`, `title` y `detail`.
- Los nombres se ordenan **como en español**: sin distinguir mayúsculas y con
  tildes y eñes en su sitio (*aceite, Nata, Ñora, Óregano*).

## Cómo está hecho

- **Java 21 + Maven multimódulo** → arquitectura hexagonal en tres módulos:
  - `domain` — entidades, value objects y servicios de dominio, sin frameworks.
  - `application` — casos de uso (`CreateProduct`, `GetProduct`,
    `ListProducts`) y el puerto `ProductRepository`. Java puro, sin Spring.
  - `infrastructure` — Spring Boot 3: la API REST y el adaptador de
    persistencia.
- **SQL escrito a mano con JDBC puro**: `Connection`, `PreparedStatement` y
  `ResultSet`, con transacciones explícitas y sin ORM.
- **PostgreSQL 16** con migraciones **Flyway** versionadas en
  `infrastructure/src/main/resources/db/migration`.
- **TDD de principio a fin**: cada regla nace de un test en rojo, y el
  historial de commits lo cuenta.
- **102 tests** (JUnit 5 + AssertJ): unitarios en el dominio y los casos de
  uso, y de integración contra un **PostgreSQL real** gracias a
  **Testcontainers**. Los nombres en español se leen como reglas del negocio:
  *«CostingService debería calcular el coste con múltiples lotes»*.
- **Integración continua** con GitHub Actions: cada push a `main` pasa todos
  los tests.

## Correrlo en local

Requisitos: Java 21, Maven y Docker.

```sh
docker compose up -d                                          # PostgreSQL
mvn verify                                                    # tests (necesita Docker)
mvn -DskipTests package                                       # construir
java -jar infrastructure/target/infrastructure-0.0.1-SNAPSHOT.jar   # API en :8080
```

## Probarla

```sh
curl -i -X POST localhost:8080/products \
  -H 'Content-Type: application/json' \
  -d '{"name": "Pan de semillas", "category": "BAKERY", "unit": "UNIT", "allergens": ["GLUTEN", "SESAME"]}'

curl localhost:8080/products
```

## Lo que viene

- [x] **Catálogo de productos**: dominio, casos de uso, API y PostgreSQL.
- [ ] **Recepción de género**: albaranes de entrada que generan lotes.
- [ ] **Recetas y escandallo** por API, con coste por ración.
- [ ] **Producción**: cocinar una receta gasta sus ingredientes por FEFO.
- [ ] Mermas e inventario.
- [ ] Pendientes técnicos: cantidades en `BigDecimal` y con unidad, lotes
  reconstruibles desde la base de datos, coste unitario sin redondeo
  intermedio, excepción de validación propia del dominio y Spring Boot al día.

## Notas

- Las categorías ya separan bebidas y alcohol pensando en un futuro TPV.
- Una migración ya aplicada no se toca nunca: los cambios van en una nueva.

<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&color=0:ffd59e,50:ffb38a,100:ff8a80&height=80&section=footer"/>
