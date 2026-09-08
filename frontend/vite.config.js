// When `npm run dev` we have no nginx server. We configure vite accordingly
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';


const BACKEND = 'http://localhost:8080';

// The animal services send no CORS headers, so the browser may display their images but may
// not read the bytes. Proxying them is what makes "Save" possible.
const animalService = (target) => ({
  target,
  changeOrigin: true,
  rewrite: (path) => path.replace(/^\/animals\/[a-z]+/, ''),
});

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': BACKEND,
      '/actuator': BACKEND,
      '/animals/cat': animalService('https://placecats.com'),
      '/animals/dog': animalService('https://place.dog'),
      '/animals/bear': animalService('https://placebear.com'),
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './vitest.setup.js',
  },
});
