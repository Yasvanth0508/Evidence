import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { GoogleAuthButton } from "@/components/auth/GoogleAuthButton";
import { ThemeToggle } from "@/components/common/ThemeToggle";
import { authService } from "@/services/authService";
import { useAuthStore, UserRole } from "@/store/authStore";
import {
  Mail,
  Lock,
  Eye,
  EyeOff,
  ArrowRight,
  ShieldCheck,
  Loader2,
  Sparkles,
} from "lucide-react";

export const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const loginToStore = useAuthStore((state) => state.login);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const redirectByRole = (role: "RECRUITER" | "CANDIDATE") => {
    const fromPath = (location.state as any)?.from?.pathname;
    if (fromPath && !fromPath.includes("/login") && !fromPath.includes("/signup")) {
      navigate(fromPath, { replace: true });
      return;
    }

    if (role === "RECRUITER") {
      navigate("/dashboard", { replace: true });
    } else {
      navigate("/candidate/dashboard", { replace: true });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    if (!email.trim()) {
      setErrorMessage("Please enter your email address.");
      return;
    }
    if (!password) {
      setErrorMessage("Please enter your password.");
      return;
    }

    setIsLoading(true);

    try {
      const response = await authService.login(email.trim().toLowerCase(), password);

      if (response && response.token) {
        loginToStore(
          {
            id: response.id,
            name: response.name,
            email: response.email,
            role: response.role,
            authProvider: response.authProvider || "LOCAL",
            avatarUrl: response.avatarUrl,
          },
          response.token
        );

        redirectByRole(response.role);
      } else {
        throw new Error("Authentication failed: No token received from server.");
      }
    } catch (err: any) {
      console.warn("Login failed:", err);
      setErrorMessage(
        err?.response?.data?.message ||
        err?.message ||
        "Invalid email or password. Please check your credentials."
      );
    } finally {
      setIsLoading(false);
    }
  };

  const [pendingGoogleCredential, setPendingGoogleCredential] = useState<string | null>(null);

  const handleGoogleSuccess = async (credential: string, selectedRole?: UserRole) => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const response = await authService.googleAuth(credential, selectedRole);

      if (response && response.token) {
        setPendingGoogleCredential(null);
        loginToStore(
          {
            id: response.id,
            name: response.name,
            email: response.email,
            role: response.role,
            authProvider: response.authProvider || "GOOGLE",
            avatarUrl: response.avatarUrl,
          },
          response.token
        );

        redirectByRole(response.role);
      }
    } catch (err: any) {
      console.warn("Google authentication failed:", err);
      const isRoleRequired =
        err?.errorCode === "ROLE_REQUIRED" ||
        err?.response?.data?.errorCode === "ROLE_REQUIRED" ||
        err?.message?.includes("ROLE_REQUIRED") ||
        err?.message?.includes("Please select whether you are a Candidate or Recruiter");

      if (isRoleRequired) {
        setPendingGoogleCredential(credential);
      } else {
        setErrorMessage(
          err?.response?.data?.message ||
          err?.message ||
          "Google sign-in failed. Please try again or use email login."
        );
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] dark:bg-[#0B0F19] text-gray-900 dark:text-slate-100 flex flex-col justify-between font-sans transition-colors duration-200">
      {/* 1. Header */}
      <header className="sticky top-0 z-50 bg-white/90 dark:bg-slate-950/90 backdrop-blur-md border-b border-gray-100 dark:border-slate-800 shadow-xs transition-colors">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <Logo size="md" />

          <nav className="hidden md:flex items-center space-x-8 text-sm font-medium text-gray-600 dark:text-slate-400">
            <Link to="/#overview" className="hover:text-gray-900 dark:hover:text-white transition-colors">
              Platform Overview
            </Link>
            <Link to="/#how-it-works" className="hover:text-gray-900 dark:hover:text-white transition-colors">
              How It Works
            </Link>
            <Link to="/#architecture" className="hover:text-gray-900 dark:hover:text-white transition-colors">
              Key Capabilities
            </Link>
            <Link to="/#portals" className="hover:text-gray-900 dark:hover:text-white transition-colors">
              Role Portals
            </Link>
          </nav>

          <div className="flex items-center space-x-3">
            <Link to="/signup">
              <Button size="sm" className="font-semibold shadow-xs bg-primary hover:bg-primary-hover text-white">
                Create Account
              </Button>
            </Link>

            {/* Dark Mode Switcher */}
            <ThemeToggle size="sm" />
          </div>
        </div>
      </header>

      {/* 2. Main Unified Login Card */}
      <main className="flex-1 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-6">
          <div className="bg-white dark:bg-slate-900 border border-gray-200/90 dark:border-slate-800 rounded-3xl p-8 sm:p-10 shadow-sm transition-colors">
            {/* Title & Subtitle */}
            <div className="text-center space-y-1.5 mb-8">
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-bold bg-primary-light dark:bg-primary/20 text-primary dark:text-primary border border-primary-border dark:border-primary/30 mb-2">
                <Sparkles className="w-3.5 h-3.5 text-primary" />
                Evidence Unified Access
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 dark:text-white tracking-tight">
                Welcome Back
              </h1>
              <p className="text-xs sm:text-sm text-gray-500 dark:text-slate-400">
                Sign in to your account to continue to your dashboard.
              </p>
            </div>

            {/* Error Message Alert */}
            {errorMessage && (
              <div className="mb-6 p-3 rounded-xl bg-rose-50 dark:bg-rose-950/50 border border-rose-200 dark:border-rose-900/50 text-xs text-rose-700 dark:text-rose-400 font-medium">
                {errorMessage}
              </div>
            )}

            {/* Google OAuth Button */}
            <div className="mb-6">
              <GoogleAuthButton
                text="continue_with"
                disabled={isLoading}
                onSuccess={handleGoogleSuccess}
                onError={(err) => setErrorMessage(err)}
              />
            </div>

            {/* Divider */}
            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-200 dark:border-slate-800" />
              </div>
              <div className="relative flex justify-center text-xs text-gray-500 dark:text-slate-400">
                <span className="bg-white dark:bg-slate-900 px-3 text-gray-400 dark:text-slate-500 font-medium transition-colors">or continue with email</span>
              </div>
            </div>

            {/* Email / Password Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Email Address */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700 dark:text-slate-300 block">
                  Email Address
                </label>
                <Input
                  type="email"
                  required
                  placeholder="Enter your email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  icon={<Mail className="w-4 h-4 text-gray-400" />}
                />
              </div>

              {/* Password */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700 dark:text-slate-300 block">
                  Password
                </label>
                <Input
                  type={showPassword ? "text" : "password"}
                  required
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  icon={<Lock className="w-4 h-4 text-gray-400" />}
                  endIcon={
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="text-gray-400 hover:text-gray-600 dark:hover:text-slate-200 focus:outline-none cursor-pointer"
                    >
                      {showPassword ? (
                        <EyeOff className="w-4 h-4" />
                      ) : (
                        <Eye className="w-4 h-4" />
                      )}
                    </button>
                  }
                />
              </div>

              {/* Remember Me & Forgot Password */}
              <div className="flex items-center justify-between text-xs pt-1">
                <label className="flex items-center gap-2 cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary accent-primary"
                  />
                  <span className="text-gray-700 dark:text-slate-300 font-medium">Remember me</span>
                </label>

                <span className="font-medium text-gray-400 dark:text-slate-500 hover:text-gray-600 dark:hover:text-slate-300 cursor-pointer">
                  Forgot Password?
                </span>
              </div>

              {/* Submit Button */}
              <Button
                type="submit"
                disabled={isLoading}
                size="lg"
                className="w-full font-bold gap-2 shadow-xs mt-3 bg-primary hover:bg-primary-hover text-white cursor-pointer"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" /> Signing In...
                  </>
                ) : (
                  <>
                    Sign In <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </Button>
            </form>

            {/* Switch to Signup */}
            <div className="mt-8 text-center text-xs text-gray-500 dark:text-slate-400">
              Don't have an account?{" "}
              <Link
                to="/signup"
                className="font-bold text-primary hover:underline"
              >
                Create an account
              </Link>
            </div>
          </div>

          {/* Security Badge */}
          <div className="flex items-center justify-center gap-1.5 text-xs text-gray-400 dark:text-slate-500">
            <ShieldCheck className="w-4 h-4 text-gray-400 dark:text-slate-500" />
            <span>Secured with cryptographic JWT role-based access</span>
          </div>
        </div>
      </main>

      {/* Role Selection Modal for New Google Users */}
      {pendingGoogleCredential && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white dark:bg-slate-900 rounded-3xl max-w-sm w-full p-6 shadow-2xl border border-gray-100 dark:border-slate-800 space-y-4 animate-in zoom-in-95 duration-200">
            <h3 className="text-lg font-extrabold text-gray-900 dark:text-white text-center">
              Complete Your Registration
            </h3>
            <p className="text-xs text-gray-500 dark:text-slate-400 text-center">
              It looks like you are new here! Please select your role to continue with Google.
            </p>
            <div className="grid grid-cols-2 gap-3 mt-4">
              <button
                onClick={() => handleGoogleSuccess(pendingGoogleCredential, "CANDIDATE")}
                className="p-3 rounded-2xl border border-gray-200 dark:border-slate-700 hover:border-emerald-500 bg-gray-50/50 dark:bg-slate-800 hover:bg-emerald-50/40 transition-all text-center"
              >
                <h4 className="font-bold text-xs text-gray-900 dark:text-white">Candidate</h4>
              </button>
              <button
                onClick={() => handleGoogleSuccess(pendingGoogleCredential, "RECRUITER")}
                className="p-3 rounded-2xl border border-gray-200 dark:border-slate-700 hover:border-primary bg-gray-50/50 dark:bg-slate-800 hover:bg-primary-light dark:hover:bg-primary/20 transition-all text-center"
              >
                <h4 className="font-bold text-xs text-gray-900 dark:text-white">Recruiter</h4>
              </button>
            </div>
            <Button
              variant="outline"
              size="sm"
              className="w-full mt-2"
              onClick={() => setPendingGoogleCredential(null)}
            >
              Cancel
            </Button>
          </div>
        </div>
      )}

      {/* 3. Footer */}
      <footer className="bg-white border-t border-gray-200 py-6">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-400 gap-4">
          <p>© 2026 EVIDENCE. Unified Technical Assessment Platform.</p>
          <div className="flex space-x-6">
            <Link to="/#overview" className="hover:text-gray-600">Home</Link>
            <Link to="/#how-it-works" className="hover:text-gray-600">How It Works</Link>
            <Link to="/#portals" className="hover:text-gray-600">Role Portals</Link>
          </div>
        </div>
      </footer>
    </div>
  );
};