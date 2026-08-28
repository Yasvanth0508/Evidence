import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { GoogleAuthButton } from "@/components/auth/GoogleAuthButton";
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

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.trim() || !emailRegex.test(email.trim())) {
      setErrorMessage("Please enter a valid email address.");
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
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col justify-between font-sans">
      {/* 1. Header Navigation */}
      <header className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-gray-100 shadow-xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <Logo size="md" />

          <nav className="hidden md:flex items-center space-x-8 text-sm font-medium text-gray-600">
            <Link to="/#overview" className="hover:text-gray-900 transition-colors">
              Platform Overview
            </Link>
            <Link to="/#how-it-works" className="hover:text-gray-900 transition-colors">
              How It Works
            </Link>
            <Link to="/#architecture" className="hover:text-gray-900 transition-colors">
              Key Capabilities
            </Link>
            <Link to="/#portals" className="hover:text-gray-900 transition-colors">
              Role Portals
            </Link>
          </nav>

          <div className="flex items-center space-x-3">
            <Link to="/login">
              <Button variant="outline" size="sm" className="font-semibold text-gray-700 hover:text-gray-900 border-gray-200">
                Sign In
              </Button>
            </Link>
          </div>
        </div>
      </header>

      {/* 2. Main Signup Form Card */}
      <main className="flex-1 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-lg w-full space-y-6">
          <div className="bg-white border border-gray-200/90 rounded-3xl p-8 sm:p-10 shadow-sm">
            {/* Top Icon Badge */}
            <div className="flex justify-center mb-3">
              <div className="w-12 h-12 rounded-2xl bg-orange-50 border border-orange-100 flex items-center justify-center text-[#F05323] shadow-2xs">
                <UserPlus className="w-5 h-5" />
              </div>
            </div>

            {/* Title & Subtitle */}
            <div className="text-center space-y-1 mb-6">
              <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
                Create Your Account
              </h1>
              <p className="text-xs sm:text-sm text-gray-500">
                Join the platform as a Candidate or Recruiter
              </p>
            </div>

            {/* Error Message Alert */}
            {errorMessage && (
              <div className="mb-6 p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700 font-medium">
                {errorMessage}
              </div>
            )}

            {/* Role Selection Dropdown Input */}
            <div className="mb-6 space-y-1.5">
              <label className="text-xs font-semibold text-gray-700 block">
                Account Type / Role <span className="text-[#F05323]">*</span>
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
                  <UserCheck className="w-4 h-4" />
                </div>
                <select
                  value={role}
                  onChange={(e) => setRole(e.target.value as UserRole)}
                  className="w-full h-11 pl-10 pr-10 rounded-2xl border border-gray-200 bg-gray-50/50 hover:bg-white focus:bg-white text-xs font-semibold text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#F05323] focus:border-[#F05323] transition-colors appearance-none cursor-pointer shadow-2xs"
                >
                  <option value="CANDIDATE">Candidate — Take assessments, submit code & view scores</option>
                  <option value="RECRUITER">Recruiter — Manage workspaces, schedule tests & view candidate reports</option>
                </select>
                <div className="absolute inset-y-0 right-0 pr-3.5 flex items-center pointer-events-none text-gray-400">
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
                <div className="w-full border-t border-gray-200" />
              </div>
              <div className="relative flex justify-center text-xs text-gray-500">
                <span className="bg-white px-3 text-gray-400 font-medium">or register with email</span>
              </div>
            </div>

            {/* Unified Single Registration Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Full Name */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700 block">
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
                <label className="text-xs font-semibold text-gray-700 block">
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
                      className="text-gray-400 hover:text-gray-600 focus:outline-none"
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
                <label className="text-xs font-semibold text-gray-700 block">
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
                      className="text-gray-400 hover:text-gray-600 focus:outline-none"
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
                className="w-full font-bold gap-2 shadow-xs mt-4 bg-[#F05323] hover:bg-[#d94417] text-white cursor-pointer"
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
            <div className="mt-8 text-center text-xs text-gray-500">
              Already have an account?{" "}
              <Link
                to="/login"
                className="font-bold text-[#F05323] hover:underline"
              >
                Sign In
              </Link>
            </div>
          </div>

          {/* Security Badge */}
          <div className="flex items-center justify-center gap-1.5 text-xs text-gray-400">
            <ShieldCheck className="w-4 h-4 text-gray-400" />
            <span>Secured with role-based access control</span>
          </div>
        </div>
      </main>

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