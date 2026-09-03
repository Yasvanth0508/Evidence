import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import { GoogleAuthButton } from "@/components/auth/GoogleAuthButton";
import { AuthTeamIllustration } from "@/components/auth/AuthTeamIllustration";
import { authService } from "@/services/authService";
import { useAuthStore, UserRole } from "@/store/authStore";
import {
  Eye,
  EyeOff,
  Loader2,
  AlertCircle,
  ArrowLeft,
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
      setErrorMessage("Please enter your business email address.");
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
          },
          response.token
        );

        redirectByRole(response.role);
      }
    } catch (err: any) {
      console.warn("Google authentication failed:", err);
      const msg = err?.response?.data?.message || err?.message || "";
      if (msg.includes("ROLE_REQUIRED") || msg.includes("Please select whether you are a Candidate or Recruiter")) {
        setPendingGoogleCredential(credential);
      } else {
        setErrorMessage(
          err?.response?.data?.message ||
          "Google sign-in failed. Please try again or use email login."
        );
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-[#EAEEF4] p-0 sm:p-4 lg:p-6 font-sans text-slate-900 selection:bg-orange-100 selection:text-orange-900 antialiased">
      {/* Main Symmetrical Container Card matching reference split design */}
      <div className="w-full max-w-[1200px] min-h-screen sm:min-h-[640px] lg:h-[88vh] max-h-[850px] bg-white rounded-none sm:rounded-3xl border sm:border-slate-200/90 shadow-2xl flex flex-col lg:flex-row overflow-hidden">
        {/* =========================================================================
            LEFT COLUMN: PROMO BANNER & TEAM COLLABORATION ILLUSTRATION (50% EQUAL SPACE)
            ========================================================================= */}
        <div className="w-full lg:w-1/2 flex-1 bg-[#F3F6F9] border-b lg:border-b-0 lg:border-r border-slate-200/80 flex flex-col justify-between p-6 sm:p-10 lg:p-12 relative overflow-hidden">
          {/* Top Utility: Back to home */}
          <div className="flex items-center justify-between w-full">
            <Link
              to="/"
              className="inline-flex items-center gap-1.5 text-xs font-medium text-slate-500 hover:text-slate-800 transition-colors py-1 px-2.5 rounded-lg hover:bg-slate-200/60"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>Back to home</span>
            </Link>
          </div>

          {/* Center Content: Symmetrical max-w-[440px] */}
          <div className="my-auto text-center flex flex-col items-center max-w-[440px] mx-auto w-full py-4">
            <h2 className="text-2xl sm:text-3xl lg:text-[32px] font-bold text-slate-800 leading-tight">
              Try Evidence free for 15 Days
            </h2>
            <p className="mt-2.5 text-xs sm:text-sm text-slate-500 leading-relaxed max-w-sm">
              Get a chance to explore the product to its fullest before choosing your ideal plan.
            </p>

            {/* Custom Collaborative Team Illustration */}
            <div className="w-full mt-6 sm:mt-8">
              <AuthTeamIllustration />
            </div>
          </div>

          {/* Subtle bottom spacing to keep exact vertical balance */}
          <div className="h-4" />
        </div>

        {/* =========================================================================
            RIGHT COLUMN: AUTHENTICATION FORM (50% EQUAL SPACE)
            ========================================================================= */}
        <div className="w-full lg:w-1/2 flex-1 bg-white flex flex-col justify-center items-center p-6 sm:p-10 lg:p-12 overflow-y-auto">
          <div className="w-full max-w-[440px] mx-auto my-auto flex flex-col items-center">
            {/* 1. Centered Brand Logo */}
            <div className="mb-4">
              <Logo size="lg" showSubtitle={false} to="/" />
            </div>

            {/* 2. Heading & Subtitle */}
            <h1 className="text-2xl sm:text-[26px] font-bold text-slate-800 text-center">
              Sign In to your account
            </h1>
            <p className="text-xs sm:text-sm text-slate-500 text-center mt-1.5 mb-6">
              Don't have an account?{" "}
              <Link
                to="/signup"
                className="font-semibold text-[#F05323] hover:underline ml-0.5"
              >
                Sign Up
              </Link>
            </p>

            {/* Error Message Alert */}
            {errorMessage && (
              <div className="w-full mb-4 p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700 font-medium flex items-start gap-2 animate-in fade-in duration-200">
                <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0 mt-0.5" />
                <div className="flex-1 leading-relaxed text-left">{errorMessage}</div>
              </div>
            )}

            {/* 3. Input Form Fields */}
            <form onSubmit={handleSubmit} className="w-full space-y-4 text-left">
              {/* Business Email Field */}
              <div>
                <label className="text-xs font-semibold text-slate-700 block mb-1.5">
                  Business Email
                </label>
                <input
                  type="email"
                  required
                  placeholder="Input your business email address"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full h-11 sm:h-12 px-4 rounded-xl bg-[#F1F5F9] border border-transparent hover:border-slate-200 focus:border-[#F05323] focus:bg-white focus:ring-2 focus:ring-[#F05323]/20 text-xs sm:text-sm text-slate-800 placeholder:text-slate-400 outline-none transition-all shadow-2xs"
                />
              </div>

              {/* Password Field */}
              <div>
                <label className="text-xs font-semibold text-slate-700 block mb-1.5">
                  Password
                </label>
                <div className="relative flex items-center">
                  <input
                    type={showPassword ? "text" : "password"}
                    required
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full h-11 sm:h-12 px-4 pr-11 rounded-xl bg-[#F1F5F9] border border-transparent hover:border-slate-200 focus:border-[#F05323] focus:bg-white focus:ring-2 focus:ring-[#F05323]/20 text-xs sm:text-sm text-slate-800 placeholder:text-slate-400 outline-none transition-all shadow-2xs"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    aria-label={showPassword ? "Hide password" : "Show password"}
                    className="absolute right-3.5 text-slate-400 hover:text-slate-600 transition-colors p-1 cursor-pointer focus:outline-none"
                  >
                    {showPassword ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              {/* Remember Me & Forgot Password Row */}
              <div className="flex items-center justify-between text-xs pt-0.5">
                <label className="flex items-center gap-2 cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    className="w-4 h-4 rounded border-slate-300 text-[#F05323] focus:ring-[#F05323] accent-[#F05323] cursor-pointer"
                  />
                  <span className="text-slate-600 font-medium">Remember me</span>
                </label>

                <span className="font-medium text-slate-500 hover:text-slate-800 transition-colors cursor-pointer">
                  Forgot Password?
                </span>
              </div>

              {/* 4. Prominent Submit CTA Button (Evidence Orange Theme) */}
              <Button
                type="submit"
                disabled={isLoading}
                className="w-full h-11 sm:h-12 rounded-xl bg-[#F05323] hover:bg-[#D94418] text-white font-bold text-xs sm:text-sm uppercase tracking-wider shadow-sm hover:shadow-md active:scale-[0.99] transition-all flex items-center justify-center cursor-pointer mt-5"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin mr-2" /> Signing In...
                  </>
                ) : (
                  "SIGN IN"
                )}
              </Button>
            </form>

            {/* 5. Divider with "Or sign in with" */}
            <div className="w-full relative flex items-center justify-center my-6">
              <div className="w-full border-t border-slate-200" />
              <span className="absolute bg-white px-3 text-[11px] sm:text-xs text-slate-400 font-medium">
                Or sign in with
              </span>
            </div>

            {/* 6. ONLY CONTINUE WITH GOOGLE BUTTON */}
            <div className="w-full flex justify-center">
              <GoogleAuthButton
                text="continue_with"
                disabled={isLoading}
                onSuccess={handleGoogleSuccess}
                onError={(err) => setErrorMessage(err)}
              />
            </div>

            {/* 7. Footer Disclaimer */}
            <p className="text-[11px] text-slate-500 text-center mt-7 leading-relaxed">
              By signing in, you agree to our{" "}
              <a href="#" className="font-semibold text-slate-700 hover:underline">
                Terms of Use
              </a>{" "}
              and{" "}
              <a href="#" className="font-semibold text-slate-700 hover:underline">
                Privacy Policy
              </a>
            </p>
          </div>
        </div>
      </div>

      {/* =========================================================================
          ROLE SELECTION MODAL (For New Google Users)
          ========================================================================= */}
      {pendingGoogleCredential && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-sm w-full p-6 shadow-2xl border border-slate-100 space-y-4 animate-in zoom-in-95 duration-200">
            <h3 className="text-lg font-bold text-slate-900 text-center">
              Complete Your Registration
            </h3>
            <p className="text-xs text-slate-500 text-center leading-relaxed">
              Please select whether you are a Candidate or Recruiter to continue.
            </p>
            <div className="grid grid-cols-2 gap-3 mt-4">
              <button
                type="button"
                onClick={() => handleGoogleSuccess(pendingGoogleCredential, "CANDIDATE")}
                className="p-3.5 rounded-2xl border border-slate-200 hover:border-emerald-500 bg-slate-50/50 hover:bg-emerald-50/40 transition-all text-center cursor-pointer group"
              >
                <h4 className="font-bold text-xs text-slate-900 group-hover:text-emerald-700">Candidate</h4>
                <p className="text-[10px] text-slate-400 mt-1">Take assessments</p>
              </button>
              <button
                type="button"
                onClick={() => handleGoogleSuccess(pendingGoogleCredential, "RECRUITER")}
                className="p-3.5 rounded-2xl border border-slate-200 hover:border-[#F05323] bg-slate-50/50 hover:bg-orange-50/40 transition-all text-center cursor-pointer group"
              >
                <h4 className="font-bold text-xs text-slate-900 group-hover:text-[#F05323]">Recruiter</h4>
                <p className="text-[10px] text-slate-400 mt-1">Manage workspaces</p>
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
    </div>
  );
};

export default LoginPage;