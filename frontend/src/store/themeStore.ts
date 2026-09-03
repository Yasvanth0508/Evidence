import { create } from "zustand";

export type ThemeMode = "light" | "dark";

interface ThemeState {
  theme: ThemeMode;
  setTheme: (theme: ThemeMode) => void;
  toggleTheme: () => void;
  initTheme: () => void;
}

const getInitialTheme = (): ThemeMode => {
  if (typeof window === "undefined") return "light";

  const savedTheme = localStorage.getItem("evidence_theme") as ThemeMode | null;
  if (savedTheme === "dark" || savedTheme === "light") {
    return savedTheme;
  }

  // Fallback to IDE preference if previously set
  const savedIdeTheme = localStorage.getItem("evidence_ide_theme");
  if (savedIdeTheme === "dark" || savedIdeTheme === "light") {
    return savedIdeTheme as ThemeMode;
  }

  // Fallback to system preference
  if (window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches) {
    return "dark";
  }

  return "light";
};

const applyThemeToDOM = (theme: ThemeMode) => {
  if (typeof document === "undefined") return;

  const root = document.documentElement;
  if (theme === "dark") {
    root.classList.add("dark");
  } else {
    root.classList.remove("dark");
  }

  // Also set data-theme attribute for any CSS selectors relying on it
  root.setAttribute("data-theme", theme);

  // Sync to local storage
  localStorage.setItem("evidence_theme", theme);
  localStorage.setItem("evidence_ide_theme", theme);
};

export const useThemeStore = create<ThemeState>((set, get) => ({
  theme: getInitialTheme(),

  setTheme: (theme: ThemeMode) => {
    applyThemeToDOM(theme);
    set({ theme });
  },

  toggleTheme: () => {
    const currentTheme = get().theme;
    const nextTheme: ThemeMode = currentTheme === "dark" ? "light" : "dark";
    applyThemeToDOM(nextTheme);
    set({ theme: nextTheme });
  },

  initTheme: () => {
    const currentTheme = get().theme;
    applyThemeToDOM(currentTheme);
  },
}));
