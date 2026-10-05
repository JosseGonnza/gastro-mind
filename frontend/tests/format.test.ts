import { describe, expect, it } from 'vitest';
import { formatDate, formatMoney, formatQuantity } from '../src/lib/format';

const plainSpaces = (text: string) => text.replace(/ | /g, ' ');

describe('Los formatos en español deberían', () => {
  it('mostrar cantidades con coma decimal y su unidad', () => {
    expect(formatQuantity(3.5, 'KILOGRAM')).toBe('3,5 kg');
    expect(formatQuantity(250, 'GRAM')).toBe('250 g');
    expect(formatQuantity(0.125, 'LITER')).toBe('0,125 L');
    expect(formatQuantity(12, 'UNIT')).toBe('12 ud');
  });

  it('mostrar importes en euros con dos decimales', () => {
    expect(plainSpaces(formatMoney(105))).toBe('105,00 €');
    expect(plainSpaces(formatMoney(37.5))).toBe('37,50 €');
    expect(plainSpaces(formatMoney(12345.6))).toBe('12.345,60 €');
  });

  it('mostrar fechas cortas sin moverse de día por la zona horaria', () => {
    expect(formatDate('2026-10-09')).toBe('9 oct 2026');
    expect(formatDate('2026-01-01')).toBe('1 ene 2026');
  });
});
