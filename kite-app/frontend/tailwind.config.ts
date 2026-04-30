import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./src/**/*.{js,ts,jsx,tsx,mdx}"],
  theme: {
    extend: {
      colors: {
        kite: {
          blue: "#387ed1",
          red: "#e54040",
          green: "#4caf50",
          orange: "#ff6f00",
          bg: "#f8f9fa",
          sidebar: "#ffffff",
          header: "#ffffff",
          text: "#444444",
          "text-light": "#999999",
          border: "#e8e8e8",
          hover: "#f5f5f5",
        },
      },
    },
  },
  plugins: [],
};
export default config;
