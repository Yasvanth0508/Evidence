/** @type {import('tailwindcss').Config} */
export default {
  darkMode: ["class"],
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    container: {
      center: true,
      padding: "2rem",
      screens: {
        "2xl": "1400px",
      },
    },
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        primary: {
          DEFAULT: "var(--theme-primary)",
          foreground: "var(--theme-primary-foreground)",
          hover: "var(--theme-primary-hover)",
          light: "var(--theme-primary-light)",
          border: "var(--theme-primary-border)",
          ring: "var(--theme-primary-ring)",
          50: "#EEF0FF",
          100: "#DEDEFC",
          200: "#C7C8FA",
          300: "#9E9EFA",
          400: "#6B6BF7",
          500: "#1515A5",
          600: "#0E0E75",
          700: "#090956",
        },
        brand: {
          DEFAULT: "var(--theme-primary)",
          foreground: "var(--theme-primary-foreground)",
          hover: "var(--theme-primary-hover)",
          light: "var(--theme-primary-light)",
          border: "var(--theme-primary-border)",
        },
        secondary: {
          DEFAULT: "hsl(var(--secondary))",
          foreground: "hsl(var(--secondary-foreground))",
        },
        destructive: {
          DEFAULT: "hsl(var(--destructive))",
          foreground: "hsl(var(--destructive-foreground))",
        },
        muted: {
          DEFAULT: "hsl(var(--muted))",
          foreground: "hsl(var(--muted-foreground))",
        },
        accent: {
          DEFAULT: "hsl(var(--accent))",
          foreground: "hsl(var(--accent-foreground))",
        },
        popover: {
          DEFAULT: "hsl(var(--popover))",
          foreground: "hsl(var(--popover-foreground))",
        },
        card: {
          DEFAULT: "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))",
        },
        // Evidence Design System Specific Semantic Tokens
        evidence: {
          primary: "var(--theme-primary)",
          primaryHover: "var(--theme-primary-hover)",
          primaryLight: "var(--theme-primary-light)",
          primaryBorder: "var(--theme-primary-border)",
          orange: "var(--theme-primary)",
          orangeHover: "var(--theme-primary-hover)",
          orangeLight: "var(--theme-primary-light)",
          green: "#10B981",
          greenLight: "#ECFDF5",
          red: "#EF4444",
          redLight: "#FEF2F2",
          purple: "#8B5CF6",
          purpleLight: "#F5F3FF",
          blue: "#3B82F6",
          blueLight: "#EFF6FF",
          amber: "#F59E0B",
          amberLight: "#FFFBEB",
          dark: "#111827",
          mutedText: "#6B7280",
          cardBg: "#FFFFFF",
          appBg: "#F9FAFB",
          borderColor: "#E5E7EB",
        }
      },
      borderRadius: {
        lg: "var(--radius)",
        md: "calc(var(--radius) - 2px)",
        sm: "calc(var(--radius) - 4px)",
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "-apple-system", "BlinkMacSystemFont", "Segoe UI", "Roboto", "sans-serif"],
        mono: ["JetBrains Mono", "Fira Code", "Menlo", "Monaco", "Consolas", "monospace"],
      },
      keyframes: {
        "accordion-down": {
          from: { height: "0" },
          to: { height: "var(--radix-accordion-content-height)" },
        },
        "accordion-up": {
          from: { height: "var(--radix-accordion-content-height)" },
          to: { height: "0" },
        },
      },
      animation: {
        "accordion-down": "accordion-down 0.2s ease-out",
        "accordion-up": "accordion-up 0.2s ease-out",
      },
    },
  },
  plugins: [],
}
