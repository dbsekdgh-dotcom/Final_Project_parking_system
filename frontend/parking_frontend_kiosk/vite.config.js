import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  envDir: '../../',
  server: {
    host: true,
    allowedHosts: true,
    port: 5203,
    proxy: {
      '/api': 'http://localhost:8081',
    },
  },
})
