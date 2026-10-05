import type { NewReceiptLine } from './api';
import type { Unit } from './catalog';

export interface ReceiptFormRow {
  productId: string;
  quantity: string;
  unit: string;
  amount: string;
  expirationDate: string;
  lotCode: string;
}

const FIELDS = ['productId', 'quantity', 'unit', 'amount', 'expirationDate', 'lotCode'] as const;

// "2,5" y "1.234,56" a la española; si no hay coma, el punto es el decimal ("3.5")
export function parseDecimal(value: string): number | null {
  const text = value.trim();
  if (text === '') {
    return null;
  }
  const normalized = text.includes(',') ? text.replaceAll('.', '').replace(',', '.') : text;
  const number = Number(normalized);
  return Number.isFinite(number) ? number : null;
}

export function readReceiptLines(rows: ReceiptFormRow[]): { lines: NewReceiptLine[]; errors: string[] } {
  const lines: NewReceiptLine[] = [];
  const errors: string[] = [];
  rows.forEach((row, index) => {
    if (row.productId.trim() === '') {
      return;
    }
    const prefix = `Línea ${index + 1}`;
    const quantity = parseDecimal(row.quantity);
    const amount = parseDecimal(row.amount);
    if (quantity === null) errors.push(`${prefix}: la cantidad no es válida`);
    if (amount === null) errors.push(`${prefix}: el importe no es válido`);
    if (row.expirationDate.trim() === '') errors.push(`${prefix}: falta la fecha de caducidad`);
    if (quantity === null || amount === null || row.expirationDate.trim() === '') {
      return;
    }
    const lotCode = row.lotCode.trim();
    lines.push({
      productId: row.productId,
      quantity,
      unit: row.unit as Unit,
      amount,
      expirationDate: row.expirationDate,
      ...(lotCode === '' ? {} : { lotCode }),
    });
  });
  return { lines, errors };
}

export function rowsFromForm(form: FormData): ReceiptFormRow[] {
  const columns = Object.fromEntries(FIELDS.map((field) => [field, form.getAll(field).map(String)]));
  return columns.productId.map((_, index) =>
    Object.fromEntries(FIELDS.map((field) => [field, columns[field][index] ?? ''])) as unknown as ReceiptFormRow,
  );
}
