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
  const response = await fetch(`${baseUrl}/products`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(product),
  });
  return readBody<Product>(response);
}

async function readBody<T>(response: Response): Promise<T> {
  if (response.ok) {
    return (await response.json()) as T;
  }
  const problem: { detail?: string } | null = await response.json().catch(() => null);
  throw new ApiError(response.status, problem?.detail ?? `La API respondió con ${response.status}`);
}
