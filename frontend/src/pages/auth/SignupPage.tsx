import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { mockAuthService } from "@/mocks/services/mockAuthService";
import { useAuthStore } from "@/store/authStore";
import {
  User,
  Mail,
  Building2,
  Users,
  Lock,
  Eye,
  EyeOff,
  ArrowRight,
  ShieldCheck,
  UserPlus,
  Loader2,
} from "lucide-react";
import {
  GithubIcon,
  LinkedinIcon,
  TwitterIcon,
  YoutubeIcon,
} from "@/components/ui/social-icons";

export const SignupPage = () => {
  const navigate = useNavigate();
  const loginToStore = useAuthStore((state) => state.login);

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [company, setCompany] = useState("");
  const [role, setRole] = useState<"RECRUITER" | "CANDIDATE">("RECRUITER");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

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
      const response = await mockAuthService.signup(fullName, email, role, company);
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
      <header className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <Logo size="md" />

          <nav className="hidden md:flex items-center space-x-8 text-sm font-medium text-gray-600">
            <Link to="/" className="hover:text-gray-900 transition-colors">Product</Link>
            <Link to="/" className="hover:text-gray-900 transition-colors">How It Works</Link>
            <Link to="/" className="hover:text-gray-900 transition-colors">Features</Link>
            <Link to="/" className="hover:text-gray-900 transition-colors">For Companies</Link>
            <Link to="/" className="hover:text-gray-900 transition-colors">Pricing</Link>
            <Link to="/" className="hover:text-gray-900 transition-colors">Resources</Link>
          </nav>

          <div className="flex items-center space-x-4">
            <Link to="/login">
              <Button variant="outline" size="sm" className="font-semibold text-gray-700">
                Log in
              </Button>
            </Link>
            <Link to="/signup">
              <Button size="sm" className="font-semibold shadow-sm">
                Get Started
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
            <div className="text-center space-y-1.5 mb-8">
              <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
                Create your account
              </h1>
              <p className="text-xs sm:text-sm text-gray-500">
                Join EVIDENCE and start assessing real-world skills.
              </p>
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
                  placeholder="Enter your full name"
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
                  placeholder="Enter your email address"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  icon={<Mail className="w-4 h-4 text-gray-400" />}
                />
              </div>

              {/* Company / Organization (Optional) */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700 block">
                  Company / Organization <span className="text-gray-400 font-normal">(Optional)</span>
                </label>
                <Input
                  type="text"
                  placeholder="Enter your company or organization name"
                  value={company}
                  onChange={(e) => setCompany(e.target.value)}
                  icon={<Building2 className="w-4 h-4 text-gray-400" />}
                />
              </div>

              {/* Role Selection */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-700 block">
                  Role
                </label>
                <div className="relative">
                  <div className="absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                    <Users className="w-4 h-4" />
                  </div>
                  <select
                    value={role}
                    onChange={(e) => setRole(e.target.value as "RECRUITER" | "CANDIDATE")}
                    className="flex h-11 w-full rounded-xl border border-gray-300 bg-white pl-10 pr-4 py-2 text-sm text-gray-900 shadow-sm focus:outline-none focus:ring-2 focus:ring-[#F05323] focus:border-[#F05323] appearance-none"
                  >
                    <option value="RECRUITER">Recruiter (Hire & Assess Candidates)</option>
                    <option value="CANDIDATE">Candidate (Take Technical Assessments)</option>
                  </select>
                  <div className="absolute right-3.5 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400 text-xs">
                    ▼
                  </div>
                </div>
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
                className="w-full font-semibold gap-2 shadow-sm mt-4"
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
                to="/login"
                className="font-bold text-[#F05323] hover:underline"
              >
                Log in
              </Link>
            </div>
          </div>

          {/* Security Badge */}
          <div className="flex items-center justify-center gap-1.5 text-xs text-gray-400">
            <ShieldCheck className="w-4 h-4 text-gray-400" />
            <span>Secured with industry-standard encryption</span>
          </div>
        </div>
      </main>

      {/* 3. Footer */}
      <footer className="bg-white border-t border-gray-200 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-5 gap-8 mb-8">
            <div className="col-span-2 space-y-2">
              <Logo size="sm" />
              <p className="text-xs text-gray-500 max-w-sm">
                The leading AI-powered platform for real-world technical assessments.
              </p>
              <div className="flex space-x-3 pt-1 text-gray-400">
                <a href="#" className="hover:text-gray-900"><GithubIcon className="w-4 h-4" /></a>
                <a href="#" className="hover:text-gray-900"><LinkedinIcon className="w-4 h-4" /></a>
                <a href="#" className="hover:text-gray-900"><TwitterIcon className="w-4 h-4" /></a>
                <a href="#" className="hover:text-gray-900"><YoutubeIcon className="w-4 h-4" /></a>
              </div>
            </div>
            <div>
              <h5 className="text-xs font-bold text-gray-900 uppercase">Product</h5>
              <ul className="text-xs text-gray-500 space-y-1.5 mt-2">
                <li><a href="#" className="hover:text-gray-900">Features</a></li>
                <li><a href="#" className="hover:text-gray-900">How It Works</a></li>
                <li><a href="#" className="hover:text-gray-900">Pricing</a></li>
              </ul>
            </div>
            <div>
              <h5 className="text-xs font-bold text-gray-900 uppercase">Company</h5>
              <ul className="text-xs text-gray-500 space-y-1.5 mt-2">
                <li><a href="#" className="hover:text-gray-900">About Us</a></li>
                <li><a href="#" className="hover:text-gray-900">Careers</a></li>
                <li><a href="#" className="hover:text-gray-900">Contact Us</a></li>
              </ul>
            </div>
            <div>
              <h5 className="text-xs font-bold text-gray-900 uppercase">Resources</h5>
              <ul className="text-xs text-gray-500 space-y-1.5 mt-2">
                <li><a href="#" className="hover:text-gray-900">Docs</a></li>
                <li><a href="#" className="hover:text-gray-900">Help Center</a></li>
                <li><a href="#" className="hover:text-gray-900">Guides</a></li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-100 pt-4 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-400">
            <p>© 2026 EVIDENCE. All rights reserved.</p>
            <div className="flex space-x-4">
              <a href="#" className="hover:text-gray-600">Privacy Policy</a>
              <a href="#" className="hover:text-gray-600">Terms of Service</a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};
