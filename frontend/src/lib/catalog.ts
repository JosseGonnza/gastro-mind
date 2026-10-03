export const ALLERGENS = [
  'GLUTEN',
  'CRUSTACEANS',
  'EGGS',
  'FISH',
  'PEANUTS',
  'SOYBEANS',
  'DAIRY',
  'NUTS',
  'CELERY',
  'MUSTARD',
  'SESAME',
  'SULFITES',
  'LUPIN',
  'MOLLUSCS',
] as const;

export type Allergen = (typeof ALLERGENS)[number];

export const ALLERGEN_LABELS: Record<Allergen, string> = {
  GLUTEN: 'Gluten',
  CRUSTACEANS: 'Crustáceos',
  EGGS: 'Huevos',
  FISH: 'Pescado',
  PEANUTS: 'Cacahuetes',
  SOYBEANS: 'Soja',
  DAIRY: 'Lácteos',
  NUTS: 'Frutos de cáscara',
  CELERY: 'Apio',
  MUSTARD: 'Mostaza',
  SESAME: 'Sésamo',
  SULFITES: 'Sulfitos',
  LUPIN: 'Altramuces',
  MOLLUSCS: 'Moluscos',
};

export const CATEGORIES = [
  'MEAT',
  'FISH',
  'SEAFOOD',
  'VEGETABLE',
  'FRUIT',
  'DAIRY',
  'GRAIN',
  'BAKERY',
  'SPICE',
  'SAUCE',
  'DESSERT',
  'SOFT_DRINK',
  'ALCOHOL',
  'CLEANING',
  'PACKAGING',
  'OTHER',
] as const;

export type Category = (typeof CATEGORIES)[number];

interface CategoryStyle {
  label: string;
  band: string;
  chip: string;
}

export const CATEGORY_STYLES: Record<Category, CategoryStyle> = {
  MEAT: { label: 'Carnes', band: 'bg-rose-400', chip: 'bg-rose-100 text-rose-800' },
  FISH: { label: 'Pescados', band: 'bg-sky-400', chip: 'bg-sky-100 text-sky-800' },
  SEAFOOD: { label: 'Mariscos', band: 'bg-teal-400', chip: 'bg-teal-100 text-teal-800' },
  VEGETABLE: { label: 'Verduras y hortalizas', band: 'bg-green-500', chip: 'bg-green-100 text-green-800' },
  FRUIT: { label: 'Frutas', band: 'bg-orange-400', chip: 'bg-orange-100 text-orange-800' },
  DAIRY: { label: 'Lácteos y huevos', band: 'bg-yellow-300', chip: 'bg-yellow-100 text-yellow-800' },
  GRAIN: { label: 'Arroces, pastas y legumbres', band: 'bg-amber-400', chip: 'bg-amber-100 text-amber-800' },
  BAKERY: { label: 'Pan y bollería', band: 'bg-amber-700', chip: 'bg-amber-100 text-amber-900' },
  SPICE: { label: 'Especias y condimentos', band: 'bg-red-500', chip: 'bg-red-100 text-red-800' },
  SAUCE: { label: 'Salsas y aceites', band: 'bg-lime-500', chip: 'bg-lime-100 text-lime-800' },
  DESSERT: { label: 'Repostería', band: 'bg-pink-400', chip: 'bg-pink-100 text-pink-800' },
  SOFT_DRINK: { label: 'Agua y refrescos', band: 'bg-cyan-400', chip: 'bg-cyan-100 text-cyan-800' },
  ALCOHOL: { label: 'Vinos, cervezas y licores', band: 'bg-purple-400', chip: 'bg-purple-100 text-purple-800' },
  CLEANING: { label: 'Limpieza', band: 'bg-slate-400', chip: 'bg-slate-100 text-slate-700' },
  PACKAGING: { label: 'Envases y take-away', band: 'bg-stone-400', chip: 'bg-stone-100 text-stone-700' },
  OTHER: { label: 'Otros', band: 'bg-neutral-300', chip: 'bg-neutral-100 text-neutral-700' },
};

export const UNITS = ['KILOGRAM', 'GRAM', 'LITER', 'MILLILITER', 'UNIT', 'BUNCH', 'PORTION'] as const;

export type Unit = (typeof UNITS)[number];

export const UNIT_LABELS: Record<Unit, string> = {
  KILOGRAM: 'kg',
  GRAM: 'g',
  LITER: 'L',
  MILLILITER: 'ml',
  UNIT: 'ud',
  BUNCH: 'manojo',
  PORTION: 'ración',
};
