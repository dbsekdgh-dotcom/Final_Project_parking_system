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
    server: {
      proxy: {
        // 브라우저에서 호출할 때 앞에 /api를 붙이도록 약속합니다.
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
          // 실제 백엔드에는 /api를 떼고 전달합니다. 
          // 예: /api/admin/logout -> /admin/logout (8080으로 전달)
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    },
  }
})