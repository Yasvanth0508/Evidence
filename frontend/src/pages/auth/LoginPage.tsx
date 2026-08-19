import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { mockAuthService } from "@/mocks/services/mockAuthService";
import { useAuthStore } from "@/store/authStore";
import {
  Mail,
  Lock,
  Eye,
  EyeOff,
  ArrowRight,
  ShieldCheck,
  Loader2,
} from "lucide-react";
import {
  GithubIcon,
  LinkedinIcon,
  TwitterIcon,
  YoutubeIcon,
} from "@/components/ui/social-icons";

export const LoginPage = () => {
  const navigate = useNavigate();
  const loginToStore = useAuthStore((state) => state.login);

  const [email, setEmail] = useState("recruiter@example.com");
  const [password, setPassword] = useState("password123");
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setErrorMessage("");

    try {
      const response = await mockAuthService.login(email, password);
      if (response.success && response.data) {
        loginToStore(response.data.user, response.data.token);
        navigate("/dashboard");
      }
    } catch {
      setErrorMessage("Invalid email or password. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleSocialLogin = async (provider: string) => {
    setIsLoading(true);
    try {
      const response = await mockAuthService.login(`${provider.toLowerCase()}@example.com`, "oauth");
      if (response.success && response.data) {
        loginToStore(response.data.user, response.data.token);
        navigate("/dashboard");
      }
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

      {/* 2. Main Login Card */}
      <main className="flex-1 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-6">
          <div className="bg-white border border-gray-200/90 rounded-3xl p-8 sm:p-10 shadow-sm">
            {/* Title */}
            <div className="text-center space-y-2 mb-8">
              <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
                Welcome back!
              </h1>
              <p className="text-xs sm:text-sm text-gray-500">
                Sign in to your EVIDENCE account
              </p>
            </div>

            {errorMessage && (
              <div className="mb-6 p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700 font-medium">
                {errorMessage}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5">
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

                <a
                  href="#forgot"
                  className="font-medium text-[#F05323] hover:underline"
                >
                  Forgot Password?
                </a>
              </div>

              {/* Submit Button */}
              <Button
                type="submit"
                disabled={isLoading}
                size="lg"
                className="w-full font-semibold gap-2 shadow-sm mt-2"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" /> Signing In...
                  </>
                ) : (
                  <>
                    Login <ArrowRight className="w-4 h-4" />
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
                <span className="bg-white px-3 text-gray-400">or continue with</span>
              </div>
            </div>

            {/* Social Buttons */}
            <div className="space-y-2.5">
              {/* Google */}
              <button
                type="button"
                onClick={() => handleSocialLogin("Google")}
                className="w-full flex items-center justify-center gap-3 h-11 px-4 border border-gray-200 rounded-xl bg-white hover:bg-gray-50 text-xs font-semibold text-gray-700 transition-colors shadow-sm"
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
                Continue with Google
              </button>

              {/* GitHub */}
              <button
                type="button"
                onClick={() => handleSocialLogin("GitHub")}
                className="w-full flex items-center justify-center gap-3 h-11 px-4 border border-gray-200 rounded-xl bg-white hover:bg-gray-50 text-xs font-semibold text-gray-700 transition-colors shadow-sm"
              >
                <GithubIcon className="w-4 h-4 text-gray-900" />
                Continue with GitHub
              </button>

              {/* Microsoft */}
              <button
                type="button"
                onClick={() => handleSocialLogin("Microsoft")}
                className="w-full flex items-center justify-center gap-3 h-11 px-4 border border-gray-200 rounded-xl bg-white hover:bg-gray-50 text-xs font-semibold text-gray-700 transition-colors shadow-sm"
              >
                <svg className="w-4 h-4" viewBox="0 0 21 21">
                  <rect x="1" y="1" width="9" height="9" fill="#f25022" />
                  <rect x="1" y="11" width="9" height="9" fill="#00a4ef" />
                  <rect x="11" y="1" width="9" height="9" fill="#7fba00" />
                  <rect x="11" y="11" width="9" height="9" fill="#ffb900" />
                </svg>
                Continue with Microsoft
              </button>
            </div>

            {/* Switch to Signup */}
            <div className="mt-8 text-center text-xs text-gray-500">
              Don't have an account?{" "}
              <Link
                to="/signup"
                className="font-bold text-[#F05323] hover:underline"
              >
                Sign up
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
