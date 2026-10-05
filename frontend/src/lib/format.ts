import { UNIT_LABELS, type Unit } from './catalog';

const quantityFormat = new Intl.NumberFormat('es-ES', { maximumFractionDigits: 3 });
const moneyFormat = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' });
const dateFormat = new Intl.DateTimeFormat('es-ES', { day: 'numeric', month: 'short', year: 'numeric', timeZone: 'UTC' });

export function formatQuantity(amount: number, unit: Unit): string {
  return `${quantityFormat.format(amount)} ${UNIT_LABELS[unit]}`;
}

export function formatMoney(amount: number): string {
  return moneyFormat.format(amount);
}

export function formatDate(isoDate: string): string {
  return dateFormat.format(new Date(`${isoDate}T00:00:00Z`)).replace('.', '');
}
