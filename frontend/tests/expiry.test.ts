import { describe, expect, it } from 'vitest';
import { expiryStatus, todayIso } from '../src/lib/expiry';

const TODAY = '2026-10-05';

describe('El estado de caducidad debería', () => {
  it('marcar como caducado lo que ya pasó de fecha', () => {
    expect(expiryStatus('2026-10-03', TODAY)).toEqual({ days: -2, level: 'expired', label: 'Caducado hace 2 días' });
    expect(expiryStatus('2026-10-04', TODAY)).toEqual({ days: -1, level: 'expired', label: 'Caducado ayer' });
  });

  it('marcar como urgente lo que caduca hoy o mañana', () => {
    expect(expiryStatus('2026-10-05', TODAY)).toEqual({ days: 0, level: 'urgent', label: 'Caduca hoy' });
    expect(expiryStatus('2026-10-06', TODAY)).toEqual({ days: 1, level: 'urgent', label: 'Caduca mañana' });
  });

  it('avisar de lo que caduca en menos de cinco días', () => {
    expect(expiryStatus('2026-10-07', TODAY)).toEqual({ days: 2, level: 'soon', label: 'Caduca en 2 días' });
    expect(expiryStatus('2026-10-09', TODAY)).toEqual({ days: 4, level: 'soon', label: 'Caduca en 4 días' });
  });

  it('dar por bueno lo que caduca dentro de cinco días o más', () => {
    expect(expiryStatus('2026-10-10', TODAY)).toEqual({ days: 5, level: 'ok', label: 'Caduca en 5 días' });
  });

  it('contar bien los días aunque cambie el mes o la hora', () => {
    expect(expiryStatus('2026-11-01', '2026-10-25').days).toBe(7);
    expect(expiryStatus('2026-10-26', '2026-10-24').days).toBe(2);
  });

  it('saber qué día es hoy en la hora local de la cocina', () => {
    expect(todayIso(new Date(2026, 9, 5, 23, 30))).toBe('2026-10-05');
  });
});
