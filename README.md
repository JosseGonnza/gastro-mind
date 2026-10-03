# gastro-mind

ERP modular para hostelería: compras, stock y escandallos, pensado desde dentro de una cocina.

> **En desarrollo.** El dominio (las reglas de negocio) está hecho con TDD. La API REST y la persistencia son lo siguiente.

## Qué hace hoy

- **Productos:** ficha con categoría, unidad de medida y los 14 alérgenos de declaración obligatoria.
- **Lotes:** cada entrada de género con su caducidad, su precio de compra y su coste unitario.
- **Stock:** cantidad disponible por producto, sumando sus lotes.
- **Consumo FEFO:** al gastar un producto se consume primero el lote que caduca antes. Si no hay stock suficiente, no se toca ningún lote.
- **Recetas:** ingredientes, pasos, raciones, tiempo y dificultad.
- **Escandallo:** coste de un ingrediente según el precio real de cada lote que consume, y coste total de una receta.

## Arquitectura

Hexagonal, en tres módulos Maven:

| Módulo | Contenido | Estado |
| --- | --- | --- |
| `domain` | Entidades, value objects y servicios de dominio, sin dependencias de frameworks | Hecho |
| `application` | Casos de uso | Pendiente |
| `infrastructure` | API REST con Spring Boot y persistencia | Pendiente |

## Cómo ejecutarlo

Requisitos: Java 21 y Maven.

```bash
mvn test
```

Los tests se leen como especificaciones del negocio, por ejemplo: *"CostingService debería calcular el coste con múltiples lotes"*.

## Cómo está hecho

- **TDD:** cada regla nace de un test en rojo; el historial de commits lo refleja.
- **Una rama y un pull request por funcionalidad.**
- **Value objects inmutables:** `Money` con `BigDecimal` y redondeo bancario, `Quantity` sin negativos.

## Stack

Java 21 · Maven · JUnit 5 · AssertJ · Spring Boot 3 (capa de infraestructura)

## Próximos pasos

- [ ] Compras: proveedores, pedidos y albaranes de entrada que generan lotes
- [ ] Casos de uso en `application`
- [ ] API REST y persistencia
- [ ] Mermas e inventario
- [ ] Coste unitario sin redondeo intermedio

---

Jose González Quevedo · [dev.jotagestudio.es](https://dev.jotagestudio.es/)
