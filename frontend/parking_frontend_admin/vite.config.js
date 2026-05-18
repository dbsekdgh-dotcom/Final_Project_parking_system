import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  // process.cwd()는 현재 vite.config.js가 있는 폴더입니다.
  // envDir이 '../../'라면 그 경로를 포함해서 읽어오도록 지정해야 합니다.
  const env = loadEnv(mode, '../../', '');

  console.log("확인된 백엔드 주소:", env.VITE_API_BASE_URL); // 터미널에 주소가 찍히는지 확인용

  return {
    plugins: [react()],
    envDir: '../../',
    define:{
      global: 'globalThis',
    },
    server: {
      port: 5201,
      proxy: {
        '/api': {
          target: 'http://localhost:8081',
          changeOrigin: true,
          // rewrite: (path) => path.replace(/^\/api/, ''),
        },
        '/ws': {
          target: 'http://localhost:8081',
          changeOrigin: true,
          ws:true,
        },
      },
    },
  }
})