import { useEffect, useRef, useState } from "react";

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
  const buttonRef = useRef<HTMLDivElement>(null);
  const [isGsiLoaded, setIsGsiLoaded] = useState(false);
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || "";

  useEffect(() => {
    // If Google GSI is already present on window
    if (window.google?.accounts?.id) {
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

    return () => {
      // Keep script cached in DOM
    };
  }, []);

  useEffect(() => {
    if (!isGsiLoaded || !clientId || !buttonRef.current || !window.google?.accounts?.id) {
      return;
    }

    try {
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: (res: any) => {
          if (res?.credential) {
            onSuccess(res.credential);
          } else if (onError) {
            onError("Google sign-in did not return a valid credential.");
          }
        },
      });

      buttonRef.current.innerHTML = "";
      window.google.accounts.id.renderButton(buttonRef.current, {
        theme: "outline",
        size: "large",
        width: 380,
        text: text,
        shape: "pill",
      });
    } catch (err) {
      console.warn("Failed to render Google Sign-In button:", err);
    }
  }, [isGsiLoaded, clientId, text, onSuccess, onError]);

  const handleCustomGoogleClick = () => {
    if (disabled) return;

    if (clientId && window.google?.accounts?.id) {
      window.google.accounts.id.prompt();
    } else {
      if (onError) {
        onError("Google Client ID is not configured. Please add VITE_GOOGLE_CLIENT_ID to your .env file.");
      } else {
        alert("Google Client ID is not configured. Please add VITE_GOOGLE_CLIENT_ID to your .env file.");
      }
    }
  };

  return (
    <div className="w-full flex justify-center">
      {/* If Google Client ID is configured and GSI rendered successfully, show Google's official widget */}
      {clientId && isGsiLoaded ? (
        <div ref={buttonRef} className="w-full flex justify-center" />
      ) : (
        /* Otherwise show high-fidelity branded Google button */
        <button
          type="button"
          onClick={handleCustomGoogleClick}
          disabled={disabled}
          className="w-full flex items-center justify-center gap-2.5 h-11 px-4 border border-gray-200 rounded-2xl bg-white hover:bg-gray-50 text-xs font-semibold text-gray-700 transition-colors shadow-2xs cursor-pointer disabled:opacity-50"
        >
          <svg className="w-4 h-4" viewBox="0 0 24 24">
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
          <span>Continue with Google</span>
        </button>
      )}
    </div>
  );
};