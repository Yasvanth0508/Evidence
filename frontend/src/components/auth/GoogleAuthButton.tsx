import { useEffect, useRef, useState } from "react";
import { Loader2 } from "lucide-react";

interface GoogleAuthButtonProps {
  onSuccess: (credential: string) => void;
  onError?: (error: string) => void;
  text?: "signin_with" | "signup_with" | "continue_with";
  disabled?: boolean;
}

declare global {
  interface Window {
    google?: any;
  }
}

export const GoogleAuthButton = ({
  onSuccess,
  onError,
  text = "continue_with",
  disabled = false,
}: GoogleAuthButtonProps) => {
  const [isGsiLoaded, setIsGsiLoaded] = useState(false);
  const [isClickLoading, setIsClickLoading] = useState(false);
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || "";

  // Keep latest callbacks in refs to avoid re-rendering issues
  const onSuccessRef = useRef(onSuccess);
  const onErrorRef = useRef(onError);

  useEffect(() => {
    onSuccessRef.current = onSuccess;
    onErrorRef.current = onError;
  }, [onSuccess, onError]);

  useEffect(() => {
    // If Google GSI is already present on window
    if (window.google?.accounts?.oauth2 || window.google?.accounts?.id) {
      setIsGsiLoaded(true);
      return;
    }

    const script = document.createElement("script");
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;
    script.onload = () => {
      setIsGsiLoaded(true);
    };
    script.onerror = () => {
      console.warn("Could not load Google Identity Services SDK");
      setIsGsiLoaded(false);
    };
    document.body.appendChild(script);
  }, []);

  const handleGoogleClick = () => {
    if (disabled || isClickLoading) return;

    if (!clientId) {
      const msg = "Google Client ID is not configured. Please add VITE_GOOGLE_CLIENT_ID to your frontend .env file.";
      if (onErrorRef.current) {
        onErrorRef.current(msg);
      } else {
        alert(msg);
      }
      return;
    }

    if (!isGsiLoaded && !window.google?.accounts?.oauth2 && !window.google?.accounts?.id) {
      const msg = "Google Identity Services SDK is still loading or was blocked by an ad-blocker. Please refresh.";
      if (onErrorRef.current) {
        onErrorRef.current(msg);
      } else {
        alert(msg);
      }
      return;
    }

    setIsClickLoading(true);

    try {
      // Use official Google OAuth2 Token Client with forced select_account prompt
      if (window.google?.accounts?.oauth2) {
        const client = window.google.accounts.oauth2.initTokenClient({
          client_id: clientId,
          scope: "email profile openid",
          prompt: "select_account",
          callback: (response: any) => {
            setIsClickLoading(false);
            if (response?.access_token) {
              onSuccessRef.current(response.access_token);
            } else if (response?.error) {
              if (response.error !== "access_denied") {
                onErrorRef.current?.(`Google sign-in error: ${response.error}`);
              }
            }
          },
          error_callback: (err: any) => {
            setIsClickLoading(false);
            console.warn("Google OAuth error:", err);
            onErrorRef.current?.("Google authentication popup was closed or interrupted.");
          },
        });
        client.requestAccessToken({ prompt: "select_account" });
      } else if (window.google?.accounts?.id) {
        window.google.accounts.id.initialize({
          client_id: clientId,
          callback: (res: any) => {
            setIsClickLoading(false);
            if (res?.credential) {
              onSuccessRef.current(res.credential);
            } else if (onErrorRef.current) {
              onErrorRef.current("Google sign-in did not return a valid credential.");
            }
          },
        });
        window.google.accounts.id.prompt();
      }
    } catch (err: any) {
      setIsClickLoading(false);
      console.error("Failed to launch Google Sign-In:", err);
      onErrorRef.current?.("Failed to launch Google Sign-In. Please check your browser popup settings.");
    }
  };

  const buttonText =
    text === "signin_with"
      ? "Sign in with Google"
      : text === "signup_with"
      ? "Sign up with Google"
      : "Continue with Google";

  return (
    <div className="w-full flex justify-center">
      <button
        type="button"
        onClick={handleGoogleClick}
        disabled={disabled || isClickLoading}
        className="w-full flex items-center justify-center gap-2.5 h-11 px-4 border border-gray-200 dark:border-slate-700 rounded-2xl bg-white dark:bg-slate-900 hover:bg-gray-50 dark:hover:bg-slate-800 text-xs font-semibold text-gray-700 dark:text-slate-200 transition-colors shadow-2xs cursor-pointer disabled:opacity-50"
      >
        {isClickLoading ? (
          <Loader2 className="w-4 h-4 animate-spin text-primary" />
        ) : (
          <svg className="w-4 h-4 shrink-0" viewBox="0 0 24 24">
            <path
              fill="#4285F4"
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
            />
            <path
              fill="#34A853"
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            />
            <path
              fill="#FBBC05"
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
            />
            <path
              fill="#EA4335"
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
            />
          </svg>
        )}
        <span>{buttonText}</span>
      </button>
    </div>
  );
};