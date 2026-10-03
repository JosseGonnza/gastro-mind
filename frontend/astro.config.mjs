import { defineConfig, envField } from 'astro/config';
import node from '@astrojs/node';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  output: 'server',
  adapter: node({ mode: 'standalone' }),
  env: {
    schema: {
      API_URL: envField.string({ context: 'server', access: 'public', default: 'http://localhost:8080' }),
    },
  },
  vite: {
    plugins: [tailwindcss()],
  },
});
