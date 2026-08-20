import { useState, useEffect } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { mockAuthService } from "@/mocks/services/mockAuthService";
import { useAuthStore } from "@/store/authStore";
import {
  User,
  Mail,
  Lock,
  Eye,
  EyeOff,
  ArrowRight,
  ShieldCheck,
  UserPlus,
  Loader2,
  Briefcase,
  Code2,
} from "lucide-react";

export const SignupPage = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const loginToStore = useAuthStore((state) => state.login);

  const roleParam = searchParams.get("role");
  const [role, setRole] = useState<"RECRUITER" | "CANDIDATE">(
    roleParam === "candidate" ? "CANDIDATE" : "RECRUITER"
  );

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("StrongPassword123");
  const [confirmPassword, setConfirmPassword] = useState("StrongPassword123");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (roleParam === "candidate") {
      setRole("CANDIDATE");
    } else if (roleParam === "recruiter") {
      setRole("RECRUITER");
    }
  }, [roleParam]);

  const handleRoleSelect = (selected: "RECRUITER" | "CANDIDATE") => {
    setRole(selected);
    setSearchParams({ role: selected.toLowerCase() });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    if (password !== confirmPassword) {
      setErrorMessage("Passwords do not match. Please verify your password.");
      return;
    }

    if (password.length < 6) {
      setErrorMessage("Password must be at least 6 characters long.");
      return;
    }

    setIsLoading(true);

    try {
      const response = await mockAuthService.signup(fullName, email, role);
      if (response.success && response.data) {
        loginToStore(response.data.user, response.data.token);
        navigate("/dashboard");
      }
    } catch {
      setErrorMessage("Failed to create account. Please try again.");
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
            <Link to={`/login?role=${role.toLowerCase()}`}>
              <Button variant="outline" size="sm" className="font-semibold text-gray-700">
                Sign In
              </Button>
            </Link>
          </div>
        </div>
      </header>

      {/* 2. Main Signup Card */}
      <main className="flex-1 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-lg w-full space-y-6">
          <div className="bg-white border border-gray-200/90 rounded-3xl p-8 sm:p-10 shadow-sm">
            {/* Top Icon Badge */}
            <div className="flex justify-center mb-4">
              <div className="w-14 h-14 rounded-full bg-orange-50 border border-orange-100 flex items-center justify-center text-[#F05323] shadow-sm">
                <UserPlus className="w-6 h-6" />
              </div>
            </div>

            {/* Title */}
            <div className="text-center space-y-1 mb-6">
              <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
                Create {role === "RECRUITER" ? "Recruiter" : "Candidate"} Account
              </h1>
              <p className="text-xs sm:text-sm text-gray-500">
                {role === "RECRUITER"
                  ? "Create workspaces and configure project-specific technical assessments"
                  : "Sign up to participate in assigned Java Spring Boot assessments"}
              </p>
            </div>

            {/* Role Switcher Tabs */}
            <div className="flex p-1 bg-gray-100/80 rounded-2xl mb-6">
              <button
                type="button"
                onClick={() => handleRoleSelect("RECRUITER")}
                className={`flex-1 flex items-center justify-center gap-2 py-2 text-xs font-bold rounded-xl transition-all ${
                  role === "RECRUITER"
                    ? "bg-white text-[#F05323] shadow-xs"
                    : "text-gray-600 hover:text-gray-900"
                }`}
              >
                <Briefcase className="w-3.5 h-3.5" />
                I am a Recruiter
              </button>
              <button
                type="button"
                onClick={() => handleRoleSelect("CANDIDATE")}
                className={`flex-1 flex items-center justify-center gap-2 py-2 text-xs font-bold rounded-xl transition-all ${
                  role === "CANDIDATE"
                    ? "bg-white text-emerald-600 shadow-xs"
                    : "text-gray-600 hover:text-gray-900"
                }`}
              >
                <Code2 className="w-3.5 h-3.5" />
                I am a Candidate
              </button>
            </div>

            {errorMessage && (
              <div className="mb-6 p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700 font-medium">
                {errorMessage}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Full Name */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700 block">
                  Full Name
                </label>
                <Input
                  type="text"
                  required
                  placeholder={role === "RECRUITER" ? "e.g. John Recruiter" : "e.g. Rahul Kumar"}
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  icon={<User className="w-4 h-4 text-gray-400" />}
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
                  placeholder={role === "RECRUITER" ? "john@company.com" : "rahul@example.com"}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  icon={<Mail className="w-4 h-4 text-gray-400" />}
                />
                <p className="text-[11px] text-gray-400">
                  {role === "CANDIDATE"
                    ? "Your email is the unique identifier recruiters will use to assign assessments."
                    : "Used for accessing your recruiter workspaces and candidate reports."}
                </p>
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

              {/* Confirm Password */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700 block">
                  Confirm Password
                </label>
                <Input
                  type={showConfirmPassword ? "text" : "password"}
                  required
                  placeholder="Confirm your password"
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
                className="w-full font-semibold gap-2 shadow-xs mt-4 bg-[#F05323] hover:bg-[#d94417]"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" /> Creating Account...
                  </>
                ) : (
                  <>
                    Sign Up as {role === "RECRUITER" ? "Recruiter" : "Candidate"} <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </Button>
            </form>

            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-200" />
              </div>
              <div className="relative flex justify-center text-xs text-gray-500">
                <span className="bg-white px-3 text-gray-400">or</span>
              </div>
            </div>

            {/* Switch to Login */}
            <div className="text-center text-xs text-gray-500">
              Already have an account?{" "}
              <Link
                to={`/login?role=${role.toLowerCase()}`}
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
