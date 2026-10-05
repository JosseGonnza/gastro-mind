import { describe, expect, it } from 'vitest';
import { parseDecimal, readReceiptLines, rowsFromForm, type ReceiptFormRow } from '../src/lib/receipt-form';

function row(overrides: Partial<ReceiptFormRow> = {}): ReceiptFormRow {
  return {
    productId: 'p-1',
    quantity: '2,5',
    unit: 'KILOGRAM',
    amount: '37,50',
    expirationDate: '2026-10-09',
    lotCode: '',
    ...overrides,
  };
}

describe('El formulario del albarán debería', () => {
  it('entender números escritos a la española', () => {
    expect(parseDecimal('2,5')).toBe(2.5);
    expect(parseDecimal('1.234,56')).toBe(1234.56);
    expect(parseDecimal('3.5')).toBe(3.5);
    expect(parseDecimal(' 12 ')).toBe(12);
    expect(parseDecimal('')).toBeNull();
    expect(parseDecimal('dos')).toBeNull();
  });

  it('convertir las filas rellenas en líneas e ignorar las vacías', () => {
    const { lines, errors } = readReceiptLines([row({ lotCode: ' GR-778 ' }), row({ productId: '' }), row({ productId: 'p-2', lotCode: '' })]);

    expect(errors).toEqual([]);
    expect(lines).toEqual([
      { productId: 'p-1', quantity: 2.5, unit: 'KILOGRAM', amount: 37.5, expirationDate: '2026-10-09', lotCode: 'GR-778' },
      { productId: 'p-2', quantity: 2.5, unit: 'KILOGRAM', amount: 37.5, expirationDate: '2026-10-09' },
    ]);
  });

  it('avisar en español de lo que falta en cada línea, con su número', () => {
    const { errors } = readReceiptLines([row(), row({ quantity: 'mucho', amount: '', expirationDate: '' })]);

    expect(errors).toEqual([
      'Línea 2: la cantidad no es válida',
      'Línea 2: el importe no es válido',
      'Línea 2: falta la fecha de caducidad',
    ]);
  });

  it('juntar los campos repetidos del formulario en filas', () => {
    const form = new FormData();
    for (const [productId, quantity] of [['p-1', '2'], ['p-2', '3']]) {
      form.append('productId', productId);
      form.append('quantity', quantity);
      form.append('unit', 'KILOGRAM');
      form.append('amount', '10');
      form.append('expirationDate', '2026-10-09');
      form.append('lotCode', '');
    }

    expect(rowsFromForm(form)).toEqual([
      { productId: 'p-1', quantity: '2', unit: 'KILOGRAM', amount: '10', expirationDate: '2026-10-09', lotCode: '' },
      { productId: 'p-2', quantity: '3', unit: 'KILOGRAM', amount: '10', expirationDate: '2026-10-09', lotCode: '' },
    ]);
  });
});
