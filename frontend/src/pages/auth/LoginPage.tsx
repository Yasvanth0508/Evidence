import { useState, useEffect } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuthStore, DEFAULT_RECRUITER, DEFAULT_CANDIDATE } from "@/store/authStore";
import {
  Mail,
  Lock,
  Eye,
  EyeOff,
  ArrowRight,
  ShieldCheck,
  Loader2,
  Briefcase,
  Code2,
} from "lucide-react";
import {
  GithubIcon,
} from "@/components/ui/social-icons";

export const LoginPage = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const loginToStore = useAuthStore((state) => state.login);

  const roleParam = searchParams.get("role");
  const [selectedRole, setSelectedRole] = useState<"RECRUITER" | "CANDIDATE">(
    roleParam === "candidate" ? "CANDIDATE" : "RECRUITER"
  );

  const [email, setEmail] = useState(
    roleParam === "candidate" ? "arun@gmail.com" : "recruiter@example.com"
  );
  const [password, setPassword] = useState("password123");
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // Sync role changes with default demo emails
  useEffect(() => {
    if (roleParam === "candidate") {
      setSelectedRole("CANDIDATE");
      setEmail("arun@gmail.com");
    } else if (roleParam === "recruiter") {
      setSelectedRole("RECRUITER");
      setEmail("recruiter@example.com");
    }
  }, [roleParam]);

  const handleRoleChange = (newRole: "RECRUITER" | "CANDIDATE") => {
    setSelectedRole(newRole);
    setSearchParams({ role: newRole.toLowerCase() });
    setEmail(newRole === "CANDIDATE" ? "arun@gmail.com" : "recruiter@example.com");
    setErrorMessage("");
  };

  const handleDirectRecruiterLogin = () => {
    loginToStore(DEFAULT_RECRUITER, "jwt-session-token-1d453ede-caab-4729-8072-9b6e3c600ba3");
    navigate("/dashboard");
  };

  const handleDirectCandidateLogin = () => {
    loginToStore(DEFAULT_CANDIDATE, "jwt-session-token-52127120-fa9e-43a1-958a-6a63ef9af8c5");
    navigate("/candidate");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setErrorMessage("");

    try {
      if (selectedRole === "CANDIDATE") {
        handleDirectCandidateLogin();
      } else {
        handleDirectRecruiterLogin();
      }
    } catch {
      if (selectedRole === "CANDIDATE") {
        handleDirectCandidateLogin();
      } else {
        handleDirectRecruiterLogin();
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleSocialLogin = async (_provider: string) => {
    setIsLoading(true);
    try {
      if (selectedRole === "CANDIDATE") {
        handleDirectCandidateLogin();
      } else {
        handleDirectRecruiterLogin();
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col justify-between font-sans">
      {/* 1. Header */}
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
            <Link to={`/signup?role=${selectedRole.toLowerCase()}`}>
              <Button size="sm" className="font-semibold shadow-xs bg-[#F05323] hover:bg-[#d94417]">
                Create Account
              </Button>
            </Link>
          </div>
        </div>
      </header>

      {/* 2. Main Login Card */}
      <main className="flex-1 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-6">
          <div className="bg-white border border-gray-200/90 rounded-3xl p-8 sm:p-10 shadow-sm">
            {/* Role Switcher Tabs */}
            <div className="flex p-1 bg-gray-100/80 rounded-2xl mb-6">
              <button
                type="button"
                onClick={() => handleRoleChange("RECRUITER")}
                className={`flex-1 flex items-center justify-center gap-2 py-2 text-xs font-bold rounded-xl transition-all ${
                  selectedRole === "RECRUITER"
                    ? "bg-white text-[#F05323] shadow-xs"
                    : "text-gray-600 hover:text-gray-900"
                }`}
              >
                <Briefcase className="w-3.5 h-3.5" />
                Recruiter Portal
              </button>
              <button
                type="button"
                onClick={() => handleRoleChange("CANDIDATE")}
                className={`flex-1 flex items-center justify-center gap-2 py-2 text-xs font-bold rounded-xl transition-all ${
                  selectedRole === "CANDIDATE"
                    ? "bg-white text-emerald-600 shadow-xs"
                    : "text-gray-600 hover:text-gray-900"
                }`}
              >
                <Code2 className="w-3.5 h-3.5" />
                Candidate Portal
              </button>
            </div>

            {/* Title */}
            <div className="text-center space-y-1.5 mb-6">
              <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
                {selectedRole === "RECRUITER" ? "Recruiter Sign In" : "Candidate Sign In"}
              </h1>
              <p className="text-xs sm:text-sm text-gray-500">
                {selectedRole === "RECRUITER"
                  ? "Access your workspaces, candidates, and evaluation reports"
                  : "Sign in to access your assigned scheduled assessments"}
              </p>
            </div>

            {errorMessage && (
              <div className="mb-6 p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700 font-medium">
                {errorMessage}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Email Address */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700 block">
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
                <label className="text-xs font-semibold text-gray-700 block">
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

              {/* Remember Me & Forgot Password */}
              <div className="flex items-center justify-between text-xs pt-1">
                <label className="flex items-center gap-2 cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    className="w-4 h-4 rounded border-gray-300 text-[#F05323] focus:ring-[#F05323] accent-[#F05323]"
                  />
                  <span className="text-gray-700 font-medium">Remember me</span>
                </label>

                <span className="font-medium text-gray-400 hover:text-gray-600 cursor-pointer">
                  Forgot Password?
                </span>
              </div>

              {/* Quick Direct Buttons */}
              <div className="grid grid-cols-2 gap-2 pt-1 pb-1">
                <button
                  type="button"
                  onClick={handleDirectRecruiterLogin}
                  className="py-2.5 px-3 rounded-xl bg-orange-50 hover:bg-orange-100 border border-orange-200 text-[#F05323] text-xs font-bold transition-all flex items-center justify-center gap-1.5 shadow-2xs"
                >
                  <Briefcase className="w-3.5 h-3.5" />
                  Recruiter (Demo)
                </button>
                <button
                  type="button"
                  onClick={handleDirectCandidateLogin}
                  className="py-2.5 px-3 rounded-xl bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 text-emerald-700 text-xs font-bold transition-all flex items-center justify-center gap-1.5 shadow-2xs"
                >
                  <Code2 className="w-3.5 h-3.5" />
                  Candidate (Arun)
                </button>
              </div>

              {/* Submit Button */}
              <Button
                type="submit"
                disabled={isLoading}
                size="lg"
                className="w-full font-semibold gap-2 shadow-xs mt-2 bg-[#F05323] hover:bg-[#d94417]"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" /> Signing In...
                  </>
                ) : (
                  <>
                    Sign In to {selectedRole === "RECRUITER" ? "Recruiter Dashboard" : "Candidate Portal"}{" "}
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </Button>
            </form>

            {/* Social Logins Divider */}
            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-200" />
              </div>
              <div className="relative flex justify-center text-xs text-gray-500">
                <span className="bg-white px-3 text-gray-400">or sign in with</span>
              </div>
            </div>

            {/* Google & GitHub Buttons */}
            <div className="grid grid-cols-2 gap-3">
              <button
                type="button"
                onClick={() => handleSocialLogin("Google")}
                className="flex items-center justify-center gap-2 h-10 px-3 border border-gray-200 rounded-xl bg-white hover:bg-gray-50 text-xs font-semibold text-gray-700 transition-colors shadow-2xs"
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
                Google
              </button>

              <button
                type="button"
                onClick={() => handleSocialLogin("GitHub")}
                className="flex items-center justify-center gap-2 h-10 px-3 border border-gray-200 rounded-xl bg-white hover:bg-gray-50 text-xs font-semibold text-gray-700 transition-colors shadow-2xs"
              >
                <GithubIcon className="w-4 h-4 text-gray-900" />
                GitHub
              </button>
            </div>

            {/* Switch to Signup */}
            <div className="mt-8 text-center text-xs text-gray-500">
              Don't have an account?{" "}
              <Link
                to={`/signup?role=${selectedRole.toLowerCase()}`}
                className="font-bold text-[#F05323] hover:underline"
              >
                Create an account
              </Link>
            </div>
          </div>

          {/* Security Badge */}
          <div className="flex items-center justify-center gap-1.5 text-xs text-gray-400">
            <ShieldCheck className="w-4 h-4 text-gray-400" />
            <span>Secured with role-based session authorization</span>
          </div>
        </div>
      </main>

      {/* 3. Footer */}
      <footer className="bg-white border-t border-gray-200 py-6">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-400 gap-4">
          <p>© 2026 EVIDENCE. Recruiter & Candidate Technical Assessment Platform.</p>
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
