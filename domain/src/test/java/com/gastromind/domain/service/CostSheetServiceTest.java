package com.gastromind.domain.service;

import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Recipe;
import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.CostSheet;
import com.gastromind.domain.valueobject.CostSheetLine;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.PurchasePrice;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.RecipeIngredient;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("CostSheetService debería")
class CostSheetServiceTest {

    private final CostSheetService costSheetService = new CostSheetService();

    private final Product hake = Product.create("Merluza entera", null, Category.FISH, UnitOfMeasure.KILOGRAM, Set.of(Allergen.FISH));
    private final Product potato = Product.create("Patata", null, Category.VEGETABLE, UnitOfMeasure.KILOGRAM, Set.of());
    private final Product oil = Product.create("Aceite de oliva virgen extra", null, Category.OTHER, UnitOfMeasure.LITER, Set.of());

    private final Map<UUID, PurchasePrice> lastPurchases = Map.of(
            hake.getId(), new PurchasePrice(Money.of(36.0), Quantity.of(3, UnitOfMeasure.KILOGRAM)),
            potato.getId(), new PurchasePrice(Money.of(4.5), Quantity.of(5, UnitOfMeasure.KILOGRAM)),
            oil.getId(), new PurchasePrice(Money.of(18.0), Quantity.of(3, UnitOfMeasure.LITER)));

    private Recipe grilledHake() {
        Recipe recipe = Recipe.create("Merluza a la plancha con patatas", null, 3, Money.of(16.5));
        recipe.addIngredient(RecipeIngredient.of(hake, Quantity.of(600, UnitOfMeasure.GRAM), new BigDecimal("50")));
        recipe.addIngredient(RecipeIngredient.of(potato, Quantity.of(500, UnitOfMeasure.GRAM)));
        recipe.addIngredient(RecipeIngredient.of(oil, Quantity.of(45, UnitOfMeasure.MILLILITER)));
        return recipe;
    }

    @Test
    @DisplayName("costear cada línea por su cantidad bruta, y dar el total y el coste por ración")
    void shouldCostEachLineByGrossQuantity() {
        CostSheet sheet = costSheetService.calculate(grilledHake(), List.of(hake, potato, oil), lastPurchases);

        assertThat(sheet.lines())
                .extracting(CostSheetLine::productName, CostSheetLine::grossQuantity, CostSheetLine::cost)
                .containsExactly(
                        tuple("Merluza entera", Quantity.of(1200, UnitOfMeasure.GRAM), Money.of(14.4)),
                        tuple("Patata", Quantity.of(500, UnitOfMeasure.GRAM), Money.of(0.45)),
                        tuple("Aceite de oliva virgen extra", Quantity.of(45, UnitOfMeasure.MILLILITER), Money.of(0.27)));
        assertThat(sheet.totalCost()).isEqualTo(Money.of(15.12));
        assertThat(sheet.costPerPortion()).isEqualTo(Money.of(5.04));
    }

    @Test
    @DisplayName("sumar el total sin redondear antes cada línea")
    void shouldAddUpTotalWithoutRoundingEachLine() {
        Product rice = Product.create("Arroz bomba", null, Category.GRAIN, UnitOfMeasure.KILOGRAM, Set.of());
        Product chickpeas = Product.create("Garbanzo", null, Category.GRAIN, UnitOfMeasure.KILOGRAM, Set.of());
        Recipe recipe = Recipe.create("Prueba", null, 1, null);
        recipe.addIngredient(RecipeIngredient.of(rice, Quantity.of(1, UnitOfMeasure.KILOGRAM)));
        recipe.addIngredient(RecipeIngredient.of(chickpeas, Quantity.of(1, UnitOfMeasure.KILOGRAM)));
        PurchasePrice tenEurosForThreeKilos = new PurchasePrice(Money.of(10.0), Quantity.of(3, UnitOfMeasure.KILOGRAM));

        CostSheet sheet = costSheetService.calculate(recipe, List.of(rice, chickpeas),
                Map.of(rice.getId(), tenEurosForThreeKilos, chickpeas.getId(), tenEurosForThreeKilos));

        assertThat(sheet.lines()).extracting(CostSheetLine::cost).containsOnly(Money.of(3.33));
        assertThat(sheet.totalCost()).isEqualTo(Money.of(6.67));
    }

    @Test
    @DisplayName("estar completo, con la última compra de cada línea, si todos los productos tienen precio")
    void shouldBeCompleteWhenEveryProductHasPrice() {
        CostSheet sheet = costSheetService.calculate(grilledHake(), List.of(hake, potato, oil), lastPurchases);

        assertThat(sheet.complete()).isTrue();
        assertThat(sheet.lines().getFirst().lastPurchase()).isEqualTo(lastPurchases.get(hake.getId()));
    }

    @Test
    @DisplayName("dejar sin precio la línea de un producto que no se ha comprado nunca y avisar de que está incompleto")
    void shouldBeIncompleteWhenAProductHasNoPrice() {
        Product salt = Product.create("Sal en escamas", null, Category.SPICE, UnitOfMeasure.GRAM, Set.of());
        Recipe recipe = grilledHake();
        recipe.addIngredient(RecipeIngredient.of(salt, Quantity.of(5, UnitOfMeasure.GRAM)));

        CostSheet sheet = costSheetService.calculate(recipe, List.of(hake, potato, oil, salt), lastPurchases);

        CostSheetLine saltLine = sheet.lines().getLast();
        assertThat(saltLine.lastPurchase()).isNull();
        assertThat(saltLine.cost()).isNull();
        assertThat(sheet.complete()).isFalse();
        assertThat(sheet.totalCost()).isEqualTo(Money.of(15.12));
    }

    @Test
    @DisplayName("lanzar excepción si falta el producto de algún ingrediente")
    void shouldThrowExceptionWhenAProductIsMissing() {
        assertThatThrownBy(() -> costSheetService.calculate(grilledHake(), List.of(hake, potato), lastPurchases))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing product for ingredient " + oil.getId());
    }
}
