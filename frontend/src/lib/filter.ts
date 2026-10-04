import type { Product } from './api';
import { ALLERGENS, ALLERGEN_LABELS, type Allergen } from './catalog';

const PARAM = 'sin';

export function parseExcluded(params: URLSearchParams): Allergen[] {
  const requested = new Set(params.getAll(PARAM));
  return ALLERGENS.filter((allergen) => requested.has(allergen));
}

export function filterProducts(products: Product[], excluded: Allergen[]): Product[] {
  return products.filter((product) => !product.allergens.some((allergen) => excluded.includes(allergen)));
}

export function toggleHref(excluded: Allergen[], allergen: Allergen): string {
  const next = excluded.includes(allergen)
    ? excluded.filter((current) => current !== allergen)
    : [...excluded, allergen];
  const ordered = ALLERGENS.filter((candidate) => next.includes(candidate));
  if (ordered.length === 0) {
    return '/';
  }
  return `/?${new URLSearchParams(ordered.map((value) => [PARAM, value]))}`;
}

export function describeExcluded(excluded: Allergen[]): string {
  const labels = excluded.map((allergen) => ALLERGEN_LABELS[allergen].toLowerCase());
  if (labels.length <= 1) {
    return labels.join('');
  }
  return `${labels.slice(0, -1).join(', ')} y ${labels.at(-1)}`;
}
