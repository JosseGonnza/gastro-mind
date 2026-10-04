import { describe, expect, it } from 'vitest';
import type { Product } from '../src/lib/api';
import { describeExcluded, filterProducts, parseExcluded, toggleHref } from '../src/lib/filter';

function product(name: string, allergens: Product['allergens']): Product {
  return { id: name, name, description: null, category: 'OTHER', unit: 'UNIT', allergens };
}

const bread = product('Pan de semillas', ['GLUTEN', 'SESAME']);
const pesto = product('Pesto genovés', ['NUTS', 'DAIRY']);
const rice = product('Arroz bomba', []);
const soySauce = product('Salsa de soja', ['SOYBEANS', 'GLUTEN']);
const catalog = [bread, pesto, rice, soySauce];

describe('El filtro de alérgenos debería', () => {
  it('leer de la URL los alérgenos a evitar, en el orden oficial y sin repetidos ni inventados', () => {
    const params = new URLSearchParams('sin=DAIRY&sin=GLUTEN&sin=PIZZA&sin=GLUTEN');

    expect(parseExcluded(params)).toEqual(['GLUTEN', 'DAIRY']);
  });

  it('dejar solo los productos que no llevan ninguno de los alérgenos a evitar', () => {
    expect(filterProducts(catalog, ['GLUTEN', 'DAIRY'])).toEqual([rice]);
  });

  it('dejar todos los productos si no hay nada que evitar', () => {
    expect(filterProducts(catalog, [])).toEqual(catalog);
  });

  it('construir el enlace que añade un alérgeno al filtro', () => {
    expect(toggleHref(['DAIRY'], 'GLUTEN')).toBe('/?sin=GLUTEN&sin=DAIRY');
  });

  it('construir el enlace que quita un alérgeno del filtro', () => {
    expect(toggleHref(['GLUTEN', 'DAIRY'], 'GLUTEN')).toBe('/?sin=DAIRY');
    expect(toggleHref(['GLUTEN'], 'GLUTEN')).toBe('/');
  });

  it('describir lo que se evita como en español', () => {
    expect(describeExcluded(['GLUTEN'])).toBe('gluten');
    expect(describeExcluded(['GLUTEN', 'DAIRY'])).toBe('gluten y lácteos');
    expect(describeExcluded(['GLUTEN', 'EGGS', 'NUTS'])).toBe('gluten, huevos y frutos de cáscara');
  });
});
