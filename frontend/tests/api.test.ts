import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, createProduct, listProducts, type Product } from '../src/lib/api';

const API = 'http://api.test';

const bread: Product = {
  id: '28ab609f-4137-45b0-a142-c809117fa508',
  name: 'Pan de semillas',
  description: 'Pan con sésamo',
  category: 'BAKERY',
  unit: 'UNIT',
  allergens: ['GLUTEN', 'SESAME'],
};

function respondWith(status: number, body: unknown) {
  const fetchMock = vi.fn().mockResolvedValue(
    new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }),
  );
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('El cliente de la API debería', () => {
  it('listar los productos', async () => {
    const fetchMock = respondWith(200, [bread]);

    const products = await listProducts(API);

    expect(fetchMock).toHaveBeenCalledWith(`${API}/products`);
    expect(products).toEqual([bread]);
  });

  it('crear un producto enviándolo como JSON', async () => {
    const fetchMock = respondWith(201, bread);
    const { id, ...newProduct } = bread;

    const created = await createProduct(API, { ...newProduct, description: newProduct.description ?? undefined });

    expect(created).toEqual(bread);
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe(`${API}/products`);
    expect(init.method).toBe('POST');
    expect(init.headers).toEqual({ 'Content-Type': 'application/json' });
    expect(JSON.parse(init.body)).toMatchObject({ name: 'Pan de semillas', allergens: ['GLUTEN', 'SESAME'] });
  });

  it('lanzar un ApiError con el detalle que devuelve la API', async () => {
    respondWith(400, { status: 400, title: 'Bad Request', detail: 'Product name cannot be empty' });

    const request = createProduct(API, { name: ' ', category: 'BAKERY', unit: 'UNIT', allergens: [] });

    await expect(request).rejects.toThrow(ApiError);
    await expect(request).rejects.toMatchObject({ status: 400, message: 'Product name cannot be empty' });
  });
});
