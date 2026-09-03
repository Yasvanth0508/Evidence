import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { GoogleAuthButton } from "@/components/auth/GoogleAuthButton";
import { ThemeToggle } from "@/components/common/ThemeToggle";
import { authService } from "@/services/authService";
import { useAuthStore, UserRole } from "@/store/authStore";
import {
  User as UserIcon,
  Mail,
  Lock,
  Eye,
  EyeOff,
  ArrowRight,
  ShieldCheck,
  UserPlus,
  Loader2,
  ChevronDown,
  UserCheck,
} from "lucide-react";

export const SignupPage = () => {
  const navigate = useNavigate();
  const loginToStore = useAuthStore((state) => state.login);

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [role, setRole] = useState<UserRole>("CANDIDATE");

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
    if (!email.trim()) {
      setErrorMessage("Please enter your email address.");
      return;
    }
    if (!password) {
      setErrorMessage("Please create a password.");
      return;
    }
    if (password.length < 6) {
      setErrorMessage("Password must be at least 6 characters long.");
      return;
    }
    if (password !== confirmPassword) {
      setErrorMessage("Passwords do not match. Please verify your password.");
      return;
    }

    setIsLoading(true);

    try {
      const response = await authService.register({
        name: fullName.trim(),
        email: email.trim().toLowerCase(),
        password,
        role,
      });

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
        throw new Error("Registration succeeded but no session token was received.");
      }
    } catch (err: any) {
      console.warn("Registration error:", err);
      setErrorMessage(
        err?.response?.data?.message ||
        err?.message ||
        "Registration failed. An account with this email may already exist."
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
      console.warn("Google signup failed:", err);
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
          "Google authentication failed. Please try again."
        );
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] dark:bg-[#0B0F19] text-gray-900 dark:text-slate-100 flex flex-col justify-between font-sans transition-colors duration-200">
      {/* 1. Header Navigation */}
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
            <Link to="/login">
              <Button variant="outline" size="sm" className="font-semibold text-gray-700 dark:text-slate-200 hover:text-gray-900 dark:hover:text-white border-gray-200 dark:border-slate-700">
                Sign In
              </Button>
            </Link>

            {/* Dark Mode Switcher */}
            <ThemeToggle size="sm" />
          </div>
        </div>
      </header>

      {/* 2. Main Signup Form Card */}
      <main className="flex-1 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-lg w-full space-y-6">
          <div className="bg-white dark:bg-slate-900 border border-gray-200/90 dark:border-slate-800 rounded-3xl p-8 sm:p-10 shadow-sm transition-colors">
            {/* Top Icon Badge */}
            <div className="flex justify-center mb-3">
              <div className="w-12 h-12 rounded-2xl bg-primary-light dark:bg-primary/20 border border-primary-border dark:border-primary/30 flex items-center justify-center text-primary dark:text-primary shadow-2xs">
                <UserPlus className="w-5 h-5" />
              </div>
            </div>

            {/* Title & Subtitle */}
            <div className="text-center space-y-1 mb-6">
              <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 dark:text-white tracking-tight">
                Create Your Account
              </h1>
              <p className="text-xs sm:text-sm text-gray-500 dark:text-slate-400">
                Join the platform as a Candidate or Recruiter
              </p>
            </div>

            {/* Error Message Alert */}
            {errorMessage && (
              <div className="mb-6 p-3 rounded-xl bg-rose-50 dark:bg-rose-950/50 border border-rose-200 dark:border-rose-900/50 text-xs text-rose-700 dark:text-rose-400 font-medium">
                {errorMessage}
              </div>
            )}

            {/* Role Selection Dropdown Input */}
            <div className="mb-6 space-y-1.5">
              <label className="text-xs font-semibold text-gray-700 dark:text-slate-300 block">
                Account Type / Role <span className="text-primary">*</span>
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400 dark:text-slate-500">
                  <UserCheck className="w-4 h-4" />
                </div>
                <select
                  value={role}
                  onChange={(e) => setRole(e.target.value as UserRole)}
                  className="w-full h-11 pl-10 pr-10 rounded-2xl border border-gray-200 dark:border-slate-700 bg-gray-50/50 dark:bg-slate-800 hover:bg-white dark:hover:bg-slate-750 focus:bg-white dark:focus:bg-slate-800 text-xs font-semibold text-gray-800 dark:text-slate-200 focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary transition-colors appearance-none cursor-pointer shadow-2xs"
                >
                  <option value="CANDIDATE">Candidate — Take assessments, submit code & view scores</option>
                  <option value="RECRUITER">Recruiter — Manage workspaces, schedule tests & view candidate reports</option>
                </select>
                <div className="absolute inset-y-0 right-0 pr-3.5 flex items-center pointer-events-none text-gray-400 dark:text-slate-500">
                  <ChevronDown className="w-4 h-4" />
                </div>
              </div>
            </div>

            {/* Google Signup Button */}
            <div className="mb-6">
              <GoogleAuthButton
                text="signup_with"
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
                <span className="bg-white dark:bg-slate-900 px-3 text-gray-400 dark:text-slate-500 font-medium transition-colors">or register with email</span>
              </div>
            </div>

            {/* Unified Single Registration Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Full Name */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700 dark:text-slate-300 block">
                  Full Name
                </label>
                <Input
                  type="text"
                  required
                  placeholder="e.g. Alex Morgan"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  icon={<UserIcon className="w-4 h-4 text-gray-400" />}
                />
              </div>

              {/* Email Address */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700 block">
                  Email Address
                </label>
                <Input
                  type="email"
                  required
                  placeholder="alex@example.com"
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
                  placeholder="Create a strong password (min 6 characters)"
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

              {/* Confirm Password */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700 dark:text-slate-300 block">
                  Confirm Password
                </label>
                <Input
                  type={showConfirmPassword ? "text" : "password"}
                  required
                  placeholder="Repeat password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  icon={<Lock className="w-4 h-4 text-gray-400" />}
                  endIcon={
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      className="text-gray-400 hover:text-gray-600 dark:hover:text-slate-200 focus:outline-none cursor-pointer"
                    >
                      {showConfirmPassword ? (
                        <EyeOff className="w-4 h-4" />
                      ) : (
                        <Eye className="w-4 h-4" />
                      )}
                    </button>
                  }
                />
              </div>

              {/* Submit Button */}
              <Button
                type="submit"
                disabled={isLoading}
                size="lg"
                className="w-full font-bold gap-2 shadow-xs mt-4 bg-primary hover:bg-primary-hover text-white cursor-pointer"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" /> Creating Account...
                  </>
                ) : (
                  <>
                    Create Account <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </Button>
            </form>

            {/* Switch to Login */}
            <div className="mt-8 text-center text-xs text-gray-500 dark:text-slate-400">
              Already have an account?{" "}
              <Link
                to="/login"
                className="font-bold text-primary hover:underline"
              >
                Sign In
              </Link>
            </div>
          </div>

          {/* Security Badge */}
          <div className="flex items-center justify-center gap-1.5 text-xs text-gray-400 dark:text-slate-500">
            <ShieldCheck className="w-4 h-4 text-gray-400 dark:text-slate-500" />
            <span>Secured with role-based access control</span>
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
              Please select whether you are joining as a Candidate or a Recruiter to continue with Google.
            </p>
            <div className="grid grid-cols-2 gap-3 mt-4">
              <button
                type="button"
                onClick={() => handleGoogleSuccess(pendingGoogleCredential, "CANDIDATE")}
                className="p-3 rounded-2xl border border-gray-200 dark:border-slate-700 hover:border-emerald-500 bg-gray-50/50 dark:bg-slate-800 hover:bg-emerald-50/40 dark:hover:bg-emerald-950/40 transition-all text-center cursor-pointer"
              >
                <h4 className="font-bold text-xs text-gray-900 dark:text-white">Candidate</h4>
                <p className="text-[10px] text-gray-400 dark:text-slate-500 mt-0.5">Take assessments</p>
              </button>
              <button
                type="button"
                onClick={() => handleGoogleSuccess(pendingGoogleCredential, "RECRUITER")}
                className="p-3 rounded-2xl border border-gray-200 dark:border-slate-700 hover:border-primary bg-gray-50/50 dark:bg-slate-800 hover:bg-primary-light dark:hover:bg-primary/20 transition-all text-center cursor-pointer"
              >
                <h4 className="font-bold text-xs text-gray-900 dark:text-white">Recruiter</h4>
                <p className="text-[10px] text-gray-400 dark:text-slate-500 mt-0.5">Create & manage drives</p>
              </button>
            </div>
            <Button
              type="button"
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
      <footer className="bg-white dark:bg-slate-950 border-t border-gray-200 dark:border-slate-800 py-6 transition-colors">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-400 dark:text-slate-500 gap-4">
          <p>© 2026 EVIDENCE. Unified Technical Assessment Platform.</p>
          <div className="flex space-x-6">
            <Link to="/#overview" className="hover:text-gray-600 dark:hover:text-slate-300">Home</Link>
            <Link to="/#how-it-works" className="hover:text-gray-600 dark:hover:text-slate-300">How It Works</Link>
            <Link to="/#portals" className="hover:text-gray-600 dark:hover:text-slate-300">Role Portals</Link>
          </div>
        </div>
      </footer>
    </div>
  );
};