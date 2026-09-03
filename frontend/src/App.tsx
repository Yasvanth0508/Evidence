import { useEffect } from "react";
import { RouterProvider } from "react-router-dom";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/lib/queryClient";
import { router } from "@/routes";
import { useThemeStore } from "@/store/themeStore";
import { FloatingThemeToggle } from "@/components/common/ThemeToggle";

export function App() {
  const initTheme = useThemeStore((state) => state.initTheme);

  useEffect(() => {
    initTheme();
  }, [initTheme]);

  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
      <FloatingThemeToggle />
    </QueryClientProvider>
  );
}

export default App;
