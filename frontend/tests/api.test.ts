import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  ApiError,
  createProduct,
  createSupplier,
  getGoodsReceipt,
  getStock,
  listGoodsReceipts,
  listProducts,
  listSuppliers,
  registerGoodsReceipt,
  type Product,
} from '../src/lib/api';

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

  it('listar los proveedores', async () => {
    const supplier = { id: 's-1', name: 'Pescados Cimadevilla', taxId: null, phone: null, email: null };
    const fetchMock = respondWith(200, [supplier]);

    const suppliers = await listSuppliers(API);

    expect(fetchMock).toHaveBeenCalledWith(`${API}/suppliers`);
    expect(suppliers).toEqual([supplier]);
  });

  it('crear un proveedor y avisar con el detalle si la API lo rechaza', async () => {
    respondWith(400, { status: 400, detail: 'Supplier name cannot be empty' });

    const request = createSupplier(API, { name: ' ' });

    await expect(request).rejects.toMatchObject({ status: 400, message: 'Supplier name cannot be empty' });
  });

  it('registrar un albarán enviando sus líneas', async () => {
    const fetchMock = respondWith(201, { id: 'r-1', total: 105 });
    const receipt = {
      supplierId: 's-1',
      deliveryNoteNumber: 'ALB-1234',
      lines: [{ productId: 'p-1', quantity: 2.5, unit: 'KILOGRAM' as const, amount: 37.5, expirationDate: '2026-10-09' }],
    };

    const created = await registerGoodsReceipt(API, receipt);

    expect(created).toMatchObject({ id: 'r-1', total: 105 });
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe(`${API}/goods-receipts`);
    expect(init.method).toBe('POST');
    expect(JSON.parse(init.body)).toEqual(receipt);
  });

  it('listar los albaranes enviando solo los filtros que tienen valor', async () => {
    const summary = {
      id: 'r-1',
      supplierId: 's-1',
      supplierName: 'Pescados Cimadevilla',
      deliveryNoteNumber: 'ALB-1234',
      receivedOn: '2026-10-05',
      lineCount: 2,
      total: 105,
    };
    const fetchMock = respondWith(200, [summary]);

    const receipts = await listGoodsReceipts(API, { supplierId: 's-1', from: '', to: '2026-10-31' });

    expect(receipts).toEqual([summary]);
    expect(fetchMock).toHaveBeenCalledWith(`${API}/goods-receipts?supplierId=s-1&to=2026-10-31`);
  });

  it('listar todos los albaranes si no hay filtros', async () => {
    const fetchMock = respondWith(200, []);

    await listGoodsReceipts(API);

    expect(fetchMock).toHaveBeenCalledWith(`${API}/goods-receipts`);
  });

  it('consultar un albarán por su id', async () => {
    const fetchMock = respondWith(200, { id: 'r-1', lines: [] });

    expect(await getGoodsReceipt(API, 'r-1')).toMatchObject({ id: 'r-1' });
    expect(fetchMock).toHaveBeenCalledWith(`${API}/goods-receipts/r-1`);
  });

  it('no dejar que el id del albarán cambie la ruta de la API', async () => {
    const fetchMock = respondWith(400, {});

    await getGoodsReceipt(API, '../stock').catch(() => undefined);

    expect(fetchMock).toHaveBeenCalledWith(`${API}/goods-receipts/..%2Fstock`);
  });

  it('consultar el stock', async () => {
    const stock = [{ productId: 'p-1', name: 'Merluza', category: 'FISH', unit: 'KILOGRAM', allergens: ['FISH'], total: 3.5, batches: [] }];
    const fetchMock = respondWith(200, stock);

    expect(await getStock(API)).toEqual(stock);
    expect(fetchMock).toHaveBeenCalledWith(`${API}/stock`);
  });
});
