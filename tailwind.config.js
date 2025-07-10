/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/main/resources/templates/**/*.html",
    "./src/main/resources/static/**/*.html",
    "./src/main/resources/static/**/*.js"
  ],
  theme: {
    extend: {
      colors: {
        primary: '#4a6cf7',
        secondary: '#64748b',
        neutral: {
          100: '#f8fafc',
          200: '#e2e8f0',
          300: '#cbd5e1',
          800: '#1e293b',
          900: '#0f172a'
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif']
      }
    }
  },
  plugins: []
}