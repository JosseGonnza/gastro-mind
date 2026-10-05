export type ExpiryLevel = 'expired' | 'urgent' | 'soon' | 'ok';

export interface ExpiryStatus {
  days: number;
  level: ExpiryLevel;
  label: string;
}

const MS_PER_DAY = 86_400_000;

// En UTC para que el cambio de hora de octubre o marzo no robe ni sume un día
function toUtcDay(isoDate: string): number {
  const [year, month, day] = isoDate.split('-').map(Number);
  return Date.UTC(year, month - 1, day) / MS_PER_DAY;
}

export function expiryStatus(expirationDate: string, today: string): ExpiryStatus {
  const days = toUtcDay(expirationDate) - toUtcDay(today);
  if (days < 0) {
    return { days, level: 'expired', label: days === -1 ? 'Caducado ayer' : `Caducado hace ${-days} días` };
  }
  if (days === 0) {
    return { days, level: 'urgent', label: 'Caduca hoy' };
  }
  if (days === 1) {
    return { days, level: 'urgent', label: 'Caduca mañana' };
  }
  return { days, level: days < 5 ? 'soon' : 'ok', label: `Caduca en ${days} días` };
}

export function todayIso(now: Date = new Date()): string {
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}
