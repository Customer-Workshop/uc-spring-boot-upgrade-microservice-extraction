import type { Config } from 'tailwindcss'

const config: Config = {
  content: ['./src/**/*.{js,ts,jsx,tsx,mdx}'],
  theme: {
    extend: {
      colors: {
        bank: {
          primary: '#1a365d',
          secondary: '#2b6cb0',
          accent: '#4299e1',
          light: '#ebf8ff',
          dark: '#0d2137',
        },
      },
    },
  },
  plugins: [],
}
export default config
