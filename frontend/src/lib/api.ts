import type { Allergen, Category, Unit } from './catalog';

export interface Product {
  id: string;
  name: string;
  description: string | null;
  category: Category;
  unit: Unit;
  allergens: Allergen[];
}

export interface NewProduct {
  name: string;
  description?: string;
  category: Category;
  unit: Unit;
  allergens: Allergen[];
}

export interface Supplier {
  id: string;
  name: string;
  taxId: string | null;
  phone: string | null;
  email: string | null;
}

export interface NewSupplier {
  name: string;
  taxId?: string;
  phone?: string;
  email?: string;
}

export interface NewReceiptLine {
  productId: string;
  quantity: number;
  unit: Unit;
  amount: number;
  expirationDate: string;
  lotCode?: string;
}

export interface NewGoodsReceipt {
  supplierId: string;
  deliveryNoteNumber: string;
  lines: NewReceiptLine[];
}

export interface GoodsReceipt {
  id: string;
  supplierId: string;
  deliveryNoteNumber: string;
  receivedOn: string;
  total: number;
  lines: {
    batchId: string;
    productId: string;
    quantity: number;
    unit: Unit;
    amount: number;
    expirationDate: string;
    lotCode: string;
  }[];
}

export interface GoodsReceiptSummary {
  id: string;
  supplierId: string;
  supplierName: string;
  deliveryNoteNumber: string;
  receivedOn: string;
  lineCount: number;
  total: number;
}

export interface ReceiptFilter {
  supplierId?: string;
  from?: string;
  to?: string;
}

export interface StockBatch {
  batchId: string;
  lotCode: string;
  quantity: number;
  entryDate: string;
  expirationDate: string;
}

export interface ProductStock {
  productId: string;
  name: string;
  category: Category;
  unit: Unit;
  allergens: Allergen[];
  total: number;
  batches: StockBatch[];
}

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export async function listProducts(baseUrl: string): Promise<Product[]> {
  const response = await fetch(`${baseUrl}/products`);
  return readBody<Product[]>(response);
}

export async function createProduct(baseUrl: string, product: NewProduct): Promise<Product> {
  return post<Product>(`${baseUrl}/products`, product);
}

export async function listSuppliers(baseUrl: string): Promise<Supplier[]> {
  const response = await fetch(`${baseUrl}/suppliers`);
  return readBody<Supplier[]>(response);
}

export async function createSupplier(baseUrl: string, supplier: NewSupplier): Promise<Supplier> {
  return post<Supplier>(`${baseUrl}/suppliers`, supplier);
}

export async function registerGoodsReceipt(baseUrl: string, receipt: NewGoodsReceipt): Promise<GoodsReceipt> {
  return post<GoodsReceipt>(`${baseUrl}/goods-receipts`, receipt);
}

export async function listGoodsReceipts(baseUrl: string, filter: ReceiptFilter = {}): Promise<GoodsReceiptSummary[]> {
  const params = new URLSearchParams();
  for (const [name, value] of Object.entries(filter)) {
    if (value) {
      params.set(name, value);
    }
  }
  const query = params.size > 0 ? `?${params}` : '';
  const response = await fetch(`${baseUrl}/goods-receipts${query}`);
  return readBody<GoodsReceiptSummary[]>(response);
}

export async function getGoodsReceipt(baseUrl: string, id: string): Promise<GoodsReceipt> {
  const response = await fetch(`${baseUrl}/goods-receipts/${encodeURIComponent(id)}`);
  return readBody<GoodsReceipt>(response);
}

export async function getStock(baseUrl: string): Promise<ProductStock[]> {
  const response = await fetch(`${baseUrl}/stock`);
  return readBody<ProductStock[]>(response);
}

async function post<T>(url: string, body: unknown): Promise<T> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  return readBody<T>(response);
}

async function readBody<T>(response: Response): Promise<T> {
  if (response.ok) {
    return (await response.json()) as T;
  }
  const problem: { detail?: string } | null = await response.json().catch(() => null);
  throw new ApiError(response.status, problem?.detail ?? `La API respondió con ${response.status}`);
}
