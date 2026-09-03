import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
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
  ChevronDown,
} from "lucide-react";

export const SignupPage = () => {
  const navigate = useNavigate();
  const loginToStore = useAuthStore((state) => state.login);

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [role, setRole] = useState<UserRole>("CANDIDATE");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const redirectByRole = (assignedRole: UserRole) => {
    if (assignedRole === "RECRUITER") {
      navigate("/dashboard", { replace: true });
    } else {
      navigate("/candidate/dashboard", { replace: true });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    if (!fullName.trim()) {
      setErrorMessage("Please enter your full name.");
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.trim() || !emailRegex.test(email.trim())) {
      setErrorMessage("Please enter a valid business email address.");
      return;
    }

    if (!password || password.length < 6) {
      setErrorMessage("Password must be at least 6 characters long.");
      return;
    }

    if (password !== confirmPassword) {
      setErrorMessage("Passwords do not match. Please verify your password.");
      return;
    }

    setIsLoading(true);

    try {
      const response = await authService.signup(
        fullName.trim(),
        email.trim().toLowerCase(),
        password,
        role
      );

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
        throw new Error("Registration succeeded but no token was returned.");
      }
    } catch (err: any) {
      console.warn("Signup failed:", err);
      setErrorMessage(
        err?.response?.data?.message ||
        err?.message ||
        "Registration failed. A user with this email may already exist."
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleSuccess = async (credential: string) => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const response = await authService.googleAuth(credential, role);

      if (response && response.token) {
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
      console.warn("Google signup failed:", err);
      setErrorMessage(
        err?.response?.data?.message ||
        "Google authentication failed. Please try again."
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-[#EAEEF4] p-0 sm:p-4 lg:p-6 font-sans text-slate-900 selection:bg-orange-100 selection:text-orange-900 antialiased">
      {/* Main Symmetrical Container Card */}
      <div className="w-full max-w-[1200px] min-h-screen sm:min-h-[640px] lg:h-[90vh] max-h-[920px] bg-white rounded-none sm:rounded-3xl border sm:border-slate-200/90 shadow-2xl flex flex-col lg:flex-row overflow-hidden">
        {/* =========================================================================
            LEFT COLUMN: EQUAL 50% SPACE (PROMO BANNER & TEAM ILLUSTRATION)
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
            RIGHT COLUMN: EQUAL 50% SPACE (SIGN UP FORM WITH GOOGLE-ONLY ACTION)
            ========================================================================= */}
        <div className="w-full lg:w-1/2 flex-1 bg-white flex flex-col justify-center items-center p-6 sm:p-10 lg:p-12 overflow-y-auto">
          {/* Inner Content: Symmetrical max-w-[440px] */}
          <div className="w-full max-w-[440px] mx-auto my-auto flex flex-col items-center">
            {/* 1. Centered Brand Logo */}
            <div className="mb-3">
              <Logo size="lg" showSubtitle={false} to="/" />
            </div>

            {/* 2. Heading & Subtitle */}
            <h1 className="text-2xl sm:text-[26px] font-bold text-slate-800 text-center">
              Sign Up to your account
            </h1>
            <p className="text-xs sm:text-sm text-slate-500 text-center mt-1.5 mb-5">
              Already have an account?{" "}
              <Link
                to="/login"
                className="font-semibold text-[#F05323] hover:underline ml-0.5"
              >
                Sign In
              </Link>
            </p>

            {/* Error Message Alert */}
            {errorMessage && (
              <div className="w-full mb-4 p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700 font-medium flex items-start gap-2 animate-in fade-in duration-200">
                <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0 mt-0.5" />
                <div className="flex-1 leading-relaxed text-left">{errorMessage}</div>
              </div>
            )}

            {/* 3. Signup Form */}
            <form onSubmit={handleSubmit} className="w-full space-y-3.5 text-left">
              {/* Full Name */}
              <div>
                <label className="text-xs font-semibold text-slate-700 block mb-1">
                  Full Name
                </label>
                <input
                  type="text"
                  required
                  placeholder="Input your full name"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  className="w-full h-11 sm:h-12 px-4 rounded-xl bg-[#F1F5F9] border border-transparent hover:border-slate-200 focus:border-[#F05323] focus:bg-white focus:ring-2 focus:ring-[#F05323]/20 text-xs sm:text-sm text-slate-800 placeholder:text-slate-400 outline-none transition-all shadow-2xs"
                />
              </div>

              {/* Business Email */}
              <div>
                <label className="text-xs font-semibold text-slate-700 block mb-1">
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

              {/* Role */}
              <div>
                <label className="text-xs font-semibold text-slate-700 block mb-1">
                  Account Type
                </label>
                <div className="relative flex items-center">
                  <select
                    value={role}
                    onChange={(e) => setRole(e.target.value as UserRole)}
                    className="w-full h-11 sm:h-12 px-4 pr-10 rounded-xl bg-[#F1F5F9] border border-transparent hover:border-slate-200 focus:border-[#F05323] focus:bg-white focus:ring-2 focus:ring-[#F05323]/20 text-xs sm:text-sm font-medium text-slate-800 outline-none transition-all cursor-pointer shadow-2xs appearance-none"
                  >
                    <option value="CANDIDATE">Candidate</option>
                    <option value="RECRUITER">Recruiter</option>
                  </select>
                  <ChevronDown className="w-4 h-4 text-slate-400 absolute right-3.5 pointer-events-none" />
                </div>
              </div>

              {/* Password */}
              <div>
                <label className="text-xs font-semibold text-slate-700 block mb-1">
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

              {/* Confirm Password */}
              <div>
                <label className="text-xs font-semibold text-slate-700 block mb-1">
                  Confirm Password
                </label>
                <div className="relative flex items-center">
                  <input
                    type={showConfirmPassword ? "text" : "password"}
                    required
                    placeholder="Confirm password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="w-full h-11 sm:h-12 px-4 pr-11 rounded-xl bg-[#F1F5F9] border border-transparent hover:border-slate-200 focus:border-[#F05323] focus:bg-white focus:ring-2 focus:ring-[#F05323]/20 text-xs sm:text-sm text-slate-800 placeholder:text-slate-400 outline-none transition-all shadow-2xs"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    aria-label={showConfirmPassword ? "Hide password" : "Show password"}
                    className="absolute right-3.5 text-slate-400 hover:text-slate-600 transition-colors p-1 cursor-pointer focus:outline-none"
                  >
                    {showConfirmPassword ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              {/* Primary Sign Up Button (Evidence Orange Theme) */}
              <Button
                type="submit"
                disabled={isLoading}
                className="w-full h-11 sm:h-12 rounded-xl bg-[#F05323] hover:bg-[#D94418] text-white font-bold text-xs sm:text-sm uppercase tracking-wider shadow-sm hover:shadow-md active:scale-[0.99] transition-all flex items-center justify-center cursor-pointer mt-4"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin mr-2" /> Creating Account...
                  </>
                ) : (
                  "SIGN UP"
                )}
              </Button>
            </form>

            {/* 4. Divider */}
            <div className="w-full relative flex items-center justify-center my-4 sm:my-5">
              <div className="w-full border-t border-slate-200" />
              <span className="absolute bg-white px-3 text-[11px] sm:text-xs text-slate-400 font-medium">
                Or sign up with
              </span>
            </div>

            {/* 5. ONLY CONTINUE WITH GOOGLE BUTTON */}
            <div className="w-full flex justify-center">
              <GoogleAuthButton
                text="continue_with"
                disabled={isLoading}
                onSuccess={handleGoogleSuccess}
                onError={(err) => setErrorMessage(err)}
              />
            </div>

            {/* 6. Terms and Privacy Disclaimer */}
            <p className="text-[11px] text-slate-500 text-center mt-5 leading-relaxed">
              By signing up, you agree to our{" "}
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
    </div>
  );
};

export default SignupPage;